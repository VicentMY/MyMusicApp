package es.vmy.musicapp.utils

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import es.vmy.musicapp.classes.ChatMessage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FireStoreManager {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun getMessagesFlow(): Flow<MutableList<ChatMessage>> = callbackFlow {

        val messagesCollection : CollectionReference?

        try {
            messagesCollection = firestore.collection(CHAT_COLLECTION_NAME)

            val subscription = messagesCollection.orderBy("timestamp").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = mutableListOf<ChatMessage>()
                    snapshot.forEach {
                        messages.add(
                            ChatMessage(
                                it.get("message").toString(),
                                it.get("sender").toString(),
                                it.get("senderEmail").toString(),
                                it.get("timestamp").toString(),
                                it.id
                            )
                        )
                    }

                    trySend(messages)
                }
            }

            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.printStackTrace().toString())
            close(e)
        }
    }

    suspend fun addMessage(msg: ChatMessage): Boolean {
        return try {
            firestore.collection(CHAT_COLLECTION_NAME).add(msg).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeMessage(messageId: String): Boolean {
        return try {
            firestore.collection(CHAT_COLLECTION_NAME).document(messageId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}