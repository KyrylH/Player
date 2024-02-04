package com.example.player.data.track.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.player.data.track.dto.TrackDto

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    suspend fun getAll(): List<TrackDto>
    @Insert
    suspend fun insertAll(trackEntities: List<TrackDto>)
    @Query("DELETE FROM tracks WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: Long)
    @Query("UPDATE tracks SET isFavorite = :state WHERE contentId = :contentId")
    suspend fun updateFavorite(state: Boolean, contentId: Long)
    @Query("SELECT (SELECT COUNT(id) FROM tracks) == 0")
    suspend fun isEmpty(): Boolean
}