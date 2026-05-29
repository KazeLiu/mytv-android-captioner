package top.yogiczy.mytv.tv.ui.screen.settings.categories
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import top.yogiczy.mytv.tv.ui.material.Snackbar
import top.yogiczy.mytv.tv.ui.screen.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.screen.settings.components.SettingsCategoryScreen
import top.yogiczy.mytv.tv.ui.screen.settings.components.SettingsListItem
import top.yogiczy.mytv.tv.ui.screen.settings.settingsVM
import top.yogiczy.mytv.tv.ui.screensold.components.SelectDialog
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner.CaptionerModelClient
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner.CaptionerModelOptions
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.utils.Configs

@Composable
fun SettingsAiCaptionerScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = settingsVM,
    onRestartPlayback: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    var captionerModelOptions by remember { mutableStateOf<CaptionerModelOptions?>(null) }
    var captionerModelsLoading by remember { mutableStateOf(false) }

    fun restartCaptioner() {
        if (settingsViewModel.captionerEnabled) onRestartPlayback()
    }

    fun currentCaptionerModelOptions() = captionerModelOptions ?: CaptionerModelOptions()
        .withFallbacks(
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

    SettingsCategoryScreen(
        modifier = modifier,
        header = { Text("设置 / AI字幕") },
        onBackPressed = onBackPressed,
    ) { firstItemFocusRequester ->
        item {
            SettingsListItem(
                modifier = Modifier.focusRequester(firstItemFocusRequester),
                headlineContent = "实验性",
                supportingContent = "需要 tv-captioner 后端配合。开启后会切换到 Media3 并向后端推送音频流。",
            )
        }

        item {
            SettingsListItem(
                headlineContent = "AI字幕",
                supportingContent = "实时识别并翻译当前播放音频",
                trailingContent = {
                    Switch(settingsViewModel.captionerEnabled, null)
                },
                onSelect = {
                    settingsViewModel.captionerEnabled = !settingsViewModel.captionerEnabled
                    onRestartPlayback()
                },
            )
        }

        item {
            SettingsListItem(
                headlineContent = "字幕后端地址",
                supportingContent = "通过推送配置页填写 tv-captioner 后端地址",
                trailingContent = settingsViewModel.captionerServerUrl,
                remoteConfig = true,
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "源语言",
                trailingContent = captionerLanguageLabel(settingsViewModel.captionerSourceLanguage),
                onSelect = { visible = true },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "源语言",
                currentDataProvider = { settingsViewModel.captionerSourceLanguage },
                dataListProvider = { captionerSourceLanguageOptions },
                dataText = { captionerLanguageLabel(it) },
                onDataSelected = {
                    settingsViewModel.captionerSourceLanguage = it
                    visible = false
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "翻译语言",
                trailingContent = captionerLanguageLabel(
                    captionerTargetLanguageOption(
                        settingsViewModel.captionerTargetLanguage,
                        settingsViewModel.captionerChineseScript,
                    )
                ),
                onSelect = { visible = true },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "翻译语言",
                currentDataProvider = {
                    captionerTargetLanguageOption(
                        settingsViewModel.captionerTargetLanguage,
                        settingsViewModel.captionerChineseScript,
                    )
                },
                dataListProvider = { captionerTargetLanguageOptions },
                dataText = { captionerLanguageLabel(it) },
                onDataSelected = {
                    setCaptionerTargetLanguage(settingsViewModel, it)
                    visible = false
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "识别模型",
                supportingContent = "源语言 ${captionerLanguageLabel(settingsViewModel.captionerSourceLanguage)}",
                trailingContent = settingsViewModel.captionerAsrModel,
                onSelect = {
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
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "翻译模型",
                supportingContent = "翻译语言 ${captionerLanguageLabel(settingsViewModel.captionerTargetLanguage)}",
                trailingContent = settingsViewModel.captionerTranslationModel,
                onSelect = {
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
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "中文输出",
                supportingContent = "影响识别与翻译输出的中文书写格式",
                trailingContent = captionerChineseScriptText(settingsViewModel.captionerChineseScript),
                onSelect = { visible = true },
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
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "分段上限",
                supportingContent = "单段语音最长等待时间，数字越大越吃后端",
                trailingContent = settingsViewModel.captionerChunkDurationMs.captionerDurationText(),
                onSelect = { visible = true },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "分段上限",
                currentDataProvider = { settingsViewModel.captionerChunkDurationMs },
                dataListProvider = { captionerSegmentDurationOptions },
                dataText = { it.captionerDurationText() },
                onDataSelected = {
                    settingsViewModel.captionerChunkDurationMs = it
                    visible = false
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "临时字幕精度",
                supportingContent = "先显示的预览字幕，数字越小越快",
                trailingContent = settingsViewModel.captionerPartialBeamSize.captionerBeamSizeText(),
                onSelect = { visible = true },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "临时字幕精度",
                currentDataProvider = { settingsViewModel.captionerPartialBeamSize },
                dataListProvider = { captionerBeamSizeOptions },
                dataText = { it.captionerBeamSizeText() },
                onDataSelected = {
                    settingsViewModel.captionerPartialBeamSize = it
                    visible = false
                    restartCaptioner()
                },
            )
        }

        item {
            var visible by remember { mutableStateOf(false) }

            SettingsListItem(
                headlineContent = "修正字幕精度",
                supportingContent = "最终替换临时字幕的结果，数字越大越稳但越慢",
                trailingContent = settingsViewModel.captionerFinalBeamSize.captionerBeamSizeText(),
                onSelect = { visible = true },
            )

            SelectDialog(
                visibleProvider = { visible },
                onDismissRequest = { visible = false },
                title = "修正字幕精度",
                currentDataProvider = { settingsViewModel.captionerFinalBeamSize },
                dataListProvider = { captionerBeamSizeOptions },
                dataText = { it.captionerBeamSizeText() },
                onDataSelected = {
                    settingsViewModel.captionerFinalBeamSize = it
                    visible = false
                    restartCaptioner()
                },
            )
        }

        item {
            SettingsListItem(
                headlineContent = "字幕偏移重置",
                supportingContent = "将播放界面调整的左右、上下偏移恢复为 0",
                trailingContent = "${settingsViewModel.captionerOffsetX}, ${settingsViewModel.captionerOffsetY}",
                onSelect = {
                    settingsViewModel.resetCaptionerOffset()
                    Snackbar.show("字幕偏移已重置")
                },
            )
        }
    }
}

private fun setCaptionerTargetLanguage(
    settingsViewModel: SettingsViewModel,
    option: String,
) {
    when (option) {
        CAPTIONER_TARGET_CHINESE_SIMPLIFIED -> {
            settingsViewModel.captionerTargetLanguage = "Chinese"
            settingsViewModel.captionerChineseScript = "simplified"
        }

        CAPTIONER_TARGET_CHINESE_TRADITIONAL -> {
            settingsViewModel.captionerTargetLanguage = "Chinese"
            settingsViewModel.captionerChineseScript = "traditional"
        }

        else -> settingsViewModel.captionerTargetLanguage = option
    }
}

private fun captionerTargetLanguageOption(targetLanguage: String, chineseScript: String): String {
    return if (targetLanguage == "Chinese") {
        when (chineseScript.lowercase()) {
            "traditional" -> CAPTIONER_TARGET_CHINESE_TRADITIONAL
            else -> CAPTIONER_TARGET_CHINESE_SIMPLIFIED
        }
    } else {
        targetLanguage
    }
}

private fun captionerChineseScriptText(script: String): String {
    return when (script.lowercase()) {
        "traditional" -> "繁体中文"
        "original" -> "保留原始输出"
        else -> "简体中文"
    }
}

private fun captionerLanguageLabel(language: String): String {
    return when (language.trim().lowercase()) {
        Configs.CAPTIONER_TARGET_NONE -> "不翻译"
        CAPTIONER_TARGET_CHINESE_SIMPLIFIED.lowercase() -> "简体中文"
        CAPTIONER_TARGET_CHINESE_TRADITIONAL.lowercase() -> "繁体中文"
        "auto" -> "自动识别"
        "zh", "chinese" -> "中文"
        "en", "english" -> "英文"
        "ja", "japanese" -> "日文"
        "ko", "korean" -> "韩文"
        "yue", "cantonese" -> "粤语"
        "fr", "french" -> "法文"
        "de", "german" -> "德文"
        "es", "spanish" -> "西文"
        "ru", "russian" -> "俄文"
        else -> language.ifBlank { "未设置" }
    }
}

private fun Long.captionerDurationText(): String {
    return if (this % 1000L == 0L) "${this / 1000L}秒" else "${this}ms"
}

private fun Int.captionerBeamSizeText(): String {
    return "beam $this"
}

private val captionerSourceLanguageOptions = listOf(
    "auto",
    "zh",
    "en",
    "ja",
    "ko",
    "yue",
    "fr",
    "de",
    "es",
    "ru",
)

private val captionerTargetLanguageOptions = listOf(
    Configs.CAPTIONER_TARGET_NONE,
    CAPTIONER_TARGET_CHINESE_SIMPLIFIED,
    CAPTIONER_TARGET_CHINESE_TRADITIONAL,
    "English",
    "Japanese",
    "Korean",
    "French",
    "German",
    "Spanish",
    "Russian",
)

private val captionerSegmentDurationOptions = listOf(3, 5, 8, 10, 15).map { it.toLong() * 1000 }
private val captionerBeamSizeOptions = listOf(1, 2, 3, 4, 5)

private const val CAPTIONER_TARGET_CHINESE_SIMPLIFIED = "ChineseSimplified"
private const val CAPTIONER_TARGET_CHINESE_TRADITIONAL = "ChineseTraditional"

@Preview(device = "id:Android TV (720p)")
@Composable
private fun SettingsAiCaptionerScreenPreview() {
    MyTvTheme {
        SettingsAiCaptionerScreen(settingsViewModel = SettingsViewModel())
    }
}
