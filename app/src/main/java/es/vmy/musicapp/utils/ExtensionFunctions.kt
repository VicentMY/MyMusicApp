package es.vmy.musicapp.utils

import android.content.Context
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Song

fun instantToFormattedString(instant: String): String {
    val newDate: String
    val newTime: String

    try {
        val instantV = instant.split("T")

        // Formats the DATE
        val date = instantV[0].split("-")
        newDate = String.format("%s/%s/%s", date[2], date[1], date[0])

        // Formats the TIME
        val time = instantV[1].split(".")[0]
        val timeV = time.split(":")
        newTime = String.format("%s:%s", timeV[0], timeV[1])

    } catch (e: IndexOutOfBoundsException) {
        // If for some reason the instant doesn't have the required format
        return ""
    }
    // Final format => "dd/MM/yyyy HH:mm"
    return String.format("%s %s", newDate, newTime)
}

fun colorToHex(color: Int): String {
    // Converts a resource color to a hex color String
    return String.format("#%06X", 0xFFFFFF and color)
}

fun idToSongList(idList: List<Long>, songList: List<Song>): MutableList<Song> {

    val newSongList: MutableList<Song> = mutableListOf()

    // For each id in the idList, find the song in the songList and add it to the newSongList
    idList.forEach { id ->
        songList.forEach { song ->
            if (song.id == id) {
                newSongList.add(song)
            }
        }
    }

    return newSongList
}

fun formatTime(mSec: Long): String {
    // Calculates the minutes and seconds of the song
    val min = (mSec / 1000) / 60
    val sec = (mSec / 1000) % 60
    return String.format("%02d:%02d", min, sec)
}

fun bytesToFormattedMBString(bytes: Long): String {
    val kb: Float = (bytes / 1024).toFloat()
    val mb: Float = kb / 1024

    return String.format("%.2f MB", mb)
}

fun summarizeSongPath(context: Context, longPath: String): String {
    val sdPrefix = context.getString(R.string.sd_card_prefix)
    val internalPrefix = context.getString(R.string.internal_storage_prefix)

    return if (longPath.startsWith("/storage/emulated/0")) {
        "$internalPrefix${longPath.substring(20)}"
    } else {
        "$sdPrefix${longPath.substring(19)}"
    }
}