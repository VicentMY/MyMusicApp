package es.vmy.musicapp.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.ChatMessage
import es.vmy.musicapp.utils.CHAT_COLOR_KEY
import es.vmy.musicapp.utils.CHAT_USERNAME_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.USER_EMAIL_KEY

class ChatAdapter(
    val items: MutableList<ChatMessage>,
    val context: Context,
    val listener: ChatViewHolder.ChatAdapterListener
): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_chat_item, parent, false)
        return ChatViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = items[position]
        holder.bindItem(message)
    }

    override fun getItemCount() = items.size

    class ChatViewHolder(v: View, private val mListener: ChatAdapterListener): RecyclerView.ViewHolder(v) {
        private val layout: ConstraintLayout = v.findViewById(R.id.msg_layout)
        private val lParams = layout.layoutParams as FrameLayout.LayoutParams
        private val senderTv: TextView = v.findViewById(R.id.tv_sender)
        private val messageTv: TextView = v.findViewById(R.id.tv_message)
        private val timeTv: TextView = v.findViewById(R.id.tv_time)

        private val colorSecondary = ContextCompat.getColor(itemView.context, R.color.md_theme_secondary)
        private val colorTertiary = ContextCompat.getColor(itemView.context, R.color.md_theme_tertiary)
        private val defChatColor = colorToHex( ContextCompat.getColor(itemView.context, R.color.md_theme_inversePrimary) )

        private val prefs = v.context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        private val userEmail = prefs.getString(USER_EMAIL_KEY, "Me")
        private val chatUsername = prefs.getString(CHAT_USERNAME_KEY, userEmail)
        private val chatColor = prefs.getString(CHAT_COLOR_KEY, defChatColor)

        fun bindItem(m: ChatMessage) {
            messageTv.text = m.message
            timeTv.text = m.timestamp

            if (m.senderEmail == userEmail || m.sender == chatUsername || m.sender == userEmail) {
                // If the message is from the current user
                senderTv.setTextColor(colorSecondary)
                senderTv.text = chatUsername

                // TODO: Set the color of the message to the user's custom color
                // The background is set to a color
                layout.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_round_color)
                // The message is aligned to the right
                lParams.gravity = Gravity.END
                layout.layoutParams = lParams
            } else {
                // else
                senderTv.setTextColor(colorTertiary)
                senderTv.text = m.sender

                // The background is set to white / black
                layout.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_round_other)
                // The message is aligned to the left
                lParams.gravity = Gravity.START
                layout.layoutParams = lParams
            }

            layout.setOnClickListener {
                mListener.onChatMessageClick(m)
            }
        }

        private fun colorToHex(color: Int): String {
            // Converts a resource color to a hex color String
            return String.format("#%06X", 0xFFFFFF and color)
        }

        interface ChatAdapterListener {
            fun onChatMessageClick(m: ChatMessage)
        }
    }
}