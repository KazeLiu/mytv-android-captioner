package top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner

import android.net.Uri
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
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import top.yogiczy.mytv.core.data.network.TrustAllSSLSocketFactory
import top.yogiczy.mytv.core.data.utils.Loggable
import top.yogiczy.mytv.tv.ui.utils.Configs
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class LiveCaptionController(
    private val coroutineScope: CoroutineScope,
    private val onCaptions: (List<SubtitleItem>) -> Unit,
) : LiveAudioCaptureSink, Loggable("LiveCaptionController") {
    private val lock = Any()
    private val sendBuffer = ByteArrayOutputStream()
    private val subtitleQueue = mutableListOf<SubtitleItem>()
    private val removeSubtitleJobs = mutableMapOf<String, Job>()
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(
                TrustAllSSLSocketFactory.sslSocketFactory,
                TrustAllSSLSocketFactory.trustManager,
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

    fun start() {
        val token: Int
        synchronized(lock) {
            active = Configs.captionerEnabled && LiveCaptionRuntimeState.enabled
            activeToken++
            token = activeToken
            modelsReady = false
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
            sendBuffer.reset()
        }
        modelRefreshJob?.cancel()
        webSocket?.send("""{"type":"flush"}""")
        closeSocket()
        clearCaptions()
    }

    override fun onPcmAudio(data: ByteArray, sampleRate: Int, channelCount: Int) {
        if (!Configs.captionerEnabled || !LiveCaptionRuntimeState.enabled || data.isEmpty()) return

        val normalizedPcm = normalizePcm16ToMono16k(data, sampleRate, channelCount)
        if (normalizedPcm.isEmpty()) return

        val chunks = mutableListOf<AudioChunk>()
        synchronized(lock) {
            if (!active || !modelsReady) return

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

            val translationEnabled = Configs.captionerTranslationEnabled
            val unavailableMessage = when {
                options.asrModels.isEmpty() -> "AI字幕已关闭：字幕模型不存在"
                translationEnabled && options.translationModels.isEmpty() -> "AI字幕已关闭：翻译模型不存在"
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
                if (translationEnabled) {
                    Configs.captionerTranslationModel = keepReadyModel(
                        Configs.captionerTranslationModel,
                        options.translationModels,
                    )
                }
                modelsReady = true
            }
        }
    }

    private fun keepReadyModel(current: String, readyModels: List<String>): String {
        val selected = current.trim()
        return if (selected.isNotBlank() && selected in readyModels) selected else readyModels.first()
    }

    private fun disableCaptionerWithStatus(message: String) {
        synchronized(lock) {
            active = false
            modelsReady = false
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
            if (!socket.send(chunk.pcm.toByteString(offset, size))) return
            offset += size
        }
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
        val apiPath = if (Configs.captionerTranslationEnabled) "/api/live/ws" else "/api/live/asr-ws"
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

        if (Configs.captionerTranslationEnabled) {
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

        val root = runCatching { JSONTokener(text).nextValue() as? JSONObject }.getOrNull() ?: return
        when (root.stringValue("type")) {
            "segment" -> handleSegmentMessage(root)
            "partial" -> handlePartialMessage(root)
            "error" -> handleErrorMessage(root)
        }
    }

    private fun handleErrorMessage(root: JSONObject) {
        val message = root.stringValue("message").ifBlank { "实时字幕后端错误" }
        log.w("实时字幕后端错误: $message")
        showStatusCaption(message)
    }

    private fun handleSegmentMessage(root: JSONObject) {
        val segments = root.optJSONArray("segments") ?: return
        val messageId = root.stringValue("id").ifBlank { "segment-${System.currentTimeMillis()}" }
        val forced = root.booleanValue("forced")
        removeLoadingCaption()
        removePartialCaption(messageId)

        segments.forEachObjectIndexed { index, segment ->
            val sourceText = segment.stringValue("text")
            val translatedText = segment.stringValue("translation")
            if (sourceText.isBlank() && translatedText.isBlank()) return@forEachObjectIndexed

            addSubtitleItem(
                SubtitleItem(
                    id = "$messageId-${segment.stringValue("id").ifBlank { index.toString() }}",
                    start = segment.doubleValue("start", root.doubleValue("start")),
                    end = segment.doubleValue("end", root.doubleValue("end")),
                    sourceText = sourceText,
                    translatedText = translatedText,
                    forced = forced,
                    partial = false,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
    }

    private fun handlePartialMessage(root: JSONObject) {
        val segments = root.optJSONArray("segments") ?: return
        val finalId = root.stringValue("finalId").ifBlank { root.stringValue("id") }
            .ifBlank { "segment-${System.currentTimeMillis()}" }
        val messageId = "partial-$finalId"
        val sourceText = segments.joinStringValue("text")
        val translatedText = segments.joinStringValue("translation")
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

    private fun scheduleSubtitleRemoval(id: String, token: Int) {
        removeSubtitleJobs.remove(id)?.cancel()
        removeSubtitleJobs[id] = coroutineScope.launch {
            delay(Configs.captionerDisplayDurationMs.coerceIn(MIN_SUBTITLE_DURATION_MS, MAX_SUBTITLE_DURATION_MS))
            val snapshot = synchronized(lock) {
                if (!active || token != activeToken) return@launch
                removeSubtitleJobs.remove(id)
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

    private inline fun JSONArray.forEachObjectIndexed(block: (Int, JSONObject) -> Unit) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            block(index, item)
        }
    }

    private fun JSONArray.joinStringValue(key: String): String {
        return (0 until length())
            .mapNotNull { index ->
                optJSONObject(index)?.stringValue(key)?.takeIf { it.isNotBlank() }
            }
            .joinToString(" ")
            .trim()
    }

    private fun JSONObject.stringValue(key: String): String {
        val value = opt(key)
        return if (value == null || value == JSONObject.NULL) "" else value.toString().trim()
    }

    private fun JSONObject.doubleValue(key: String, defaultValue: Double = 0.0): Double {
        return optDouble(key, defaultValue)
    }

    private fun JSONObject.booleanValue(key: String): Boolean {
        return optBoolean(key, false)
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
