package es.vmy.musicapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.vmy.musicapp.classes.Song

@Dao
interface SongDAO {

    @Query("SELECT * FROM songs")
    suspend fun getAll(): MutableList<Song>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun findById(id: Long): Song

    @Query("SELECT * FROM songs WHERE title LIKE :name LIMIT 1")
    suspend fun findByName(name: String): Song

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(songs: MutableList<Song>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song): Long

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)
}