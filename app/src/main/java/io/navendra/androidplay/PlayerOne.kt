package io.navendra.androidplay

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import android.content.ContextWrapper
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE
import android.annotation.SuppressLint
import android.view.View


class PlayerOne(private var context: Context, private val playerView: PlayerView){

    private var player : ExoPlayer? = null
    private var playbackPosition :Long = 0
    private var currentWindow: Int =0
    private var playWhenReady: Boolean = true

    private var currUri:Uri? = null

    private val lifecycleObserver = PlayerLifecycleObserver(this)


    init {
        while (context !is LifecycleOwner) {
            context = (context as ContextWrapper).baseContext
        }
        lifecycleObserver.registerLifecycle((context as LifecycleOwner).lifecycle)

    }


    fun play(uri: Uri){
        currUri = uri
        initPlayer()
        preparePlayer(currUri!!)
        player?.playWhenReady = true
        hideSystemUi()

    }



     private fun initPlayer(){
        if(player == null){

            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context), DefaultTrackSelector(), DefaultLoadControl()
            ).apply {
                playWhenReady = true
                playWhenReady = playWhenReady
                seekTo(currentWindow, playbackPosition)
            }

            playerView?.player = player

        }
    }

    private fun preparePlayer(uri: Uri){
        val mediaSource = buildMediaSource(uri)
        player?.prepare(mediaSource, true, false)
    }


    private fun buildMediaSource(uri: Uri) : MediaSource {
        val userAgent = AppConstants.USER_AGENT
        val lastPath = uri.lastPathSegment?:""

        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)

        if(lastPath.contains(AppConstants.FORMAT_MP3) || lastPath.contains(AppConstants.FORMAT_MP4)){

            return ExtractorMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(uri)

        }else if(lastPath.contains(AppConstants.FORMAT_M3U8)){

            return HlsMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(uri)

        }else{
            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(defaultHttpDataSourceFactory)

            return DashMediaSource.Factory(dashChunkSourceFactory, defaultHttpDataSourceFactory)
                .createMediaSource(uri)

        }

    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player?.currentPosition ?: 0L
            currentWindow = player?.currentWindowIndex ?: 0
            playWhenReady = player?.playWhenReady ?: true
            player?.release()
            player = null
        }
    }


    //region Handle Lifecycle

    fun startPlayer() {
        player?.playWhenReady = true
        player?.playbackState
        play(currUri!!)
    }

    fun pausePlayer() {
//        releasePlayer()
        player?.playWhenReady = false
        player?.playbackState

    }

    fun stopPlayer(){
        releasePlayer()
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

}