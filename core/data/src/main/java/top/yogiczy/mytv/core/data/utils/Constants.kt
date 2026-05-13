package top.yogiczy.mytv.core.data.utils

import top.yogiczy.mytv.core.data.entities.epgsource.EpgSourceList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSourceList

/**
 * 常量
 */
object Constants {
    /**
     * 应用 标题
     */
    const val APP_TITLE = "我的电视"

    /**
     * 应用 代码仓库
     */
    const val APP_REPO = "https://github.com/KazeLiu/mytv-android-captioner"

    /**
     * GitHub加速代理地址
     */
    const val GITHUB_PROXY = "https://gh.monlor.com/"

    const val WEBVIEW_CHANNELS_URL="https://raw.githubusercontent.com/minyoad/my-iptv/refs/heads/master/list/webview_channels.txt"

    /**
     * IPTV直播源
     */
    val IPTV_SOURCE_LIST = IptvSourceList()

    /**
     * IPTV源缓存时间（毫秒）
     */
    const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 60 * 24L // 24小时

    /**
     * 节目单来源
     */
    val EPG_SOURCE_LIST = EpgSourceList()

    /**
     * 频道logo来源
     */
    const val CHANNEL_LOGO_SOURCE="https://iptv-cdn.mybacc.com/logo/"

    /**
     * 节目单刷新时间阈值（小时）
     */
    const val EPG_REFRESH_TIME_THRESHOLD = 2 // 不到2点不刷新

    /**
     * Git最新版本信息
     */
    val GIT_RELEASE_LATEST_URL = mapOf(
        "stable" to GITHUB_PROXY+"https://raw.githubusercontent.com/minyoad/mytv-android-update/main/tv-stable.json",
        "beta" to GITHUB_PROXY+"https://raw.githubusercontent.com/minyoad/mytv-android-update/main/tv-beta.json",
    )

    /**
     * HTTP请求重试次数
     */
    const val HTTP_RETRY_COUNT = 10L

    /**
     * HTTP请求重试间隔时间（毫秒）
     */
    const val HTTP_RETRY_INTERVAL = 3000L

    /**
     * 播放器 userAgent
     */
    const val VIDEO_PLAYER_USER_AGENT = "ExoPlayer"

    /**
     * 播放器加载超时
     */
    const val VIDEO_PLAYER_LOAD_TIMEOUT = 1000L * 15 // 15秒

    /**
     * 日志历史最大保留条数
     */
    const val LOG_HISTORY_MAX_SIZE = 50

    /**
     * 界面 临时频道界面显示时间
     */
    const val UI_TEMP_CHANNEL_SCREEN_SHOW_DURATION = 1500L // 1.5秒

    /**
     * 界面 超时未操作自动关闭界面
     */
    const val UI_SCREEN_AUTO_CLOSE_DELAY = 1000L * 15 // 15秒

    /**
     * 界面 时间显示前后范围
     */
    const val UI_TIME_SCREEN_SHOW_DURATION = 1000L * 30 // 前后30秒
}
