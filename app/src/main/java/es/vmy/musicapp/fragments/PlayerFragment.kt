package es.vmy.musicapp.fragments

import android.content.Context
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.FragmentPlayerBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG

class PlayerFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private var mListener: PlayerFragmentListener? = null

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

        return binding.root
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
        fun skipPrevious()
        fun skipNext()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_play -> {
                Toast.makeText(requireActivity(), "PLAY", Toast.LENGTH_SHORT).show()
                mListener?.playPause(binding.btnPlay)
            }
            R.id.btn_skip_prev -> {
                Toast.makeText(requireActivity(), "PREVIOUS", Toast.LENGTH_SHORT).show()
                mListener?.skipPrevious()
            }
            R.id.btn_skip_next -> {
                Toast.makeText(requireActivity(), "NEXT", Toast.LENGTH_SHORT).show()
                mListener?.skipNext()
            }
        }
    }
}