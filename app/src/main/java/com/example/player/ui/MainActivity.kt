package com.example.player.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.player.R
import com.example.player.databinding.ActivityMainBinding
import com.example.player.service.PlayerService
import com.example.player.ui.fragment.tracklist.ELEMENT
import com.example.player.ui.fragment.tracklist.TrackList
import com.example.player.ui.fragment.tracknotfound.TrackNotFound
import com.example.player.util.BottomNavPlayerSelection
import com.example.player.util.DurationCalcUtil
import com.example.player.viewmodel.TrackViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PlayerButtonsListener {
    @Inject
    lateinit var exoPlayer: ExoPlayer
    private var bound = false
    private var selection = BottomNavPlayerSelection.ALL
    private val trackViewModel : TrackViewModel by viewModels()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bound = true

            setMediaItems()
            exoPlayer.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    setPlayerViewViews()
                    setPlayerControllerViews()
                    super.onMediaItemTransition(mediaItem, reason)
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
                        binding.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
                    } else {
                        binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
                        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
                    }
                    super.onIsPlayingChanged(isPlaying)
                }
            })
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    private var notGrantedPermission: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                LATEST_VERSION_PERMISSIONS, REQ_CODE)
        } else {
            ActivityCompat.requestPermissions(this,
                OLDER_VERSION_PERMISSION, REQ_CODE)
        }
        for (permission in if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            LATEST_VERSION_PERMISSIONS else OLDER_VERSION_PERMISSION) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermission.add(permission)
            }
        }
        doBindService(this)
        // write when on permission granted and draw track list when permission granted
        if (notGrantedPermission.isEmpty()) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, TrackList::class.java, Bundle().apply {
                    putString(
                        ELEMENT,
                        BottomNavPlayerSelection.ALL.name)
                })
            }
        }
        setPlayerViewListeners()
        setBottomNavListener()
        setPlayerControllerListener()

        onBackPressedDispatcher.addCallback(this) {
            if (binding.playerView.playerView.visibility == View.VISIBLE) {
                binding.playerView.playerView.visibility = View.GONE
                binding.contentWrapper.visibility = View.VISIBLE
            } else {
                finish()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    notGrantedPermission.remove(permissions[i])
                }
            }
            trackViewModel.onReinit()
            if (notGrantedPermission.isEmpty()) {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container, TrackList::class.java, Bundle().apply {
                        putString(
                            ELEMENT,
                            BottomNavPlayerSelection.ALL.name)
                    })
                }
            } else {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container, TrackNotFound::class.java, Bundle().apply {
                        putString(
                            ELEMENT,
                            BottomNavPlayerSelection.ALL.name)
                    })
                }
                Snackbar.make(binding.root, "Permission not granted", Snackbar.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onDestroy() {
        exoPlayer.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        doUnbindService(this)
        super.onDestroy()
    }
    private fun setPlayerControllerViews() {
        exoPlayer.currentMediaItem?.let {
            binding.trackName.text = it.mediaMetadata.title
            binding.trackAuthor.text = it.mediaMetadata.artist
        }
        exoPlayer.currentMediaItem?.mediaMetadata?.artworkData?.let {
            binding.coverImage.setImageBitmap(
                BitmapFactory.decodeByteArray(it, 0, it.size)
            )
        }
    }
    private fun setPlayerControllerListener() {
        binding.playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                exoPlayer.play()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
            }
        }
        binding.playerControl.setOnClickListener {
            binding.playerView.playerView.visibility = View.VISIBLE
            binding.contentWrapper.visibility = View.GONE
        }
        binding.nextButton.setOnClickListener {
            exoPlayer.seekToNext()
        }

        binding.previousButton.setOnClickListener {
            exoPlayer.seekToPrevious()
        }
    }
    private fun setBottomNavListener() {
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.tracks -> {
                    selection = BottomNavPlayerSelection.ALL
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, TrackList::class.java, Bundle().apply {
                            putString(
                                ELEMENT,
                                BottomNavPlayerSelection.ALL.name)
                        })
                    }
                    true
                }
                R.id.favorites -> {
                    selection = BottomNavPlayerSelection.FAVORITES
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, TrackList::class.java, Bundle().apply {
                            putString(
                                ELEMENT,
                                BottomNavPlayerSelection.FAVORITES.name)
                        })
                    }
                    true
                }
                else -> false
            }
        }
    }
    private fun setPlayerViewViews() {
        binding.playerView.name.text = exoPlayer.currentMediaItem!!.mediaMetadata.title!!
        Glide.with(this)
            .load(exoPlayer.currentMediaItem!!.mediaMetadata.artworkData?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } ?: BitmapFactory.decodeResource(resources, R.drawable.baseline_music_note_24))
            .placeholder(R.drawable.baseline_music_note_24)
            .circleCrop()
            .into(binding.playerView.cover)
        if (exoPlayer.isPlaying) {
            binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
        } else {
            binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
        }
        binding.playerView.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progress = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                this.progress = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    exoPlayer.seekTo(progress.toLong())
                }
            }
        })
        updateSeekBarPosition()
    }
    private fun setPlayerViewListeners() {
        binding.playerView.backButton.setOnClickListener {
            binding.playerView.playerView.visibility = View.GONE
            binding.contentWrapper.visibility = View.VISIBLE
        }
        binding.playerView.playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                exoPlayer.play()
                binding.playerView.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
            }
        }
        binding.playerView.nextButton.setOnClickListener {
            exoPlayer.seekToNext()
        }
        binding.playerView.previousButton.setOnClickListener {
            exoPlayer.seekToPrevious()
        }
    }

    private fun showPlayerController(mediaItem: MediaItem) {
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause_24)
        binding.playerControl.visibility = View.VISIBLE
        binding.trackName.text = mediaItem.mediaMetadata.title
        binding.trackAuthor.text = mediaItem.mediaMetadata.artist
        Glide.with(this).load(if (mediaItem.mediaMetadata.artworkData != null) {
            BitmapFactory.decodeByteArray(mediaItem.mediaMetadata.artworkData, 0, mediaItem.mediaMetadata.artworkData!!.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.baseline_music_note_24)
        }).circleCrop()
            .placeholder(R.drawable.baseline_music_note_24)
            .into(binding.coverImage)
    }

    private fun doBindService(context: Context) {
        val intent = Intent(context, PlayerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
    private fun doUnbindService(context: Context) {
        if (bound) {
            context.unbindService(connection)
        }
    }

    private fun updateSeekBarPosition() {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                if (exoPlayer.isPlaying) {
                    binding.playerView.seekBar.max = exoPlayer.duration.toInt()
                    binding.playerView.trackDuration.text = DurationCalcUtil.calcDuration(
                        exoPlayer.duration
                    )
                    binding.playerView.seekBar.progress = exoPlayer.currentPosition.toInt()
                    binding.playerView.currentDuration.text = DurationCalcUtil.calcDuration(
                        exoPlayer.currentPosition
                    )
                }
                updateSeekBarPosition()
            }, 1000)
        }
    }

    override fun onItemClicked(position: Int) {
        play(position, 0)
        showPlayerController(exoPlayer.currentMediaItem!!)
    }
    override fun setMediaItems() {
        exoPlayer.setMediaItems(when(selection) {
            BottomNavPlayerSelection.ALL -> trackViewModel.getMediaItems()
            BottomNavPlayerSelection.FAVORITES -> trackViewModel.getMediaItems(isFavorite = true)
        })
    }
    override fun play(idx: Int, pos: Long) {
        setMediaItems()
        exoPlayer.apply {
            seekTo(idx, pos)
            prepare()
            play()
        }
    }
    override fun pause() {
        exoPlayer.pause()
    }

    companion object {
        private const val WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val LATEST_VERSION_PERMISSIONS = arrayOf(NOTIFICATION_PERMISSION,READ_MEDIA_AUDIO)
        private val OLDER_VERSION_PERMISSION = arrayOf(
            WRITE_PERMISSION,
            READ_EXTERNAL_STORAGE,
        )
        private const val REQ_CODE = 0
    }
}
