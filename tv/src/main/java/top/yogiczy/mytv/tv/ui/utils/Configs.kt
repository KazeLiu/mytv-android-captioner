package top.yogiczy.mytv.tv.ui.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.yogiczy.mytv.core.data.entities.epg.EpgProgrammeReserveList
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSource
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSourceList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSource
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSourceList
import top.yogiczy.mytv.core.data.utils.Constants
import top.yogiczy.mytv.core.data.utils.SP
import top.yogiczy.mytv.tv.ui.screens.videoplayer.VideoPlayerDisplayMode

/**
 * 应用配置
 */
object Configs {
    const val CAPTIONER_TARGET_NONE = "none"

    enum class KEY {
        /** ==================== 应用 ==================== */
        /** 开机自启 */
        APP_BOOT_LAUNCH,

        /** 上一次最新版本 */
        APP_LAST_LATEST_VERSION,

        /** 协议已同意 */
        APP_AGREEMENT_AGREED,

        /** ==================== 调式 ==================== */
        /** 显示fps */
        DEBUG_SHOW_FPS,

        /** 播放器详细信息 */
        DEBUG_SHOW_VIDEO_PLAYER_METADATA,

        /** 显示布局网格 */
        DEBUG_SHOW_LAYOUT_GRIDS,

        /** ==================== 直播源 ==================== */
        /** 上一次频道序号 */
        IPTV_LAST_CHANNEL_IDX,

        /** 换台反转 */
        IPTV_CHANNEL_CHANGE_FLIP,

        /** 当前直播源 */
        IPTV_SOURCE_CURRENT,

        /** 直播源列表 */
        IPTV_SOURCE_LIST,

        /** 直播源缓存时间（毫秒） */
        IPTV_SOURCE_CACHE_TIME,

        /** 直播源可播放host列表 */
        IPTV_PLAYABLE_HOST_LIST,

        /** 是否启用数字选台 */
        IPTV_CHANNEL_NO_SELECT_ENABLE,

        /** 是否启用直播源频道收藏 */
        IPTV_CHANNEL_FAVORITE_ENABLE,

        /** 显示直播源频道收藏列表 */
        IPTV_CHANNEL_FAVORITE_LIST_VISIBLE,

        /** 直播源频道收藏列表 */
        IPTV_CHANNEL_FAVORITE_LIST,

        /** 直播源频道收藏换台边界跳出 */
        IPTV_CHANNEL_FAVORITE_CHANGE_BOUNDARY_JUMP_OUT,

        /** 直播源分组隐藏列表 */
        IPTV_CHANNEL_GROUP_HIDDEN_LIST,

        /** 混合模式 */
        IPTV_HYBRID_MODE,

        IPTV_CHANNEL_URL_INDEX,

        /** ==================== 节目单 ==================== */
        /** 启用节目单 */
        EPG_ENABLE,

        /** 当前节目单来源 */
        EPG_SOURCE_CURRENT,

        /** 节目单来源列表 */
        EPG_SOURCE_LIST,

        /** 节目单刷新时间阈值（小时） */
        EPG_REFRESH_TIME_THRESHOLD,

        /** 节目预约列表 */
        EPG_CHANNEL_RESERVE_LIST,

        /** ==================== 界面 ==================== */
        /** 显示节目进度 */
        UI_SHOW_EPG_PROGRAMME_PROGRESS,

        /** 显示常驻节目进度 */
        UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS,

        /** 显示台标 */
        UI_SHOW_CHANNEL_LOGO,

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

        /** ==================== 播放器 ==================== */
        /** 播放器 自定义ua */
        VIDEO_PLAYER_USER_AGENT,

        /** 播放器 加载超时 */
        VIDEO_PLAYER_LOAD_TIMEOUT,

        /** 播放器 显示模式 */
        VIDEO_PLAYER_DISPLAY_MODE,

        /** 播放器 强制音频软解 */
        VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE,

        /** 播放器 渲染方式 */
        VIDEO_PLAYER_RENDER_MODE,

        /** 播放器 类型 */
        VIDEO_PLAYER_TYPE,

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
    }

    /** ==================== 应用 ==================== */
    /** 开机自启 */
    var appBootLaunch: Boolean
        get() = SP.getBoolean(KEY.APP_BOOT_LAUNCH.name, false)
        set(value) = SP.putBoolean(KEY.APP_BOOT_LAUNCH.name, value)

    /** 上一次最新版本 */
    var appLastLatestVersion: String
        get() = SP.getString(KEY.APP_LAST_LATEST_VERSION.name, "")
        set(value) = SP.putString(KEY.APP_LAST_LATEST_VERSION.name, value)

    /** 协议已同意 */
    var appAgreementAgreed: Boolean
        get() = SP.getBoolean(KEY.APP_AGREEMENT_AGREED.name, false)
        set(value) = SP.putBoolean(KEY.APP_AGREEMENT_AGREED.name, value)

    /** ==================== 调式 ==================== */
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
    /** 上一次直播源序号 */
    var iptvLastChannelIdx: Int
        get() = SP.getInt(KEY.IPTV_LAST_CHANNEL_IDX.name, 0)
        set(value) = SP.putInt(KEY.IPTV_LAST_CHANNEL_IDX.name, value)

    /** 换台反转 */
    var iptvChannelChangeFlip: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_CHANGE_FLIP.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_CHANGE_FLIP.name, value)

    /** 当前直播源 */
    var iptvSourceCurrent: IptvSource
        get() = Json.decodeFromString(SP.getString(KEY.IPTV_SOURCE_CURRENT.name, "")
            .ifBlank { Json.encodeToString(IptvSource()) })
        set(value) = SP.putString(KEY.IPTV_SOURCE_CURRENT.name, Json.encodeToString(value))

    /** 直播源列表 */
    var iptvSourceList: IptvSourceList
        get() = Json.decodeFromString(
            SP.getString(KEY.IPTV_SOURCE_LIST.name, Json.encodeToString(IptvSourceList()))
        )
        set(value) = SP.putString(KEY.IPTV_SOURCE_LIST.name, Json.encodeToString(value))

    /** 直播源缓存时间（毫秒） */
    var iptvSourceCacheTime: Long
        get() = SP.getLong(KEY.IPTV_SOURCE_CACHE_TIME.name, Constants.IPTV_SOURCE_CACHE_TIME)
        set(value) = SP.putLong(KEY.IPTV_SOURCE_CACHE_TIME.name, value)

    var iptvChannelUrlIdx: Map<String, Int>
        get() {
            var jsonString=SP.getString(KEY.IPTV_CHANNEL_URL_INDEX.name, "")
            if (jsonString.isBlank()) {
                jsonString = "{}"
            }
            // 将 JSON 字符串转换回 Map
            val gson = Gson()
            val type = object : TypeToken<Map<String, Int>>() {}.type
            return gson.fromJson(jsonString, type)
        }
        set(value) {    // 将 Map 转换为 JSON 字符串
            val gson = Gson()
            val jsonString = gson.toJson(value)
            SP.putString(KEY.IPTV_CHANNEL_URL_INDEX.name, jsonString)
        }


    /** 直播源可播放host列表 */
    var iptvPlayableHostList: Set<String>
        get() = SP.getStringSet(KEY.IPTV_PLAYABLE_HOST_LIST.name, emptySet())
        set(value) = SP.putStringSet(KEY.IPTV_PLAYABLE_HOST_LIST.name, value)

    /** 是否启用数字选台 */
    var iptvChannelNoSelectEnable: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_NO_SELECT_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_NO_SELECT_ENABLE.name, value)

    /** 是否启用直播源频道收藏 */
    var iptvChannelFavoriteEnable: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_FAVORITE_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_FAVORITE_ENABLE.name, value)

    /** 显示直播源频道收藏列表 */
    var iptvChannelFavoriteListVisible: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_FAVORITE_LIST_VISIBLE.name, false)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_FAVORITE_LIST_VISIBLE.name, value)

    /** 直播源频道收藏列表 */
    var iptvChannelFavoriteList: Set<String>
        get() = SP.getStringSet(KEY.IPTV_CHANNEL_FAVORITE_LIST.name, emptySet())
        set(value) = SP.putStringSet(KEY.IPTV_CHANNEL_FAVORITE_LIST.name, value)

    /** 直播源频道收藏换台边界跳出 */
    var iptvChannelFavoriteChangeBoundaryJumpOut: Boolean
        get() = SP.getBoolean(KEY.IPTV_CHANNEL_FAVORITE_CHANGE_BOUNDARY_JUMP_OUT.name, true)
        set(value) = SP.putBoolean(KEY.IPTV_CHANNEL_FAVORITE_CHANGE_BOUNDARY_JUMP_OUT.name, value)

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

    /** ==================== 节目单 ==================== */
    /** 启用节目单 */
    var epgEnable: Boolean
        get() = SP.getBoolean(KEY.EPG_ENABLE.name, true)
        set(value) = SP.putBoolean(KEY.EPG_ENABLE.name, value)

    /** 当前节目单来源 */
    var epgSourceCurrent: EpgSource
        get() = Json.decodeFromString(SP.getString(KEY.EPG_SOURCE_CURRENT.name, "")
            .ifBlank { Json.encodeToString(EpgSource()) })
        set(value) = SP.putString(KEY.EPG_SOURCE_CURRENT.name, Json.encodeToString(value))

    /** 节目单来源列表 */
    var epgSourceList: EpgSourceList
        get() = Json.decodeFromString(
            SP.getString(KEY.EPG_SOURCE_LIST.name, Json.encodeToString(EpgSourceList()))
        )
        set(value) = SP.putString(KEY.EPG_SOURCE_LIST.name, Json.encodeToString(value))

    /** 节目单刷新时间阈值（小时） */
    var epgRefreshTimeThreshold: Int
        get() = SP.getInt(KEY.EPG_REFRESH_TIME_THRESHOLD.name, Constants.EPG_REFRESH_TIME_THRESHOLD)
        set(value) = SP.putInt(KEY.EPG_REFRESH_TIME_THRESHOLD.name, value)

    /** 节目预约列表 */
    var epgChannelReserveList: EpgProgrammeReserveList
        get() = Json.decodeFromString(
            SP.getString(
                KEY.EPG_CHANNEL_RESERVE_LIST.name, Json.encodeToString(EpgProgrammeReserveList())
            )
        )
        set(value) = SP.putString(KEY.EPG_CHANNEL_RESERVE_LIST.name, Json.encodeToString(value))

    /** ==================== 界面 ==================== */
    /** 显示节目进度 */
    var uiShowEpgProgrammeProgress: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PROGRESS.name, false)
        set(value) = SP.putBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PROGRESS.name, value)

    /** 显示常驻节目进度 */
    var uiShowEpgProgrammePermanentProgress: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS.name, false)
        set(value) = SP.putBoolean(KEY.UI_SHOW_EPG_PROGRAMME_PERMANENT_PROGRESS.name, value)

    /** 显示台标 */
    var uiShowChannelLogo: Boolean
        get() = SP.getBoolean(KEY.UI_SHOW_CHANNEL_LOGO.name, false)
        set(value) = SP.putBoolean(KEY.UI_SHOW_CHANNEL_LOGO.name, value)

    /** 使用经典选台界面 */
    var uiUseClassicPanelScreen: Boolean
        get() = SP.getBoolean(KEY.UI_USE_CLASSIC_PANEL_SCREEN.name, true)
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
            SP.getInt(KEY.UI_TIME_SHOW_MODE.name, UiTimeShowMode.HALF_HOUR.value)
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

    /** ==================== 播放器 ==================== */
    /** 播放器 自定义ua */
    var videoPlayerUserAgent: String
        get() = SP.getString(KEY.VIDEO_PLAYER_USER_AGENT.name, "").ifBlank {
            Constants.VIDEO_PLAYER_USER_AGENT
        }
        set(value) = SP.putString(KEY.VIDEO_PLAYER_USER_AGENT.name, value)

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
    var videoPlayerForceSoftDecode: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE.name, false)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_FORCE_AUDIO_SOFT_DECODE.name, value)

    /** 播放器 渲染方式 */
    var videoPlayerRenderMode: VideoPlayerRenderMode
        get() = VideoPlayerRenderMode.fromValue(
            SP.getInt(KEY.VIDEO_PLAYER_RENDER_MODE.name, VideoPlayerRenderMode.SURFACE_VIEW.value)
        )
        set(value) = SP.putInt(KEY.VIDEO_PLAYER_RENDER_MODE.name, value.value)

    /** 播放器 停止上一媒体项 */
    var videoPlayerStopPreviousMediaItem: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_STOP_PREVIOUS_MEDIA_ITEM.name, true)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_STOP_PREVIOUS_MEDIA_ITEM.name, value)

    /** 播放器 跳过同一VSync渲染多帧 */
    var videoPlayerSkipMultipleFramesOnSameVSync: Boolean
        get() = SP.getBoolean(KEY.VIDEO_PLAYER_SKIP_MULTIPLE_FRAMES_ON_SAME_VSYNC.name, false)
        set(value) = SP.putBoolean(KEY.VIDEO_PLAYER_SKIP_MULTIPLE_FRAMES_ON_SAME_VSYNC.name, value)

    /** 播放器类型 */
    enum class VideoPlayerType(val label: String) {
        IJK("IJK Player"),
        MEDIA3("Media3 Player")
    }

    /** 播放器类型 */
    var videoPlayerType: VideoPlayerType
        get() = VideoPlayerType.valueOf(SP.getString(KEY.VIDEO_PLAYER_TYPE.name, VideoPlayerType.IJK.name))
        set(value) = SP.putString(KEY.VIDEO_PLAYER_TYPE.name, value.name)

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
        get() = normalizeCaptionerSourceLanguage(SP.getString(KEY.CAPTIONER_SOURCE_LANGUAGE.name, "auto"))
        set(value) = SP.putString(KEY.CAPTIONER_SOURCE_LANGUAGE.name, normalizeCaptionerSourceLanguage(value))

    /** AI字幕 目标语言 */
    var captionerTargetLanguage: String
        get() = normalizeCaptionerTargetLanguage(SP.getString(KEY.CAPTIONER_TARGET_LANGUAGE.name, "Chinese"))
        set(value) {
            when (value.trim().lowercase()) {
                "chinesesimplified", "simplified chinese", "zh-hans", "hans", "简体", "简体中文" ->
                    captionerChineseScript = "simplified"

                "chinesetraditional", "traditional chinese", "zh-hant", "hant", "繁体", "繁體",
                "繁体中文", "繁體中文" -> captionerChineseScript = "traditional"
            }
            SP.putString(KEY.CAPTIONER_TARGET_LANGUAGE.name, normalizeCaptionerTargetLanguage(value))
        }

    /** AI字幕 中文输出书写格式 */
    var captionerChineseScript: String
        get() = normalizeCaptionerChineseScript(SP.getString(KEY.CAPTIONER_CHINESE_SCRIPT.name, "simplified"))
        set(value) = SP.putString(KEY.CAPTIONER_CHINESE_SCRIPT.name, normalizeCaptionerChineseScript(value))

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
        set(value) = SP.putString(KEY.CAPTIONER_ASR_MODEL.name, value.trim().ifBlank { "large-v2" })

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
        set(value) = SP.putLong(KEY.CAPTIONER_CHUNK_DURATION_MS.name, value.coerceIn(1000L, 15000L))

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
        set(value) = SP.putLong(KEY.CAPTIONER_DISPLAY_DURATION_MS.name, value.coerceIn(1000L, 30000L))

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
        set(value) = SP.putInt(KEY.CAPTIONER_PRIMARY_FONT_SIZE.name, value.coerceIn(12, 50))

    /** AI字幕 副字幕字号 */
    var captionerSecondaryFontSize: Int
        get() = SP.getInt(KEY.CAPTIONER_SECONDARY_FONT_SIZE.name, 18)
        set(value) = SP.putInt(KEY.CAPTIONER_SECONDARY_FONT_SIZE.name, value.coerceIn(12, 50))

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

    enum class UiTimeShowMode(val value: Int) {
        /** 隐藏 */
        HIDDEN(0),

        /** 常显 */
        ALWAYS(1),

        /** 整点 */
        EVERY_HOUR(2),

        /** 半点 */
        HALF_HOUR(3);

        companion object {
            fun fromValue(value: Int): UiTimeShowMode {
                return entries.firstOrNull { it.value == value } ?: ALWAYS
            }
        }
    }

    enum class IptvHybridMode(val value: Int) {
        /** 禁用 */
        DISABLE(0),

        /** 直播源优先 */
        IPTV_FIRST(1),

        /** 混合优先 */
        HYBRID_FIRST(2);

        companion object {
            fun fromValue(value: Int): IptvHybridMode {
                return entries.firstOrNull { it.value == value } ?: DISABLE
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
}
