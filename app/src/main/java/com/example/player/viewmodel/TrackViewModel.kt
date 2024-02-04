package com.example.player.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.player.database.TrackDataBase
import com.example.player.data.track.dto.TrackDto
import com.example.player.data.track.repository.TrackRepository
import com.example.player.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class TrackViewModel (
    private val application: Application
): AndroidViewModel(application) {
    private val trackRepository: TrackRepository by lazy {
        TrackRepository(TrackDataBase.getDataBase(application.applicationContext))
    }

    private val images = mutableMapOf<Long, ByteArray>()

    private val _trackList: MutableLiveData<List<Track>> = getContent().let {
        val resolvedTracks = MutableLiveData(it.sortedBy { track -> track.contentId })
        viewModelScope.launch {
            val tracksFromDb = trackRepository.getAll().sortedBy { dto -> dto.contentId }
            if (resolvedTracks.value?.count() != tracksFromDb.count()) {
                tracksFromDb.forEach { dto ->
                    trackRepository.deleteByContentId(dto.contentId)
                }
            }
            if (trackRepository.isEmpty()) {
                trackRepository.insertAll(it.map { track ->  TrackDto(track.contentId, false) })
            } else {
                resolvedTracks.value?.map {track -> tracksFromDb.forEach {dto ->
                    if (track.contentId == dto.contentId) {
                        track.favorite = dto.isFavorite
                    }
                }}
            }
        }
        resolvedTracks
    }

    //    creating livedata for getting values from activity
    fun getAll(): LiveData<List<Track>> {
        return _trackList
    }
    //    for changing state of tracks is favorite or not in track item adapter
    fun changeFavoriteTrackState(contentId: Long) {
        viewModelScope.launch {
            val temp = _trackList.value!!.map { track ->
                if (track.contentId == contentId) {
                    track.favorite = !track.favorite
                    trackRepository.updateFavorite(contentId, track.favorite)
                }
                track
            }
            _trackList.value = temp
        }
    }
    //    creating livedata for getting values for favorites from activity
    fun getFavorites(): LiveData<List<Track>> {
        val temp = _trackList.value!!.filter { t -> t.favorite }
        return MutableLiveData(temp)
    }
    fun getMediaItems(isFavorite: Boolean = false): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        val tracks = if (isFavorite) _trackList.value!!.filter { t -> t.favorite }
                        else _trackList.value!!
        for (track in tracks) {
            mediaItems.add(
                MediaItem.Builder()
                    .setUri(track.path)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .build())
                    .build())
        }
        return mediaItems
    }
    //    fetching data from content provider
    private fun getContent(): List<Track> {
        val mediaMetadataRetriever : MediaMetadataRetriever = MediaMetadataRetriever()
        val trackList: MutableList<Track> = mutableListOf()
        val mediaUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        application.applicationContext.contentResolver.query(
            mediaUri,
            PROJ,
            SELECTION,
            null,
            null
        )?.use { cursor ->
                while (cursor.moveToNext()) {
                    with(cursor) {
                        trackList.add(Track(
                            getLong(getColumnIndexOrThrow(PROJ[0])),
                            getString(getColumnIndexOrThrow(PROJ[1])),
                            getString(getColumnIndexOrThrow(PROJ[2])),
                            getString(getColumnIndexOrThrow(PROJ[3])),
                            getLong(getColumnIndexOrThrow(PROJ[4])),
                            getString(getColumnIndexOrThrow(PROJ[5]))
                        ))
                    }
                }
        }
        return trackList
    }

    companion object {
        //   Selection query for music
        private const val SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0 and title != ''"

        //   projection of track model
        private val PROJ: Array<String> = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
        )
    }
}
