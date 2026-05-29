package top.yogiczy.mytv.tv.ui.screensold.videoplayer

import android.view.SurfaceView
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.SubtitleView
import androidx.tv.material3.Text
import top.yogiczy.mytv.tv.ui.material.Visibility
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screen.settings.settingsVM
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner.LiveCaptionController
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner.LiveCaptionPreviewState
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner.LiveCaptionRuntimeState
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.components.VideoPlayerError
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.components.VideoPlayerMetadata
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.player.Media3VideoPlayer
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.player.VideoPlayer
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.Configs

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    modifier: Modifier = Modifier,
    state: VideoPlayerState = rememberVideoPlayerState(),
    showMetadataProvider: () -> Boolean = { false },
) {
    val context = LocalContext.current
    val settingsViewModel = settingsVM

    Box(modifier = modifier.fillMaxSize()) {
        val displayModeModifier = when (state.displayMode) {
            VideoPlayerDisplayMode.ORIGINAL -> Modifier.aspectRatio(state.aspectRatio)
            VideoPlayerDisplayMode.FILL -> Modifier.fillMaxSize()
            VideoPlayerDisplayMode.CROP -> Modifier
                .fillMaxWidth()
                .aspectRatio(state.aspectRatio)

            VideoPlayerDisplayMode.FOUR_THREE -> Modifier.aspectRatio(4f / 3)
            VideoPlayerDisplayMode.SIXTEEN_NINE -> Modifier.aspectRatio(16f / 9)
            VideoPlayerDisplayMode.WIDE -> Modifier.aspectRatio(2.35f / 1)
        }

        when (settingsViewModel.videoPlayerRenderMode) {
            Configs.VideoPlayerRenderMode.SURFACE_VIEW -> {
                AndroidView(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(displayModeModifier),
                    factory = { SurfaceView(context) },
                    update = { state.setVideoSurfaceView(it) },
                )
            }

            Configs.VideoPlayerRenderMode.TEXTURE_VIEW -> {
                AndroidView(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(displayModeModifier),
                    factory = { TextureView(context) },
                    update = { state.setVideoTextureView(it) },
                )
            }
        }

        if (state.instance is Media3VideoPlayer) {
            AndroidView(
                factory = { SubtitleView(context) },
                update = {
                    state.instance.onCues { cues -> it.setCues(cues) }
                },
            )
        }
    }

    VideoPlayerScreenCover(
        showMetadataProvider = showMetadataProvider,
        metadataProvider = state::metadata,
        errorProvider = state::error,
        captionItemsProvider = state::captionItems,
        captionBilingualProvider = { settingsViewModel.captionerBilingualEnabled },
        captionTextColorProvider = { settingsViewModel.captionerTextColor.value.toComposeColor() },
        captionBackgroundColorProvider = { settingsViewModel.captionerBackgroundColor.value.toComposeColor() },
        captionPositionProvider = { settingsViewModel.captionerPosition },
        captionOffsetXProvider = { settingsViewModel.captionerOffsetX },
        captionOffsetYProvider = { settingsViewModel.captionerOffsetY },
        captionTextAlignProvider = { settingsViewModel.captionerTextAlign },
        captionSingleLineModeProvider = { settingsViewModel.captionerSingleLineMode },
        captionPrimaryFontSizeProvider = { settingsViewModel.captionerPrimaryFontSize },
        captionSecondaryFontSizeProvider = { settingsViewModel.captionerSecondaryFontSize },
    )
}

@Composable
private fun VideoPlayerScreenCover(
    modifier: Modifier = Modifier,
    showMetadataProvider: () -> Boolean = { false },
    metadataProvider: () -> VideoPlayer.Metadata = { VideoPlayer.Metadata() },
    errorProvider: () -> String? = { null },
    captionItemsProvider: () -> List<LiveCaptionController.SubtitleItem> = { emptyList() },
    captionBilingualProvider: () -> Boolean = { true },
    captionTextColorProvider: () -> Color = { Color.White },
    captionBackgroundColorProvider: () -> Color = { Color.Black.copy(alpha = 0.68f) },
    captionPositionProvider: () -> Configs.CaptionerPosition = { Configs.CaptionerPosition.BOTTOM },
    captionOffsetXProvider: () -> Int = { 0 },
    captionOffsetYProvider: () -> Int = { 0 },
    captionTextAlignProvider: () -> Configs.CaptionerTextAlign = { Configs.CaptionerTextAlign.CENTER },
    captionSingleLineModeProvider: () -> Boolean = { false },
    captionPrimaryFontSizeProvider: () -> Int = { 26 },
    captionSecondaryFontSizeProvider: () -> Int = { 18 },
) {
    val childPadding = rememberChildPadding()
    val previewItems = remember {
        listOf(
            LiveCaptionController.SubtitleItem(
                id = "caption-preview-1",
                start = 0.0,
                end = 1.0,
                sourceText = "Live caption preview line one",
                translatedText = "字幕预览第一行",
                forced = false,
                createdAt = 0L,
            ),
            LiveCaptionController.SubtitleItem(
                id = "caption-preview-2",
                start = 1.0,
                end = 2.0,
                sourceText = "Use offset and alignment to fit the screen edge",
                translatedText = "调整偏移与对齐，观察屏幕边界",
                forced = false,
                createdAt = 0L,
            ),
            LiveCaptionController.SubtitleItem(
                id = "caption-preview-3",
                start = 2.0,
                end = 3.0,
                sourceText = "New subtitles roll in from the bottom",
                translatedText = "新字幕会从底部平滑滚入",
                forced = false,
                createdAt = 0L,
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Visibility(showMetadataProvider) {
            VideoPlayerMetadata(
                modifier = Modifier.padding(start = childPadding.start, top = childPadding.top),
                metadataProvider = metadataProvider,
            )
        }

        VideoPlayerError(
            modifier = Modifier.align(Alignment.Center),
            errorProvider = errorProvider,
        )

        val captionItems = if (LiveCaptionPreviewState.visible) {
            previewItems
        } else if (LiveCaptionRuntimeState.enabled) {
            captionItemsProvider()
        } else {
            emptyList()
        }

        Visibility({ captionItems.isNotEmpty() }) {
            val position = captionPositionProvider()
            val captionAlignment = when (position) {
                Configs.CaptionerPosition.TOP -> Alignment.TopCenter
                Configs.CaptionerPosition.CENTER -> Alignment.Center
                Configs.CaptionerPosition.BOTTOM -> Alignment.BottomCenter
            }
            val captionModifier = when (position) {
                Configs.CaptionerPosition.TOP -> Modifier
                    .align(captionAlignment)
                    .padding(top = childPadding.top + 32.dp)

                Configs.CaptionerPosition.CENTER -> Modifier.align(captionAlignment)
                Configs.CaptionerPosition.BOTTOM -> Modifier
                    .align(captionAlignment)
                    .padding(bottom = childPadding.bottom + 32.dp)
            }

            LiveCaptionOverlay(
                modifier = captionModifier.offset {
                    IntOffset(captionOffsetXProvider(), captionOffsetYProvider())
                },
                itemsProvider = { captionItems },
                bilingualProvider = captionBilingualProvider,
                textColorProvider = captionTextColorProvider,
                backgroundColorProvider = captionBackgroundColorProvider,
                textAlignProvider = captionTextAlignProvider,
                singleLineModeProvider = captionSingleLineModeProvider,
                primaryFontSizeProvider = captionPrimaryFontSizeProvider,
                secondaryFontSizeProvider = captionSecondaryFontSizeProvider,
            )
        }
    }
}

@Composable
private fun LiveCaptionOverlay(
    modifier: Modifier = Modifier,
    itemsProvider: () -> List<LiveCaptionController.SubtitleItem> = { emptyList() },
    bilingualProvider: () -> Boolean = { true },
    textColorProvider: () -> Color = { Color.White },
    backgroundColorProvider: () -> Color = { Color.Black.copy(alpha = 0.68f) },
    textAlignProvider: () -> Configs.CaptionerTextAlign = { Configs.CaptionerTextAlign.CENTER },
    singleLineModeProvider: () -> Boolean = { false },
    primaryFontSizeProvider: () -> Int = { 26 },
    secondaryFontSizeProvider: () -> Int = { 18 },
) {
    val textColor = textColorProvider()
    val textAlign = textAlignProvider()
    val singleLineMode = singleLineModeProvider()
    val primaryFontSize = primaryFontSizeProvider()
    val secondaryFontSize = secondaryFontSizeProvider()
    val displayItems = if (singleLineMode) itemsProvider().takeLast(1) else itemsProvider()

    Column(
        modifier = modifier
            .fillMaxWidth(0.82f)
            .widthIn(max = 960.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColorProvider())
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .animateContentSize(animationSpec = tween(220)),
        horizontalAlignment = textAlign.toHorizontalAlignment(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        displayItems.forEach { item ->
            key(item.id) {
                LiveCaptionItem(
                    item = item,
                    bilingual = bilingualProvider(),
                    textColor = textColor,
                    textAlign = textAlign.toTextAlign(),
                    singleLineMode = singleLineMode,
                    primaryFontSize = primaryFontSize,
                    secondaryFontSize = secondaryFontSize,
                )
            }
        }
    }
}

@Composable
private fun LiveCaptionItem(
    item: LiveCaptionController.SubtitleItem,
    bilingual: Boolean,
    textColor: Color,
    textAlign: TextAlign,
    singleLineMode: Boolean,
    primaryFontSize: Int,
    secondaryFontSize: Int,
) {
    var visible by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(item.id) { visible = true }

    val primaryText = item.translatedText.ifBlank { item.sourceText }
    val secondaryText = item.sourceText.takeIf { bilingual && it.isNotBlank() && it != primaryText }
    val itemTextColor = if (item.partial) textColor.copy(alpha = 0.86f) else textColor

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(180)) + slideInVertically(
            animationSpec = tween(220),
            initialOffsetY = { it / 2 },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = when (textAlign) {
                TextAlign.Left -> Alignment.Start
                TextAlign.Right -> Alignment.End
                else -> Alignment.CenterHorizontally
            },
        ) {
            Text(
                text = primaryText,
                color = itemTextColor,
                fontSize = primaryFontSize.sp,
                textAlign = textAlign,
                lineHeight = (primaryFontSize + 6).sp,
                maxLines = if (singleLineMode) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            if (secondaryText != null) {
                Text(
                    text = secondaryText,
                    color = itemTextColor.copy(alpha = 0.72f),
                    fontSize = secondaryFontSize.sp,
                    textAlign = textAlign,
                    lineHeight = (secondaryFontSize + 4).sp,
                    maxLines = if (singleLineMode) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun Int.toComposeColor(): Color = Color(toLong() and 0xFFFFFFFF)

private fun Configs.CaptionerTextAlign.toTextAlign(): TextAlign {
    return when (this) {
        Configs.CaptionerTextAlign.LEFT -> TextAlign.Left
        Configs.CaptionerTextAlign.CENTER -> TextAlign.Center
        Configs.CaptionerTextAlign.RIGHT -> TextAlign.Right
    }
}

private fun Configs.CaptionerTextAlign.toHorizontalAlignment(): Alignment.Horizontal {
    return when (this) {
        Configs.CaptionerTextAlign.LEFT -> Alignment.Start
        Configs.CaptionerTextAlign.CENTER -> Alignment.CenterHorizontally
        Configs.CaptionerTextAlign.RIGHT -> Alignment.End
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun VideoPlayerScreenCoverPreview() {
    MyTvTheme {
        PreviewWithLayoutGrids {
            VideoPlayerScreenCover(
                showMetadataProvider = { true },
                metadataProvider = {
                    VideoPlayer.Metadata(
                        video = VideoPlayer.Metadata.Video(
                            width = 1920,
                            height = 1080,
                            color = "BT2020/Limited range/HLG/8/8",
                            bitrate = 10605096,
                            mimeType = "video/hevc",
                            decoder = "c2.goldfish.h264.decoder",
                        ),

                        audio = VideoPlayer.Metadata.Audio(
                            channels = 2,
                            sampleRate = 32000,
                            bitrate = 256 * 1024,
                            mimeType = "audio/mp4a-latm",
                        ),
                    )
                },
                errorProvider = { "ERROR_CODE_BEHIND_LIVE_WINDOW" },
            )
        }
    }
}
