package top.yogiczy.mytv.tv.ui.screens.videoplayer.captioner

fun interface LiveAudioCaptureSink {
    fun onPcmAudio(data: ByteArray, sampleRate: Int, channelCount: Int)
}
