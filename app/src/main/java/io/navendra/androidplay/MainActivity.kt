package io.navendra.androidplay

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.navendra.player.AndroidPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        val uri = Uri.parse( "https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        playVideo(uri)
    }

    private fun playVideo(uri: Uri){
        AndroidPlayer(this, videoView).play(uri)
    }

}
