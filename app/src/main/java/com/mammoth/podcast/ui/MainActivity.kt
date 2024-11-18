package com.mammoth.podcast.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.mammoth.podcast.CastApp
import com.mammoth.podcast.ui.theme.MammothTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val displayFeatures = calculateDisplayFeatures(this)
            MammothTheme {
                CastApp(
                    displayFeatures
                )
            }
        }
    }
}