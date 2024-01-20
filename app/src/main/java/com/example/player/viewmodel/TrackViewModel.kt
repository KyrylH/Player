package com.example.player.viewmodel

import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.player.database.TrackDataBase
import com.example.player.database.dto.TrackDto
import com.example.player.database.repository.TrackRepository
import com.example.player.model.Track
import kotlinx.coroutines.launch

class TrackViewModel(
    private val dataBase: TrackDataBase,
    private val contentResolver: ContentResolver
): ViewModel() {
    //    lazy initialization of repository
    private val trackRepository: TrackRepository by lazy{
        TrackRepository(dataBase)
    }
    private val _trackList: MutableLiveData<List<Track>> = getContent().let {
        val resolvedTracks = MutableLiveData(it.sortedBy { r -> r.contentId })
        viewModelScope.launch {
            val tracksFromDb = trackRepository.getAll().sortedBy { g -> g.contentId }
            if (resolvedTracks.value?.count() != tracksFromDb.count()) {
                tracksFromDb.forEach { tfd ->
                    trackRepository.deleteByContentId(tfd.contentId)
                }
            }
            if (trackRepository.isEmpty()) {
                trackRepository.insertAll(it.map { m ->  TrackDto(m.contentId, false) })
            } else {
                resolvedTracks.value?.map {rt -> tracksFromDb.forEach {tfd ->
                    if (rt.contentId == tfd.contentId) {
                        rt.favorite = tfd.isFavorite
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
    fun favorite(contentId: Long) {
        viewModelScope.launch {
            val temp = _trackList.value!!.map { t ->
                if (t.contentId == contentId) {
                    t.favorite = !t.favorite
                    trackRepository.updateFavorite(contentId, t.favorite)
                }
                t
            }
            _trackList.value = temp
        }
    }
    //    creating livedata for getting values for favorites from activity
    fun getFavorites(): LiveData<List<Track>> {
        val temp = _trackList.value!!.filter { t -> t.favorite }
        return MutableLiveData(temp)
    }
    fun getMediaItems(): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        for (track in _trackList.value!!) {
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
    fun getMediaItemsFavorites(): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        for (track in _trackList.value!!.filter { t -> t.favorite }) {
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
        val trackList: MutableList<Track> = mutableListOf()
        val mediaUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        contentResolver.query(
            mediaUri,
            PROJ,
            SELECTION,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                Log.d(TAG, "getContent: ${cursor.getString(cursor.getColumnIndexOrThrow(PROJ[1]))}")
                trackList.add(Track(
                    cursor.getLong(cursor.getColumnIndexOrThrow(PROJ[0])),
                    cursor.getString(cursor.getColumnIndexOrThrow(PROJ[1])),
                    cursor.getString(cursor.getColumnIndexOrThrow(PROJ[2])),
                    cursor.getString(cursor.getColumnIndexOrThrow(PROJ[3])),
                    cursor.getLong(cursor.getColumnIndexOrThrow(PROJ[4])),
                    cursor.getString(cursor.getColumnIndexOrThrow(PROJ[5]))
                ))
            }
        }
        return trackList
    }

    companion object {
        //   Selection query for music
            private const val SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

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