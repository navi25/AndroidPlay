package io.navendra.androidplay

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.navendra.player.AndroidPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private enum class PlayMode { SINGLE_VIDEO, PLAY_LIST, PLAY_WITH_ADS }

    private var playMode = PlayMode.PLAY_WITH_ADS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        startDemoOfAndroidPlayer()
    }

    private fun startDemoOfAndroidPlayer(){
        val uri = Uri.parse(AppConstants.VIDEO_URL)
        val adUri = Uri.parse(AppConstants.AD_URL)

        val uriList = arrayOf<Uri>(uri,uri,uri,uri)

        when(playMode){
            PlayMode.SINGLE_VIDEO -> playVideo(uri)
            PlayMode.PLAY_LIST -> playVideoList(uriList)
            PlayMode.PLAY_WITH_ADS -> playVideoWithAds(uri,adUri)
        }

    }

    private fun playVideo(uri: Uri){
        AndroidPlayer(this, videoView).play(uri)
    }

    private fun playVideoList(uriList: Array<Uri>){
        AndroidPlayer(this, videoView).play(uriList)
    }

    private fun playVideoWithAds(uri: Uri, adUri: Uri){
        AndroidPlayer(this, videoView, exo_content_frame).playWithAds(uri,adUri)
    }

}
