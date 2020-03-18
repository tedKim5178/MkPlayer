package com.example.mkplayer.feature.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mkplayer.R
import com.example.mkplayer.databinding.FragmentVideoListBinding
import com.example.mkplayer.feature.playback.model.Video

// TODO :: loading view
class VideoListFragment : Fragment() {
    private lateinit var binding: FragmentVideoListBinding
    private val videoViewModel: VideoViewModel by activityViewModels()
    // TODO :: into viewModel?
    private var playlist: MutableList<Video> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPlayButton()

        videoViewModel.getVideos().observe(this, Observer {
            addVideosToRecyclerView(it)
        })
    }

    private fun setupPlayButton() {
        binding.play.apply {
            setOnClickListener {
                if (playlist.isNullOrEmpty()) {
                    Toast.makeText(context, "재생할 영상을 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                videoViewModel.setPlaylist(playlist)
                val navController = findNavController()
                navController.navigate(R.id.action_videoListFragment_to_playerFragment)
            }
        }.run {
            bringToFront()
        }
    }

    private fun setupRecyclerView() {
        binding.list.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = VideoAdapter().apply {
                setOnItemClickListener(object : VideoAdapter.OnItemClickListener {
                    override fun onItemClick(video: Video) {
                        video.isChecked = !video.isChecked
                        if (video.isChecked) playlist.add(video) else playlist.remove(video)
                    }
                })
            }
        }
    }

    private fun addVideosToRecyclerView(videos: List<Video>) {
        (binding.list.adapter as? VideoAdapter)?.let {
            it.addItems(videos)
            it.notifyDataSetChanged()
        }
    }
}