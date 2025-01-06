package es.vmy.musicapp.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import es.vmy.musicapp.R
import es.vmy.musicapp.adapters.ChatAdapter
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.ChatMessage
import es.vmy.musicapp.databinding.FragmentChatBinding
import es.vmy.musicapp.utils.CHAT_COLLECTION_NAME
import es.vmy.musicapp.utils.CHAT_USERNAME_KEY
import es.vmy.musicapp.utils.LOG_TAG
import es.vmy.musicapp.utils.OFFLINE_MODE_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.USER_EMAIL_KEY
import es.vmy.musicapp.utils.getDateFromFormattedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment(), ChatAdapter.ChatViewHolder.ChatAdapterListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: ChatAdapter
    private var messages: MutableList<ChatMessage> = mutableListOf()

    private val db = Firebase.firestore

    private val prefs: SharedPreferences by lazy { requireActivity().getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE) }
    private lateinit var userEmail: String
    private lateinit var userName: String
    private var inOfflineMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        userEmail = prefs.getString(USER_EMAIL_KEY, "Me") ?: "Me"
        userName = prefs.getString(CHAT_USERNAME_KEY, userEmail) ?: userEmail
        inOfflineMode = prefs.getBoolean(OFFLINE_MODE_KEY, false)

        setUpRecycler()

        if (inOfflineMode) {
            getMessagesFromRoom()
        } else {
            getMessages()
        }

        binding.fabSendMessage.setOnClickListener {
            val sender = userName
            val content = binding.etTypeMessage.text.toString()

            if (sender == "Me") {
                Toast.makeText(requireContext(), getString(R.string.relogin_required), Toast.LENGTH_SHORT).show()
            } else {
                val msg = ChatMessage(content, sender, userEmail)

                if (inOfflineMode) {
                    storeMessageInRoom(msg)
                } else {
                    sendMessage(msg)
                }
                messages.add(msg)
                mAdapter.notifyDataSetChanged()
                binding.etTypeMessage.text = Editable.Factory.getInstance().newEditable("")
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setUpRecycler()
    }

    private fun sendMessage(msg: ChatMessage) {
        if (msg.message.isEmpty()) { return }

        lifecycleScope.launch(Dispatchers.IO) {
            db.collection(CHAT_COLLECTION_NAME)
                .add(msg)
                .addOnSuccessListener { documentReference ->
                    Log.d(LOG_TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                }
                .addOnFailureListener {
                    Log.w(LOG_TAG, "Error adding document")
                }

            withContext(Dispatchers.Main) {
                Log.d(LOG_TAG, "Message Sent")
            }
        }
    }

    private fun getMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.collection(CHAT_COLLECTION_NAME)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {

                        val msg = ChatMessage(
                            document.data["message"].toString(),
                            document.data["sender"].toString(),
                            document.data["senderEmail"].toString(),
                            document.data["timestamp"].toString()
                        )
                        messages.add(msg)
                    }
                }
                .addOnFailureListener {
                    Log.d(LOG_TAG, "Error getting documents: ")
                }

            withContext(Dispatchers.Main) {
                Log.d(LOG_TAG, "Messages retrieved")
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun storeMessageInRoom(msg: ChatMessage) {
        if (msg.message.isEmpty()) { return }
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDB.getInstance(requireContext()).ChatMessageDAO()
            val chatMessages = db.getAll()

            if (!chatMessages.contains(msg)) {
                db.insert(msg)
            }

            withContext(Dispatchers.Main) {
                Log.d(LOG_TAG, "Messages stored")
            }
        }
    }

    private fun getMessagesFromRoom() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDB.getInstance(requireContext()).ChatMessageDAO()
            messages = db.getAll()

            withContext(Dispatchers.Main) {
                Log.d(LOG_TAG, "Messages retrieved")
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpRecycler() {

        messages.sortBy { getDateFromFormattedString(it.timestamp) }

        mAdapter = ChatAdapter(messages, requireContext(), this@ChatFragment)
        binding.rvChats.adapter = mAdapter
        binding.rvChats.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onChatMessageClick(m: ChatMessage) {
        Toast.makeText(requireContext(), m.senderEmail, Toast.LENGTH_SHORT).show()
    }
}