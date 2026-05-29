package top.yogiczy.mytv.tv.ui.screensold.videoplayer.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSource
import top.yogiczy.mytv.core.data.network.LiveNetworkProxy
import top.yogiczy.mytv.core.data.network.TrustAllSSLSocketFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class OkHttpLiveHttpDataSource private constructor(
    private val client: OkHttpClient,
    private val defaultRequestProperties: HttpDataSource.RequestProperties,
    private val userAgent: String?,
) : BaseDataSource(true), HttpDataSource {
    private val requestProperties = HttpDataSource.RequestProperties()
    private var dataSpec: DataSpec? = null
    private var response: Response? = null
    private var responseSource: BufferedSource? = null
    private var opened = false
    private var openedUri: Uri? = null
    private var responseCode = 0
    private var responseHeaders: Map<String, List<String>> = emptyMap()
    private var bytesRemaining = C.LENGTH_UNSET.toLong()

    override fun setRequestProperty(name: String, value: String) {
        requestProperties.set(name, value)
    }

    override fun clearRequestProperty(name: String) {
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    override fun getResponseCode(): Int = responseCode

    override fun getResponseHeaders(): Map<String, List<String>> = responseHeaders

    override fun getUri(): Uri? = openedUri

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        transferInitializing(dataSpec)

        try {
            val request = buildRequest(dataSpec)
            val response = client.newCall(request).execute()
            this.response = response
            responseCode = response.code
            responseHeaders = response.headers.toMultimap()
            openedUri = Uri.parse(response.request.url.toString())

            if (!response.isSuccessful) {
                throw HttpDataSource.InvalidResponseCodeException(
                    responseCode,
                    response.message,
                    null,
                    responseHeaders,
                    dataSpec,
                    response.body?.bytes() ?: ByteArray(0),
                )
            }

            val responseBody = response.body
                ?: throw HttpDataSource.HttpDataSourceException(dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
            responseSource = responseBody.source()

            val bytesToSkip = if (responseCode == 200 && dataSpec.position > 0) dataSpec.position else 0L
            if (bytesToSkip > 0) {
                responseSource?.skip(bytesToSkip)
            }

            opened = true
            transferStarted(dataSpec)

            bytesRemaining = dataSpec.length.takeIf { it != C.LENGTH_UNSET.toLong() }
                ?: responseBody.contentLength().takeIf { it >= 0 }?.let {
                    (it - bytesToSkip).coerceAtLeast(0)
                }
                ?: C.LENGTH_UNSET.toLong()
            return bytesRemaining
        } catch (ex: IOException) {
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                ex,
                dataSpec,
                HttpDataSource.HttpDataSourceException.TYPE_OPEN,
            )
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0

        val currentDataSpec = dataSpec
            ?: throw HttpDataSource.HttpDataSourceException(
                "Data source is not opened",
                DataSpec(Uri.EMPTY),
                HttpDataSource.HttpDataSourceException.TYPE_READ,
            )

        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT
        val readLength = if (bytesRemaining == C.LENGTH_UNSET.toLong()) {
            length
        } else {
            minOf(length.toLong(), bytesRemaining).toInt()
        }

        return try {
            val bytesRead = responseSource?.read(buffer, offset, readLength) ?: -1
            if (bytesRead == -1) {
                C.RESULT_END_OF_INPUT
            } else {
                if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
                    bytesRemaining -= bytesRead
                }
                bytesTransferred(bytesRead)
                bytesRead
            }
        } catch (ex: IOException) {
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                ex,
                currentDataSpec,
                HttpDataSource.HttpDataSourceException.TYPE_READ,
            )
        }
    }

    override fun close() {
        val currentDataSpec = dataSpec
        try {
            responseSource = null
            response?.close()
            response = null
            openedUri = null
        } catch (ex: Exception) {
            if (currentDataSpec != null) {
                throw HttpDataSource.HttpDataSourceException(
                    ex as? IOException ?: IOException(ex),
                    currentDataSpec,
                    HttpDataSource.HttpDataSourceException.TYPE_CLOSE,
                )
            }
        } finally {
            if (opened) {
                opened = false
                transferEnded()
            }
            dataSpec = null
            responseCode = 0
            responseHeaders = emptyMap()
            bytesRemaining = C.LENGTH_UNSET.toLong()
        }
    }

    private fun buildRequest(dataSpec: DataSpec): Request {
        val builder = Request.Builder().url(dataSpec.uri.toString())
        val headers = linkedMapOf<String, String>()

        headers += defaultRequestProperties.getSnapshot()
        headers += dataSpec.httpRequestHeaders
        headers += requestProperties.getSnapshot()

        if (!headers.keys.any { it.equals("User-Agent", ignoreCase = true) }) {
            userAgent?.takeIf { it.isNotBlank() }?.let { headers["User-Agent"] = it }
        }

        if (!headers.keys.any { it.equals("Range", ignoreCase = true) }) {
            buildRangeHeader(dataSpec)?.let { headers["Range"] = it }
        }

        headers.forEach { (name, value) -> builder.header(name, value) }

        return when (dataSpec.httpMethod) {
            DataSpec.HTTP_METHOD_POST -> builder.post((dataSpec.httpBody ?: ByteArray(0)).toRequestBody()).build()
            DataSpec.HTTP_METHOD_HEAD -> builder.head().build()
            else -> builder.get().build()
        }
    }

    private fun buildRangeHeader(dataSpec: DataSpec): String? {
        val position = dataSpec.position
        val length = dataSpec.length
        if (position == 0L && length == C.LENGTH_UNSET.toLong()) return null

        val end = if (length == C.LENGTH_UNSET.toLong()) "" else (position + length - 1).toString()
        return "bytes=$position-$end"
    }

    class Factory : HttpDataSource.Factory {
        private var defaultRequestProperties = emptyMap<String, String>()
        private var userAgent: String? = null
        private var connectTimeoutMs: Int = 8000
        private var readTimeoutMs: Int = 8000

        override fun setDefaultRequestProperties(defaultRequestProperties: Map<String, String>): Factory {
            this.defaultRequestProperties = defaultRequestProperties.toMap()
            return this
        }

        fun setUserAgent(userAgent: String): Factory {
            this.userAgent = userAgent
            return this
        }

        fun setConnectTimeoutMs(connectTimeoutMs: Int): Factory {
            this.connectTimeoutMs = connectTimeoutMs
            return this
        }

        fun setReadTimeoutMs(readTimeoutMs: Int): Factory {
            this.readTimeoutMs = readTimeoutMs
            return this
        }

        override fun createDataSource(): OkHttpLiveHttpDataSource {
            val requestProperties = HttpDataSource.RequestProperties().apply {
                set(defaultRequestProperties)
            }
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
                .sslSocketFactory(
                    TrustAllSSLSocketFactory.sslSocketFactory,
                    TrustAllSSLSocketFactory.trustManager,
                )
                .hostnameVerifier { _, _ -> true }

            LiveNetworkProxy.applyTo(clientBuilder)

            return OkHttpLiveHttpDataSource(
                client = clientBuilder.build(),
                defaultRequestProperties = requestProperties,
                userAgent = userAgent,
            )
        }
    }
}
