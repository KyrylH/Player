package com.example.player.database.repository

import com.example.player.database.TrackDataBase
import com.example.player.database.dto.TrackDto

class TrackRepository(private val trackDb: TrackDataBase) {
    private val trackDao = trackDb.trackDao()

    suspend fun getAll(): List<TrackDto> {
        return trackDao.getAll()
    }
    suspend fun updateFavorite(contentId: Long, state: Boolean) {
        trackDao.updateFavorite(state, contentId)
    }
    suspend fun insertAll(trackEntities: List<TrackDto>) {
        trackDao.insertAll(trackEntities)
    }
    suspend fun deleteByContentId(contentId: Long) {
        trackDao.deleteByContentId(contentId)
    }
    suspend fun isEmpty(): Boolean {
        return trackDao.isEmpty()
    }
}