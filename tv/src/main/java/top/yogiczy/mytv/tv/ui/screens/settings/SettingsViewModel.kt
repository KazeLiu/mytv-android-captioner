package top.yogiczy.mytv.tv.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import top.yogiczy.mytv.core.data.entities.epg.EpgProgrammeReserveList
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSource
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSourceList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSource
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSourceList
import top.yogiczy.mytv.tv.ui.screens.videoplayer.VideoPlayerDisplayMode
import top.yogiczy.mytv.tv.ui.utils.Configs

class SettingsViewModel : ViewModel() {
    var onVideoPlayerTypeChanged: ((Configs.VideoPlayerType) -> Unit)? = null
    private var _appBootLaunch by mutableStateOf(Configs.appBootLaunch)
    var appBootLaunch: Boolean
        get() = _appBootLaunch
        set(value) {
            _appBootLaunch = value
            Configs.appBootLaunch = value
        }

    private var _appLastLatestVersion by mutableStateOf(Configs.appLastLatestVersion)
    var appLastLatestVersion: String
        get() = _appLastLatestVersion
        set(value) {
            _appLastLatestVersion = value
            Configs.appLastLatestVersion = value
        }

    private var _appAgreementAgreed by mutableStateOf(Configs.appAgreementAgreed)
    var appAgreementAgreed: Boolean
        get() = _appAgreementAgreed
        set(value) {
            _appAgreementAgreed = value
            Configs.appAgreementAgreed = value
        }

    private var _debugShowFps by mutableStateOf(Configs.debugShowFps)
    var debugShowFps: Boolean
        get() = _debugShowFps
        set(value) {
            _debugShowFps = value
            Configs.debugShowFps = value
        }

    private var _debugShowVideoPlayerMetadata by mutableStateOf(Configs.debugShowVideoPlayerMetadata)
    var debugShowVideoPlayerMetadata: Boolean
        get() = _debugShowVideoPlayerMetadata
        set(value) {
            _debugShowVideoPlayerMetadata = value
            Configs.debugShowVideoPlayerMetadata = value
        }

    private var _debugShowLayoutGrids by mutableStateOf(Configs.debugShowLayoutGrids)
    var debugShowLayoutGrids: Boolean
        get() = _debugShowLayoutGrids
        set(value) {
            _debugShowLayoutGrids = value
            Configs.debugShowLayoutGrids = value
        }

    private var _iptvLastChannelIdx by mutableIntStateOf(Configs.iptvLastChannelIdx)
    var iptvLastChannelIdx: Int
        get() = _iptvLastChannelIdx
        set(value) {
            _iptvLastChannelIdx = value
            Configs.iptvLastChannelIdx = value
        }

    private var _iptvChannelUrlIdxMap= mutableStateOf(mutableMapOf<String, Int>().apply {
        putAll(Configs.iptvChannelUrlIdx) // 将 Configs.urlIdxs 中的数据放入 myMap
    })
    fun getIptvChannelUrlIdx(channel_name:String):Int{
        return _iptvChannelUrlIdxMap.value.getOrDefault(channel_name,0)
    }
    fun setIptvChannelUrlIdx(channel_name:String,value:Int){
        _iptvChannelUrlIdxMap.value[channel_name] = value
        Configs.iptvChannelUrlIdx = _iptvChannelUrlIdxMap.value
    }
//    var iptvChannelUrlIdx: Int
//        get(channel_name) = _iptvChannelUrlIdxMap.getOrDefault(channel_name,0)
//        set(channel_name,value) {
//            _iptvChannelUrlIdx = value
//            Configs.iptvChannelUrlIdx = value
//        }

//    private var _iptvChannelUrlIdx by mutableStateOf(Configs.iptvChannelUrlIdx)
//    var iptvChannelUrlIdx: Map<String, Int>
//        get() = _iptvChannelUrlIdx
//        set(value) {
//            _iptvChannelUrlIdx = value
//            Configs.iptvChannelUrlIdx = value
//        }

    private var _iptvChannelChangeFlip by mutableStateOf(Configs.iptvChannelChangeFlip)
    var iptvChannelChangeFlip: Boolean
        get() = _iptvChannelChangeFlip
        set(value) {
            _iptvChannelChangeFlip = value
            Configs.iptvChannelChangeFlip = value
        }

    private var _iptvSourceCacheTime by mutableLongStateOf(Configs.iptvSourceCacheTime)
    var iptvSourceCacheTime: Long
        get() = _iptvSourceCacheTime
        set(value) {
            _iptvSourceCacheTime = value
            Configs.iptvSourceCacheTime = value
        }

    private var _iptvSourceCurrent by mutableStateOf(Configs.iptvSourceCurrent)
    var iptvSourceCurrent: IptvSource
        get() = _iptvSourceCurrent
        set(value) {
            _iptvSourceCurrent = value
            Configs.iptvSourceCurrent = value
        }

    private var _iptvSourceList by mutableStateOf(Configs.iptvSourceList)
    var iptvSourceList: IptvSourceList
        get() = _iptvSourceList
        set(value) {
            _iptvSourceList = value
            Configs.iptvSourceList = value
        }

    private var _iptvPlayableHostList by mutableStateOf(Configs.iptvPlayableHostList)
    var iptvPlayableHostList: Set<String>
        get() = _iptvPlayableHostList
        set(value) {
            _iptvPlayableHostList = value
            Configs.iptvPlayableHostList = value
        }

    private var _iptvChannelNoSelectEnable by mutableStateOf(Configs.iptvChannelNoSelectEnable)
    var iptvChannelNoSelectEnable: Boolean
        get() = _iptvChannelNoSelectEnable
        set(value) {
            _iptvChannelNoSelectEnable = value
            Configs.iptvChannelNoSelectEnable = value
        }

    private var _iptvChannelFavoriteEnable by mutableStateOf(Configs.iptvChannelFavoriteEnable)
    var iptvChannelFavoriteEnable: Boolean
        get() = _iptvChannelFavoriteEnable
        set(value) {
            _iptvChannelFavoriteEnable = value
            Configs.iptvChannelFavoriteEnable = value
        }

    private var _iptvChannelFavoriteListVisible by mutableStateOf(Configs.iptvChannelFavoriteListVisible)
    var iptvChannelFavoriteListVisible: Boolean
        get() = _iptvChannelFavoriteListVisible
        set(value) {
            _iptvChannelFavoriteListVisible = value
            Configs.iptvChannelFavoriteListVisible = value
        }

    private var _iptvChannelFavoriteList by mutableStateOf(Configs.iptvChannelFavoriteList)
    var iptvChannelFavoriteList: Set<String>
        get() = _iptvChannelFavoriteList
        set(value) {
            _iptvChannelFavoriteList = value
            Configs.iptvChannelFavoriteList = value
        }

    private var _iptvChannelFavoriteChangeBoundaryJumpOut by mutableStateOf(Configs.iptvChannelFavoriteChangeBoundaryJumpOut)
    var iptvChannelFavoriteChangeBoundaryJumpOut: Boolean
        get() = _iptvChannelFavoriteChangeBoundaryJumpOut
        set(value) {
            _iptvChannelFavoriteChangeBoundaryJumpOut = value
            Configs.iptvChannelFavoriteChangeBoundaryJumpOut = value
        }

    private var _iptvChannelGroupHiddenList by mutableStateOf(Configs.iptvChannelGroupHiddenList)
    var iptvChannelGroupHiddenList: Set<String>
        get() = _iptvChannelGroupHiddenList
        set(value) {
            _iptvChannelGroupHiddenList = value
            Configs.iptvChannelGroupHiddenList = value
        }

    private var _iptvHybridMode by mutableStateOf(Configs.iptvHybridMode)
    var iptvHybridMode: Configs.IptvHybridMode
        get() = _iptvHybridMode
        set(value) {
            _iptvHybridMode = value
            Configs.iptvHybridMode = value
        }
        
    private var _videoPlayerType by mutableStateOf(Configs.videoPlayerType)
    var videoPlayerType: Configs.VideoPlayerType
        get() = _videoPlayerType
        set(value) {
            _videoPlayerType = value
            Configs.videoPlayerType = value
            onVideoPlayerTypeChanged?.invoke(value)
        }

    var videoPlayerTypeValue: Configs.VideoPlayerType = Configs.VideoPlayerType.MEDIA3
        get() = videoPlayerType
    
    fun getVideoPlayerTypeLabel(type: Configs.VideoPlayerType): String = when (type) {
        Configs.VideoPlayerType.IJK -> "IJK播放器"
        Configs.VideoPlayerType.MEDIA3 -> "Media3播放器"
    }

    private var _captionerEnabled by mutableStateOf(Configs.captionerEnabled)
    var captionerEnabled: Boolean
        get() = _captionerEnabled
        set(value) {
            _captionerEnabled = value
            Configs.captionerEnabled = value
        }

    private var _captionerServerUrl by mutableStateOf(Configs.captionerServerUrl)
    var captionerServerUrl: String
        get() = _captionerServerUrl
        set(value) {
            _captionerServerUrl = value
            Configs.captionerServerUrl = value
        }

    private var _captionerSourceLanguage by mutableStateOf(Configs.captionerSourceLanguage)
    var captionerSourceLanguage: String
        get() = _captionerSourceLanguage
        set(value) {
            Configs.captionerSourceLanguage = value
            _captionerSourceLanguage = Configs.captionerSourceLanguage
        }

    private var _captionerTargetLanguage by mutableStateOf(Configs.captionerTargetLanguage)
    var captionerTargetLanguage: String
        get() = _captionerTargetLanguage
        set(value) {
            Configs.captionerTargetLanguage = value
            _captionerTargetLanguage = Configs.captionerTargetLanguage
            _captionerChineseScript = Configs.captionerChineseScript
        }

    private var _captionerChineseScript by mutableStateOf(Configs.captionerChineseScript)
    var captionerChineseScript: String
        get() = _captionerChineseScript
        set(value) {
            Configs.captionerChineseScript = value
            _captionerChineseScript = Configs.captionerChineseScript
        }

    private var _captionerBilingualEnabled by mutableStateOf(Configs.captionerBilingualEnabled)
    var captionerBilingualEnabled: Boolean
        get() = _captionerBilingualEnabled
        set(value) {
            _captionerBilingualEnabled = value
            Configs.captionerBilingualEnabled = value
        }

    private var _captionerAsrModel by mutableStateOf(Configs.captionerAsrModel)
    var captionerAsrModel: String
        get() = _captionerAsrModel
        set(value) {
            _captionerAsrModel = value
            Configs.captionerAsrModel = value
        }

    private var _captionerTranslationModel by mutableStateOf(Configs.captionerTranslationModel)
    var captionerTranslationModel: String
        get() = _captionerTranslationModel
        set(value) {
            _captionerTranslationModel = value
            Configs.captionerTranslationModel = value
        }

    private var _captionerTranslationMode by mutableStateOf(Configs.captionerTranslationMode)
    var captionerTranslationMode: Configs.CaptionerTranslationMode
        get() = _captionerTranslationMode
        set(value) {
            _captionerTranslationMode = value
            Configs.captionerTranslationMode = value
        }

    private var _captionerDeepSeekApiUrl by mutableStateOf(Configs.captionerDeepSeekApiUrl)
    var captionerDeepSeekApiUrl: String
        get() = _captionerDeepSeekApiUrl
        set(value) {
            _captionerDeepSeekApiUrl = value
            Configs.captionerDeepSeekApiUrl = value
        }

    private var _captionerDeepSeekApiKey by mutableStateOf(Configs.captionerDeepSeekApiKey)
    var captionerDeepSeekApiKey: String
        get() = _captionerDeepSeekApiKey
        set(value) {
            _captionerDeepSeekApiKey = value
            Configs.captionerDeepSeekApiKey = value
        }

    private var _captionerDeepSeekPrompt by mutableStateOf(Configs.captionerDeepSeekPrompt)
    var captionerDeepSeekPrompt: String
        get() = _captionerDeepSeekPrompt
        set(value) {
            _captionerDeepSeekPrompt = value
            Configs.captionerDeepSeekPrompt = value
        }

    private var _captionerChunkDurationMs by mutableLongStateOf(Configs.captionerChunkDurationMs)
    var captionerChunkDurationMs: Long
        get() = _captionerChunkDurationMs
        set(value) {
            _captionerChunkDurationMs = value
            Configs.captionerChunkDurationMs = value
        }

    private var _captionerPartialBeamSize by mutableIntStateOf(Configs.captionerPartialBeamSize)
    var captionerPartialBeamSize: Int
        get() = _captionerPartialBeamSize
        set(value) {
            _captionerPartialBeamSize = value
            Configs.captionerPartialBeamSize = value
        }

    private var _captionerFinalBeamSize by mutableIntStateOf(Configs.captionerFinalBeamSize)
    var captionerFinalBeamSize: Int
        get() = _captionerFinalBeamSize
        set(value) {
            _captionerFinalBeamSize = value
            Configs.captionerFinalBeamSize = value
        }

    private var _captionerDisplayDurationMs by mutableLongStateOf(Configs.captionerDisplayDurationMs)
    var captionerDisplayDurationMs: Long
        get() = _captionerDisplayDurationMs
        set(value) {
            _captionerDisplayDurationMs = value
            Configs.captionerDisplayDurationMs = value
        }

    private var _captionerTextColor by mutableStateOf(Configs.captionerTextColor)
    var captionerTextColor: Configs.CaptionerTextColor
        get() = _captionerTextColor
        set(value) {
            _captionerTextColor = value
            Configs.captionerTextColor = value
        }

    private var _captionerBackgroundColor by mutableStateOf(Configs.captionerBackgroundColor)
    var captionerBackgroundColor: Configs.CaptionerBackgroundColor
        get() = _captionerBackgroundColor
        set(value) {
            _captionerBackgroundColor = value
            Configs.captionerBackgroundColor = value
        }

    private var _captionerPosition by mutableStateOf(Configs.captionerPosition)
    var captionerPosition: Configs.CaptionerPosition
        get() = _captionerPosition
        set(value) {
            _captionerPosition = value
            Configs.captionerPosition = value
        }

    private var _captionerOffsetX by mutableIntStateOf(Configs.captionerOffsetX)
    var captionerOffsetX: Int
        get() = _captionerOffsetX
        set(value) {
            _captionerOffsetX = value.coerceIn(-1200, 1200)
            Configs.captionerOffsetX = _captionerOffsetX
        }

    private var _captionerOffsetY by mutableIntStateOf(Configs.captionerOffsetY)
    var captionerOffsetY: Int
        get() = _captionerOffsetY
        set(value) {
            _captionerOffsetY = value.coerceIn(-700, 700)
            Configs.captionerOffsetY = _captionerOffsetY
        }

    private var _captionerTextAlign by mutableStateOf(Configs.captionerTextAlign)
    var captionerTextAlign: Configs.CaptionerTextAlign
        get() = _captionerTextAlign
        set(value) {
            _captionerTextAlign = value
            Configs.captionerTextAlign = value
        }

    private var _captionerSingleLineMode by mutableStateOf(Configs.captionerSingleLineMode)
    var captionerSingleLineMode: Boolean
        get() = _captionerSingleLineMode
        set(value) {
            _captionerSingleLineMode = value
            Configs.captionerSingleLineMode = value
        }

    private var _captionerPrimaryFontSize by mutableIntStateOf(Configs.captionerPrimaryFontSize)
    var captionerPrimaryFontSize: Int
        get() = _captionerPrimaryFontSize
        set(value) {
            _captionerPrimaryFontSize = value.coerceIn(12, 50)
            Configs.captionerPrimaryFontSize = _captionerPrimaryFontSize
        }

    private var _captionerSecondaryFontSize by mutableIntStateOf(Configs.captionerSecondaryFontSize)
    var captionerSecondaryFontSize: Int
        get() = _captionerSecondaryFontSize
        set(value) {
            _captionerSecondaryFontSize = value.coerceIn(12, 50)
            Configs.captionerSecondaryFontSize = _captionerSecondaryFontSize
        }

    fun resetCaptionerOffset() {
        captionerOffsetX = 0
        captionerOffsetY = 0
    }

    private var _epgEnable by mutableStateOf(Configs.epgEnable)
    var epgEnable: Boolean
        get() = _epgEnable
        set(value) {
            _epgEnable = value
            Configs.epgEnable = value
        }

    private var _epgSourceCurrent by mutableStateOf(Configs.epgSourceCurrent)
    var epgSourceCurrent: EpgSource
        get() = _epgSourceCurrent
        set(value) {
            _epgSourceCurrent = value
            Configs.epgSourceCurrent = value
        }

    private var _epgSourceList by mutableStateOf(Configs.epgSourceList)
    var epgSourceList: EpgSourceList
        get() = _epgSourceList
        set(value) {
            _epgSourceList = value
            Configs.epgSourceList = value
        }

    private var _epgRefreshTimeThreshold by mutableIntStateOf(Configs.epgRefreshTimeThreshold)
    var epgRefreshTimeThreshold: Int
        get() = _epgRefreshTimeThreshold
        set(value) {
            _epgRefreshTimeThreshold = value
            Configs.epgRefreshTimeThreshold = value
        }

    private var _epgChannelReserveList by mutableStateOf(Configs.epgChannelReserveList)
    var epgChannelReserveList: EpgProgrammeReserveList
        get() = _epgChannelReserveList
        set(value) {
            _epgChannelReserveList = value
            Configs.epgChannelReserveList = value
        }

    private var _uiShowEpgProgrammeProgress by mutableStateOf(Configs.uiShowEpgProgrammeProgress)
    var uiShowEpgProgrammeProgress: Boolean
        get() = _uiShowEpgProgrammeProgress
        set(value) {
            _uiShowEpgProgrammeProgress = value
            Configs.uiShowEpgProgrammeProgress = value
        }

    private var _uiShowEpgProgrammePermanentProgress by mutableStateOf(Configs.uiShowEpgProgrammePermanentProgress)
    var uiShowEpgProgrammePermanentProgress: Boolean
        get() = _uiShowEpgProgrammePermanentProgress
        set(value) {
            _uiShowEpgProgrammePermanentProgress = value
            Configs.uiShowEpgProgrammePermanentProgress = value
        }

    private var _uiShowChannelLogo by mutableStateOf(Configs.uiShowChannelLogo)
    var uiShowChannelLogo: Boolean
        get() = _uiShowChannelLogo
        set(value) {
            _uiShowChannelLogo = value
            Configs.uiShowChannelLogo = value
        }

    private var _uiUseClassicPanelScreen by mutableStateOf(Configs.uiUseClassicPanelScreen)
    var uiUseClassicPanelScreen: Boolean
        get() = _uiUseClassicPanelScreen
        set(value) {
            _uiUseClassicPanelScreen = value
            Configs.uiUseClassicPanelScreen = value
        }

    private var _uiDensityScaleRatio by mutableFloatStateOf(Configs.uiDensityScaleRatio)
    var uiDensityScaleRatio: Float
        get() = _uiDensityScaleRatio
        set(value) {
            _uiDensityScaleRatio = value
            Configs.uiDensityScaleRatio = value
        }

    private var _uiFontScaleRatio by mutableFloatStateOf(Configs.uiFontScaleRatio)
    var uiFontScaleRatio: Float
        get() = _uiFontScaleRatio
        set(value) {
            _uiFontScaleRatio = value
            Configs.uiFontScaleRatio = value
        }

    private var _uiTimeShowMode by mutableStateOf(Configs.uiTimeShowMode)
    var uiTimeShowMode: Configs.UiTimeShowMode
        get() = _uiTimeShowMode
        set(value) {
            _uiTimeShowMode = value
            Configs.uiTimeShowMode = value
        }

    private var _uiFocusOptimize by mutableStateOf(Configs.uiFocusOptimize)
    var uiFocusOptimize: Boolean
        get() = _uiFocusOptimize
        set(value) {
            _uiFocusOptimize = value
            Configs.uiFocusOptimize = value
        }

    private var _uiScreenAutoCloseDelay by mutableLongStateOf(Configs.uiScreenAutoCloseDelay)
    var uiScreenAutoCloseDelay: Long
        get() = _uiScreenAutoCloseDelay
        set(value) {
            _uiScreenAutoCloseDelay = value
            Configs.uiScreenAutoCloseDelay = value
        }

    private var _updateForceRemind by mutableStateOf(Configs.updateForceRemind)
    var updateForceRemind: Boolean
        get() = _updateForceRemind
        set(value) {
            _updateForceRemind = value
            Configs.updateForceRemind = value
        }

    private var _updateChannel by mutableStateOf(Configs.updateChannel)
    var updateChannel: String
        get() = _updateChannel
        set(value) {
            _updateChannel = value
            Configs.updateChannel = value
        }

    private var _videoPlayerUserAgent by mutableStateOf(Configs.videoPlayerUserAgent)
    var videoPlayerUserAgent: String
        get() = _videoPlayerUserAgent
        set(value) {
            _videoPlayerUserAgent = value
            Configs.videoPlayerUserAgent = value
        }

    private var _videoPlayerLoadTimeout by mutableLongStateOf(Configs.videoPlayerLoadTimeout)
    var videoPlayerLoadTimeout: Long
        get() = _videoPlayerLoadTimeout
        set(value) {
            _videoPlayerLoadTimeout = value
            Configs.videoPlayerLoadTimeout = value
        }

    private var _videoPlayerAspectRatio by mutableStateOf(Configs.videoPlayerDisplayMode)
    var videoPlayerDisplayMode: VideoPlayerDisplayMode
        get() = _videoPlayerAspectRatio
        set(value) {
            _videoPlayerAspectRatio = value
            Configs.videoPlayerDisplayMode = value
        }

    private var _videoPlayerForceAudioSoftDecode by mutableStateOf(false)
    var videoPlayerForceSoftDecode: Boolean
        get() = _videoPlayerForceAudioSoftDecode
        set(value) {
            _videoPlayerForceAudioSoftDecode = value
            Configs.videoPlayerForceSoftDecode = value
        }

    private var _videoPlayerRenderMode by mutableStateOf(Configs.VideoPlayerRenderMode.SURFACE_VIEW)
    var videoPlayerRenderMode: Configs.VideoPlayerRenderMode
        get() = _videoPlayerRenderMode
        set(value) {
            _videoPlayerRenderMode = value
            Configs.videoPlayerRenderMode = value
        }

    private var _videoPlayerStopPreviousMediaItem by mutableStateOf(false)
    var videoPlayerStopPreviousMediaItem: Boolean
        get() = _videoPlayerStopPreviousMediaItem
        set(value) {
            _videoPlayerStopPreviousMediaItem = value
            Configs.videoPlayerStopPreviousMediaItem = value
        }

    private var _videoPlayerSkipMultipleFramesOnSameVSync by mutableStateOf(false)
    var videoPlayerSkipMultipleFramesOnSameVSync: Boolean
        get() = _videoPlayerSkipMultipleFramesOnSameVSync
        set(value) {
            _videoPlayerSkipMultipleFramesOnSameVSync = value
            Configs.videoPlayerSkipMultipleFramesOnSameVSync = value
        }

    init {
        // 删除过期的预约
        _epgChannelReserveList = EpgProgrammeReserveList(
            _epgChannelReserveList.filter {
                System.currentTimeMillis() < it.startAt + 60 * 1000
            }
        )
    }

    fun refresh() {
        _appBootLaunch = Configs.appBootLaunch
        _appLastLatestVersion = Configs.appLastLatestVersion
        _appAgreementAgreed = Configs.appAgreementAgreed
        _debugShowFps = Configs.debugShowFps
        _debugShowVideoPlayerMetadata = Configs.debugShowVideoPlayerMetadata
        _debugShowLayoutGrids = Configs.debugShowLayoutGrids
        _iptvLastChannelIdx = Configs.iptvLastChannelIdx
        _iptvChannelChangeFlip = Configs.iptvChannelChangeFlip
        _iptvSourceCacheTime = Configs.iptvSourceCacheTime
        _iptvSourceCurrent = Configs.iptvSourceCurrent
        _iptvSourceList = Configs.iptvSourceList
        _iptvPlayableHostList = Configs.iptvPlayableHostList
        _iptvChannelNoSelectEnable = Configs.iptvChannelNoSelectEnable
        _iptvChannelFavoriteEnable = Configs.iptvChannelFavoriteEnable
        _iptvChannelFavoriteListVisible = Configs.iptvChannelFavoriteListVisible
        _iptvChannelFavoriteList = Configs.iptvChannelFavoriteList
        _iptvChannelFavoriteChangeBoundaryJumpOut = Configs.iptvChannelFavoriteChangeBoundaryJumpOut
        _iptvChannelGroupHiddenList = Configs.iptvChannelGroupHiddenList
//        _iptvChannelUrlIdx = Configs.iptvChannelUrlIdx
        _iptvHybridMode = Configs.iptvHybridMode
        _epgEnable = Configs.epgEnable
        _epgSourceCurrent = Configs.epgSourceCurrent
        _epgSourceList = Configs.epgSourceList
        _epgRefreshTimeThreshold = Configs.epgRefreshTimeThreshold
        _epgChannelReserveList = Configs.epgChannelReserveList
        _uiShowEpgProgrammeProgress = Configs.uiShowEpgProgrammeProgress
        _uiShowEpgProgrammePermanentProgress = Configs.uiShowEpgProgrammePermanentProgress
        _uiShowChannelLogo = Configs.uiShowChannelLogo
        _uiUseClassicPanelScreen = Configs.uiUseClassicPanelScreen
        _uiDensityScaleRatio = Configs.uiDensityScaleRatio
        _uiFontScaleRatio = Configs.uiFontScaleRatio
        _uiTimeShowMode = Configs.uiTimeShowMode
        _uiFocusOptimize = Configs.uiFocusOptimize
        _uiScreenAutoCloseDelay = Configs.uiScreenAutoCloseDelay
        _updateForceRemind = Configs.updateForceRemind
        _updateChannel = Configs.updateChannel
        _videoPlayerUserAgent = Configs.videoPlayerUserAgent
        _videoPlayerLoadTimeout = Configs.videoPlayerLoadTimeout
        _videoPlayerAspectRatio = Configs.videoPlayerDisplayMode
        _videoPlayerForceAudioSoftDecode = Configs.videoPlayerForceSoftDecode
        _videoPlayerRenderMode = Configs.videoPlayerRenderMode
        _videoPlayerSkipMultipleFramesOnSameVSync = Configs.videoPlayerSkipMultipleFramesOnSameVSync
        _videoPlayerType = Configs.videoPlayerType
        _captionerEnabled = Configs.captionerEnabled
        _captionerServerUrl = Configs.captionerServerUrl
        _captionerSourceLanguage = Configs.captionerSourceLanguage
        _captionerTargetLanguage = Configs.captionerTargetLanguage
        _captionerChineseScript = Configs.captionerChineseScript
        _captionerBilingualEnabled = Configs.captionerBilingualEnabled
        _captionerAsrModel = Configs.captionerAsrModel
        _captionerTranslationModel = Configs.captionerTranslationModel
        _captionerTranslationMode = Configs.captionerTranslationMode
        _captionerDeepSeekApiUrl = Configs.captionerDeepSeekApiUrl
        _captionerDeepSeekApiKey = Configs.captionerDeepSeekApiKey
        _captionerDeepSeekPrompt = Configs.captionerDeepSeekPrompt
        _captionerChunkDurationMs = Configs.captionerChunkDurationMs
        _captionerPartialBeamSize = Configs.captionerPartialBeamSize
        _captionerFinalBeamSize = Configs.captionerFinalBeamSize
        _captionerDisplayDurationMs = Configs.captionerDisplayDurationMs
        _captionerTextColor = Configs.captionerTextColor
        _captionerBackgroundColor = Configs.captionerBackgroundColor
        _captionerPosition = Configs.captionerPosition
        _captionerOffsetX = Configs.captionerOffsetX
        _captionerOffsetY = Configs.captionerOffsetY
        _captionerTextAlign = Configs.captionerTextAlign
        _captionerSingleLineMode = Configs.captionerSingleLineMode
        _captionerPrimaryFontSize = Configs.captionerPrimaryFontSize
        _captionerSecondaryFontSize = Configs.captionerSecondaryFontSize
    }
}
