package com.example.mkplayer.feature.playback

import android.app.PictureInPictureParams
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.mkplayer.AD_URL_TAG_KEY
import com.example.mkplayer.R
import com.example.mkplayer.SelectorFragment
import com.example.mkplayer.databinding.FragmentPlayerBinding
import com.example.mkplayer.feature.playback.model.Video
import com.example.mkplayer.feature.playback.model.VideoSelectionOverrideInfo
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlin.math.max

class PlayerFragment : Fragment() {
    private var player: SimpleExoPlayer? = null
    private var imaAdsLoader: ImaAdsLoader? = null
    private var dataSourceFactory: DefaultDataSourceFactory? = null
    private var trackSelector: DefaultTrackSelector? = null
    // TODO :: startPosition, startWindow
    private var startPosition = 0L
    private var startWindow = 0

    private var adUrlTag: String? = null
    private var hasAds = false
    private var selectorFragment: SelectorFragment? = null
    private val videoOverrides: SparseArray<DefaultTrackSelector.SelectionOverride> = SparseArray()
    private var videoRendererIndex =
        VIDEO_RENDERER_INDEX_UNSET

    private var playlist = listOf<Video>()
    private lateinit var binding: FragmentPlayerBinding
    private var shouldPlayWithDelay = true

    companion object {
        const val VIDEO_RENDERER_INDEX_UNSET = -1
    }

    private val videoViewModel: VideoViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoViewModel.getPlaylist().observe(this, Observer {
            this.playlist = it
            initializePlayer()
            shouldPlayWithDelay = false
        })

        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        binding.playerView.setControllerVisibilityListener {
            if (it == View.VISIBLE) {
                binding.selectTracksButton.visibility = View.VISIBLE
                binding.pip.visibility = View.VISIBLE
                updateButtonEnabled()
            } else {
                binding.selectTracksButton.visibility = View.GONE
                binding.pip.visibility = View.GONE
            }
        }

        binding.selectTracksButton.setOnClickListener {
            val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
            var trackGroupArray: TrackGroupArray? = null
            mappedTrackInfo?.let {
                for (rendererIndex in 0 until it.rendererCount) {
                    if (C.TRACK_TYPE_VIDEO == it.getRendererType(rendererIndex)) {
                        videoRendererIndex = rendererIndex
                        trackGroupArray = it.getTrackGroups(rendererIndex)
                        break
                    }
                }
            }

            if (trackGroupArray?.isEmpty == false) {
                showTrackSelectionSelector(trackGroupArray!!)
            }
        }

        binding.pip.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builder = PictureInPictureParams.Builder().build()
                activity?.enterPictureInPictureMode(builder)
            }
        }

        // TODO :: use viewModel? or arguments?
        arguments?.let {
            adUrlTag = it.getString(AD_URL_TAG_KEY)?.also {
                hasAds = true
            }
        }
        clearStartPosition()

        // TODO :: Cast
        return binding.root
    }

    private fun showTrackSelectionSelector(trackGroupArray: TrackGroupArray) {
        val heights: ArrayList<String> = arrayListOf()
        val tracks: ArrayList<VideoSelectionOverrideInfo> = arrayListOf()
        heights.add("Auto")
        // Auto
        tracks.add(VideoSelectionOverrideInfo.AUTO)
        for (groupIndex in 0 until trackGroupArray.length) {
            val trackGroup = trackGroupArray.get(groupIndex)
            for (trackIndex in 0 until trackGroup.length) {
                val trackHeight = trackGroup.getFormat(trackIndex).height.toString()
                heights.add(trackHeight)
                tracks.add(
                    VideoSelectionOverrideInfo(
                        groupIndex,
                        trackIndex
                    )
                )
            }
        }

        selectorFragment = SelectorFragment.getInstance(heights).also {
            it.setSelectorItemClickedCallback(object :
                SelectorFragment.SelectorItemClickedCallback {
                override fun onClick(position: Int) {
                    SelectorFragment.hide(it)
                    if (position == RecyclerView.NO_POSITION) return
                    val trackInfo = tracks[position]
                    changeTrack(trackInfo, trackGroupArray)
                }
            })
            SelectorFragment.show(
                activity as AppCompatActivity,
                R.id.overlay,
                it
            )
        }
    }

    private fun changeTrack(
        videoTrackInfo: VideoSelectionOverrideInfo,
        trackGroupArray: TrackGroupArray
    ) {
        // question :: why overrides.get(0) ?
        // at most one SelectionOverride per track group.
        // not sure when allowMultipleTracks == true
        // normally, don't need to care
        trackSelector?.let { trackSelector ->
            val builder = trackSelector.parameters.buildUpon()
            if (videoRendererIndex != VIDEO_RENDERER_INDEX_UNSET) {
                builder.clearSelectionOverrides(videoRendererIndex)
                if (videoTrackInfo != VideoSelectionOverrideInfo.AUTO) {
                    val trackIndex = videoTrackInfo.trackIndex
                    val groupIndex = videoTrackInfo.groupIndex
                    videoOverrides.clear()
                    videoOverrides.put(
                        groupIndex,
                        DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex)
                    )

                    val videoOverrideList =
                        ArrayList<DefaultTrackSelector.SelectionOverride>(videoOverrides.size())
                    val size = videoOverrides.size()
                    for (index in 0 until size) {
                        videoOverrideList.add(videoOverrides.valueAt(index))
                    }
                    builder.setSelectionOverride(
                        videoRendererIndex,
                        trackGroupArray,
                        videoOverrideList[0]
                    )
                }
                trackSelector.setParameters(builder)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (shouldPlayWithDelay) return
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.playerView.onResume()
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun initializePlayer() {
        // tip1 :: default is Adaptive.
        // tip2 :: if only one track is available, use Fixed instead of Adpative internally
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(trackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        binding.playerView.player = player
        dataSourceFactory = DefaultDataSourceFactory(context, "exoplayer-codelab")

        // TODO :: add Ad later
//        if (hasAds) {
//            source = getAdsMediaSource(source)
//        }

        val mediaSourceArray = arrayOfNulls<MediaSource>(playlist.size)
        for ((index, video) in playlist.withIndex()) {
            mediaSourceArray[index] = getLocalMediaSource(video.contentUri)
        }
        val concatenatedSource = ConcatenatingMediaSource(*mediaSourceArray)
        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player?.seekTo(startWindow, startPosition)
        }
        player?.prepare(concatenatedSource, !haveStartPosition, false)
        player?.playWhenReady = true
    }

    private fun getMediaSource(contentUrl: String): MediaSource {
        return HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(contentUrl))
    }

    // TODO :: parsing url, giving right type of media source
    private fun getLocalMediaSource(url: String): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
    }

    private fun getAdsMediaSource(mediaSource: MediaSource): MediaSource {
        imaAdsLoader = ImaAdsLoader(context, Uri.parse(adUrlTag))
        imaAdsLoader?.setPlayer(player)
        return AdsMediaSource(
            mediaSource,
            dataSourceFactory,
            imaAdsLoader,
            binding.playerView
        )
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.onPause()
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        updateStartPosition()
        player?.release()
        player = null
        dataSourceFactory = null
        imaAdsLoader?.setPlayer(null)
    }

    private fun updateStartPosition() {
        startPosition = max(0, player?.contentPosition ?: 0)
        startWindow = player?.currentWindowIndex ?: 0
    }

    private fun updateButtonEnabled() {
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
        var hasEnoughTrack = false
        mappedTrackInfo?.let {
            for (rendererIndex in 0 until it.rendererCount) {
                val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
                if (trackGroupArray.length == 0) continue
                when (mappedTrackInfo.getRendererType(rendererIndex)) {
                    C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT -> {
                        hasEnoughTrack = true
                    }
                    else -> {
                        Unit
                    }
                }
            }
        }
        binding.selectTracksButton.isEnabled = player != null && hasEnoughTrack
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        binding.playerView.useController = !isInPictureInPictureMode
    }

    private fun clearStartPosition() {
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }
}