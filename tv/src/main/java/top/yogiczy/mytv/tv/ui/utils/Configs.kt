package top.yogiczy.mytv.tv.ui.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import top.yogiczy.mytv.core.data.network.LiveNetworkProxyConfig
import top.yogiczy.mytv.core.data.entities.channel.Channel
import top.yogiczy.mytv.core.data.entities.channel.ChannelFavoriteList
import top.yogiczy.mytv.core.data.entities.epg.EpgProgrammeReserveList
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSource
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSourceList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSource
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSourceList
import top.yogiczy.mytv.core.data.utils.Constants
import top.yogiczy.mytv.core.data.utils.Globals
import top.yogiczy.mytv.core.data.utils.SP
import top.yogiczy.mytv.tv.sync.CloudSyncProvider
import top.yogiczy.mytv.tv.ui.screen.Screens
import top.yogiczy.mytv.tv.ui.screen.components.AppThemeDef
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.VideoPlayerDisplayMode

/**
 * 应用配置
 */
object Configs {
    const val CAPTIONER_TARGET_NONE = "none"

    enum class KEY {
        /** ==================== 应用 ==================== */
        /** 开机自启 */
        APP_BOOT_LAUNCH,

        /** 画中画启用 */
        APP_PIP_ENABLE,

        /** 上一次最新版本 */
        APP_LAST_LATEST_VERSION,

        /** 协议已同意 */
        APP_AGREEMENT_AGREED,

        /** 打开直接进入直播 */
        APP_STARTUP_SCREEN,

        /** ==================== 调式 ==================== */
        /** 开发者模式 */
        DEBUG_DEVELOPER_MODE,

        /** 显示fps */
        DEBUG_SHOW_FPS,

        /** 播放器详细信息 */
        DEBUG_SHOW_VIDEO_PLAYER_METADATA,

        /** 显示布局网格 */
        DEBUG_SHOW_LAYOUT_GRIDS,

        /** ==================== 直播源 ==================== */
        /** 当前直播源 */
        IPTV_SOURCE_CURRENT,

        /** 直播源列表 */
        IPTV_SOURCE_LIST,

        /** 直播源缓存时间（毫秒） */
        IPTV_SOURCE_CACHE_TIME,

        /** 直播源分组隐藏列表 */
        IPTV_CHANNEL_GROUP_HIDDEN_LIST,

        /** 混合模式 */
        IPTV_HYBRID_MODE,

        /** 相似频道合并 */
        IPTV_SIMILAR_CHANNEL_MERGE,

        /** 频道图标提供 */
        IPTV_CHANNEL_LOGO_PROVIDER,

        /** 频道图标覆盖 */
        IPTV_CHANNEL_LOGO_OVERRIDE,

        /** 是否启用直播源频道收藏 */
        IPTV_CHANNEL_FAVORITE_ENABLE,

        /** 显示直播源频道收藏列表 */
        IPTV_CHANNEL_FAVORITE_LIST_VISIBLE,

        /** 直播源频道收藏列表 */
        IPTV_CHANNEL_FAVORITE_LIST,

        /** 上一次播放频道 */
        IPTV_CHANNEL_LAST_PLAY,

        /** 直播源线路可播放host列表 */
        IPTV_CHANNEL_LINE_PLAYABLE_HOST_LIST,

        /** 直播源线路可播放地址列表 */
        IPTV_CHANNEL_LINE_PLAYABLE_URL_LIST,

        /** 换台反转 */
        IPTV_CHANNEL_CHANGE_FLIP,

        /** 是否启用数字选台 */
        IPTV_CHANNEL_NO_SELECT_ENABLE,

        /** 换台列表首尾循环 **/
        IPTV_CHANNEL_CHANGE_LIST_LOOP,

        /** ==================== 节目单 ==================== */
        /** 启用节目单 */
        EPG_ENABLE,

        /** 当前节目单来源 */
        EPG_SOURCE_CURRENT,

        /** 节目单来源列表 */
        EPG_SOURCE_LIST,

        /** 节目单刷新时间阈值（小时） */
        EPG_REFRESH_TIME_THRESHOLD,

        /** 节目单跟随直播源 */
        EPG_SOURCE_FOLLOW_IPTV,

        /** 节目预约列表 */
        EPG_CHANNEL_RESERVE_LIST,

        /** ==================== 界面 ==================== */
        /** 显示节目进度 */
        UI_SHOW_EPG_PROGRAMME_PROGRESS,

        /** 显示常驻节目进度 */
        UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS,

        /** 显示台标 */
        UI_SHOW_CHANNEL_LOGO,

        /** 显示频道预览 */
        UI_SHOW_CHANNEL_PREVIEW,

        /** 使用经典选台界面 */
        UI_USE_CLASSIC_PANEL_SCREEN,

        /** 界面密度缩放比例 */
        UI_DENSITY_SCALE_RATIO,

        /** 界面字体缩放比例 */
        UI_FONT_SCALE_RATIO,

        /** 时间显示模式 */
        UI_TIME_SHOW_MODE,

        /** 焦点优化 */
        UI_FOCUS_OPTIMIZE,

        /** 自动关闭界面延时 */
        UI_SCREEN_AUTO_CLOSE_DELAY,

        /** ==================== 更新 ==================== */
        /** 更新强提醒 */
        UPDATE_FORCE_REMIND,

        /** 更新通道 */
        UPDATE_CHANNEL,

        /** ==================== 网络 ==================== */
        /** 直播网络代理 开启 */
        LIVE_NETWORK_PROXY_ENABLE,

        /** 直播网络代理 主机 */
        LIVE_NETWORK_PROXY_HOST,

        /** 直播网络代理 端口 */
        LIVE_NETWORK_PROXY_PORT,

        /** ==================== 播放器 ==================== */
        /** 播放器 内核 */
        VIDEO_PLAYER_CORE,

        /** 播放器 渲染方式 */
        VIDEO_PLAYER_RENDER_MODE,

        /** 播放器 自定义ua */
        VIDEO_PLAYER_USER_AGENT,

        /** 播放器 自定义headers */
        VIDEO_PLAYER_HEADERS,

        /** 播放器 加载超时 */
        VIDEO_PLAYER_LOAD_TIMEOUT,

        /** 播放器 显示模式 */
        VIDEO_PLAYER_DISPLAY_MODE,

        /** 播放器 强制音频软解 */
        VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE,

        /** 播放器 停止上一媒体项 */
        VIDEO_PLAYER_STOP_PREVIOUS_MEDIA_ITEM,

        /** 播放器 跳过同一VSync渲染多帧 */
        VIDEO_PLAYER_SKIP_MULTIPLE_FRAMES_ON_SAME_VSYNC,

        /** AI字幕 开启 */
        CAPTIONER_ENABLED,

        /** AI字幕 后端地址 */
        CAPTIONER_SERVER_URL,

        /** AI字幕 源语言 */
        CAPTIONER_SOURCE_LANGUAGE,

        /** AI字幕 目标语言 */
        CAPTIONER_TARGET_LANGUAGE,

        /** AI字幕 中文输出书写格式 */
        CAPTIONER_CHINESE_SCRIPT,

        /** AI字幕 双语字幕 */
        CAPTIONER_BILINGUAL_ENABLED,

        /** AI字幕 ASR模型 */
        CAPTIONER_ASR_MODEL,

        /** AI字幕 翻译模型 */
        CAPTIONER_TRANSLATION_MODEL,

        /** AI字幕 分段上限 */
        CAPTIONER_CHUNK_DURATION_MS,

        /** AI字幕 临时字幕搜索宽度 */
        CAPTIONER_PARTIAL_BEAM_SIZE,

        /** AI字幕 修正字幕搜索宽度 */
        CAPTIONER_FINAL_BEAM_SIZE,

        /** AI字幕 显示时长 */
        CAPTIONER_DISPLAY_DURATION_MS,

        /** AI字幕 字幕颜色 */
        CAPTIONER_TEXT_COLOR,

        /** AI字幕 背景颜色 */
        CAPTIONER_BACKGROUND_COLOR,

        /** AI字幕 显示位置 */
        CAPTIONER_POSITION,

        /** AI字幕 水平偏移 */
        CAPTIONER_OFFSET_X,

        /** AI字幕 垂直偏移 */
        CAPTIONER_OFFSET_Y,

        /** AI字幕 文字对齐 */
        CAPTIONER_TEXT_ALIGN,

        /** AI字幕 单行模式 */
        CAPTIONER_SINGLE_LINE_MODE,

        /** AI字幕 主字幕字号 */
        CAPTIONER_PRIMARY_FONT_SIZE,

        /** AI字幕 副字幕字号 */
        CAPTIONER_SECONDARY_FONT_SIZE,

        /** ==================== 主题 ==================== */
        /** 当前应用主题 */
        THEME_APP_CURRENT,

        /** ==================== 云同步 ==================== */
        /** 云同步 自动拉取 */
        CLOUD_SYNC_AUTO_PULL,

        /** 云同步 提供商 */
        CLOUD_SYNC_PROVIDER,

        /** 云同步 github gist id */
        CLOUD_SYNC_GITHUB_GIST_ID,

        /** 云同步 github gist token */
        CLOUD_SYNC_GITHUB_GIST_TOKEN,

        /** 云同步 gitee gist id */
        CLOUD_SYNC_GITEE_GIST_ID,

        /** 云同步 gitee gist token */
        CLOUD_SYNC_GITEE_GIST_TOKEN,

        /** 云同步 网络链接 */
        CLOUD_SYNC_NETWORK_URL,

        /** 云同步 本地文件 */
        CLOUD_SYNC_LOCAL_FILE,

        /** 云同步 webdav url */
        CLOUD_SYNC_WEBDAV_URL,

        /** 云同步 webdav 用户名 */
        CLOUD_SYNC_WEBDAV_USERNAME,

        /** 云同步 webdav 密码 */
        CLOUD_SYNC_WEBDAV_PASSWORD,

        /** 肥羊 AllInOne 文件路径 */
        FEIYANG_ALLINONE_FILE_PATH,
    }

    /** ==================== 应用 ==================== */
    /** 开机自启 */
    var appBootLaunch: Boolean
        get() = SP.getBoolean(KEY.APP_BOOT_LAUNCH.name, false)
        set(value) = SP.putBoolean(KEY.APP_BOOT_LAUNCH.name, value)

    /** 画中画启用 */
    var appPipEnable: Boolean
        get() = SP.getBoolean(KEY.APP_PIP_ENABLE.name, false)
        set(value) = SP.putBoolean(KEY.APP_PIP_ENABLE.name, value)

    /** 上一次最新版本 */
    var appLastLatestVersion: String
        get() = SP.getString(KEY.APP_LAST_LATEST_VERSION.name, "")
        set(value) = SP.putString(KEY.APP_LAST_LATEST_VERSION.name, value)

    /** 协议已同意 */
    var appAgreementAgreed: Boolean
        get() = SP.getBoolean(KEY.APP_AGREEMENT_AGREED.name, false)
        set(value) = SP.putBoolean(KEY.APP_AGREEMENT_AGREED.name, value)

    /** 起始界面 */
    var appStartupScreen: String
        get() = SP.getString(KEY.APP_STARTUP_SCREEN.name, Screens.Dashboard.name)
        set(value) = SP.putString(KEY.APP_STARTUP_SCREEN.name, value)

    /** ==================== 调式 ==================== */
    /** 开发者模式 */
    var debugDeveloperMode: Boolean
        get() = SP.getBoolean(KEY.DEBUG_DEVELOPER_MODE.name, false)
        set(value) = SP.putBoolean(KEY.DEBUG_DEVELOPER_MODE.name, value)

    /** 显示fps */
    var debugShowFps: Boolean
        get() = SP.getBoolean(KEY.DEBUG_SHOW_FPS.name, false)
        set(value) = SP.putBoolean(KEY.DEBUG_SHOW_FPS.name, value)

    /** 播放器详细信息 */
    var debugShowVideoPlayerMetadata: Boolean
        get() = SP.getBoolean(KEY.DEBUG_SHOW_VIDEO_PLAYER_METADATA.name, false)
        set(value) = SP.putBoolean(KEY.DEBUG_SHOW_VIDEO_PLAYER_METADATA.name, value)

    /** 显示布局网格 */
    var debugShowLayoutGrids: Boolean
        get() = SP.getBoolean(KEY.DEBUG_SHOW_LAYOUT_GRIDS.name, false)
        set(value) = SP.putBoolean(KEY.DEBUG_SHOW_LAYOUT_GRIDS.name, value)

    /** ==================== 直播源 ==================== */
    /** 当前直播源 */
    var iptvSourceCurrent: IptvSource
        get() = Globals.json.decodeFromString(SP.getString(KEY.IPTV_SOURCE_CURRENT.name, "")
            .ifBlank { Globals.json.encodeToString(Constants.IPTV_SOURCE_LIST.first()) })
        set(value) = SP.putString(KEY.IPTV_SOURCE_CURRENT.name, Globals.json.encodeToString(value))

    /** 直播源列表 */
    var iptvSourceList: IptvSourceList
        get() = Globals.json.decodeFromString(
            SP.getString(KEY.IPTV_SOURCE_LIST.name, Globals.json.encodeToString(IptvSourceList()))
        )
        set(value) = SP.putString(KEY.IPTV_SOURCE_LIST.name, Globals.json.encodeToString(value))

    /** 直播源缓存时间（毫秒） */
    var iptvSourceCacheTime: Long
        get() = SP.getLong(KEY.IPTV_SOURCE_CACHE_TIME.name, Constants.IPTV_SOURCE_CACHE_TIME)
        set(value) = SP.putLong(KEY.IPTV_SOURCE_CACHE_TIME.name, value)

    /** 直播源分组隐藏列表 */
    var iptvChannelGroupHiddenList: Set<String>
        get() = SP.getStringSet(KEY.IPTV_CHANNEL_GROUP_HIDDEN_LIST.name, emptySet())
        set(value) = SP.putStringSet(KEY.IPTV_CHANNEL_GROUP_HIDDEN_LIST.name, value)

    /** 混合模式 */
    var iptvHybridMode: IptvHybridMode
        get() = IptvHybridMode.fromValue(
            SP.getInt(KEY.IPTV_HYBRID_MODE.name, IptvHybridMode.DISABLE.value)
        )
        set(value) = SP.putInt(KEY.IPTV_HYBRID_MODE.name, value.value)

    /** 相似频道合并 */
    var iptvSimilarChannelMerge: Boolean
        get() = SP.getBoolean(KEY.IPTV_SIMILAR_CHANNEL_MERGE.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_SIMILAR_CHANNEL_MERGE.name, value)

    /** 频道图标提供 */
    var iptvChannelLogoProvider: String
        get() = SP.getString(KEY.IPTV_CHANNEL_LOGO_PROVIDER.name, Constants.CHANNEL_LOGO_PROVIDER)
        set(value) = SP.putString(KEY.IPTV_CHANNEL_LOGO_PROVIDER.name, value)

    /** 频道图标覆盖 */
    var iptvChannelLogoOverride: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_LOGO_OVERRIDE.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_LOGO_OVERRIDE.name, value)

    /** 是否启用直播源频道收藏 */
    var iptvChannelFavoriteEnable: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_FAVORITE_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_FAVORITE_ENABLE.name, value)

    /** 显示直播源频道收藏列表 */
    var iptvChannelFavoriteListVisible: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_FAVORITE_LIST_VISIBLE.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_FAVORITE_LIST_VISIBLE.name, value)

    /** 直播源频道收藏列表 */
    var iptvChannelFavoriteList: ChannelFavoriteList
        get() = Globals.json.decodeFromString(
            SP.getString(
                KEY.IPTV_CHANNEL_FAVORITE_LIST.name,
                Globals.json.encodeToString(ChannelFavoriteList())
            )
        )
        set(value) = SP.putString(
            KEY.IPTV_CHANNEL_FAVORITE_LIST.name,
            Globals.json.encodeToString(value)
        )

    /** 上一次播放频道 */
    var iptvChannelLastPlay: Channel
        get() = Globals.json.decodeFromString(
            SP.getString(
                KEY.IPTV_CHANNEL_LAST_PLAY.name,
                Globals.json.encodeToString(Channel.EMPTY)
            )
        )
        set(value) = SP.putString(
            KEY.IPTV_CHANNEL_LAST_PLAY.name,
            Globals.json.encodeToString(value)
        )

    /** 直播源线路可播放host列表 */
    var iptvChannelLinePlayableHostList: Set<String>
        get() = SP.getStringSet(KEY.IPTV_CHANNEL_LINE_PLAYABLE_HOST_LIST.name, emptySet())
        set(value) = SP.putStringSet(KEY.IPTV_CHANNEL_LINE_PLAYABLE_HOST_LIST.name, value)

    /** 直播源线路可播放地址列表 */
    // IPTV_CHANNEL_LINE_PLAYABLE_URL_LIST,
    var iptvChannelLinePlayableUrlList: Set<String>
        get() = SP.getStringSet(KEY.IPTV_CHANNEL_LINE_PLAYABLE_URL_LIST.name, emptySet())
        set(value) = SP.putStringSet(KEY.IPTV_CHANNEL_LINE_PLAYABLE_URL_LIST.name, value)

    /** 换台反转 */
    var iptvChannelChangeFlip: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_CHANGE_FLIP.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_CHANGE_FLIP.name, value)

    /** 是否启用数字选台 */
    var iptvChannelNoSelectEnable: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_NO_SELECT_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_NO_SELECT_ENABLE.name, value)

    /** 换台列表首尾循环 **/
    var iptvChannelChangeListLoop: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_CHANGE_LIST_LOOP.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_CHANGE_LIST_LOOP.name, value)

    /** ==================== 节目单 ==================== */
    /** 启用节目单 */
    var epgEnable: Boolean
        get() = SP.getBoolean(KEY.EPG_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.EPG_ENABLE.name, value)

    /** 当前节目单来源 */
    var epgSourceCurrent: EpgSource
        get() = Globals.json.decodeFromString(SP.getString(KEY.EPG_SOURCE_CURRENT.name, "")
            .ifBlank { Globals.json.encodeToString(Constants.EPG_SOURCE_LIST.first()) })
        set(value) = SP.putString(KEY.EPG_SOURCE_CURRENT.name, Globals.json.encodeToString(value))

    /** 节目单来源列表 */
    var epgSourceList: EpgSourceList
        get() = Globals.json.decodeFromString(
            SP.getString(KEY.EPG_SOURCE_LIST.name, Globals.json.encodeToString(EpgSourceList()))
        )
        set(value) = SP.putString(KEY.EPG_SOURCE_LIST.name, Globals.json.encodeToString(value))

    /** 节目单刷新时间阈值（小时） */
    var epgRefreshTimeThreshold: Int
        get() = SP.getInt(KEY.EPG_REFRESH_TIME_THRESHOLD.name, Constants.EPG_REFRESH_TIME_THRESHOLD)
        set(value) = SP.putInt(KEY.EPG_REFRESH_TIME_THRESHOLD.name, value)

    /** 节目单跟随直播源 */
    var epgSourceFollowIptv: Boolean
        get() = SP.getBoolean(KEY.EPG_SOURCE_FOLLOW_IPTV.name, false)
        set(value) = SP.putBoolean(KEY.EPG_SOURCE_FOLLOW_IPTV.name, value)

    /** 节目预约列表 */
    var epgChannelReserveList: EpgProgrammeReserveList
        get() = Globals.json.decodeFromString(
            SP.getString(
                KEY.EPG_CHANNEL_RESERVE_LIST.name,
                Globals.json.encodeToString(EpgProgrammeReserveList())
            )
        )
        set(value) = SP.putString(
            KEY.EPG_CHANNEL_RESERVE_LIST.name,
            Globals.json.encodeToString(value)
        )

    /** ==================== 界面 ==================== */
    /** 显示节目进度 */
    var uiShowEpgProgrammeProgress: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PROGRESS.name, true)
        set(value) = SP.putBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PROGRESS.name, value)

    /** 显示常驻节目进度 */
    var uiShowEpgProgrammePermanentProgress: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS.name, false)
        set(value) = SP.putBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS.name, value)

    /** 显示台标 */
    var uiShowChannelLogo: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_CHANNEL_LOGO.name, true)
        set(value) = SP.putBoolean(KEY.UI_SHOW_CHANNEL_LOGO.name, value)

    /** 显示频道预览 */
    var uiShowChannelPreview: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_CHANNEL_PREVIEW.name, false)
        set(value) = SP.putBoolean(KEY.UI_SHOW_CHANNEL_PREVIEW.name, value)

    /** 使用经典选台界面 */
    var uiUseClassicPanelScreen: Boolean
        get() = SP.getBoolean(KEY.UI_USE_CLASSIC_PANEL_SCREEN.name, false)
        set(value) = SP.putBoolean(KEY.UI_USE_CLASSIC_PANEL_SCREEN.name, value)

    /** 界面密度缩放比例 */
    var uiDensityScaleRatio: Float
        get() = SP.getFloat(KEY.UI_DENSITY_SCALE_RATIO.name, 0f)
        set(value) = SP.putFloat(KEY.UI_DENSITY_SCALE_RATIO.name, value)

    /** 界面字体缩放比例 */
    var uiFontScaleRatio: Float
        get() = SP.getFloat(KEY.UI_FONT_SCALE_RATIO.name, 1f)
        set(value) = SP.putFloat(KEY.UI_FONT_SCALE_RATIO.name, value)

    /** 时间显示模式 */
    var uiTimeShowMode: UiTimeShowMode
        get() = UiTimeShowMode.fromValue(
            SP.getInt(KEY.UI_TIME_SHOW_MODE.name, UiTimeShowMode.HIDDEN.value)
        )
        set(value) = SP.putInt(KEY.UI_TIME_SHOW_MODE.name, value.value)

    /** 焦点优化 */
    var uiFocusOptimize: Boolean
        get() = SP.getBoolean(KEY.UI_FOCUS_OPTIMIZE.name, true)
        set(value) = SP.putBoolean(KEY.UI_FOCUS_OPTIMIZE.name, value)

    /** 自动关闭界面延时 */
    var uiScreenAutoCloseDelay: Long
        get() =
            SP.getLong(KEY.UI_SCREEN_AUTO_CLOSE_DELAY.name, Constants.UI_SCREEN_AUTO_CLOSE_DELAY)
        set(value) = SP.putLong(KEY.UI_SCREEN_AUTO_CLOSE_DELAY.name, value)

    /** ==================== 更新 ==================== */
    /** 更新强提醒 */
    var updateForceRemind: Boolean
        get() = SP.getBoolean(KEY.UPDATE_FORCE_REMIND.name, false)
        set(value) = SP.putBoolean(KEY.UPDATE_FORCE_REMIND.name, value)

    /** 更新通道 */
    var updateChannel: String
        get() = SP.getString(KEY.UPDATE_CHANNEL.name, "stable")
        set(value) = SP.putString(KEY.UPDATE_CHANNEL.name, value)

    /** ==================== 网络 ==================== */
    /** 直播网络代理 开启 */
    var liveNetworkProxyEnable: Boolean
        get() = SP.getBoolean(KEY.LIVE_NETWORK_PROXY_ENABLE.name, false)
        set(value) = SP.putBoolean(KEY.LIVE_NETWORK_PROXY_ENABLE.name, value)

    /** 直播网络代理 主机 */
    var liveNetworkProxyHost: String
        get() = SP.getString(KEY.LIVE_NETWORK_PROXY_HOST.name, "")
        set(value) = SP.putString(KEY.LIVE_NETWORK_PROXY_HOST.name, value.trim())

    /** 直播网络代理 端口 */
    var liveNetworkProxyPort: Int
        get() = SP.getInt(KEY.LIVE_NETWORK_PROXY_PORT.name, 0)
        set(value) = SP.putInt(KEY.LIVE_NETWORK_PROXY_PORT.name, value.coerceIn(0, 65535))

    val liveNetworkProxyConfig: LiveNetworkProxyConfig
        get() = LiveNetworkProxyConfig(
            enabled = liveNetworkProxyEnable,
            host = liveNetworkProxyHost,
            port = liveNetworkProxyPort,
        )

    /** ==================== 播放器 ==================== */
    /** 播放器 内核 */
    var videoPlayerCore: VideoPlayerCore
        get() = VideoPlayerCore.fromValue(
            SP.getInt(KEY.VIDEO_PLAYER_CORE.name, VideoPlayerCore.MEDIA3.value)
        )
        set(value) = SP.putInt(KEY.VIDEO_PLAYER_CORE.name, value.value)

    /** 播放器 渲染方式 */
    var videoPlayerRenderMode: VideoPlayerRenderMode
        get() = VideoPlayerRenderMode.fromValue(
            SP.getInt(KEY.VIDEO_PLAYER_RENDER_MODE.name, VideoPlayerRenderMode.SURFACE_VIEW.value)
        )
        set(value) = SP.putInt(KEY.VIDEO_PLAYER_RENDER_MODE.name, value.value)

    /** 播放器 自定义ua */
    var videoPlayerUserAgent: String
        get() = SP.getString(KEY.VIDEO_PLAYER_USER_AGENT.name, "").ifBlank {
            Constants.VIDEO_PLAYER_USER_AGENT
        }
        set(value) = SP.putString(KEY.VIDEO_PLAYER_USER_AGENT.name, value)

    /** 播放器 自定义headers */
    var videoPlayerHeaders: String
        get() = SP.getString(KEY.VIDEO_PLAYER_HEADERS.name, "")
        set(value) = SP.putString(KEY.VIDEO_PLAYER_HEADERS.name, value)

    /** 播放器 加载超时 */
    var videoPlayerLoadTimeout: Long
        get() = SP.getLong(KEY.VIDEO_PLAYER_LOAD_TIMEOUT.name, Constants.VIDEO_PLAYER_LOAD_TIMEOUT)
        set(value) = SP.putLong(KEY.VIDEO_PLAYER_LOAD_TIMEOUT.name, value)

    /** 播放器 显示模式 */
    var videoPlayerDisplayMode: VideoPlayerDisplayMode
        get() = VideoPlayerDisplayMode.fromValue(
            SP.getInt(KEY.VIDEO_PLAYER_DISPLAY_MODE.name, VideoPlayerDisplayMode.ORIGINAL.value)
        )
        set(value) = SP.putInt(KEY.VIDEO_PLAYER_DISPLAY_MODE.name, value.value)

    /** 播放器 强制音频软解 */
    var videoPlayerForceAudioSoftDecode: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE.name, false)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE.name, value)

    /** 播放器 停止上一媒体项 */
    var videoPlayerStopPreviousMediaItem: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_STOP_PREVIOUS_MEDIA_ITEM.name, true)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_STOP_PREVIOUS_MEDIA_ITEM.name, value)

    /** 播放器 跳过同一VSync渲染多帧 */
    var videoPlayerSkipMultipleFramesOnSameVSync: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_SKIP_MULTIPLE_FRAMES_ON_SAME_VSYNC.name, true)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_SKIP_MULTIPLE_FRAMES_ON_SAME_VSYNC.name, value)

    /** AI字幕 开启 */
    var captionerEnabled: Boolean
        get() = SP.getBoolean(KEY.CAPTIONER_ENABLED.name, false)
        set(value) = SP.putBoolean(KEY.CAPTIONER_ENABLED.name, value)

    /** AI字幕 后端地址 */
    var captionerServerUrl: String
        get() = SP.getString(KEY.CAPTIONER_SERVER_URL.name, "http://127.0.0.1:8765")
        set(value) = SP.putString(KEY.CAPTIONER_SERVER_URL.name, value.trim())

    /** AI字幕 源语言 */
    var captionerSourceLanguage: String
        get() = normalizeCaptionerSourceLanguage(
            SP.getString(KEY.CAPTIONER_SOURCE_LANGUAGE.name, "auto")
        )
        set(value) = SP.putString(
            KEY.CAPTIONER_SOURCE_LANGUAGE.name,
            normalizeCaptionerSourceLanguage(value)
        )

    /** AI字幕 目标语言 */
    var captionerTargetLanguage: String
        get() = normalizeCaptionerTargetLanguage(
            SP.getString(KEY.CAPTIONER_TARGET_LANGUAGE.name, "Chinese")
        )
        set(value) {
            when (value.trim().lowercase()) {
                "chinesesimplified", "simplified chinese", "zh-hans", "hans", "简体", "简体中文" ->
                    captionerChineseScript = "simplified"

                "chinesetraditional", "traditional chinese", "zh-hant", "hant", "繁体", "繁體",
                "繁体中文", "繁體中文" -> captionerChineseScript = "traditional"
            }
            SP.putString(
                KEY.CAPTIONER_TARGET_LANGUAGE.name,
                normalizeCaptionerTargetLanguage(value)
            )
        }

    /** AI字幕 中文输出书写格式 */
    var captionerChineseScript: String
        get() = normalizeCaptionerChineseScript(
            SP.getString(KEY.CAPTIONER_CHINESE_SCRIPT.name, "simplified")
        )
        set(value) = SP.putString(
            KEY.CAPTIONER_CHINESE_SCRIPT.name,
            normalizeCaptionerChineseScript(value)
        )

    /** AI字幕 是否启用翻译 */
    val captionerTranslationEnabled: Boolean
        get() = captionerTargetLanguage != CAPTIONER_TARGET_NONE

    /** AI字幕 双语字幕 */
    var captionerBilingualEnabled: Boolean
        get() = SP.getBoolean(KEY.CAPTIONER_BILINGUAL_ENABLED.name, true)
        set(value) = SP.putBoolean(KEY.CAPTIONER_BILINGUAL_ENABLED.name, value)

    /** AI字幕 ASR模型 */
    var captionerAsrModel: String
        get() = SP.getString(KEY.CAPTIONER_ASR_MODEL.name, "large-v2").ifBlank { "large-v2" }
        set(value) = SP.putString(
            KEY.CAPTIONER_ASR_MODEL.name,
            value.trim().ifBlank { "large-v2" }
        )

    /** AI字幕 翻译模型 */
    var captionerTranslationModel: String
        get() = SP.getString(
            KEY.CAPTIONER_TRANSLATION_MODEL.name,
            "qwen2.5-1.5b-instruct-gguf"
        ).ifBlank { "qwen2.5-1.5b-instruct-gguf" }
        set(value) = SP.putString(
            KEY.CAPTIONER_TRANSLATION_MODEL.name,
            value.trim().ifBlank { "qwen2.5-1.5b-instruct-gguf" },
        )

    /** AI字幕 分段上限 */
    var captionerChunkDurationMs: Long
        get() = SP.getLong(KEY.CAPTIONER_CHUNK_DURATION_MS.name, 5000L)
        set(value) = SP.putLong(
            KEY.CAPTIONER_CHUNK_DURATION_MS.name,
            value.coerceIn(1000L, 15000L)
        )

    /** AI字幕 临时字幕搜索宽度 */
    var captionerPartialBeamSize: Int
        get() = SP.getInt(KEY.CAPTIONER_PARTIAL_BEAM_SIZE.name, 1).coerceIn(1, 5)
        set(value) = SP.putInt(KEY.CAPTIONER_PARTIAL_BEAM_SIZE.name, value.coerceIn(1, 5))

    /** AI字幕 修正字幕搜索宽度 */
    var captionerFinalBeamSize: Int
        get() = SP.getInt(KEY.CAPTIONER_FINAL_BEAM_SIZE.name, 3).coerceIn(1, 5)
        set(value) = SP.putInt(KEY.CAPTIONER_FINAL_BEAM_SIZE.name, value.coerceIn(1, 5))

    /** AI字幕 显示时长 */
    var captionerDisplayDurationMs: Long
        get() = SP.getLong(KEY.CAPTIONER_DISPLAY_DURATION_MS.name, 7000L)
        set(value) = SP.putLong(
            KEY.CAPTIONER_DISPLAY_DURATION_MS.name,
            value.coerceIn(1000L, 30000L)
        )

    /** AI字幕 字幕颜色 */
    var captionerTextColor: CaptionerTextColor
        get() = CaptionerTextColor.fromValue(
            SP.getInt(KEY.CAPTIONER_TEXT_COLOR.name, CaptionerTextColor.WHITE.value)
        )
        set(value) = SP.putInt(KEY.CAPTIONER_TEXT_COLOR.name, value.value)

    /** AI字幕 背景颜色 */
    var captionerBackgroundColor: CaptionerBackgroundColor
        get() = CaptionerBackgroundColor.fromValue(
            SP.getInt(KEY.CAPTIONER_BACKGROUND_COLOR.name, CaptionerBackgroundColor.BLACK.value)
        )
        set(value) = SP.putInt(KEY.CAPTIONER_BACKGROUND_COLOR.name, value.value)

    /** AI字幕 显示位置 */
    var captionerPosition: CaptionerPosition
        get() = CaptionerPosition.fromValue(
            SP.getInt(KEY.CAPTIONER_POSITION.name, CaptionerPosition.BOTTOM.value)
        )
        set(value) = SP.putInt(KEY.CAPTIONER_POSITION.name, value.value)

    /** AI字幕 水平偏移 */
    var captionerOffsetX: Int
        get() = SP.getInt(KEY.CAPTIONER_OFFSET_X.name, 0)
        set(value) = SP.putInt(KEY.CAPTIONER_OFFSET_X.name, value.coerceIn(-1200, 1200))

    /** AI字幕 垂直偏移 */
    var captionerOffsetY: Int
        get() = SP.getInt(KEY.CAPTIONER_OFFSET_Y.name, 0)
        set(value) = SP.putInt(KEY.CAPTIONER_OFFSET_Y.name, value.coerceIn(-700, 700))

    /** AI字幕 文字对齐 */
    var captionerTextAlign: CaptionerTextAlign
        get() = CaptionerTextAlign.fromValue(
            SP.getInt(KEY.CAPTIONER_TEXT_ALIGN.name, CaptionerTextAlign.CENTER.value)
        )
        set(value) = SP.putInt(KEY.CAPTIONER_TEXT_ALIGN.name, value.value)

    /** AI字幕 单行模式 */
    var captionerSingleLineMode: Boolean
        get() = SP.getBoolean(KEY.CAPTIONER_SINGLE_LINE_MODE.name, false)
        set(value) = SP.putBoolean(KEY.CAPTIONER_SINGLE_LINE_MODE.name, value)

    /** AI字幕 主字幕字号 */
    var captionerPrimaryFontSize: Int
        get() = SP.getInt(KEY.CAPTIONER_PRIMARY_FONT_SIZE.name, 26)
        set(value) = SP.putInt(
            KEY.CAPTIONER_PRIMARY_FONT_SIZE.name,
            value.coerceIn(12, 50)
        )

    /** AI字幕 副字幕字号 */
    var captionerSecondaryFontSize: Int
        get() = SP.getInt(KEY.CAPTIONER_SECONDARY_FONT_SIZE.name, 18)
        set(value) = SP.putInt(
            KEY.CAPTIONER_SECONDARY_FONT_SIZE.name,
            value.coerceIn(12, 50)
        )

    /** ==================== 主题 ==================== */
    /** 当前应用主题 */
    var themeAppCurrent: AppThemeDef?
        get() = SP.getString(KEY.THEME_APP_CURRENT.name, "").let {
            if (it.isBlank()) null else Globals.json.decodeFromString(it)
        }
        set(value) = SP.putString(
            KEY.THEME_APP_CURRENT.name,
            value?.let { Globals.json.encodeToString(value) } ?: "")

    /** ==================== 云同步 ==================== */
    /** 云同步 自动拉取 */
    var cloudSyncAutoPull: Boolean
        get() = SP.getBoolean(KEY.CLOUD_SYNC_AUTO_PULL.name, false)
        set(value) = SP.putBoolean(KEY.CLOUD_SYNC_AUTO_PULL.name, value)

    /** 云同步 提供商 */
    var cloudSyncProvider: CloudSyncProvider
        get() = CloudSyncProvider.fromValue(
            SP.getInt(KEY.CLOUD_SYNC_PROVIDER.name, CloudSyncProvider.GITHUB_GIST.value)
        )
        set(value) = SP.putInt(KEY.CLOUD_SYNC_PROVIDER.name, value.value)

    /** 云同步 github gist id */
    var cloudSyncGithubGistId: String
        get() = SP.getString(KEY.CLOUD_SYNC_GITHUB_GIST_ID.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_GITHUB_GIST_ID.name, value)

    /** 云同步 github gist token */
    var cloudSyncGithubGistToken: String
        get() = SP.getString(KEY.CLOUD_SYNC_GITHUB_GIST_TOKEN.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_GITHUB_GIST_TOKEN.name, value)

    /** 云同步 gitee gist id */
    var cloudSyncGiteeGistId: String
        get() = SP.getString(KEY.CLOUD_SYNC_GITEE_GIST_ID.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_GITEE_GIST_ID.name, value)

    /** 云同步 gitee gist token */
    var cloudSyncGiteeGistToken: String
        get() = SP.getString(KEY.CLOUD_SYNC_GITEE_GIST_TOKEN.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_GITEE_GIST_TOKEN.name, value)

    /** 云同步 网络链接 */
    var cloudSyncNetworkUrl: String
        get() = SP.getString(KEY.CLOUD_SYNC_NETWORK_URL.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_NETWORK_URL.name, value)

    /** 云同步 本地文件 */
    var cloudSyncLocalFilePath: String
        get() = SP.getString(KEY.CLOUD_SYNC_LOCAL_FILE.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_LOCAL_FILE.name, value)

    /** 云同步 webdav url */
    var cloudSyncWebDavUrl: String
        get() = SP.getString(KEY.CLOUD_SYNC_WEBDAV_URL.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_WEBDAV_URL.name, value)

    /** 云同步 webdav 用户名 */
    // CLOUD_SYNC_WEBDAV_USERNAME,
    var cloudSyncWebDavUsername: String
        get() = SP.getString(KEY.CLOUD_SYNC_WEBDAV_USERNAME.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_WEBDAV_USERNAME.name, value)

    /** 云同步 webdav 密码 */
    var cloudSyncWebDavPassword: String
        get() = SP.getString(KEY.CLOUD_SYNC_WEBDAV_PASSWORD.name, "")
        set(value) = SP.putString(KEY.CLOUD_SYNC_WEBDAV_PASSWORD.name, value)

    /** 肥羊 AllInOne 文件路径 */
    var feiyangAllInOneFilePath: String
        get() = SP.getString(KEY.FEIYANG_ALLINONE_FILE_PATH.name, "")
        set(value) = SP.putString(KEY.FEIYANG_ALLINONE_FILE_PATH.name, value)

    enum class UiTimeShowMode(val value: Int, val label: String) {
        /** 隐藏 */
        HIDDEN(0, "隐藏"),

        /** 常显 */
        ALWAYS(1, "常显"),

        /** 整点 */
        EVERY_HOUR(2, "整点"),

        /** 半点 */
        HALF_HOUR(3, "半点");

        companion object {
            fun fromValue(value: Int): UiTimeShowMode {
                return entries.firstOrNull { it.value == value } ?: ALWAYS
            }
        }
    }

    enum class IptvHybridMode(val value: Int, val label: String) {
        /** 禁用 */
        DISABLE(0, "禁用"),

        /** 直播源优先 */
        IPTV_FIRST(1, "直播源优先"),

        /** 混合优先 */
        HYBRID_FIRST(2, "混合优先");

        companion object {
            fun fromValue(value: Int): IptvHybridMode {
                return entries.firstOrNull { it.value == value } ?: DISABLE
            }
        }
    }

    enum class VideoPlayerCore(val value: Int, val label: String) {
        /** Media3 */
        MEDIA3(0, "Media3"),

        /** IJK */
        IJK(1, "IjkPlayer");

        companion object {
            fun fromValue(value: Int): VideoPlayerCore {
                return entries.firstOrNull { it.value == value } ?: MEDIA3
            }
        }
    }

    enum class VideoPlayerRenderMode(val value: Int, val label: String) {
        /** SurfaceView */
        SURFACE_VIEW(0, "SurfaceView"),

        /** TextureView */
        TEXTURE_VIEW(1, "TextureView");

        companion object {
            fun fromValue(value: Int): VideoPlayerRenderMode {
                return entries.firstOrNull { it.value == value } ?: SURFACE_VIEW
            }
        }
    }

    enum class CaptionerTextColor(val value: Int, val label: String) {
        /** 白色 */
        WHITE(0xFFFFFFFF.toInt(), "白色"),

        /** 柔黄 */
        YELLOW(0xFFFFF176.toInt(), "柔黄"),

        /** 青蓝 */
        CYAN(0xFF80DEEA.toInt(), "青蓝"),

        /** 浅绿 */
        GREEN(0xFFA5D6A7.toInt(), "浅绿");

        companion object {
            fun fromValue(value: Int): CaptionerTextColor {
                return entries.firstOrNull { it.value == value } ?: WHITE
            }
        }
    }

    enum class CaptionerBackgroundColor(val value: Int, val label: String) {
        /** 深黑 */
        BLACK(0xAD000000.toInt(), "深黑"),

        /** 半透明 */
        TRANSLUCENT(0x80000000.toInt(), "半透明"),

        /** 深蓝 */
        BLUE(0xB30B1F3A.toInt(), "深蓝"),

        /** 无背景 */
        TRANSPARENT(0x00000000, "无背景");

        companion object {
            fun fromValue(value: Int): CaptionerBackgroundColor {
                return entries.firstOrNull { it.value == value } ?: BLACK
            }
        }
    }

    enum class CaptionerPosition(val value: Int, val label: String) {
        /** 顶部 */
        TOP(0, "顶部"),

        /** 居中 */
        CENTER(1, "居中"),

        /** 底部 */
        BOTTOM(2, "底部");

        companion object {
            fun fromValue(value: Int): CaptionerPosition {
                return entries.firstOrNull { it.value == value } ?: BOTTOM
            }
        }
    }

    enum class CaptionerTextAlign(val value: Int, val label: String) {
        /** 居左 */
        LEFT(0, "居左"),

        /** 居中 */
        CENTER(1, "居中"),

        /** 居右 */
        RIGHT(2, "居右");

        companion object {
            fun fromValue(value: Int): CaptionerTextAlign {
                return entries.firstOrNull { it.value == value } ?: CENTER
            }
        }
    }

    private fun normalizeCaptionerSourceLanguage(language: String): String {
        val value = language.trim()
        if (value.isBlank()) return "auto"

        return when (value.lowercase()) {
            "auto", "automatic", "自动", "自动识别" -> "auto"
            "zh", "cn", "chinese", "中文", "汉语", "普通话" -> "zh"
            "yue", "cantonese", "粤语" -> "yue"
            "en", "eng", "english", "英文", "英语" -> "en"
            "ja", "jp", "japanese", "日文", "日语" -> "ja"
            "ko", "kr", "korean", "韩文", "韩语" -> "ko"
            "fr", "french", "法文", "法语" -> "fr"
            "de", "german", "德文", "德语" -> "de"
            "es", "spanish", "西文", "西班牙语" -> "es"
            "ru", "russian", "俄文", "俄语" -> "ru"
            else -> value
        }
    }

    private fun normalizeCaptionerTargetLanguage(language: String): String {
        val value = language.trim()
        if (value.isBlank()) return "Chinese"

        return when (value.lowercase()) {
            CAPTIONER_TARGET_NONE, "off", "false", "no", "disable", "disabled",
            "asr", "asr-only", "notranslate", "no-translate", "no translate",
            "不翻译", "不翻譯", "关闭翻译", "只转写", "仅转写" -> CAPTIONER_TARGET_NONE

            "chinesesimplified", "chinesetraditional", "simplified chinese", "traditional chinese",
            "zh-hans", "zh-hant", "hans", "hant", "简体", "简体中文", "繁体", "繁體",
            "繁体中文", "繁體中文" -> "Chinese"

            else -> value
        }
    }

    private fun normalizeCaptionerChineseScript(script: String): String {
        val value = script.trim()
        if (value.isBlank()) return "simplified"

        return when (value.lowercase()) {
            "simplified", "simple", "hans", "zh-hans", "sc", "cn", "简体", "简体中文" -> "simplified"
            "traditional", "trad", "hant", "zh-hant", "tc", "繁体", "繁體", "繁体中文", "繁體中文" -> "traditional"
            "original", "raw", "keep", "保留原文", "原始" -> "original"
            else -> value
        }
    }

    fun toPartial(): Partial {
        return Partial(
            appBootLaunch = appBootLaunch,
            appPipEnable = appPipEnable,
            appLastLatestVersion = appLastLatestVersion,
            appAgreementAgreed = appAgreementAgreed,
            appStartupScreen = appStartupScreen,
            debugDeveloperMode = debugDeveloperMode,
            debugShowFps = debugShowFps,
            debugShowVideoPlayerMetadata = debugShowVideoPlayerMetadata,
            debugShowLayoutGrids = debugShowLayoutGrids,
            iptvSourceCacheTime = iptvSourceCacheTime,
            iptvSourceCurrent = iptvSourceCurrent,
            iptvSourceList = iptvSourceList,
            iptvChannelGroupHiddenList = iptvChannelGroupHiddenList,
            iptvHybridMode = iptvHybridMode,
            iptvSimilarChannelMerge = iptvSimilarChannelMerge,
            iptvChannelLogoProvider = iptvChannelLogoProvider,
            iptvChannelLogoOverride = iptvChannelLogoOverride,
            iptvChannelFavoriteEnable = iptvChannelFavoriteEnable,
            iptvChannelFavoriteListVisible = iptvChannelFavoriteListVisible,
            iptvChannelFavoriteList = iptvChannelFavoriteList,
            iptvChannelLastPlay = iptvChannelLastPlay,
            iptvChannelLinePlayableHostList = iptvChannelLinePlayableHostList,
            iptvChannelLinePlayableUrlList = iptvChannelLinePlayableUrlList,
            iptvChannelChangeFlip = iptvChannelChangeFlip,
            iptvChannelNoSelectEnable = iptvChannelNoSelectEnable,
            iptvChannelChangeListLoop = iptvChannelChangeListLoop,
            epgEnable = epgEnable,
            epgSourceCurrent = epgSourceCurrent,
            epgSourceList = epgSourceList,
            epgRefreshTimeThreshold = epgRefreshTimeThreshold,
            epgSourceFollowIptv = epgSourceFollowIptv,
            epgChannelReserveList = epgChannelReserveList,
            uiShowEpgProgrammeProgress = uiShowEpgProgrammeProgress,
            uiShowEpgProgrammePermanentProgress = uiShowEpgProgrammePermanentProgress,
            uiShowChannelLogo = uiShowChannelLogo,
            uiShowChannelPreview = uiShowChannelPreview,
            uiUseClassicPanelScreen = uiUseClassicPanelScreen,
            uiDensityScaleRatio = uiDensityScaleRatio,
            uiFontScaleRatio = uiFontScaleRatio,
            uiTimeShowMode = uiTimeShowMode,
            uiFocusOptimize = uiFocusOptimize,
            uiScreenAutoCloseDelay = uiScreenAutoCloseDelay,
            updateForceRemind = updateForceRemind,
            updateChannel = updateChannel,
            liveNetworkProxyEnable = liveNetworkProxyEnable,
            liveNetworkProxyHost = liveNetworkProxyHost,
            liveNetworkProxyPort = liveNetworkProxyPort,
            videoPlayerCore = videoPlayerCore,
            videoPlayerRenderMode = videoPlayerRenderMode,
            videoPlayerUserAgent = videoPlayerUserAgent,
            videoPlayerHeaders = videoPlayerHeaders,
            videoPlayerLoadTimeout = videoPlayerLoadTimeout,
            videoPlayerDisplayMode = videoPlayerDisplayMode,
            videoPlayerForceAudioSoftDecode = videoPlayerForceAudioSoftDecode,
            videoPlayerStopPreviousMediaItem = videoPlayerStopPreviousMediaItem,
            videoPlayerSkipMultipleFramesOnSameVSync = videoPlayerSkipMultipleFramesOnSameVSync,
            captionerEnabled = captionerEnabled,
            captionerServerUrl = captionerServerUrl,
            captionerSourceLanguage = captionerSourceLanguage,
            captionerTargetLanguage = captionerTargetLanguage,
            captionerChineseScript = captionerChineseScript,
            captionerBilingualEnabled = captionerBilingualEnabled,
            captionerAsrModel = captionerAsrModel,
            captionerTranslationModel = captionerTranslationModel,
            captionerChunkDurationMs = captionerChunkDurationMs,
            captionerPartialBeamSize = captionerPartialBeamSize,
            captionerFinalBeamSize = captionerFinalBeamSize,
            captionerDisplayDurationMs = captionerDisplayDurationMs,
            captionerTextColor = captionerTextColor,
            captionerBackgroundColor = captionerBackgroundColor,
            captionerPosition = captionerPosition,
            captionerOffsetX = captionerOffsetX,
            captionerOffsetY = captionerOffsetY,
            captionerTextAlign = captionerTextAlign,
            captionerSingleLineMode = captionerSingleLineMode,
            captionerPrimaryFontSize = captionerPrimaryFontSize,
            captionerSecondaryFontSize = captionerSecondaryFontSize,
            themeAppCurrent = themeAppCurrent,
            cloudSyncAutoPull = cloudSyncAutoPull,
            cloudSyncProvider = cloudSyncProvider,
            cloudSyncGithubGistId = cloudSyncGithubGistId,
            cloudSyncGithubGistToken = cloudSyncGithubGistToken,
            cloudSyncGiteeGistId = cloudSyncGiteeGistId,
            cloudSyncGiteeGistToken = cloudSyncGiteeGistToken,
            cloudSyncNetworkUrl = cloudSyncNetworkUrl,
            cloudSyncLocalFilePath = cloudSyncLocalFilePath,
            cloudSyncWebDavUrl = cloudSyncWebDavUrl,
            cloudSyncWebDavUsername = cloudSyncWebDavUsername,
            cloudSyncWebDavPassword = cloudSyncWebDavPassword,
            feiyangAllInOneFilePath = feiyangAllInOneFilePath,
        )
    }

    fun fromPartial(configs: Partial) {
        configs.appBootLaunch?.let { appBootLaunch = it }
        configs.appPipEnable?.let { appPipEnable = it }
        configs.appLastLatestVersion?.let { appLastLatestVersion = it }
        configs.appAgreementAgreed?.let { appAgreementAgreed = it }
        configs.appStartupScreen?.let { appStartupScreen = it }
        configs.debugDeveloperMode?.let { debugDeveloperMode = it }
        configs.debugShowFps?.let { debugShowFps = it }
        configs.debugShowVideoPlayerMetadata?.let { debugShowVideoPlayerMetadata = it }
        configs.debugShowLayoutGrids?.let { debugShowLayoutGrids = it }
        configs.iptvSourceCacheTime?.let { iptvSourceCacheTime = it }
        configs.iptvSourceCurrent?.let { iptvSourceCurrent = it }
        configs.iptvSourceList?.let { iptvSourceList = it }
        configs.iptvChannelGroupHiddenList?.let { iptvChannelGroupHiddenList = it }
        configs.iptvHybridMode?.let { iptvHybridMode = it }
        configs.iptvSimilarChannelMerge?.let { iptvSimilarChannelMerge = it }
        configs.iptvChannelLogoProvider?.let { iptvChannelLogoProvider = it }
        configs.iptvChannelLogoOverride?.let { iptvChannelLogoOverride = it }
        configs.iptvChannelFavoriteEnable?.let { iptvChannelFavoriteEnable = it }
        configs.iptvChannelFavoriteListVisible?.let { iptvChannelFavoriteListVisible = it }
        configs.iptvChannelFavoriteList?.let { iptvChannelFavoriteList = it }
        configs.iptvChannelLastPlay?.let { iptvChannelLastPlay = it }
        configs.iptvChannelLinePlayableHostList?.let { iptvChannelLinePlayableHostList = it }
        configs.iptvChannelLinePlayableUrlList?.let { iptvChannelLinePlayableUrlList = it }
        configs.iptvChannelChangeFlip?.let { iptvChannelChangeFlip = it }
        configs.iptvChannelNoSelectEnable?.let { iptvChannelNoSelectEnable = it }
        configs.iptvChannelChangeListLoop?.let { iptvChannelChangeListLoop = it }
        configs.epgEnable?.let { epgEnable = it }
        configs.epgSourceCurrent?.let { epgSourceCurrent = it }
        configs.epgSourceList?.let { epgSourceList = it }
        configs.epgRefreshTimeThreshold?.let { epgRefreshTimeThreshold = it }
        configs.epgSourceFollowIptv?.let { epgSourceFollowIptv = it }
        configs.epgChannelReserveList?.let { epgChannelReserveList = it }
        configs.uiShowEpgProgrammeProgress?.let { uiShowEpgProgrammeProgress = it }
        configs.uiShowEpgProgrammePermanentProgress?.let {
            uiShowEpgProgrammePermanentProgress = it
        }
        configs.uiShowChannelLogo?.let { uiShowChannelLogo = it }
        configs.uiShowChannelPreview?.let { uiShowChannelPreview = it }
        configs.uiUseClassicPanelScreen?.let { uiUseClassicPanelScreen = it }
        configs.uiDensityScaleRatio?.let { uiDensityScaleRatio = it }
        configs.uiFontScaleRatio?.let { uiFontScaleRatio = it }
        configs.uiTimeShowMode?.let { uiTimeShowMode = it }
        configs.uiFocusOptimize?.let { uiFocusOptimize = it }
        configs.uiScreenAutoCloseDelay?.let { uiScreenAutoCloseDelay = it }
        configs.updateForceRemind?.let { updateForceRemind = it }
        configs.updateChannel?.let { updateChannel = it }
        configs.liveNetworkProxyEnable?.let { liveNetworkProxyEnable = it }
        configs.liveNetworkProxyHost?.let { liveNetworkProxyHost = it }
        configs.liveNetworkProxyPort?.let { liveNetworkProxyPort = it }
        configs.videoPlayerCore?.let { videoPlayerCore = it }
        configs.videoPlayerRenderMode?.let { videoPlayerRenderMode = it }
        configs.videoPlayerUserAgent?.let { videoPlayerUserAgent = it }
        configs.videoPlayerHeaders?.let { videoPlayerHeaders = it }
        configs.videoPlayerLoadTimeout?.let { videoPlayerLoadTimeout = it }
        configs.videoPlayerDisplayMode?.let { videoPlayerDisplayMode = it }
        configs.videoPlayerForceAudioSoftDecode?.let { videoPlayerForceAudioSoftDecode = it }
        configs.videoPlayerStopPreviousMediaItem?.let { videoPlayerStopPreviousMediaItem = it }
        configs.videoPlayerSkipMultipleFramesOnSameVSync?.let {
            videoPlayerSkipMultipleFramesOnSameVSync = it
        }
        configs.captionerEnabled?.let { captionerEnabled = it }
        configs.captionerServerUrl?.let { captionerServerUrl = it }
        configs.captionerSourceLanguage?.let { captionerSourceLanguage = it }
        configs.captionerTargetLanguage?.let { captionerTargetLanguage = it }
        configs.captionerChineseScript?.let { captionerChineseScript = it }
        configs.captionerBilingualEnabled?.let { captionerBilingualEnabled = it }
        configs.captionerAsrModel?.let { captionerAsrModel = it }
        configs.captionerTranslationModel?.let { captionerTranslationModel = it }
        configs.captionerChunkDurationMs?.let { captionerChunkDurationMs = it }
        configs.captionerPartialBeamSize?.let { captionerPartialBeamSize = it }
        configs.captionerFinalBeamSize?.let { captionerFinalBeamSize = it }
        configs.captionerDisplayDurationMs?.let { captionerDisplayDurationMs = it }
        configs.captionerTextColor?.let { captionerTextColor = it }
        configs.captionerBackgroundColor?.let { captionerBackgroundColor = it }
        configs.captionerPosition?.let { captionerPosition = it }
        configs.captionerOffsetX?.let { captionerOffsetX = it }
        configs.captionerOffsetY?.let { captionerOffsetY = it }
        configs.captionerTextAlign?.let { captionerTextAlign = it }
        configs.captionerSingleLineMode?.let { captionerSingleLineMode = it }
        configs.captionerPrimaryFontSize?.let { captionerPrimaryFontSize = it }
        configs.captionerSecondaryFontSize?.let { captionerSecondaryFontSize = it }
        configs.themeAppCurrent?.let { themeAppCurrent = it }
        configs.cloudSyncAutoPull?.let { cloudSyncAutoPull = it }
        configs.cloudSyncProvider?.let { cloudSyncProvider = it }
        configs.cloudSyncGithubGistId?.let { cloudSyncGithubGistId = it }
        configs.cloudSyncGithubGistToken?.let { cloudSyncGithubGistToken = it }
        configs.cloudSyncGiteeGistId?.let { cloudSyncGiteeGistId = it }
        configs.cloudSyncGiteeGistToken?.let { cloudSyncGiteeGistToken = it }
        configs.cloudSyncNetworkUrl?.let { cloudSyncNetworkUrl = it }
        configs.cloudSyncLocalFilePath?.let { cloudSyncLocalFilePath = it }
        configs.cloudSyncWebDavUrl?.let { cloudSyncWebDavUrl = it }
        configs.cloudSyncWebDavUsername?.let { cloudSyncWebDavUsername = it }
        configs.cloudSyncWebDavPassword?.let { cloudSyncWebDavPassword = it }
        configs.feiyangAllInOneFilePath?.let { feiyangAllInOneFilePath = it }
    }

    @Serializable
    data class Partial(
        val appBootLaunch: Boolean? = null,
        val appPipEnable: Boolean? = null,
        val appLastLatestVersion: String? = null,
        val appAgreementAgreed: Boolean? = null,
        val appStartupScreen: String? = null,
        val debugDeveloperMode: Boolean? = null,
        val debugShowFps: Boolean? = null,
        val debugShowVideoPlayerMetadata: Boolean? = null,
        val debugShowLayoutGrids: Boolean? = null,
        val iptvSourceCacheTime: Long? = null,
        val iptvSourceCurrent: IptvSource? = null,
        val iptvSourceList: IptvSourceList? = null,
        val iptvChannelGroupHiddenList: Set<String>? = null,
        val iptvHybridMode: IptvHybridMode? = null,
        val iptvSimilarChannelMerge: Boolean? = null,
        val iptvChannelLogoProvider: String? = null,
        val iptvChannelLogoOverride: Boolean? = null,
        val iptvChannelFavoriteEnable: Boolean? = null,
        val iptvChannelFavoriteListVisible: Boolean? = null,
        val iptvChannelFavoriteList: ChannelFavoriteList? = null,
        val iptvChannelLastPlay: Channel? = null,
        val iptvChannelLinePlayableHostList: Set<String>? = null,
        val iptvChannelLinePlayableUrlList: Set<String>? = null,
        val iptvChannelChangeFlip: Boolean? = null,
        val iptvChannelNoSelectEnable: Boolean? = null,
        val iptvChannelChangeListLoop: Boolean? = null,
        val epgEnable: Boolean? = null,
        val epgSourceCurrent: EpgSource? = null,
        val epgSourceList: EpgSourceList? = null,
        val epgRefreshTimeThreshold: Int? = null,
        val epgSourceFollowIptv: Boolean? = null,
        val epgChannelReserveList: EpgProgrammeReserveList? = null,
        val uiShowEpgProgrammeProgress: Boolean? = null,
        val uiShowEpgProgrammePermanentProgress: Boolean? = null,
        val uiShowChannelLogo: Boolean? = null,
        val uiShowChannelPreview: Boolean? = null,
        val uiUseClassicPanelScreen: Boolean? = null,
        val uiDensityScaleRatio: Float? = null,
        val uiFontScaleRatio: Float? = null,
        val uiTimeShowMode: UiTimeShowMode? = null,
        val uiFocusOptimize: Boolean? = null,
        val uiScreenAutoCloseDelay: Long? = null,
        val updateForceRemind: Boolean? = null,
        val updateChannel: String? = null,
        val liveNetworkProxyEnable: Boolean? = null,
        val liveNetworkProxyHost: String? = null,
        val liveNetworkProxyPort: Int? = null,
        val videoPlayerCore: VideoPlayerCore? = null,
        val videoPlayerRenderMode: VideoPlayerRenderMode? = null,
        val videoPlayerUserAgent: String? = null,
        val videoPlayerHeaders: String? = null,
        val videoPlayerLoadTimeout: Long? = null,
        val videoPlayerDisplayMode: VideoPlayerDisplayMode? = null,
        val videoPlayerForceAudioSoftDecode: Boolean? = null,
        val videoPlayerStopPreviousMediaItem: Boolean? = null,
        val videoPlayerSkipMultipleFramesOnSameVSync: Boolean? = null,
        val captionerEnabled: Boolean? = null,
        val captionerServerUrl: String? = null,
        val captionerSourceLanguage: String? = null,
        val captionerTargetLanguage: String? = null,
        val captionerChineseScript: String? = null,
        val captionerBilingualEnabled: Boolean? = null,
        val captionerAsrModel: String? = null,
        val captionerTranslationModel: String? = null,
        val captionerChunkDurationMs: Long? = null,
        val captionerPartialBeamSize: Int? = null,
        val captionerFinalBeamSize: Int? = null,
        val captionerDisplayDurationMs: Long? = null,
        val captionerTextColor: CaptionerTextColor? = null,
        val captionerBackgroundColor: CaptionerBackgroundColor? = null,
        val captionerPosition: CaptionerPosition? = null,
        val captionerOffsetX: Int? = null,
        val captionerOffsetY: Int? = null,
        val captionerTextAlign: CaptionerTextAlign? = null,
        val captionerSingleLineMode: Boolean? = null,
        val captionerPrimaryFontSize: Int? = null,
        val captionerSecondaryFontSize: Int? = null,
        val themeAppCurrent: AppThemeDef? = null,
        val cloudSyncAutoPull: Boolean? = null,
        val cloudSyncProvider: CloudSyncProvider? = null,
        val cloudSyncGithubGistId: String? = null,
        val cloudSyncGithubGistToken: String? = null,
        val cloudSyncGiteeGistId: String? = null,
        val cloudSyncGiteeGistToken: String? = null,
        val cloudSyncNetworkUrl: String? = null,
        val cloudSyncLocalFilePath: String? = null,
        val cloudSyncWebDavUrl: String? = null,
        val cloudSyncWebDavUsername: String? = null,
        val cloudSyncWebDavPassword: String? = null,
        val feiyangAllInOneFilePath: String? = null,
    ) {
        fun desensitized() = copy(
            cloudSyncAutoPull = null,
            cloudSyncProvider = null,
            cloudSyncGithubGistId = null,
            cloudSyncGithubGistToken = null,
            cloudSyncGiteeGistId = null,
            cloudSyncGiteeGistToken = null,
            cloudSyncNetworkUrl = null,
            cloudSyncLocalFilePath = null,
            cloudSyncWebDavUrl = null,
            cloudSyncWebDavUsername = null,
            cloudSyncWebDavPassword = null,
            liveNetworkProxyEnable = null,
            liveNetworkProxyHost = null,
            liveNetworkProxyPort = null,
            iptvChannelLastPlay = null,
            iptvChannelLinePlayableHostList = null,
            iptvChannelLinePlayableUrlList = null,
        )
    }
}
