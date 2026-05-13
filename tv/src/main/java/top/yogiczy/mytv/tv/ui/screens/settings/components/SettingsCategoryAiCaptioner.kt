package top.yogiczy.mytv.tv.ui.screens.settings.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Switch
import kotlinx.coroutines.launch
import top.yogiczy.mytv.tv.ui.material.LocalPopupManager
import top.yogiczy.mytv.tv.ui.material.Snackbar
import top.yogiczy.mytv.tv.ui.material.SimplePopup
import top.yogiczy.mytv.tv.ui.screens.components.SelectDialog
import top.yogiczy.mytv.tv.ui.screens.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner.CaptionerModelClient
import top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner.CaptionerModelOptions
import top.yogiczy.mytv.tv.ui.utils.Configs

@Composable
fun SettingsCategoryAiCaptioner(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var captionerModelOptions by remember { mutableStateOf<CaptionerModelOptions?>(null) }
    var captionerModelsLoading by remember { mutableStateOf(false) }

    fun currentCaptionerModelOptions() = captionerModelOptions ?: CaptionerModelOptions().withFallbacks(
        asrModel = settingsViewModel.captionerAsrModel,
        translationModel = settingsViewModel.captionerTranslationModel,
    )

    fun loadCaptionerModels(onReady: () -> Unit) {
        if (captionerModelsLoading) return

        val cachedOptions = captionerModelOptions
        if (cachedOptions != null && !cachedOptions.isEmpty) {
            onReady()
            return
        }

        captionerModelsLoading = true
        Snackbar.show("正在获取字幕模型", leadingLoading = true, id = "captioner-models")
        coroutineScope.launch {
            captionerModelOptions = runCatching {
                val options = CaptionerModelClient.fetch(settingsViewModel.captionerServerUrl)
                if (
                    options.asrModels.isNotEmpty() &&
                    settingsViewModel.captionerAsrModel !in options.asrModels
                ) {
                    settingsViewModel.captionerAsrModel = options.asrModels.first()
                }
                if (
                    options.translationModels.isNotEmpty() &&
                    settingsViewModel.captionerTranslationModel !in options.translationModels
                ) {
                    settingsViewModel.captionerTranslationModel = options.translationModels.first()
                }
                options
            }.getOrElse {
                Snackbar.show("获取字幕模型失败，将显示当前模型")
                CaptionerModelOptions().withFallbacks(
                    asrModel = settingsViewModel.captionerAsrModel,
                    translationModel = settingsViewModel.captionerTranslationModel,
                )
            }
            captionerModelsLoading = false
            onReady()
        }
    }

    SettingsContentList(modifier) {
        item {
            SettingsListItem(
                modifier = Modifier.focusRequester(it),
                headlineContent = "实验性",
                supportingContent = "AI字幕需要 tv-captioner 后端配合，默认关闭。开启后会切换到Media3并向后端推送音频流。",
            )
        }

        item {
            SettingsListItem(
                headlineContent = "AI字幕",
                supportingContent = "实时识别并翻译当前播放音频",
                trailingContent = {
                    Switch(settingsViewModel.captionerEnabled, null)
                },
                onSelected = {
                    settingsViewModel.captionerEnabled = !settingsViewModel.captionerEnabled
                    if (
                        settingsViewModel.captionerEnabled &&
                        settingsViewModel.videoPlayerType != Configs.VideoPlayerType.MEDIA3
                    ) {
                        settingsViewModel.videoPlayerType = Configs.VideoPlayerType.MEDIA3
                    }
                    context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
                },
            )
        }

        item {
            val popupManager = LocalPopupManager.current
            val focusRequester = remember { FocusRequester() }
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                modifier = Modifier.focusRequester(focusRequester),
                headlineContent = "字幕后端地址",
                supportingContent = "扫码打开配置页填写tv-captioner后端地址",
                trailingContent = settingsViewModel.captionerServerUrl,
                onSelected = {
                    popupManager.push(focusRequester, true)
                    visible = true
                },
                remoteConfig = true,
            )

            SimplePopup(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
            ) {
                SettingsCategoryPush()
            }
        }

        item {
            val popupManager = LocalPopupManager.current
            val focusRequester = remember { FocusRequester() }
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                modifier = Modifier.focusRequester(focusRequester),
                headlineContent = "识别模型",
                supportingContent = "源语言 ${settingsViewModel.captionerSourceLanguage}",
                trailingContent = settingsViewModel.captionerAsrModel,
                onSelected = {
                    popupManager.push(focusRequester, true)
                    loadCaptionerModels { visible = true }
                },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "识别模型",
                currentDataProvider = { settingsViewModel.captionerAsrModel },
                dataListProvider = { currentCaptionerModelOptions().asrModels },
                dataText = { it },
                onDataSelected = {
                    settingsViewModel.captionerAsrModel = it
                    visible = false
                    if (settingsViewModel.captionerEnabled) {
                        context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
                    }
                },
            )
        }

        item {
            val popupManager = LocalPopupManager.current
            val focusRequester = remember { FocusRequester() }
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                modifier = Modifier.focusRequester(focusRequester),
                headlineContent = "中文输出",
                supportingContent = "语音转文字默认使用简体中文",
                trailingContent = captionerChineseScriptText(settingsViewModel.captionerChineseScript),
                onSelected = {
                    popupManager.push(focusRequester, true)
                    visible = true
                },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "中文输出",
                currentDataProvider = { settingsViewModel.captionerChineseScript },
                dataListProvider = { listOf("simplified", "traditional", "original") },
                dataText = { captionerChineseScriptText(it) },
                onDataSelected = {
                    settingsViewModel.captionerChineseScript = it
                    visible = false
                    if (settingsViewModel.captionerEnabled) {
                        context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
                    }
                },
            )
        }

        item {
            val popupManager = LocalPopupManager.current
            val focusRequester = remember { FocusRequester() }
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                modifier = Modifier.focusRequester(focusRequester),
                headlineContent = "翻译模型",
                supportingContent = "翻译语言 ${captionerTargetLanguageText(settingsViewModel)}",
                trailingContent = settingsViewModel.captionerTranslationModel,
                onSelected = {
                    popupManager.push(focusRequester, true)
                    loadCaptionerModels { visible = true }
                },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "翻译模型",
                currentDataProvider = { settingsViewModel.captionerTranslationModel },
                dataListProvider = { currentCaptionerModelOptions().translationModels },
                dataText = { it },
                onDataSelected = {
                    settingsViewModel.captionerTranslationModel = it
                    visible = false
                    if (settingsViewModel.captionerEnabled) {
                        context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
                    }
                },
            )
        }

        item {
            SettingsListItem(
                headlineContent = "字幕偏移重置",
                supportingContent = "将播放界面调整的左右、上下偏移恢复为0",
                trailingContent = "${settingsViewModel.captionerOffsetX}, ${settingsViewModel.captionerOffsetY}",
                onSelected = {
                    settingsViewModel.resetCaptionerOffset()
                    Snackbar.show("字幕偏移已重置")
                },
            )
        }

    }
}

private fun captionerTargetLanguageText(settingsViewModel: SettingsViewModel): String {
    return when (settingsViewModel.captionerTargetLanguage) {
        Configs.CAPTIONER_TARGET_NONE -> "不翻译"
        "Chinese" -> captionerChineseScriptText(settingsViewModel.captionerChineseScript)
        else -> settingsViewModel.captionerTargetLanguage
    }
}

private fun captionerChineseScriptText(script: String): String {
    return when (script.lowercase()) {
        "traditional" -> "繁体中文"
        "original" -> "保留原始输出"
        else -> "简体中文"
    }
}

private const val RESTART_PLAY_ACTION = "top.yogiczy.mytv.tv.RESTART_PLAY"
