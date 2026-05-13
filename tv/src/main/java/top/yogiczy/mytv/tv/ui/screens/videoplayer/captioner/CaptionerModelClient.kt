package top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import top.yogiczy.mytv.core.util.utils.UnsafeTrustManager
import top.yogiczy.mytv.tv.ui.utils.Configs
import java.util.concurrent.TimeUnit

@Serializable
data class CaptionerModelOptions(
    val asrModels: List<String> = emptyList(),
    val translationModels: List<String> = emptyList(),
) {
    fun withFallbacks(
        asrModel: String = Configs.captionerAsrModel,
        translationModel: String = Configs.captionerTranslationModel,
    ) = CaptionerModelOptions(
        asrModels = asrModels.withCurrent(asrModel),
        translationModels = translationModels.withCurrent(translationModel),
    )

    val isEmpty: Boolean get() = asrModels.isEmpty() && translationModels.isEmpty()
}

object CaptionerModelClient {
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(
                UnsafeTrustManager.getSSLSocketFactory(),
                UnsafeTrustManager()
            )
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    suspend fun fetch(serverUrl: String = Configs.captionerServerUrl): CaptionerModelOptions =
        withContext(Dispatchers.IO) { fetchBlocking(serverUrl) }

    fun fetchBlocking(serverUrl: String = Configs.captionerServerUrl): CaptionerModelOptions {
        val errors = mutableListOf<Throwable>()

        runCatching {
            parseCombinedModels(requestText(serverUrl, "/api/models"))
        }.onSuccess { options ->
            if (!options.isEmpty) return options
        }.onFailure { errors += it }

        runCatching {
            val asrModels = parseReadyModels(requestText(serverUrl, "/api/models/asr"))
            val translationModels = parseReadyModels(requestText(serverUrl, "/api/models/translate"))
            CaptionerModelOptions(asrModels = asrModels, translationModels = translationModels)
        }.onSuccess { options ->
            if (!options.isEmpty) return options
        }.onFailure { errors += it }

        throw errors.firstOrNull() ?: IllegalStateException("未获取到可用模型")
    }

    private fun requestText(serverUrl: String, endpoint: String): String {
        val request = Request.Builder()
            .url(buildEndpointUrl(serverUrl, endpoint))
            .get()
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("$endpoint HTTP ${response.code}")
            response.body?.string().orEmpty()
        }
    }

    private fun buildEndpointUrl(serverUrl: String, endpoint: String): String {
        val base = serverUrl.trim().trimEnd('/')
        if (base.isBlank()) throw IllegalStateException("字幕后端地址为空")

        val normalizedBase = when {
            base.startsWith("http://") || base.startsWith("https://") -> base
            base.startsWith("ws://") -> "http://${base.removePrefix("ws://")}"
            base.startsWith("wss://") -> "https://${base.removePrefix("wss://")}"
            else -> "http://$base"
        }

        return Uri.parse(normalizedBase)
            .buildUpon()
            .encodedPath(endpoint)
            .build()
            .toString()
    }

    private fun parseCombinedModels(text: String): CaptionerModelOptions {
        val root = JSONTokener(text).nextValue() as? JSONObject ?: return CaptionerModelOptions()
        return parseCombinedObject(root)
    }

    private fun parseCombinedObject(root: JSONObject): CaptionerModelOptions {
        val data = root.optJSONObject("data")
        if (data != null) return parseCombinedObject(data)

        val readyAsrModels = root.optJSONArray("readyAsrModels")?.toModelNames(readyOnly = false).orEmpty()
        val readyTranslationModels =
            root.optJSONArray("readyTranslationModels")?.toModelNames(readyOnly = false).orEmpty()

        val asrModels = readyAsrModels.ifEmpty {
            root.optJSONArray("asrModels")?.toModelNames(readyOnly = true).orEmpty()
        }
        val translationModels = readyTranslationModels.ifEmpty {
            root.optJSONArray("translationModels")?.toModelNames(readyOnly = true).orEmpty()
        }

        return CaptionerModelOptions(asrModels.distinct(), translationModels.distinct())
    }

    private fun parseReadyModels(text: String): List<String> {
        return when (val root = JSONTokener(text).nextValue()) {
            is JSONArray -> root.toModelNames(readyOnly = true)
            is JSONObject -> when (val data = root.opt("data")) {
                is JSONArray -> data.toModelNames(readyOnly = true)
                else -> root.optJSONArray("models")?.toModelNames(readyOnly = true).orEmpty()
            }

            else -> emptyList()
        }
    }

    private fun JSONArray.toModelNames(readyOnly: Boolean): List<String> {
        return (0 until length())
            .mapNotNull { index ->
                when (val item = opt(index)) {
                    is String -> item
                    is JSONObject -> item
                        .takeIf { model -> !readyOnly || model.optBoolean("ready", false) }
                        ?.modelName()

                    else -> null
                }?.trim()?.takeIf { it.isNotBlank() }
            }
            .distinct()
    }

    private fun JSONObject.modelName(): String? {
        val name = optString("key")
            .ifBlank { optString("name") }
            .ifBlank { optString("id") }
            .ifBlank { optString("model") }
            .ifBlank { optString("value") }
        return name.takeIf { it.isNotBlank() }
    }
}

private fun List<String>.withCurrent(current: String): List<String> {
    val trimmed = current.trim()
    return if (trimmed.isBlank() || any { it == trimmed }) this else listOf(trimmed) + this
}
