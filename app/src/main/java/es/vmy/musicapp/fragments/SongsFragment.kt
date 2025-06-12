package es.vmy.musicapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentSongsBinding
import es.vmy.musicapp.dialogs.TrackInfoDialog
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsFragment : Fragment(), SongsAdapter.SongViewHolder.SongsAdapterListener {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: SongsFragmentListener? = null

    private val db by lazy { AppDB.getInstance(requireContext()) }

    private lateinit var songs: MutableList<Song>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is SongsFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG SongsFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            binding.musicProgressBar.visibility = View.VISIBLE
            songs = db.SongDAO().getAll()

            withContext(Dispatchers.Main) {
                binding.musicProgressBar.visibility = View.GONE

                if (songs.isEmpty()) {
                    binding.rvSongs.visibility = View.GONE
                    binding.tvNoSongsFound.visibility = View.VISIBLE
                }
                setUpRecycler()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun setUpRecycler() {
        mAdapter = SongsAdapter(songs, requireContext(), this@SongsFragment)
        binding.rvSongs.adapter = mAdapter

        binding.rvSongs.layoutManager = LinearLayoutManager(
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

    interface SongsFragmentListener {
        fun onSongSelected(song: Song, songList: MutableList<Song>)
        fun onFavoriteSong(favoriteBtn: ImageView, song: Song)
    }

    private  fun performContextMenuClick(position: Int, song: Song) {
        val viewHolder = binding.rvSongs.findViewHolderForAdapterPosition(position)
        if (viewHolder != null) {
            val popupMenu = PopupMenu(
                requireContext(),
                viewHolder.itemView.findViewById(R.id.tv_song_title)
            )

            popupMenu.inflate(R.menu.song_context_menu)

            popupMenu.menu[0].isVisible = false

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_remove_from_playlist -> {
                        // Not used
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
}