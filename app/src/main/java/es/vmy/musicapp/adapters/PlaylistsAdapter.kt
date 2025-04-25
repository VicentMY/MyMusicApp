package es.vmy.musicapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Playlist

class PlaylistsAdapter(
    val items: MutableList<Playlist>,
    val context: Context,
    val mListener: PlaylistViewHolder.PlaylistsAdapterListener
): RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_playlist_item, parent, false)
        return PlaylistViewHolder(view, context, mListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = items[position]
        holder.bindItem(playlist)
    }

    override fun getItemCount() = items.size

    class PlaylistViewHolder(v: View, private val mContext: Context, private val mListener: PlaylistsAdapterListener): RecyclerView.ViewHolder(v) {

        private val playlistView: ConstraintLayout = v.findViewById(R.id.playlist_layout)
        private val thumbnail: ImageView = v.findViewById(R.id.iv_playlist_thumbnail)
        private val title: TextView = v.findViewById(R.id.tv_playlist_title)
        private val trackAmount: TextView = v.findViewById(R.id.tv_playlist_track_amount)

        fun bindItem(p: Playlist) {
            if (p.thumbnail != null) {
                thumbnail.setImageBitmap(p.thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_action_playlist)
            }
            title.text = p.title
            trackAmount.text = String.format("%d %s", p.songs.size, getString(mContext, R.string.tracks))

            playlistView.setOnClickListener {
                mListener.onPlaylistClick(p)
            }

            playlistView.setOnLongClickListener {
                mListener.onPlaylistLongClick(p)
                true
            }
        }

        interface PlaylistsAdapterListener {
            fun onPlaylistClick(p: Playlist)
            fun onPlaylistLongClick(p: Playlist)
        }
    }
}