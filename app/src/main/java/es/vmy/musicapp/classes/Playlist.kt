package es.vmy.musicapp.classes

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    var title: String,
    var thumbnail: Bitmap?,
    var songs: MutableList<Long> = mutableListOf(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    override fun toString(): String {
        return String.format("ID: %d - Name: %s - Songs: %s", id, title, songs)
    }
}