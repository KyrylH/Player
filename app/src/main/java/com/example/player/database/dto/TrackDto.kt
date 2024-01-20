package com.example.player.database.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackDto (
    val contentId: Long,
    val isFavorite: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}