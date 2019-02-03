# AndroidPlay
A exoplayer based Media player for Android with Interactive Media Ads

### Dependencies [build.gradle (Module: app)]

```kotlin
dependencies {
    //Other dependenices
    
    //Exoplayer
    implementation "com.google.android.exoplayer:exoplayer:$exoPlayerVersion"

    //Exoplayer IMA(Interactive Media Ads)
    implementation ("com.google.android.exoplayer:extension-ima:$exoPlayerVersion"){
        exclude module: 'support-v4'
    }
}
```



### Setting up Player

```kotlin
class AndroidPlayer(private var context: Context, 
                    private val playerView: PlayerView){

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

	
    private fun initPlayer(){
        if(player == null){
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context), 
                DefaultTrackSelector(), 
                DefaultLoadControl()
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
        player?.apply {
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

}
```

### Building MediaSourceBuilder

```kotlin
class MediaSourceBuilder {

    //Build various MediaSource depending upon the type of Media for a given video uri
    fun build(uri: Uri): MediaSource {
        val userAgent = PlayerConstants.USER_AGENT
        val lastPath = uri.lastPathSegment?:""

        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)

        if(lastPath.contains(PlayerConstants.FORMAT_MP3) || 
           lastPath.contains(PlayerConstants.FORMAT_MP4)){
            
            return ExtractorMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(uri)

        }else if(lastPath.contains(PlayerConstants.FORMAT_M3U8)){

            return HlsMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(uri)

        }else{
            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                defaultHttpDataSourceFactory
            )

            return DashMediaSource.Factory(dashChunkSourceFactory, 
                                           defaultHttpDataSourceFactory
                )
                .createMediaSource(uri)

        }
    }


    //Overloaded function to Build various MediaSource for whole playlist of video uri
    fun build(uriList: Array<Uri>) : MediaSource{
        val playlistMediaSource = DynamicConcatenatingMediaSource()
        uriList.forEach { playlistMediaSource.addMediaSource(build(it)) }
        return playlistMediaSource
    }

}
```

### Handling Player Lifecycle

```kotlin
class PlayerLifecycleObserver(private val player: AndroidPlayer) : LifecycleObserver{

    fun registerLifecycle(lifecycle: Lifecycle){
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(){
        player.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        player.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(){
        player.stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(){

    }
}
```

## Using AndroidPlayer

### Setting up Layout for the video player

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"   
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
        
        <com.google.android.exoplayer2.ui.PlayerView      
            android:id="@+id/videoView"
            android:layout_height="wrap_content"	            
            android:layout_width="match_parent"	 
            />
            
</android.support.constraint.ConstraintLayout>
```

### Calling Player from Activity

```kotlin
class MainActivity : AppCompatActivity() {
    
    //Other code
    
    private enum class PlayMode { SINGLE_VIDEO, PLAY_LIST }

    private var playMode = PlayMode.SINGLE_VIDEO

	private fun startDemoOfAndroidPlayer(){
        val videoUrl = "https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
        val uri = Uri.parse(videoUrl)
        val uriList = arrayOf<Uri>(uri,uri,uri,uri)
        
        when(playMode){
            PlayMode.SINGLE_VIDEO -> playVideo(uri)
            PlayMode.PLAY_LIST -> playVideoList(uriList)
        }
    }

    private fun playVideo(uri: Uri){
     	//Here the videoView is 
        AndroidPlayer(this, videoView).play(uri)
    }

    private fun playVideoList(uriList: Array<Uri>){
        AndroidPlayer(this, videoView).play(uriList)
    }
}
```

## Additional Resources

* [Exoplayer Offlicial Developer blog](https://medium.com/google-exoplayer)
* [Exoplayer Official Github page](https://github.com/google/ExoPlayer)
* [Exoplayer Official Home Page](https://google.github.io/ExoPlayer/)
* [Building Video Player App Tutotrial by Google Developers](https://medium.com/androiddevelopers/building-a-video-player-app-in-android-part-1-5-d95770ef762d)
* [Exoplayer Tech Talk at Google I/O - 2018](https://www.youtube.com/watch?v=svdq1BWl4r8)
* [Exoplayer Tech Talk at Google I/O - 2014](https://www.youtube.com/watch?v=6VjF638VObA)



Happy Coding!

