package com.example.mkplayer.feature.playback

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mkplayer.databinding.ItemVideoThumbnailBinding
import com.example.mkplayer.feature.playback.model.Video

class VideoAdapter : RecyclerView.Adapter<VideoThumbnailViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(video: Video)
    }

    private var listener: OnItemClickListener? = null
    private val datas = mutableListOf<Video>()

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoThumbnailViewHolder {
        val binding = ItemVideoThumbnailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.listener = this.listener
        return VideoThumbnailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoThumbnailViewHolder, position: Int) {
        // TODO:: -1처리
        val video = datas[position]
        holder.binding.model = video
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun get(position: Int): Video {
        return this.datas[position]
    }

    fun addItems(list: List<Video>) {
        this.datas.addAll(list)
    }
}

class VideoThumbnailViewHolder(val binding: ItemVideoThumbnailBinding) :
    RecyclerView.ViewHolder(binding.root)

@BindingAdapter("thumbnail")
fun loadThumbnail(view: ImageView, contentUri: String) {
    Glide.with(view.context)
        .load(contentUri)
        .into(view)
}
