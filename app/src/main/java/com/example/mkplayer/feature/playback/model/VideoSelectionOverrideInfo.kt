package com.example.mkplayer.feature.playback.model

data class VideoSelectionOverrideInfo(
    val groupIndex: Int,
    val trackIndex: Int
) {
    companion object {
        val AUTO = VideoSelectionOverrideInfo(-1, -1)
    }
}