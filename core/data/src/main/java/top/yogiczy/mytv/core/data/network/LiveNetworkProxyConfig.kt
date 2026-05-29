package top.yogiczy.mytv.core.data.network

import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * 直播网络代理配置，仅用于直播源、节目单与播放流相关请求。
 */
data class LiveNetworkProxyConfig(
    val enabled: Boolean = false,
    val host: String = "",
    val port: Int = 0,
) {
    fun toProxy(): Proxy? {
        val normalizedHost = host.trim()
        if (!enabled || normalizedHost.isBlank() || port !in 1..65535) return null

        return Proxy(
            Proxy.Type.HTTP,
            InetSocketAddress.createUnresolved(normalizedHost, port),
        )
    }

    fun toHttpProxyUrl(): String? {
        val normalizedHost = host.trim()
        if (toProxy() == null) return null

        val hostForUrl = if (
            normalizedHost.contains(":") &&
            !normalizedHost.startsWith("[") &&
            !normalizedHost.endsWith("]")
        ) {
            "[$normalizedHost]"
        } else {
            normalizedHost
        }
        return "http://$hostForUrl:$port"
    }
}

object LiveNetworkProxy {
    @Volatile
    var configProvider: () -> LiveNetworkProxyConfig = { LiveNetworkProxyConfig() }

    fun current(): LiveNetworkProxyConfig = configProvider()

    fun applyTo(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        current().toProxy()?.let { builder.proxy(it) }
        return builder
    }
}
