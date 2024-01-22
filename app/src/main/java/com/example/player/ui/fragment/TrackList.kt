package com.example.player.ui.fragment

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.player.adapter.TrackItemAdapter
import com.example.player.databinding.FragmentTracklistBinding
import com.example.player.ui.MainActivity
import com.example.player.ui.PlayerButtonsListener
import com.example.player.util.BottomNavPlayerSelection
import com.example.player.viewmodel.TrackViewModel

const val ELEMENT = "element"

class TrackList : Fragment(), OnElementClickedListener {
    private lateinit var selection: BottomNavPlayerSelection
    private lateinit var listener: PlayerButtonsListener
    private val viewModel: TrackViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = requireActivity() as MainActivity
        arguments?.run {
            selection = BottomNavPlayerSelection.valueOf(getString(ELEMENT)!!)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTracklistBinding.inflate(inflater, container, false)
        val navPlayerSelectionStrategy = mapOf(
            BottomNavPlayerSelection.ALL to viewModel.getAll(),
            BottomNavPlayerSelection.FAVORITES to viewModel.getFavorites()
        )
        val itemAdapter = TrackItemAdapter(listener = this@TrackList)

        binding.root.apply {
            adapter = itemAdapter
            navPlayerSelectionStrategy[selection]!!.observe(this@TrackList as LifecycleOwner) {
                itemAdapter.submitList(it)
            }
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context,(layoutManager as LinearLayoutManager).orientation)
            )
        }
        return binding.root
    }

    override fun onFavoriteClicked(contentId: Long) {
        viewModel.favorite(contentId)
    }
    override fun onItemClicked(position: Int) {
        listener.apply {
            onItemClicked(position)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(element: BottomNavPlayerSelection) =
            TrackList().apply {
                arguments = Bundle().apply {
                    ELEMENT to element
                }
            }
    }
}
