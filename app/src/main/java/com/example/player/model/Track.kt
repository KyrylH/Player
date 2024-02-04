package com.example.player.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track (
    var contentId: Long,
    var title: String,
    var artist: String,
    var album: String,
    var duration: Long,
    var path: String,
    var favorite: Boolean = false
) : Parcelable