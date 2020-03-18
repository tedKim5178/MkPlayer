package com.example.mkplayer.feature.playback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// TODO :: var?
@Parcelize
data class Video(
    val id: Int,
    val contentUri: String,
    val thumbnailPath: String,
    val title: String,
    var isChecked: Boolean = false
) : Parcelable