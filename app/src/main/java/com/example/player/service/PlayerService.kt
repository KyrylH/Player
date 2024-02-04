package com.example.player.service


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.media3.common.AudioAttributes
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.example.player.R

@OptIn(UnstableApi::class)
class PlayerService : Service() {
    lateinit var exoPlayer : ExoPlayer
    private val binder = PlayerBinder()
    private lateinit var notificationManager: PlayerNotificationManager
    //    notification listener
    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int,
                                             dismissedByUser: Boolean) {
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
            }
            stopForeground(notificationId)
        }
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            startForeground(notificationId, notification)
        }
    }
    //    notification description adapter
    private val descriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            return player.currentMediaItem?.mediaMetadata?.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent = Intent(this@PlayerService, PlayerService::class.java)
            return PendingIntent.getActivity(this@PlayerService, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        override fun getCurrentContentText(player: Player): String? {
            return player.currentMediaItem?.mediaMetadata?.artist.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val bitmap = player.currentMediaItem?.mediaMetadata?.artworkUri
            var image : Bitmap? = if (bitmap == null) {
                AppCompatResources.getDrawable(this@PlayerService, R.drawable.baseline_music_note_24)?.toBitmap()
            }else {
                BitmapDrawable(resources, bitmap.toString()).bitmap
            }
            return image
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.setAudioAttributes(audioAttributes, true)
        val notificationId = 1999999999
        val channelId = R.string.app_name.toString() + "_channel"
        notificationManager = PlayerNotificationManager
            .Builder(this, notificationId, channelId)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setNotificationListener(notificationListener)
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setSmallIconResourceId(R.drawable.baseline_music_note_24)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setNextActionIconResourceId(R.drawable.baseline_skip_next_24)
            .setPreviousActionIconResourceId(R.drawable.baseline_skip_previous_24)
            .setPlayActionIconResourceId(R.drawable.baseline_play_arrow_24)
            .setPauseActionIconResourceId(R.drawable.baseline_pause_24)
            .setChannelNameResourceId(R.string.app_name)
            .build()
        notificationManager.apply {
            setPlayer(exoPlayer)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setUseStopAction(false)
        }
    }
    override fun onDestroy() {
        if (exoPlayer.isPlaying) {
            exoPlayer.stop()
        }
        notificationManager.setPlayer(null)
        exoPlayer.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onDestroy()
    }

    inner class PlayerBinder : Binder() {
        fun getService() = this@PlayerService
    }
}
