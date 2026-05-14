package top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import top.yogiczy.mytv.core.util.utils.UnsafeTrustManager
import top.yogiczy.mytv.tv.ui.utils.Configs
import java.util.concurrent.TimeUnit

object DeepSeekTranslationClient {
    const val MODEL = "deepseek-v4-flash"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(
                UnsafeTrustManager.getSSLSocketFactory(),
                UnsafeTrustManager()
            )
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    suspend fun translate(
        text: String,
        sourceLanguage: String = Configs.captionerSourceLanguage,
        targetLanguage: String = Configs.captionerTargetLanguage,
        apiUrl: String = Configs.captionerDeepSeekApiUrl,
        apiKey: String = Configs.captionerDeepSeekApiKey,
        prompt: String = Configs.captionerDeepSeekPrompt,
    ): String = withContext(Dispatchers.IO) {
        translateBlocking(text, sourceLanguage, targetLanguage, apiUrl, apiKey, prompt)
    }

    fun testBlocking(
        apiUrl: String,
        apiKey: String,
        prompt: String,
    ): String {
        return translateBlocking(
            text = "Hello, this is a DeepSeek translation connection test.",
            sourceLanguage = "en",
            targetLanguage = "Chinese",
            apiUrl = apiUrl,
            apiKey = apiKey,
            prompt = prompt,
        )
    }

    fun translateBlocking(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        apiUrl: String,
        apiKey: String,
        prompt: String,
    ): String {
        val sourceText = text.trim()
        if (sourceText.isBlank()) return ""
        if (apiKey.isBlank()) error("DeepSeek API Key 为空")

        val body = JSONObject()
            .put("model", MODEL)
            .put("stream", false)
            .put("temperature", 0.1)
            .put("thinking", JSONObject().put("type", "disabled"))
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", prompt.ifBlank { Configs.DEFAULT_DEEPSEEK_TRANSLATION_PROMPT })
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put(
                                "content",
                                "源语言：$sourceLanguage\n目标语言：$targetLanguage\n字幕原文：\n$sourceText",
                            )
                    )
            )

        val request = Request.Builder()
            .url(buildChatCompletionsUrl(apiUrl))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()

        return httpClient.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("DeepSeek HTTP ${response.code}: ${responseText.take(160)}")
            }

            val root = JSONObject(responseText)
            root.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
                ?.trim('"', '“', '”')
                ?.takeIf { it.isNotBlank() }
                ?: error("DeepSeek 返回为空")
        }
    }

    private fun buildChatCompletionsUrl(apiUrl: String): String {
        val value = apiUrl.trim().trimEnd('/')
        if (value.isBlank()) error("DeepSeek API 地址为空")

        val normalized = when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            else -> "https://$value"
        }

        if (normalized.endsWith("/chat/completions")) return normalized

        return Uri.parse(normalized)
            .buildUpon()
            .appendEncodedPath("chat/completions")
            .build()
            .toString()
    }
}
