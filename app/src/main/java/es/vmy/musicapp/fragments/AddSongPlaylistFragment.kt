package es.vmy.musicapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.R
import es.vmy.musicapp.activities.MainActivity
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentAddSongPlaylistBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import es.vmy.musicapp.utils.LOG_TAG

class AddSongPlaylistFragment : Fragment(),
    OnClickListener,
    SongsAdapter.SongViewHolder.SongsAdapterListener {

    private var _binding: FragmentAddSongPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: AddSongPlaylistFragmentListener? = null

    private lateinit var mainActivity: MainActivity
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

        mainActivity = activity as MainActivity
        songs = mainActivity.getSongs()

        binding.btnAcceptSelection.setOnClickListener(this)
        binding.btnCancelSelection.setOnClickListener(this)

        return binding.root
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

                songSelection.forEach {
                    Log.d(LOG_TAG, "${it.title}, ${it.thumbnail}")
                }

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

    override fun onSongLongClick(position: Int) {
        // Not used
    }
}