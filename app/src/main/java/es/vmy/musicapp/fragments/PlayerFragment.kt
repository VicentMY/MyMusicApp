package es.vmy.musicapp.fragments

import android.content.Context
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.vmy.musicapp.R
import es.vmy.musicapp.activities.MainActivity
import es.vmy.musicapp.databinding.FragmentPlayerBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG

class PlayerFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var mListener: PlayerFragmentListener? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var music: MediaPlayer

    fun updateSeekBarAndTimers(progress: Int, currentTime: String, totalTime: String) {
        binding.seekBar.progress = progress
        binding.tvCurrentTime.text = currentTime
        binding.tvTotalTime.text = totalTime
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PlayerFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG PlayerFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)

        binding.btnPlay.setOnClickListener(this)
        binding.btnSkipPrev.setOnClickListener(this)
        binding.btnSkipNext.setOnClickListener(this)

        // SeekBar listener
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Obligatory to implement, not used
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Obligatory to implement, not used
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mListener?.onSeekBarChange(seekBar!!.progress)
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainActivity = activity as MainActivity
        music = mainActivity.getMusic()

        updateFragment()

        if (music.isPlaying) {
            binding.btnPlay.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.ic_action_pause))
        } else {
            binding.btnPlay.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.ic_action_play))
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface PlayerFragmentListener {
        fun playPause(fab: FloatingActionButton)
        fun skipPrevNext(forward: Boolean, fab: FloatingActionButton? = null)
        fun onSeekBarChange(progress: Int)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_play -> {
                mListener?.playPause(binding.btnPlay)
            }
            R.id.btn_skip_prev -> {
                mListener?.skipPrevNext(false, binding.btnPlay)
            }
            R.id.btn_skip_next -> {
                mListener?.skipPrevNext(true, binding.btnPlay)
            }
        }
        updateFragment()
    }

    fun updateFragment() {
        val song = mainActivity.getCurrentSong()

        binding.tvSongTitle.text = song.title
        binding.tvSongArtist.text = song.artist

        if (song.thumbnail != null) {
            binding.ivThumbnail.setImageBitmap(song.thumbnail)
        } else {
            binding.ivThumbnail.setImageResource(R.drawable.ic_action_song)
        }
    }

}