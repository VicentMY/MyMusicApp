package es.vmy.musicapp.classes

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    val title: String,
    val thumbnail: Bitmap?,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    @Ignore
    var isSelected: Boolean = false
    override fun toString(): String {
        return String.format("ID: %d - Title: %s - Artist: %s - Duration: %d - Path: %s", id, title, artist, duration, path)
    }
}
