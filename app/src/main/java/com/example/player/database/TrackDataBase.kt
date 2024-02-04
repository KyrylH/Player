package com.example.player.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.player.data.track.dao.TrackDao
import com.example.player.data.track.dto.TrackDto
import kotlin.concurrent.Volatile

@Database(entities = [TrackDto::class,], version = 1,)
abstract class TrackDataBase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    companion object {
        private fun buildInstance(context: Context) =
            Room.databaseBuilder(context, TrackDataBase::class.java, "TrackDB.db").build()
        @Volatile
        private var INSTANCE: TrackDataBase? = null

        fun getDataBase(context: Context): TrackDataBase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = buildInstance(context = context)
                }
            }
            return INSTANCE!!
        }
    }
}