package com.example.player.ui.fragment.tracknotfound

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.player.databinding.FragmentTrackNotFoundBinding

class TrackNotFoundFragment : Fragment() {
    private val binding : FragmentTrackNotFoundBinding by lazy {
        FragmentTrackNotFoundBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            TrackNotFoundFragment()
    }
}