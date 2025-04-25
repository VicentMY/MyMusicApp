package es.vmy.musicapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.vmy.musicapp.classes.Playlist

@Dao
interface PlaylistDAO {

    @Query("SELECT * FROM playlists")
    suspend fun getAll(): MutableList<Playlist>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun findById(id: Long): Playlist

    @Query("SELECT * FROM playlists WHERE title LIKE :name LIMIT 1")
    suspend fun findByName(name: String): Playlist

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(playlists: MutableList<Playlist>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: Playlist): Long

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist): Int
}