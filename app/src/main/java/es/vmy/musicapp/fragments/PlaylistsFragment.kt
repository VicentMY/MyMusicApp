package es.vmy.musicapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.adapters.PlaylistsAdapter
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Playlist
import es.vmy.musicapp.databinding.FragmentPlaylistsBinding
import es.vmy.musicapp.dialogs.CreatePlaylistDialog
import es.vmy.musicapp.dialogs.DeletePlaylistDialog
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistsFragment : Fragment(),
    PlaylistsAdapter.PlaylistViewHolder.PlaylistsAdapterListener,
    CreatePlaylistDialog.CreatePlaylistDialogListener,
    DeletePlaylistDialog.DeletePlaylistDialogListener {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: PlaylistsAdapter
    private var mListener: PlaylistsFragmentListener? = null

    private val db by lazy { AppDB.getInstance(requireContext()) }

    private var playlists = mutableListOf<Playlist>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PlaylistsFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG PlaylistsFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)

        binding.addPlaylistFab.setOnClickListener {
            CreatePlaylistDialog(this@PlaylistsFragment).show(parentFragmentManager, "CREATE PLAYLIST DIALOG")
        }

        setUpRecycler()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadPlaylistsFromDB()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun setUpRecycler() {
        mAdapter = PlaylistsAdapter(playlists, requireContext(), this@PlaylistsFragment)
        binding.rvPlaylists.adapter = mAdapter

        binding.rvPlaylists.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPlaylistsFromDB() {
        playlists.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            playlists.addAll(db.PlaylistDAO().getAll())

            withContext(Dispatchers.Main) {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onPlaylistClick(p: Playlist) {
        mListener?.onPlaylistSelected(p)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPlaylistLongClick(p: Playlist) {
        DeletePlaylistDialog(p, this@PlaylistsFragment).show(parentFragmentManager, "DELETE PLAYLIST DIALOG")
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreatePlaylist(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist = Playlist(name, null)
            db.PlaylistDAO().insert(playlist)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), getString(R.string.playlist_created), Toast.LENGTH_SHORT).show()
                playlists.add(playlist)
                loadPlaylistsFromDB()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPlaylistRemove(playlist: Playlist) {

        lifecycleScope.launch(Dispatchers.IO) {
            val result = db.PlaylistDAO().delete(playlist)

            withContext(Dispatchers.Main) {
                if (result == 1) {
                    Toast.makeText(requireContext(), getString(R.string.playlist_deleted_correctly), Toast.LENGTH_SHORT).show()
                    playlists.remove(playlist)
                    mAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.err_removing_playlist), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    interface PlaylistsFragmentListener {
        fun onPlaylistSelected(p: Playlist)
    }
}