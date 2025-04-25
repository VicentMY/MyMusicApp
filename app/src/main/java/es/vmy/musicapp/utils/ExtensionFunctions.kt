package es.vmy.musicapp.utils

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