package es.vmy.musicapp.classes


import java.time.Instant

data class ChatMessage(
    val message: String,
    val sender: String,
    val senderEmail: String,
    val timestamp: String? = Instant.now().toString(),
    val documentId: String? = ""
) {
    override fun toString(): String {
        return String().format("User %s (%s), at %s sent message: %s", sender, senderEmail, timestamp, message)
    }
}