package es.vmy.musicapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
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

    // Retrieves list of songs from selectedPlaylist in MainActivity
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

        songs = idToSongList(selectedPlaylist.songs, mainActivity.getSongs())

        binding.tvInPlaylistTitle.text = selectedPlaylist.title

        if (selectedPlaylist.thumbnail != null) {
            binding.ivInPlaylistThumbnail.setImageBitmap(selectedPlaylist.thumbnail)
        } else {
            binding.ivInPlaylistThumbnail.setImageResource(R.drawable.ic_action_playlist)
        }

        binding.tvInPlaylistTitle.setOnClickListener(this)

        binding.ivInPlaylistBack.setOnClickListener(this)

        binding.addPlaylistFab.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.tv_in_playlist_title -> {
                RenamePlaylistDialog(this@InPlaylistFragment, selectedPlaylist.title).show(parentFragmentManager, "RENAME PLAYLIST DIALOG")
            }
            R.id.iv_in_playlist_back -> {
                mListener?.onBackBtnPressed()
            }
            R.id.add_playlist_fab -> {
                mListener?.onSongAddFab()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setUpRecycler()
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
        mListener?.onSongSelected(s)
    }

    override fun onSongLongClick(position: Int) {
        performContextMenuClick(position)
    }


    private  fun performContextMenuClick(position: Int) {
        val popupMenu = PopupMenu(
            requireContext(),
            binding.rvInPlaySongs[position].findViewById(R.id.tv_song_title)
        )

        popupMenu.inflate(R.menu.song_context_menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_remove_from_playlist -> {
                    removeSongFromPlaylist(position)
                    true
                }

                R.id.track_info -> {
                    // TODO: Show track info
                    Toast.makeText(requireContext(), "Show track info", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
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
        fun onSongSelected(song: Song)
        fun onBackBtnPressed()
        fun onSongAddFab()
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