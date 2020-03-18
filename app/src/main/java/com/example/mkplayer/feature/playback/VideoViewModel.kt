package com.example.mkplayer.feature.playback

import android.app.Application
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mkplayer.feature.playback.model.Video

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val videos: MutableLiveData<List<Video>> by lazy {
        MutableLiveData<List<Video>>().also {
            it.value = loadPlaylist()
        }
    }

    private val playlist = MutableLiveData<List<Video>>()

    fun setPlaylist(playlist: List<Video>) {
        this.playlist.value = playlist
    }

    fun getPlaylist(): LiveData<List<Video>> {
        return playlist
    }

    fun getVideos(): LiveData<List<Video>> {
        return videos
    }

    private fun loadPlaylist(): List<Video> {
        val videos = mutableListOf<Video>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        // TODO :: projection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().contentResolver.query(collection, null, null, null).use {
                it?.let { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(it.getColumnIndex(MediaStore.Video.Media._ID))
                        val image = cursor.getString(it.getColumnIndex(MediaStore.Video.Media.DATA))
                        val title =
                            cursor.getString(it.getColumnIndex(MediaStore.Video.Media.TITLE))
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id.toLong()
                        )
                        val video = Video(
                            id = id,
                            contentUri = contentUri.toString(),
                            thumbnailPath = image,
                            title = title
                        )
                        videos.add(video)
                    }
                    cursor.close()
                    return videos
                }
            }
        }
        return mutableListOf()
    }
}