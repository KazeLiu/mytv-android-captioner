package top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LiveCaptionPreviewState {
    var visible by mutableStateOf(false)
}

object LiveCaptionRuntimeState {
    var enabled by mutableStateOf(true)
}
