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
        return SongViewHolder(view, context, listener)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = items[position]
        holder.bindItem(song, position)
    }

    override fun getItemCount() = items.size

    class SongViewHolder(v: View, mContext: Context, private val mListener: SongsAdapterListener): RecyclerView.ViewHolder(v) {

        private val songView: ConstraintLayout = v.findViewById(R.id.song_layout)
        private val favoriteBtn: ImageView = v.findViewById(R.id.iv_btn_favorite)
        private val thumbnail: ImageView = v.findViewById(R.id.iv_song_thumbnail)
        private val title: TextView = v.findViewById(R.id.tv_song_title)
        private val artist: TextView = v.findViewById(R.id.tv_song_artist)

        private val normalBGColor = mContext.getColor(R.color.md_theme_background)
        private val selectedBGColor = mContext.getColor(R.color.md_theme_inversePrimary)

        fun bindItem(s: Song, position: Int) {
            if (s.thumbnail != null) {
                thumbnail.setImageBitmap(s.thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_action_song)
            }
            title.text = s.title
            artist.text = s.artist

            if (s.favorite) {
                favoriteBtn.setImageResource(R.drawable.ic_action_favorite_on)
            } else {
                favoriteBtn.setImageResource(R.drawable.ic_action_favorite)
            }

            songView.setBackgroundColor(
                if (s.isSelected) selectedBGColor else normalBGColor
            )

            songView.setOnClickListener {
                mListener.onSongClick(s)
            }

            songView.setOnLongClickListener {
                mListener.onSongLongClick(position, s)
                true
            }

            favoriteBtn.setOnClickListener {
                mListener.onFavoriteSong(favoriteBtn, s)
            }
        }

        interface SongsAdapterListener {
            fun onSongClick(s: Song)
            fun onSongLongClick(position: Int, song: Song)
            fun onFavoriteSong(favoriteBtn: ImageView, song: Song)
        }
    }
}