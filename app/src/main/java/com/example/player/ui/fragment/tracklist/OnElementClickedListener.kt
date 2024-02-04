package com.example.player.ui.fragment.tracklist

interface OnElementClickedListener {
    fun onFavoriteClicked(contentId: Long)
    fun onItemClicked(position: Int)
}
