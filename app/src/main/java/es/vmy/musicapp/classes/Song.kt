package es.vmy.musicapp.classes

import android.graphics.Bitmap

data class Song(
    val title: String,
    val thumbnail: Bitmap?,
    val artist: String,
    val duration: Long = 0,
    val path: String
)
