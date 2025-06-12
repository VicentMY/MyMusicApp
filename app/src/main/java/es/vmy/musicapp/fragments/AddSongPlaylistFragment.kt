package es.vmy.musicapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentAddSongPlaylistBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddSongPlaylistFragment : Fragment(),
    OnClickListener,
    SongsAdapter.SongViewHolder.SongsAdapterListener {

    private var _binding: FragmentAddSongPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: AddSongPlaylistFragmentListener? = null

    private val db by lazy { AppDB.getInstance(requireContext()) }
    private lateinit var songs: MutableList<Song>

    private var songSelection = mutableListOf<Song>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is AddSongPlaylistFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG AddSongPlaylistFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSongPlaylistBinding.inflate(inflater, container, false)

        binding.btnAcceptSelection.setOnClickListener(this)
        binding.btnCancelSelection.setOnClickListener(this)

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
                    binding.rvSongsSelection.visibility = View.GONE
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
        mAdapter = SongsAdapter(songs, requireContext(), this@AddSongPlaylistFragment)
        binding.rvSongsSelection.adapter = mAdapter

        binding.rvSongsSelection.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btn_accept_selection -> {
                mListener?.onAccept(songSelection)
            }
            R.id.btn_cancel_selection -> { mListener?.onCancel() }
        }

        songSelection.forEach { song ->
            song.isSelected = false
        }
    }

    interface AddSongPlaylistFragmentListener {
        fun onAccept(songSelection: MutableList<Song>)
        fun onCancel()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSongClick(s: Song) {
        s.isSelected = !s.isSelected

        if (s.isSelected) {
            songSelection.add(s)
        } else {
            songSelection.remove(s)
        }

        mAdapter.notifyDataSetChanged()
    }

    override fun onSongLongClick(position: Int, song: Song) {
        // Not used
    }

    override fun onFavoriteSong(favoriteBtn: ImageView, song: Song) {
        // Not used
    }


}