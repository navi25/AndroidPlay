package io.navendra.player

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import android.content.ContextWrapper
import android.annotation.SuppressLint
import android.view.View


class AndroidPlayer(private var context: Context, private val playerView: PlayerSurface){

    private var player : ExoPlayer? = null
    private var playbackPosition :Long = 0
    private var currentWindowIndex: Int =0
    private var playWhenReady: Boolean = true

    private var currUri:Uri? = null
    private var currUriList: Array<Uri>? = null

    private val lifecycleObserver = PlayerLifecycleObserver(this)


    init {
        while (context !is LifecycleOwner) {
            context = (context as ContextWrapper).baseContext
        }
        lifecycleObserver.registerLifecycle((context as LifecycleOwner).lifecycle)

    }

    //Play single video
    fun play(uri: Uri?){
        if(uri == null) return
        currUri = uri
        initPlayer()
        preparePlayer(currUri!!)
        hideSystemUi()
    }


    //Overloaded function to play the whole playlist
    fun play(uriList : Array<Uri>?){
        if(uriList == null) return
        currUriList = uriList
        initPlayer()
        preparePlayer(currUriList!!)
        hideSystemUi()
    }

    private fun initPlayer(){
        if(player == null){
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context), DefaultTrackSelector(), DefaultLoadControl()
            )
            loadState()
            playerView.player = player
        }else{
            loadState()
        }
    }

    //Build MediaSource for one video and prepare player
    private fun preparePlayer(uri: Uri){
        val mediaSource = MediaSourceBuilder().build(uri)
        player?.prepare(mediaSource, true, false)
    }

    //Overloaded function to build MediaSource for whole playlist and prepare player
    private fun preparePlayer(uriList: Array<Uri>){
        val mediaSource = MediaSourceBuilder().build(uriList)
        player?.prepare(mediaSource, true, false)
    }

    private fun saveState(){
        if (player != null) {
            playbackPosition = player?.currentPosition ?: 0L
            currentWindowIndex = player?.currentWindowIndex ?: 0
            playWhenReady = player?.playWhenReady ?: true
        }
    }

    private fun loadState(){
        player!!.apply {
            playWhenReady = playWhenReady
            seekTo(currentWindowIndex, playbackPosition)
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            saveState()
            player?.release()
            player = null
        }
    }


    //region Handle Lifecycle

    fun start() {
        player?.playWhenReady = true
        player?.playbackState
        currUri?: play(currUri)
        currUriList?: play(currUriList)
    }

    fun resume(){
        player?.playWhenReady = true
        currUri?: play(currUri)
        currUriList?: play(currUriList)
        loadState()
    }

    fun pause() {
        player?.playWhenReady = false
        player?.playbackState
        saveState()
    }

    fun stop(){
        releasePlayer()
        player?.stop(true)
    }

    //endregion

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