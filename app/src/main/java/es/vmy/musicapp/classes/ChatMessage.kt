package es.vmy.musicapp.classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import es.vmy.musicapp.utils.getFormattedDateString

@Entity(tableName = "chatMessages")
data class ChatMessage(
    val message: String,
    val sender: String,
    val senderEmail: String,
    val timestamp: String = getFormattedDateString(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    override fun toString(): String {
        return String().format("User %s sent message %s at %d", sender, message, timestamp)
    }
}