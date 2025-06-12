package es.vmy.musicapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.activities.MainActivity
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Playlist
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentInPlaylistBinding
import es.vmy.musicapp.dialogs.RenamePlaylistDialog
import es.vmy.musicapp.dialogs.TrackInfoDialog
import es.vmy.musicapp.utils.FAVORITE_SONGS_LIST_ID
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import es.vmy.musicapp.utils.idToSongList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InPlaylistFragment : Fragment(),
    OnClickListener,
    SongsAdapter.SongViewHolder.SongsAdapterListener,
    RenamePlaylistDialog.RenamePlaylistDialogListener {

    private var _binding: FragmentInPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: InPlaylistFragmentListener? = null

    private val db by lazy { AppDB.getInstance(requireContext()) }

    private lateinit var mainActivity: MainActivity
    private lateinit var songs: MutableList<Song>

    private lateinit var selectedPlaylist: Playlist

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is InPlaylistFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG InPlaylistFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInPlaylistBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        selectedPlaylist = mainActivity.getSelectedPlaylist()

        lifecycleScope.launch(Dispatchers.IO) {
            binding.rvInPlaySongs.visibility = View.GONE
            binding.musicProgressBar.visibility = View.VISIBLE

            songs = idToSongList(selectedPlaylist.songs, db.SongDAO().getAll())

            withContext(Dispatchers.Main) {
                binding.musicProgressBar.visibility = View.GONE
                binding.rvInPlaySongs.visibility = View.VISIBLE
                setUpRecycler()
            }
        }

        binding.tvInPlaylistTitle.text = selectedPlaylist.title

        if (selectedPlaylist.id == FAVORITE_SONGS_LIST_ID) {
            binding.ivInPlaylistThumbnail.setImageResource(R.drawable.ic_action_favorite_on)
            binding.addPlaylistFab.visibility = View.GONE
        } else {
            if (selectedPlaylist.thumbnail != null) {
                binding.ivInPlaylistThumbnail.setImageBitmap(selectedPlaylist.thumbnail)
            } else {
                binding.ivInPlaylistThumbnail.setImageResource(R.drawable.ic_action_playlist)
                binding.addPlaylistFab.visibility = View.VISIBLE
            }
        }

        binding.tvInPlaylistTitle.setOnClickListener(this)

        binding.ivInPlaylistBack.setOnClickListener(this)

        binding.addPlaylistFab.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.tv_in_playlist_title -> {
                if (selectedPlaylist.id != FAVORITE_SONGS_LIST_ID) {
                    RenamePlaylistDialog(this@InPlaylistFragment, selectedPlaylist.title).show(parentFragmentManager, "RENAME PLAYLIST DIALOG")
                }
            }
            R.id.iv_in_playlist_back -> {
                mListener?.onBackBtnPressed()
            }
            R.id.add_playlist_fab -> {
                if (selectedPlaylist.id == FAVORITE_SONGS_LIST_ID) {
                    Toast.makeText(requireContext(), getString(R.string.cannot_man_add_fav_songs), Toast.LENGTH_SHORT).show()
                } else {
                    mListener?.onSongAddFab()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mAdapter.isInitialized) mAdapter.notifyDataSetChanged()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun setUpRecycler() {
        mAdapter = SongsAdapter(songs, requireContext(), this@InPlaylistFragment)
        binding.rvInPlaySongs.adapter = mAdapter

        binding.rvInPlaySongs.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onSongClick(s: Song) {
        mListener?.onSongSelected(s, songs)
    }

    override fun onSongLongClick(position: Int, song: Song) {
        performContextMenuClick(position, song)
    }

    override fun onFavoriteSong(favoriteBtn: ImageView, song: Song) {
        mListener?.onFavoriteSong(favoriteBtn, song)
    }


    private  fun performContextMenuClick(position: Int, song: Song) {
        val viewHolder = binding.rvInPlaySongs.findViewHolderForAdapterPosition(position)
        if (viewHolder != null) {
            val popupMenu = PopupMenu(
                requireContext(),
                viewHolder.itemView.findViewById(R.id.tv_song_title)
            )

            popupMenu.inflate(R.menu.song_context_menu)

            popupMenu.menu[0].isEnabled = selectedPlaylist.id != FAVORITE_SONGS_LIST_ID

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_remove_from_playlist -> {
                        removeSongFromPlaylist(songs.indexOf(song))
                        true
                    }

                    R.id.track_info -> {
                        // Shows a dialog with the track's info
                        TrackInfoDialog(song).show(parentFragmentManager, "TRACK INFO DIALOG")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeSongFromPlaylist(position: Int) {

        lifecycleScope.launch(Dispatchers.IO) {

            selectedPlaylist.songs.removeAt(position)

            val tmpList = idToSongList(selectedPlaylist.songs, mainActivity.getSongs())

            if (tmpList.isNotEmpty()) {
                selectedPlaylist.thumbnail = tmpList[0].thumbnail
            }

            db.PlaylistDAO().update(selectedPlaylist)

            withContext(Dispatchers.Main) {

                if (selectedPlaylist.thumbnail != null) {
                    binding.ivInPlaylistThumbnail.setImageBitmap(selectedPlaylist.thumbnail)
                } else {
                    binding.ivInPlaylistThumbnail.setImageResource(R.drawable.ic_action_playlist)
                }

                songs.removeAt(position)
                Toast.makeText(requireContext(),getString(R.string.song_removed_from_playlist), Toast.LENGTH_SHORT).show()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    interface InPlaylistFragmentListener {
        fun onSongSelected(song: Song, songList: MutableList<Song>)
        fun onBackBtnPressed()
        fun onSongAddFab()
        fun onFavoriteSong(favoriteBtn: ImageView, song: Song)
    }

    override fun onRenamePlaylist(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            selectedPlaylist.title = name

            db.PlaylistDAO().update(selectedPlaylist)

            withContext(Dispatchers.Main) {
                binding.tvInPlaylistTitle.text = name
                Toast.makeText(requireContext(), getString(R.string.playlist_renamed), Toast.LENGTH_SHORT).show()
            }
        }
    }

}