package es.vmy.musicapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import es.vmy.musicapp.activities.MainActivity
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentSongsBinding
import es.vmy.musicapp.dialogs.TrackInfoDialog
import es.vmy.musicapp.utils.LISTENER_EX_MSG

class SongsFragment : Fragment(), SongsAdapter.SongViewHolder.SongsAdapterListener {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: SongsFragmentListener? = null

    // Retrieves the list of songs from the MainActivity
    private lateinit var mainActivity: MainActivity
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

        mainActivity = activity as MainActivity
        songs = mainActivity.getSongs()

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
        mAdapter = SongsAdapter(songs, requireContext(), this@SongsFragment)
        binding.rvSongs.adapter = mAdapter

        binding.rvSongs.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onSongClick(s: Song) {
        mListener?.onSongSelected(s)
    }

    override fun onSongLongClick(position: Int) {
        // Shows a dialog with the track's info
        TrackInfoDialog(songs.get(position)).show(parentFragmentManager, "TRACK INFO DIALOG")
    }

    interface SongsFragmentListener {
        fun onSongSelected(song: Song)
    }
}