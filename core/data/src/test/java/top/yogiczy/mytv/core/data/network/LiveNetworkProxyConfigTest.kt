package top.yogiczy.mytv.core.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Proxy

class LiveNetworkProxyConfigTest {
    @Test
    fun `disabled config does not create proxy`() {
        val config = LiveNetworkProxyConfig(
            enabled = false,
            host = "127.0.0.1",
            port = 7890,
        )

        assertNull(config.toProxy())
    }

    @Test
    fun `blank host does not create proxy`() {
        val config = LiveNetworkProxyConfig(
            enabled = true,
            host = " ",
            port = 7890,
        )

        assertNull(config.toProxy())
    }

    @Test
    fun `invalid port does not create proxy`() {
        val config = LiveNetworkProxyConfig(
            enabled = true,
            host = "127.0.0.1",
            port = 70000,
        )

        assertNull(config.toProxy())
    }

    @Test
    fun `valid config creates http proxy`() {
        val config = LiveNetworkProxyConfig(
            enabled = true,
            host = "127.0.0.1",
            port = 7890,
        )

        val proxy = config.toProxy()
        val address = proxy?.address() as InetSocketAddress

        assertEquals(Proxy.Type.HTTP, proxy.type())
        assertEquals("127.0.0.1", address.hostString)
        assertEquals(7890, address.port)
    }

    @Test
    fun `valid config creates http proxy url`() {
        val config = LiveNetworkProxyConfig(
            enabled = true,
            host = " 127.0.0.1 ",
            port = 7890,
        )

        assertEquals("http://127.0.0.1:7890", config.toHttpProxyUrl())
    }

    @Test
    fun `ipv6 config creates bracketed http proxy url`() {
        val config = LiveNetworkProxyConfig(
            enabled = true,
            host = "::1",
            port = 7890,
        )

        assertEquals("http://[::1]:7890", config.toHttpProxyUrl())
    }
}
