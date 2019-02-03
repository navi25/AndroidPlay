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
import android.view.ViewGroup
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.ui.SimpleExoPlayerView


class AndroidPlayer(private var context: Context, private val playerView: PlayerSurface,
                    private var adView: ViewGroup? = null){

    private var player : ExoPlayer? = null
    private var playbackPosition :Long = 0
    private var currentWindowIndex: Int =0
    private var playWhenReady: Boolean = true
    private var currUri:Uri? = null
    private var currUriList: Array<Uri>? = null


    private var imaAdsLoader: ImaAdsLoader? = null

    private val lifecycleObserver = PlayerLifecycleObserver(this)


    init {
        while (context !is LifecycleOwner) {
            context = (context as ContextWrapper).baseContext
        }
        lifecycleObserver.registerLifecycle((context as LifecycleOwner).lifecycle)
        adView = (playerView).overlayFrameLayout
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


    fun playWithAds(uri:Uri, adUri: Uri){
        initPlayerWithAd()
        imaAdsLoader = ImaAdsLoader(context,adUri)
        preparePlayerWithAds(uri,adUri)
    }

    fun initPlayerWithAd(){
        val loadControl = DefaultLoadControl(
            DefaultAllocator(true, 16),
            VideoPlayerConfig.MIN_BUFFER_DURATION,
            VideoPlayerConfig.MAX_BUFFER_DURATION,
            VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
            VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER,
            VideoPlayerConfig.bufferForPlaybackAfterRebufferMs,
            true
        )
        player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(context),
            DefaultTrackSelector(),
            loadControl
        )
        loadState()
        playerView.player = player
    }

    private fun preparePlayerWithAds(uri:Uri, adUri: Uri){
        val contentMediaSource = MediaSourceBuilder().build(uri)
        val adsDataFactory = DefaultHttpDataSourceFactory(PlayerConstants.USER_AGENT)

        val mediaSourceWithAds = AdsMediaSource(contentMediaSource,adsDataFactory,imaAdsLoader!!,adView!!)

        player?.seekTo(playbackPosition)
        player?.prepare(mediaSourceWithAds)
        player?.playWhenReady = true
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
        imaAdsLoader?.release()
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

    internal object VideoPlayerConfig {
        //Minimum Video you want to buffer while Playing
        val MIN_BUFFER_DURATION = 25000
        //Max Video you want to buffer during PlayBack
        val MAX_BUFFER_DURATION = 30000
        //Min Video you want to buffer before start Playing it
        val MIN_PLAYBACK_START_BUFFER = 10000
        //Min video You want to buffer when user resumes video
        val MIN_PLAYBACK_RESUME_BUFFER = 10000

        val bufferForPlaybackAfterRebufferMs = 10000
        //Video Url
        val VIDEO_URL = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_30mb.mp4"
    }

}