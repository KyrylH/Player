package com.example.player

import android.app.Application
import android.provider.MediaStore
import com.example.player.model.Track

class PlayerApp: Application() {

    fun getTrackData(): List<Track> {
        val trackList: MutableList<Track> = mutableListOf()
        val mediaUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        this.applicationContext.contentResolver.query(
            mediaUri,
            PROJ,
            SELECTION,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                with(cursor) {
                    trackList.add(
                        Track(
                        getLong(getColumnIndexOrThrow(PROJ[0])),
                        getString(getColumnIndexOrThrow(PROJ[1])),
                        getString(getColumnIndexOrThrow(PROJ[2])),
                        getString(getColumnIndexOrThrow(PROJ[3])),
                        getLong(getColumnIndexOrThrow(PROJ[4])),
                        getString(getColumnIndexOrThrow(PROJ[5]))
                    )
                    )
                }
            }
        }
        return trackList
    }
}

private const val SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0 and title != ''"
private val PROJ: Array<String> = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.DISPLAY_NAME,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.DATA,
)