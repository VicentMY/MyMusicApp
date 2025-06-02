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
    val duration: Long,
    val album: String,
    val track: Int,
    val size: Long,
    val path: String,
    var favorite: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    @Ignore
    var isSelected: Boolean = false
    override fun toString(): String {
        return String.format("ID: %d - Title: %s - Artist: %s - Duration: %d - Is Favorite: %s - Album: %s - Track: %d - Size: %d - Path: %s", id, title, artist, duration, favorite, album, track, size, path)
    }
}
