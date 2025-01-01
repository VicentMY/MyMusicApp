package es.vmy.musicapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Song

class SongsAdapter(
    val items: MutableList<Song>,
    val context: Context,
    val listener: SongViewHolder.SongsAdapterListener
): RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_song_item, parent, false)
        return SongViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = items[position]
        holder.bindItem(song)
    }

    override fun getItemCount() = items.size

    class SongViewHolder(v: View, private val mListener: SongsAdapterListener): RecyclerView.ViewHolder(v) {

        private val songView : ConstraintLayout = v.findViewById(R.id.song_layout)
        private val thumbnail: ImageView = v.findViewById(R.id.iv_thumbnail)
        private val title: TextView = v.findViewById(R.id.tv_song_title)
        private val artist: TextView = v.findViewById(R.id.tv_song_artist)

        fun bindItem(s: Song) {
            if (s.thumbnail != null) {
                thumbnail.setImageBitmap(s.thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_action_song)
            }
            title.text = s.title
            artist.text = s.artist

            songView.setOnClickListener {
                mListener.onSongClick(s)
            }
        }

        interface SongsAdapterListener {
            fun onSongClick(s: Song)
        }
    }
}