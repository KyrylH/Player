package com.example.player.ui.fragment

import androidx.media3.common.MediaItem

interface OnElementClickedListener {
    fun onFavoriteClicked(contentId: Long)
    fun onItemClicked(position: Int)
    fun setMediaItems(mediaItems: List<MediaItem>)
}
