package top.yogiczy.mytv.tv.ui.screens.quickop.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.flow.distinctUntilChanged
import top.yogiczy.mytv.tv.ui.material.LocalPopupManager
import top.yogiczy.mytv.tv.ui.material.SimplePopup
import top.yogiczy.mytv.tv.ui.material.Snackbar
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screens.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner.LiveCaptionPreviewState
import top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner.LiveCaptionRuntimeState
import top.yogiczy.mytv.tv.ui.theme.MyTVTheme
import top.yogiczy.mytv.tv.ui.utils.Configs
import top.yogiczy.mytv.tv.ui.utils.focusOnLaunched
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents
import kotlin.math.abs

@Composable
fun QuickOpBtnList(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    onShowEpg: () -> Unit = {},
    onShowChannelUrl: () -> Unit = {},
    onShowVideoPlayerController: () -> Unit = {},
    onShowVideoPlayerDisplayMode: () -> Unit = {},
    onShowMoreSettings: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onUserAction: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { _ -> onUserAction() }
    }

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = childPadding.start, end = childPadding.end),
    ) {
        item {
            QuickOpBtn(
                modifier = Modifier.focusOnLaunched(),
                title = { Text("节目单") },
                onSelect = onShowEpg,
            )
        }

        item {
            QuickOpBtn(
                title = { Text("多线路") },
                onSelect = onShowChannelUrl,
            )
        }

        item {
            QuickOpBtn(
                title = { Text("播放控制") },
                onSelect = onShowVideoPlayerController,
            )
        }

        if (settingsViewModel.captionerEnabled) {
            item {
                QuickOpCaptionerSettingsBtn(
                    settingsViewModel = settingsViewModel,
                    onUserAction = onUserAction,
                )
            }
        }

        item {
            QuickOpBtn(
                title = { Text("显示模式") },
                onSelect = onShowVideoPlayerDisplayMode,
            )
        }

        item {
            QuickOpBtn(
                title = { Text("清除缓存") },
                onSelect = onClearCache,
            )
        }
        item {
            QuickOpBtn(
                title = { Text("更多设置") },
                onSelect = onShowMoreSettings,
            )
        }
    }
}

@Composable
private fun QuickOpCaptionerSettingsBtn(
    settingsViewModel: SettingsViewModel,
    onUserAction: () -> Unit,
) {
    val context = LocalContext.current
    val popupManager = LocalPopupManager.current
    val focusRequester = remember { FocusRequester() }
    var visible by remember { mutableStateOf(false) }

    QuickOpBtn(
        modifier = Modifier.focusRequester(focusRequester),
        title = { Text(if (LiveCaptionRuntimeState.enabled) "AI字幕：已开" else "AI字幕：已关") },
        onSelect = {
            popupManager.push(focusRequester, true)
            visible = true
            onUserAction()
        },
    )

    SimplePopup(
        visibleProvider = { visible },
        onDismissRequest = { visible = false },
    ) {
        DisposableEffect(Unit) {
            LiveCaptionPreviewState.visible = true
            onDispose { LiveCaptionPreviewState.visible = false }
        }

        CaptionerFloatingPanel(
            settingsViewModel = settingsViewModel,
            onToggleRuntimeCaptioner = {
                LiveCaptionRuntimeState.enabled = !LiveCaptionRuntimeState.enabled
                context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
                Snackbar.show(if (LiveCaptionRuntimeState.enabled) "AI字幕已临时开启" else "AI字幕已临时关闭")
            },
            onRestartCaptioner = {
                context.sendBroadcast(Intent(RESTART_PLAY_ACTION))
            },
            onUserAction = onUserAction,
        )
    }
}

@Composable
private fun CaptionerFloatingPanel(
    settingsViewModel: SettingsViewModel,
    onToggleRuntimeCaptioner: () -> Unit,
    onRestartCaptioner: () -> Unit,
    onUserAction: () -> Unit,
) {
    val offsetWarning = captionerOffsetWarning(
        settingsViewModel.captionerOffsetX,
        settingsViewModel.captionerOffsetY,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 42.dp)
                .fillMaxWidth(0.44f)
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xD0101010))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI字幕设置", style = MaterialTheme.typography.titleMedium)
                    Text("总开关在设置页，这里只临时开关字幕", style = MaterialTheme.typography.bodySmall)
                }
                CaptionerPanelButton(
                    text = if (LiveCaptionRuntimeState.enabled) "临时关闭" else "开启字幕",
                    modifier = Modifier.focusOnLaunched(),
                    onSelect = {
                        onToggleRuntimeCaptioner()
                        onUserAction()
                    },
                )
            }

            CaptionerOptionRow(
                title = "源语言",
                currentValue = settingsViewModel.captionerSourceLanguage,
                options = captionerSourceLanguageOptions,
                optionText = { captionerLanguageLabel(it) },
                onSelected = {
                    settingsViewModel.captionerSourceLanguage = it
                    onRestartCaptioner()
                    Snackbar.show("源语言已切换为${captionerLanguageLabel(it)}")
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "翻译",
                currentValue = captionerTargetLanguageOption(
                    settingsViewModel.captionerTargetLanguage,
                    settingsViewModel.captionerChineseScript,
                ),
                options = captionerTargetLanguageOptions,
                optionText = { captionerLanguageLabel(it) },
                onSelected = {
                    when (it) {
                        CAPTIONER_TARGET_CHINESE_SIMPLIFIED -> {
                            settingsViewModel.captionerTargetLanguage = "Chinese"
                            settingsViewModel.captionerChineseScript = "simplified"
                        }

                        CAPTIONER_TARGET_CHINESE_TRADITIONAL -> {
                            settingsViewModel.captionerTargetLanguage = "Chinese"
                            settingsViewModel.captionerChineseScript = "traditional"
                        }

                        else -> settingsViewModel.captionerTargetLanguage = it
                    }
                    onRestartCaptioner()
                    Snackbar.show("翻译语言已切换为${captionerLanguageLabel(it)}")
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "显示",
                currentValue = if (settingsViewModel.captionerBilingualEnabled) "bilingual" else "single",
                options = listOf("single", "bilingual"),
                optionText = { if (it == "bilingual") "双语" else "单语" },
                onSelected = {
                    settingsViewModel.captionerBilingualEnabled = it == "bilingual"
                    Snackbar.show(if (settingsViewModel.captionerBilingualEnabled) "双语字幕已开启" else "只显示主字幕")
                    onUserAction()
                },
            )

            CaptionerNumberStepRow(
                title = "主字幕大小",
                value = settingsViewModel.captionerPrimaryFontSize,
                decreaseText = "缩小",
                increaseText = "变大",
                onDecrease = {
                    settingsViewModel.captionerPrimaryFontSize -= CAPTIONER_FONT_SIZE_STEP
                    onUserAction()
                },
                onIncrease = {
                    settingsViewModel.captionerPrimaryFontSize += CAPTIONER_FONT_SIZE_STEP
                    onUserAction()
                },
            )

            CaptionerNumberStepRow(
                title = "副字幕大小",
                value = settingsViewModel.captionerSecondaryFontSize,
                decreaseText = "缩小",
                increaseText = "变大",
                onDecrease = {
                    settingsViewModel.captionerSecondaryFontSize -= CAPTIONER_FONT_SIZE_STEP
                    onUserAction()
                },
                onIncrease = {
                    settingsViewModel.captionerSecondaryFontSize += CAPTIONER_FONT_SIZE_STEP
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "行数",
                currentValue = if (settingsViewModel.captionerSingleLineMode) "single-line" else "stack",
                options = listOf("single-line", "stack"),
                optionText = { if (it == "single-line") "单行" else "多行堆叠" },
                onSelected = {
                    settingsViewModel.captionerSingleLineMode = it == "single-line"
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "位置",
                currentValue = settingsViewModel.captionerPosition,
                options = Configs.CaptionerPosition.entries,
                optionText = { it.label },
                onSelected = {
                    settingsViewModel.captionerPosition = it
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "时长",
                currentValue = settingsViewModel.captionerDisplayDurationMs,
                options = captionerDisplayDurationOptions,
                optionText = { it.captionerDurationText() },
                onSelected = {
                    settingsViewModel.captionerDisplayDurationMs = it
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "分段",
                currentValue = settingsViewModel.captionerChunkDurationMs,
                options = captionerSegmentDurationOptions,
                optionText = { it.captionerDurationText() },
                onSelected = {
                    settingsViewModel.captionerChunkDurationMs = it
                    onRestartCaptioner()
                    Snackbar.show("分段上限已切换为${it.captionerDurationText()}")
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "颜色",
                currentValue = settingsViewModel.captionerTextColor,
                options = Configs.CaptionerTextColor.entries,
                optionText = { it.label },
                onSelected = {
                    settingsViewModel.captionerTextColor = it
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "背景",
                currentValue = settingsViewModel.captionerBackgroundColor,
                options = Configs.CaptionerBackgroundColor.entries,
                optionText = { it.label },
                onSelected = {
                    settingsViewModel.captionerBackgroundColor = it
                    onUserAction()
                },
            )

            CaptionerOffsetRow(
                title = "左右偏移",
                value = settingsViewModel.captionerOffsetX,
                offsetWarning = null,
                decreaseText = "左移",
                increaseText = "右移",
                onDecrease = {
                    settingsViewModel.captionerOffsetX -= CAPTIONER_OFFSET_STEP_PX
                    onUserAction()
                },
                onIncrease = {
                    settingsViewModel.captionerOffsetX += CAPTIONER_OFFSET_STEP_PX
                    onUserAction()
                },
            )

            CaptionerOffsetRow(
                title = "上下偏移",
                value = settingsViewModel.captionerOffsetY,
                offsetWarning = offsetWarning,
                decreaseText = "上移",
                increaseText = "下移",
                onDecrease = {
                    settingsViewModel.captionerOffsetY -= CAPTIONER_OFFSET_STEP_PX
                    onUserAction()
                },
                onIncrease = {
                    settingsViewModel.captionerOffsetY += CAPTIONER_OFFSET_STEP_PX
                    onUserAction()
                },
            )

            CaptionerOptionRow(
                title = "对齐",
                currentValue = settingsViewModel.captionerTextAlign,
                options = Configs.CaptionerTextAlign.entries,
                optionText = { it.label },
                onSelected = {
                    settingsViewModel.captionerTextAlign = it
                    onUserAction()
                },
            )

        }
    }
}

@Composable
private fun <T> CaptionerOptionRow(
    title: String,
    currentValue: T,
    options: List<T>,
    optionText: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(0.16f),
            style = MaterialTheme.typography.bodyMedium,
        )
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(options) { option ->
                CaptionerPanelButton(
                    text = optionText(option),
                    selected = option == currentValue,
                    onSelect = { onSelected(option) },
                )
            }
        }
    }
}

@Composable
private fun CaptionerOffsetRow(
    title: String,
    value: Int,
    offsetWarning: String?,
    decreaseText: String,
    increaseText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(0.16f),
            style = MaterialTheme.typography.bodyMedium,
        )

        CaptionerOffsetControl(
            value = value,
            decreaseText = decreaseText,
            increaseText = increaseText,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
        )

        offsetWarning?.let {
            Text(it, color = Color(0xFFFFE082), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CaptionerNumberStepRow(
    title: String,
    value: Int,
    decreaseText: String,
    increaseText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(0.16f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )

        CaptionerNumberStepControl(
            value = value,
            decreaseText = decreaseText,
            increaseText = increaseText,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
        )
    }
}

@Composable
private fun CaptionerOffsetControl(
    value: Int,
    decreaseText: String,
    increaseText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CaptionerPanelButton(text = decreaseText, onSelect = onDecrease)
        CaptionerNumberValue(value = value)
        CaptionerPanelButton(text = increaseText, onSelect = onIncrease)
    }
}

@Composable
private fun CaptionerNumberStepControl(
    value: Int,
    decreaseText: String,
    increaseText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CaptionerPanelButton(text = decreaseText, onSelect = onDecrease)
        CaptionerNumberValue(value = value)
        CaptionerPanelButton(text = increaseText, onSelect = onIncrease)
    }
}

@Composable
private fun CaptionerNumberValue(value: Int) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
        )
    }
}

@Composable
private fun CaptionerPanelButton(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelect: () -> Unit,
) {
    Button(
        modifier = modifier
            .heightIn(min = 40.dp)
            .handleKeyEvents(onSelect = onSelect),
        onClick = {},
        shape = ButtonDefaults.shape(shape = MaterialTheme.shapes.small),
        colors = ButtonDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Text(text)
    }
}

private fun captionerOffsetWarning(offsetX: Int, offsetY: Int): String? {
    return when {
        abs(offsetX) >= 1100 || abs(offsetY) >= 650 -> "偏移已接近限制"
        abs(offsetX) > 600 || abs(offsetY) > 350 -> "偏移较大，可能贴近边缘"
        else -> null
    }
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
private val captionerDisplayDurationOptions = listOf(3, 5, 7, 10, 15).map { it.toLong() * 1000 }

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

private const val RESTART_PLAY_ACTION = "top.yogiczy.mytv.tv.RESTART_PLAY"
private const val CAPTIONER_OFFSET_STEP_PX = 50
private const val CAPTIONER_FONT_SIZE_STEP = 2
private const val CAPTIONER_TARGET_CHINESE_SIMPLIFIED = "ChineseSimplified"
private const val CAPTIONER_TARGET_CHINESE_TRADITIONAL = "ChineseTraditional"

@Preview
@Composable
private fun QuickOpBtnListPreview() {
    MyTVTheme {
        QuickOpBtnList()
    }
}
