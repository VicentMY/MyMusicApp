package es.vmy.musicapp.adapters

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.ChatMessage
import es.vmy.musicapp.utils.CHAT_COLOR_OTHER_KEY
import es.vmy.musicapp.utils.CHAT_COLOR_SELF_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.colorToHex
import es.vmy.musicapp.utils.instantToFormattedString

class ChatAdapter(
    private val items: MutableList<ChatMessage>,
    private val mContext: Context,
    private val userEmail: String,
    private val mListener: ChatViewHolder.ChatAdapterListener
): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.rv_chat_item, parent, false)
        return ChatViewHolder(view, mContext, userEmail, mListener)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = items[position]
        holder.bindItem(message)
    }

    override fun getItemCount() = items.size

    class ChatViewHolder(view: View, mContext: Context, private val userEmail: String, private val mListener: ChatAdapterListener): RecyclerView.ViewHolder(view) {
        // View elements
        private val messageCv: CardView = view.findViewById(R.id.msg_cardview)
        private val senderTv: TextView = view.findViewById(R.id.tv_sender)
        private val messageTv: TextView = view.findViewById(R.id.tv_message)
        private val timeTv: TextView = view.findViewById(R.id.tv_time)

        // SharedPreferences
        private val prefs = view.context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

        // Message background colors
        private val colorInversePrimary = mContext.getColor(R.color.md_theme_inversePrimary)
        private val colorInverseOnSurface = mContext.getColor(R.color.md_theme_inverseOnSurface)

        // Custom chat background colors
        private var selfChatColor = prefs.getString(CHAT_COLOR_SELF_KEY, colorToHex(colorInversePrimary)) ?: colorToHex(colorInversePrimary)
        private var otherChatColor = prefs.getString(CHAT_COLOR_OTHER_KEY, colorToHex(colorInverseOnSurface)) ?: colorToHex(colorInverseOnSurface)

        // Message sender font colors
        private val colorSecondary = mContext.getColor(R.color.md_theme_secondary)
        private val colorTertiary = mContext.getColor(R.color.md_theme_tertiary)

        fun bindItem(msg: ChatMessage) {

            // Makes sure the chat colors are preceded by '#' to avoid unwanted exceptions
            if (!selfChatColor.startsWith("#")) {
                selfChatColor = "#$selfChatColor"

                // Edits the preference so it starts with '#'
                with(prefs.edit()) {
                    putString(CHAT_COLOR_SELF_KEY, selfChatColor)
                    apply()
                }
            }
            if (!otherChatColor.startsWith("#")) {
                otherChatColor = "#$otherChatColor"

                with(prefs.edit()) {
                    putString(CHAT_COLOR_OTHER_KEY, otherChatColor)
                    apply()
                }
            }

            // If the any of the custom chat colors is not the right length the default color will be used
            //  - This also lets the user return to the default colors after using custom ones
            if (selfChatColor.length < 7) {
                selfChatColor = colorToHex(colorInversePrimary)
            }
            if (otherChatColor.length < 7) {
                otherChatColor = colorToHex(colorInverseOnSurface)
            }

            // Sets the message layout parameters
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            val cvColor: Int

            // Sets the message layout
            if (userEmail != msg.senderEmail) {
                // ... for other users' messages
                params.apply {
                    gravity = Gravity.START
                    topMargin = 25
                }
                senderTv.setTextColor(colorTertiary)
                cvColor = Color.parseColor(otherChatColor)
            } else {
                // ... for the user's messages
                params.apply {
                    gravity = Gravity.END
                    topMargin = 25
                }
                senderTv.setTextColor(colorSecondary)
                cvColor = Color.parseColor(selfChatColor)
            }

            messageCv.layoutParams = params
            messageCv.setCardBackgroundColor(cvColor)

            // Makes sure the message is readable
            val fontColor = if (ColorUtils.calculateLuminance(cvColor) < 0.5) Color.WHITE else Color.BLACK
            messageTv.setTextColor(fontColor)
            timeTv.setTextColor(fontColor)

            // Sets the message data
            senderTv.text = msg.sender
            messageTv.text = msg.message
            timeTv.text = if (msg.timestamp != null) instantToFormattedString(msg.timestamp) else ""

            // Adds the delete message listener
            messageCv.setOnClickListener {
                mListener.onDeleteChatMessage(msg, userEmail == msg.senderEmail)
            }
        }

        interface ChatAdapterListener {
            fun onDeleteChatMessage(message: ChatMessage, ownMessage: Boolean)
        }
    }
}