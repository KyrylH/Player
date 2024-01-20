package com.example.player.ui

interface PlayerButtonsListener {
    fun onItemClicked(position: Int)
    fun setMediaItems()
    fun play(idx: Int, pos: Long)
    fun pause()
}
