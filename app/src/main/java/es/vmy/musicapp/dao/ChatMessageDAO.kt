package es.vmy.musicapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.vmy.musicapp.classes.ChatMessage

@Dao
interface ChatMessageDAO {
    
    @Query("SELECT * FROM chatMessages")
    suspend fun getAll(): MutableList<ChatMessage>

    @Query("SELECT * FROM chatMessages WHERE id = :id")
    suspend fun findById(id: Long): ChatMessage

    @Query("SELECT * FROM ChatMessages WHERE senderEmail LIKE :name LIMIT 1")
    suspend fun findBySenderEmail(name: String): ChatMessage

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(chatMessages: MutableList<ChatMessage>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chatMessage: ChatMessage): Long

    @Update
    suspend fun update(chatMessage: ChatMessage)

    @Delete
    suspend fun delete(chatMessage: ChatMessage)
}