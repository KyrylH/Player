package com.example.player.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.player.R
import com.example.player.databinding.TrackItemBinding
import com.example.player.model.Track
import com.example.player.ui.fragment.OnElementClickedListener
import com.example.player.util.DurationCalcUtil

class TrackItemAdapter(
    private val listener: OnElementClickedListener
): ListAdapter<Track, TrackItemAdapter.ViewHolder>(TrackItemDiffCallback()) {
    private lateinit var binding: TrackItemBinding
    private lateinit var ctx: Context
    private val metadataRetriever = MediaMetadataRetriever()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = TrackItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        ctx = binding.root.context
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { track ->
                with(holder.binding){
                authorName.text = track.artist
                trackName.text = track.title.lastIndexOf(".").let {
                    track.title.subSequence(0, it)
                }
                trackDuration.text = DurationCalcUtil.calcDuration(track.duration)

                val temp: ByteArray? = metadataRetriever.let{
                    try {
                        metadataRetriever.setDataSource(track.path)
                        it.embeddedPicture
                    } catch (e : Exception) {
                        null
                    }
                }

                Glide.with(ctx)
                    .load(temp)
                    .placeholder(R.drawable.baseline_music_note_24)
                    .into(coverImage)

                Glide.with(ctx).load(if (track.favorite)
                    R.drawable.baseline_favorite_24
                else
                    R.drawable.baseline_favorite_border_24)
                    .into(isFavorite)

                isFavorite.setOnClickListener {
                    listener.onFavoriteClicked(track.contentId)

                    Glide.with(ctx).load(if (track.favorite)
                        R.drawable.baseline_favorite_24
                    else
                        R.drawable.baseline_favorite_border_24)
                        .into(isFavorite)
                }
                itemLayoutWrapper.setOnClickListener {
                    listener.onItemClicked(position)
                }
            }
        }
    }



    class ViewHolder(
        val binding: TrackItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
    private class TrackItemDiffCallback: DiffUtil.ItemCallback<Track>() {

        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.contentId == newItem.contentId
        }
        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}
