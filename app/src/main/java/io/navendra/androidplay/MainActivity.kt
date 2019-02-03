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
        val uri = Uri.parse( "https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val adUri = Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&amp;iu=/124319096/external/ad_rule_samples&amp;ciu_szs=300x250&amp;ad_rule=1&amp;impl=s&amp;gdfp_req=1&amp;env=vp&amp;output=vmap&amp;unviewed_position_start=1&amp;cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostoptimizedpodbumper&amp;cmsid=496&amp;vid=short_onecue&amp;correlator=")

        val uriList = arrayOf<Uri>(
            Uri.parse("https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),
            Uri.parse("https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),
            Uri.parse("https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),
            Uri.parse("https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        )


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
