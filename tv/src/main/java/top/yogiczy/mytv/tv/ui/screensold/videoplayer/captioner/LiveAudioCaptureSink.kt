package top.yogiczy.mytv.tv.ui.screensold.videoplayer.captioner

fun interface LiveAudioCaptureSink {
    fun onPcmAudio(data: ByteArray, sampleRate: Int, channelCount: Int)
}
