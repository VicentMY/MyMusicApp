package es.vmy.musicapp.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.adapters.ChatAdapter
import es.vmy.musicapp.classes.ChatMessage
import es.vmy.musicapp.databinding.FragmentChatBinding
import es.vmy.musicapp.dialogs.DeleteMessageDialog
import es.vmy.musicapp.utils.CHAT_USERNAME_KEY
import es.vmy.musicapp.utils.FireStoreManager
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.USER_EMAIL_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment(), ChatAdapter.ChatViewHolder.ChatAdapterListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: ChatAdapter
    private var messages: MutableList<ChatMessage> = mutableListOf()

    private val fireStoreManager: FireStoreManager by lazy { FireStoreManager() }

    private val prefs: SharedPreferences by lazy { requireActivity().getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE) }
    private lateinit var userEmail: String
    private lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        userEmail = prefs.getString(USER_EMAIL_KEY, "Me") ?: "Me"
        userName = prefs.getString(CHAT_USERNAME_KEY, userEmail) ?: userEmail

        setUpRecycler()

        binding.fabSendMessage.setOnClickListener {
            val sender = userName
            val message = binding.etTypeMessage.text.toString()

            if (sender == "Me") {
                Toast.makeText(requireContext(), getString(R.string.relogin_required), Toast.LENGTH_SHORT).show()
            } else if (message.isNotBlank()) {
                sendMessage(message)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setUpRecycler()
    }

    private fun sendMessage(msg: String) {
        val newMsg = ChatMessage(msg, userName, userEmail)

        lifecycleScope.launch(Dispatchers.IO) {
            val result = fireStoreManager.addMessage(newMsg)
            if (result) {
                withContext(Dispatchers.Main) {
                    binding.etTypeMessage.text = Editable.Factory.getInstance().newEditable("")
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.err_add_msg), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            fireStoreManager.getMessagesFlow()
                .collect {newMessages ->
                    messages.clear()
                    messages.addAll(newMessages)

                    withContext(Dispatchers.Main) {
                        mAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    private fun setUpRecycler() {
        binding.rvChats.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false )

        mAdapter = ChatAdapter(messages, requireContext(), userEmail, this@ChatFragment)
        binding.rvChats.adapter = mAdapter

        getMessages()
    }

    override fun onDeleteChatMessage(message: ChatMessage, ownMessage: Boolean) {
        if (ownMessage) {
            val docId = message.documentId
            if (docId != null) {
                DeleteMessageDialog(fireStoreManager, message.documentId, requireContext()).show(parentFragmentManager, "DELETE MESSAGE DIALOG")
            } else {
                Toast.makeText(requireContext(), getString(R.string.err_retrieving_msg), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.msg_deletion_not_permitted), Toast.LENGTH_SHORT).show()
        }
    }
}