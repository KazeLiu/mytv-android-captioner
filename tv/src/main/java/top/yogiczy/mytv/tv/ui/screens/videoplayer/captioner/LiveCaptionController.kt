package top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import top.yogiczy.mytv.core.data.utils.Loggable
import top.yogiczy.mytv.core.util.utils.UnsafeTrustManager
import top.yogiczy.mytv.tv.ui.utils.Configs
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class LiveCaptionController(
    private val coroutineScope: CoroutineScope,
    private val onCaptions: (List<SubtitleItem>) -> Unit,
) : LiveAudioCaptureSink, Loggable() {
    private val lock = Any()
    private val gson = Gson()
    private val sendBuffer = ByteArrayOutputStream()
    private val subtitleQueue = mutableListOf<SubtitleItem>()
    private val removeSubtitleJobs = mutableMapOf<String, Job>()
    private val translationJobs = mutableMapOf<String, Job>()
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(
                UnsafeTrustManager.getSSLSocketFactory(),
                UnsafeTrustManager()
            )
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private var active = false
    private var activeToken = 0
    private var modelsReady = false
    private var modelRefreshJob: Job? = null
    private var sampleRate = 0
    private var channelCount = 0
    private var webSocket: WebSocket? = null
    private var connectionConfig: ConnectionConfig? = null

    fun start() {
        val token: Int
        synchronized(lock) {
            active = Configs.captionerEnabled && LiveCaptionRuntimeState.enabled
            activeToken++
            token = activeToken
            modelsReady = false
            connectionConfig = null
            sampleRate = 0
            channelCount = 0
            sendBuffer.reset()
        }

        closeSocket()
        if (active) {
            showLoadingCaption()
            refreshReadyModels(token)
        } else {
            clearCaptions()
        }
    }

    fun stop() {
        synchronized(lock) {
            active = false
            activeToken++
            modelsReady = false
            connectionConfig = null
            sendBuffer.reset()
        }
        modelRefreshJob?.cancel()
        webSocket?.send("""{"type":"flush"}""")
        closeSocket()
        clearCaptions()
    }

    override fun onPcmAudio(data: ByteArray, sampleRate: Int, channelCount: Int) {
        reconcileRuntimeState()
        if (!isCaptionerEnabled() || data.isEmpty()) return

        val normalizedPcm = normalizePcm16ToMono16k(data, sampleRate, channelCount)
        if (normalizedPcm.isEmpty()) return

        val chunks = mutableListOf<AudioChunk>()
        synchronized(lock) {
            if (!active || !modelsReady) return

            val currentConfig = ConnectionConfig.fromCurrent()
            if (connectionConfig != currentConfig) {
                restartLockedForConfigChange(currentConfig)
                return
            }

            if (this.sampleRate != TARGET_SAMPLE_RATE || this.channelCount != TARGET_CHANNELS) {
                this.sampleRate = TARGET_SAMPLE_RATE
                this.channelCount = TARGET_CHANNELS
                sendBuffer.reset()
                connectLocked()
            } else if (webSocket == null) {
                connectLocked()
            }

            sendBuffer.write(normalizedPcm)

            val frameBytes = TARGET_CHANNELS * BYTES_PER_SAMPLE
            val targetBytes = PCM_FRAMES_PER_CHUNK * frameBytes
            if (frameBytes <= 0 || sendBuffer.size() < targetBytes) return

            val pcm = sendBuffer.toByteArray()
            val sendableBytes = pcm.size - (pcm.size % targetBytes)
            sendBuffer.reset()
            if (sendableBytes < pcm.size) {
                sendBuffer.write(pcm, sendableBytes, pcm.size - sendableBytes)
            }

            var offset = 0
            while (offset < sendableBytes) {
                chunks += AudioChunk(
                    token = activeToken,
                    pcm = pcm.copyOfRange(offset, offset + targetBytes),
                    sampleRate = TARGET_SAMPLE_RATE,
                    channelCount = TARGET_CHANNELS,
                )
                offset += targetBytes
            }
        }

        chunks.forEach { upload(it) }
    }

    private fun refreshReadyModels(token: Int) {
        modelRefreshJob?.cancel()
        modelRefreshJob = coroutineScope.launch {
            val options = runCatching {
                CaptionerModelClient.fetch(Configs.captionerServerUrl)
            }.getOrElse {
                log.w("获取实时字幕模型失败: ${it.message}", it)
                if (!isTokenActive(token)) return@launch
                disableCaptionerWithStatus("AI字幕已关闭：后端连接失败")
                return@launch
            }

            val localTranslationEnabled = isLocalBackendTranslationEnabled()
            val unavailableMessage = when {
                options.asrModels.isEmpty() -> "AI字幕已关闭：字幕模型不存在"
                localTranslationEnabled && options.translationModels.isEmpty() -> "AI字幕已关闭：翻译模型不存在"
                isOnlineTranslationEnabled() && Configs.captionerDeepSeekApiKey.isBlank() ->
                    "AI字幕已关闭：DeepSeek API Key 为空"

                else -> null
            }
            if (unavailableMessage != null) {
                log.w("实时字幕模型不可用: $unavailableMessage")
                if (!isTokenActive(token)) return@launch
                disableCaptionerWithStatus(unavailableMessage)
                return@launch
            }

            synchronized(lock) {
                if (!active || token != activeToken) return@launch
                Configs.captionerAsrModel = keepReadyModel(Configs.captionerAsrModel, options.asrModels)
                if (localTranslationEnabled) {
                    Configs.captionerTranslationModel = keepReadyModel(
                        Configs.captionerTranslationModel,
                        options.translationModels,
                    )
                }
                connectionConfig = ConnectionConfig.fromCurrent()
                modelsReady = true
            }
        }
    }

    private fun reconcileRuntimeState() {
        val enabled = isCaptionerEnabled()
        var shouldStart = false
        var shouldStop = false

        synchronized(lock) {
            if (enabled && !active) {
                shouldStart = true
            } else if (!enabled && active) {
                shouldStop = true
            }
        }

        when {
            shouldStart -> start()
            shouldStop -> stop()
        }
    }

    private fun isCaptionerEnabled(): Boolean {
        return Configs.captionerEnabled && LiveCaptionRuntimeState.enabled
    }

    private fun isLocalBackendTranslationEnabled(): Boolean {
        return Configs.captionerTranslationEnabled &&
                Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.LOCAL
    }

    private fun isOnlineTranslationEnabled(): Boolean {
        return Configs.captionerTranslationEnabled &&
                Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.ONLINE
    }

    private fun restartLockedForConfigChange(currentConfig: ConnectionConfig) {
        activeToken++
        modelsReady = false
        connectionConfig = null
        sampleRate = 0
        channelCount = 0
        sendBuffer.reset()
        closeSocket()

        val token = activeToken
        showLoadingCaption()
        refreshReadyModels(token)
        log.d("实时字幕配置变化，重连WebSocket: $currentConfig")
    }

    private fun keepReadyModel(current: String, readyModels: List<String>): String {
        val selected = current.trim()
        return if (selected.isNotBlank() && selected in readyModels) selected else readyModels.first()
    }

    private fun disableCaptionerWithStatus(message: String) {
        synchronized(lock) {
            active = false
            modelsReady = false
            connectionConfig = null
            sendBuffer.reset()
        }
        Configs.captionerEnabled = false
        closeSocket()
        showStatusCaption(message)
    }

    private fun isTokenActive(token: Int): Boolean {
        return synchronized(lock) { active && token == activeToken }
    }

    private fun connectLocked() {
        closeSocket()
        val token = activeToken
        val request = runCatching {
            Request.Builder()
                .url(buildWebSocketUrl())
                .build()
        }.getOrElse {
            log.w("实时字幕WebSocket地址无效: ${it.message}", it)
            return
        }

        webSocket = httpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    synchronized(lock) {
                        if (token != activeToken) return
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleMessage(token, text)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    synchronized(lock) {
                        if (token == activeToken && this@LiveCaptionController.webSocket == webSocket) {
                            this@LiveCaptionController.webSocket = null
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    log.w("实时字幕WebSocket断开: ${t.message}", t)
                    synchronized(lock) {
                        if (token == activeToken && this@LiveCaptionController.webSocket == webSocket) {
                            this@LiveCaptionController.webSocket = null
                        }
                    }
                }
            },
        )
    }

    private fun upload(chunk: AudioChunk) {
        val socket = synchronized(lock) {
            if (!active || chunk.token != activeToken) null else webSocket
        } ?: return

        var offset = 0
        val frameBytes = chunk.channelCount * 2
        val maxBytes = maxOf(frameBytes, chunk.sampleRate * frameBytes * 100 / 1000)
        while (offset < chunk.pcm.size) {
            val size = minOf(maxBytes, chunk.pcm.size - offset)
            if (!socket.send(chunk.pcm.toByteString(offset, size))) {
                handleSocketSendFailed(socket, chunk.token)
                return
            }
            offset += size
        }
    }

    private fun handleSocketSendFailed(socket: WebSocket, token: Int) {
        synchronized(lock) {
            if (active && token == activeToken && webSocket == socket) {
                webSocket = null
            }
        }
        socket.close(1001, "send failed")
    }

    private fun buildWebSocketUrl(): String {
        val serverUrl = Configs.captionerServerUrl.trim().trimEnd('/')
        if (serverUrl.isBlank()) {
            throw IllegalStateException("字幕后端地址为空")
        }

        val wsBase = when {
            serverUrl.startsWith("https://") -> "wss://${serverUrl.removePrefix("https://")}"
            serverUrl.startsWith("http://") -> "ws://${serverUrl.removePrefix("http://")}"
            serverUrl.startsWith("ws://") || serverUrl.startsWith("wss://") -> serverUrl
            else -> "ws://$serverUrl"
        }

        val wsUri = Uri.parse(wsBase)
        val path = wsUri.path.orEmpty().trimEnd('/')
        val apiPath = if (isLocalBackendTranslationEnabled()) "/api/live/ws" else "/api/live/asr-ws"
        val endpointBase = when {
            path.endsWith("/api/live/ws") || path.endsWith("/api/live/asr-ws") ->
                wsBase.removeSuffix(path).trimEnd('/')

            else -> wsBase.trimEnd('/')
        }
        val endpoint = "$endpointBase$apiPath"

        val builder = Uri.parse(endpoint)
            .buildUpon()
            .appendQueryParameter("sourceLanguage", Configs.captionerSourceLanguage)
            .appendQueryParameter("asrModel", Configs.captionerAsrModel)
            .appendQueryParameter("codec", "pcm_s16le")
            .appendQueryParameter("sampleRate", TARGET_SAMPLE_RATE.toString())
            .appendQueryParameter("channels", TARGET_CHANNELS.toString())
            .appendQueryParameter("silenceMs", CAPTIONER_SILENCE_MS.toString())
            .appendQueryParameter("maxSegmentMs", Configs.captionerChunkDurationMs.toString())
            .appendQueryParameter("partialBeamSize", Configs.captionerPartialBeamSize.toString())
            .appendQueryParameter("finalBeamSize", Configs.captionerFinalBeamSize.toString())
            .appendQueryParameter("chineseScript", Configs.captionerChineseScript)

        if (isLocalBackendTranslationEnabled()) {
            builder
                .appendQueryParameter("targetLanguage", Configs.captionerTargetLanguage)
                .appendQueryParameter("translationModel", Configs.captionerTranslationModel)
        }

        return builder.build().toString()
    }

    private fun handleMessage(token: Int, text: String) {
        synchronized(lock) {
            if (!active || token != activeToken) return
        }

        val root = runCatching { gson.fromJson(text, JsonObject::class.java) }.getOrNull() ?: return
        when (root.get("type")?.asString) {
            "segment" -> handleSegmentMessage(root)
            "partial" -> handlePartialMessage(root)
            "error" -> handleErrorMessage(root)
        }
    }

    private fun handleErrorMessage(root: JsonObject) {
        val message = root.stringValue("message").ifBlank { "实时字幕后端错误" }
        log.w("实时字幕后端错误: $message")
        showStatusCaption(message)
    }

    private fun handleSegmentMessage(root: JsonObject) {
        val segments = root.getAsJsonArray("segments") ?: return
        val messageId = root.stringValue("id").ifBlank { "segment-${System.currentTimeMillis()}" }
        val forced = root.booleanValue("forced")
        removeLoadingCaption()
        removePartialCaption(messageId)

        segments.forEachIndexed { index, item ->
            val segment = item.asJsonObject
            val sourceText = segment.stringValue("text")
            val translatedText = segment.stringValue("translation")
            if (sourceText.isBlank() && translatedText.isBlank()) return@forEachIndexed

            val subtitleItem = SubtitleItem(
                id = "$messageId-${segment.stringValue("id").ifBlank { index.toString() }}",
                start = segment.doubleValue("start", root.doubleValue("start")),
                end = segment.doubleValue("end", root.doubleValue("end")),
                sourceText = sourceText,
                translatedText = translatedText,
                forced = forced,
                partial = false,
                createdAt = System.currentTimeMillis(),
            )
            addSubtitleItem(subtitleItem)
            translateOnlineIfNeeded(subtitleItem)
        }
    }

    private fun handlePartialMessage(root: JsonObject) {
        val segments = root.getAsJsonArray("segments") ?: return
        val finalId = root.stringValue("finalId").ifBlank { root.stringValue("id") }
            .ifBlank { "segment-${System.currentTimeMillis()}" }
        val messageId = "partial-$finalId"
        val sourceText = segments.joinToString(" ") { it.asJsonObject.stringValue("text") }.trim()
        val translatedText = segments.joinToString(" ") { it.asJsonObject.stringValue("translation") }.trim()
        if (sourceText.isBlank() && translatedText.isBlank()) return

        removeLoadingCaption()
        addOrReplaceSubtitleItem(
            SubtitleItem(
                id = messageId,
                start = root.doubleValue("start"),
                end = root.doubleValue("end"),
                sourceText = sourceText,
                translatedText = translatedText,
                forced = false,
                partial = true,
                createdAt = System.currentTimeMillis(),
            ),
            scheduleRemoval = false,
        )
    }

    private fun showLoadingCaption() {
        showStatusCaption("正在加载模型")
    }

    private fun showStatusCaption(text: String) {
        addOrReplaceSubtitleItem(
            SubtitleItem(
                id = LOADING_SUBTITLE_ID,
                start = 0.0,
                end = 0.0,
                sourceText = text,
                translatedText = "",
                forced = false,
                partial = true,
                createdAt = System.currentTimeMillis(),
            ),
            scheduleRemoval = false,
        )
    }

    private fun removeLoadingCaption() {
        val snapshot: List<SubtitleItem>
        synchronized(lock) {
            removeSubtitleJobs.remove(LOADING_SUBTITLE_ID)?.cancel()
            val removed = subtitleQueue.removeAll { it.id == LOADING_SUBTITLE_ID }
            if (!removed) return
            snapshot = subtitleQueue.toList()
        }
        publishCaptions(snapshot)
    }

    private fun removePartialCaption(finalId: String) {
        val snapshot: List<SubtitleItem>
        synchronized(lock) {
            val partialId = "partial-$finalId"
            removeSubtitleJobs.remove(partialId)?.cancel()
            val removed = subtitleQueue.removeAll { it.id == partialId }
            if (!removed) return
            snapshot = subtitleQueue.toList()
        }
        publishCaptions(snapshot)
    }

    private fun addSubtitleItem(item: SubtitleItem) {
        addOrReplaceSubtitleItem(item, scheduleRemoval = true)
    }

    private fun addOrReplaceSubtitleItem(item: SubtitleItem, scheduleRemoval: Boolean) {
        val snapshot: List<SubtitleItem>
        val token = synchronized(lock) { activeToken }
        synchronized(lock) {
            val existingIndex = subtitleQueue.indexOfFirst { it.id == item.id }
            if (existingIndex >= 0) {
                subtitleQueue[existingIndex] = item
            } else {
                subtitleQueue += item
            }
            while (subtitleQueue.size > MAX_SUBTITLE_ITEMS) {
                val removed = subtitleQueue.removeAt(0)
                removeSubtitleJobs.remove(removed.id)?.cancel()
            }
            snapshot = subtitleQueue.toList()
        }
        publishCaptions(snapshot)
        if (scheduleRemoval) {
            scheduleSubtitleRemoval(item.id, token)
        }
    }

    private fun translateOnlineIfNeeded(item: SubtitleItem) {
        if (!isOnlineTranslationEnabled() || item.sourceText.isBlank()) return

        val token = synchronized(lock) { activeToken }
        translationJobs.remove(item.id)?.cancel()
        translationJobs[item.id] = coroutineScope.launch {
            val translation = runCatching {
                DeepSeekTranslationClient.translate(item.sourceText)
            }.getOrElse {
                log.w("DeepSeek字幕翻译失败: ${it.message}", it)
                return@launch
            }

            val snapshot = synchronized(lock) {
                if (!active || token != activeToken) return@launch
                translationJobs.remove(item.id)
                val index = subtitleQueue.indexOfFirst { it.id == item.id }
                if (index < 0) return@launch
                subtitleQueue[index] = subtitleQueue[index].copy(translatedText = translation)
                subtitleQueue.toList()
            }
            publishCaptions(snapshot)
        }
    }

    private fun scheduleSubtitleRemoval(id: String, token: Int) {
        removeSubtitleJobs.remove(id)?.cancel()
        removeSubtitleJobs[id] = coroutineScope.launch {
            delay(Configs.captionerDisplayDurationMs.coerceIn(MIN_SUBTITLE_DURATION_MS, MAX_SUBTITLE_DURATION_MS))
            val snapshot = synchronized(lock) {
                if (!active || token != activeToken) return@launch
                removeSubtitleJobs.remove(id)
                translationJobs.remove(id)?.cancel()
                subtitleQueue.removeAll { it.id == id }
                subtitleQueue.toList()
            }
            publishCaptions(snapshot)
        }
    }

    private fun publishCaptions(captions: List<SubtitleItem>) {
        coroutineScope.launch { onCaptions(captions) }
    }

    private fun clearCaptions() {
        removeSubtitleJobs.values.forEach { it.cancel() }
        removeSubtitleJobs.clear()
        translationJobs.values.forEach { it.cancel() }
        translationJobs.clear()
        val hadCaptions = synchronized(lock) {
            val hadItems = subtitleQueue.isNotEmpty()
            subtitleQueue.clear()
            hadItems
        }
        if (hadCaptions) publishCaptions(emptyList())
    }

    private fun normalizePcm16ToMono16k(
        data: ByteArray,
        inputSampleRate: Int,
        inputChannelCount: Int,
    ): ByteArray {
        if (inputSampleRate <= 0 || inputChannelCount <= 0) return ByteArray(0)

        val inputFrameBytes = inputChannelCount * BYTES_PER_SAMPLE
        val inputFrames = data.size / inputFrameBytes
        if (inputFrames <= 0) return ByteArray(0)

        val monoSamples = IntArray(inputFrames)
        for (frame in 0 until inputFrames) {
            var mixed = 0
            for (channel in 0 until inputChannelCount) {
                val byteIndex = frame * inputFrameBytes + channel * BYTES_PER_SAMPLE
                if (byteIndex + 1 >= data.size) continue
                val low = data[byteIndex].toInt() and 0xFF
                val high = data[byteIndex + 1].toInt()
                mixed += ((high shl 8) or low).toShort().toInt()
            }
            monoSamples[frame] = mixed / inputChannelCount
        }

        val outputFrames = if (inputSampleRate == TARGET_SAMPLE_RATE) {
            inputFrames
        } else {
            (inputFrames.toDouble() * TARGET_SAMPLE_RATE / inputSampleRate).roundToInt()
        }
        if (outputFrames <= 0) return ByteArray(0)

        val output = ByteArray(outputFrames * BYTES_PER_SAMPLE)
        for (frame in 0 until outputFrames) {
            val sourceFrame = if (inputSampleRate == TARGET_SAMPLE_RATE) {
                frame
            } else {
                (frame.toLong() * inputSampleRate / TARGET_SAMPLE_RATE).toInt()
                    .coerceIn(0, inputFrames - 1)
            }
            val sample = monoSamples[sourceFrame]
            val byteIndex = frame * BYTES_PER_SAMPLE
            output[byteIndex] = (sample and 0xFF).toByte()
            output[byteIndex + 1] = ((sample shr 8) and 0xFF).toByte()
        }

        return output
    }

    private fun JsonObject.stringValue(key: String): String {
        return safeGet(key)?.asString.orEmpty().trim()
    }

    private fun JsonObject.doubleValue(key: String, defaultValue: Double = 0.0): Double {
        return safeGet(key)?.asDouble ?: defaultValue
    }

    private fun JsonObject.booleanValue(key: String): Boolean {
        return safeGet(key)?.asBoolean ?: false
    }

    private fun JsonObject.safeGet(key: String): JsonElement? {
        return if (has(key) && !get(key).isJsonNull) get(key) else null
    }

    private fun closeSocket() {
        webSocket?.close(1000, null)
        webSocket = null
    }

    data class SubtitleItem(
        val id: String,
        val start: Double,
        val end: Double,
        val sourceText: String,
        val translatedText: String,
        val forced: Boolean,
        val createdAt: Long,
        val partial: Boolean = false,
    )

    private data class AudioChunk(
        val token: Int,
        val pcm: ByteArray,
        val sampleRate: Int,
        val channelCount: Int,
    )

    private data class ConnectionConfig(
        val serverUrl: String,
        val sourceLanguage: String,
        val translationEnabled: Boolean,
        val translationMode: Configs.CaptionerTranslationMode,
        val targetLanguage: String,
        val chineseScript: String,
        val asrModel: String,
        val translationModel: String,
        val deepSeekApiUrl: String,
        val deepSeekApiKeyHash: Int,
        val deepSeekPromptHash: Int,
        val chunkDurationMs: Long,
        val partialBeamSize: Int,
        val finalBeamSize: Int,
    ) {
        companion object {
            fun fromCurrent(): ConnectionConfig {
                val translationEnabled = Configs.captionerTranslationEnabled
                return ConnectionConfig(
                    serverUrl = Configs.captionerServerUrl.trim().trimEnd('/'),
                    sourceLanguage = Configs.captionerSourceLanguage,
                    translationEnabled = translationEnabled,
                    translationMode = if (translationEnabled) {
                        Configs.captionerTranslationMode
                    } else {
                        Configs.CaptionerTranslationMode.LOCAL
                    },
                    targetLanguage = if (translationEnabled) Configs.captionerTargetLanguage else Configs.CAPTIONER_TARGET_NONE,
                    chineseScript = Configs.captionerChineseScript,
                    asrModel = Configs.captionerAsrModel,
                    translationModel = if (
                        translationEnabled &&
                        Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.LOCAL
                    ) {
                        Configs.captionerTranslationModel
                    } else {
                        ""
                    },
                    deepSeekApiUrl = if (
                        translationEnabled &&
                        Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.ONLINE
                    ) {
                        Configs.captionerDeepSeekApiUrl
                    } else {
                        ""
                    },
                    deepSeekApiKeyHash = if (
                        translationEnabled &&
                        Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.ONLINE
                    ) {
                        Configs.captionerDeepSeekApiKey.hashCode()
                    } else {
                        0
                    },
                    deepSeekPromptHash = if (
                        translationEnabled &&
                        Configs.captionerTranslationMode == Configs.CaptionerTranslationMode.ONLINE
                    ) {
                        Configs.captionerDeepSeekPrompt.hashCode()
                    } else {
                        0
                    },
                    chunkDurationMs = Configs.captionerChunkDurationMs,
                    partialBeamSize = Configs.captionerPartialBeamSize,
                    finalBeamSize = Configs.captionerFinalBeamSize,
                )
            }
        }
    }

    private companion object {
        const val BYTES_PER_SAMPLE = 2
        const val TARGET_SAMPLE_RATE = 16000
        const val TARGET_CHANNELS = 1
        const val PCM_FRAMES_PER_CHUNK = 1024
        const val CAPTIONER_SILENCE_MS = 300
        const val LOADING_SUBTITLE_ID = "captioner-loading"
        const val MAX_SUBTITLE_ITEMS = 3
        const val MIN_SUBTITLE_DURATION_MS = 3000L
        const val MAX_SUBTITLE_DURATION_MS = 7000L
    }
}
