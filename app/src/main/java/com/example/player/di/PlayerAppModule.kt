package com.example.player.di

import android.content.ContentResolver
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.example.player.data.track.repository.TrackRepository
import com.example.player.database.TrackDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerAppModule {
    @Provides
    @Singleton
    fun getTrackDataBase(@ApplicationContext applicationContext: Context): TrackDataBase {
        return TrackDataBase.getDataBase(applicationContext)
    }

    @Provides
    @Singleton
    fun getTrackRepository(trackDataBase: TrackDataBase): TrackRepository {
        return TrackRepository(trackDataBase)
    }

    @Provides
    @Singleton
    fun getExoPlayer(@ApplicationContext applicationContext: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        val exoPlayer = ExoPlayer.Builder(applicationContext).build()
        exoPlayer.setAudioAttributes(audioAttributes, true)
        return exoPlayer
    }

    @Provides
    @Singleton
    fun getResolver(@ApplicationContext applicationContext: Context): ContentResolver {
        return applicationContext.contentResolver
    }
}