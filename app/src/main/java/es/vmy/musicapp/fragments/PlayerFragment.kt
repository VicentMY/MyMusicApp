package es.vmy.musicapp.fragments

import android.content.Context
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.vmy.musicapp.R
import es.vmy.musicapp.activities.MainActivity
import es.vmy.musicapp.adapters.SongsAdapter
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.FragmentPlayerBinding
import es.vmy.musicapp.dialogs.TrackInfoDialog
import es.vmy.musicapp.utils.LISTENER_EX_MSG

class PlayerFragment : Fragment(), View.OnClickListener, SongsAdapter.SongViewHolder.SongsAdapterListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SongsAdapter
    private var mListener: PlayerFragmentListener? = null

//    private val db by lazy { AppDB.getInstance(requireContext()) }

    private lateinit var mainActivity: MainActivity
    private lateinit var music: MediaPlayer
    private var currentSong: Song? = null
    private lateinit var playingSongList: MutableList<Song>

    private var songListShown: Boolean = false

    // FIXME: Retrieve these from MediaPlayer
    private var shuffleOn: Boolean = false
    private var repeatState: Int = 0

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

        binding.btnShowPlaylist.setOnClickListener(this)
        binding.btnGetInfo.setOnClickListener(this)
        binding.btnFavorite.setOnClickListener(this)
        binding.btnShuffle.setOnClickListener(this)
        binding.btnRepeat.setOnClickListener(this)

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

        playingSongList = mainActivity.getSongs()
        currentSong = mainActivity.getCurrentSong()

        updateFragment()

        if (music.isPlaying) {
            binding.btnPlay.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.ic_action_pause))
        } else {
            binding.btnPlay.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.ic_action_play))
        }

        if (currentSong != null && currentSong!!.favorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_action_favorite_on)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_action_favorite)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        playingSongList.forEach {
            it.isSelected = false
        }
    }

    interface PlayerFragmentListener {
        fun playPause(fab: FloatingActionButton)
        fun skipPrevNext(forward: Boolean, fab: FloatingActionButton? = null)
        fun onSeekBarChange(progress: Int)
        fun onFavoriteSong(favoriteBtn: ImageView, song: Song)
        fun onSongSelected(song: Song, songList: MutableList<Song>)
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
            R.id.btn_show_playlist -> {
                showHidePlaylist()
            }
            R.id.btn_get_info -> {
                if (currentSong != null) {
                    // Shows a dialog with the track's info
                    TrackInfoDialog(currentSong!!).show(parentFragmentManager, "TRACK INFO DIALOG")
                }
            }
            R.id.btn_favorite -> {
                if (currentSong != null) {
                    mListener?.onFavoriteSong(binding.btnFavorite, currentSong!!)
                }
            }
            R.id.btn_shuffle -> {
                // TODO: SHUFFLE FUNCTION
                val shuffleBtn = binding.btnShuffle
                shuffleOn = !shuffleOn

                if (shuffleOn) {
                    shuffleBtn.setImageResource(R.drawable.ic_action_shuffle_on)
                } else {
                    shuffleBtn.setImageResource(R.drawable.ic_action_shuffle)
                }
            }
            R.id.btn_repeat -> {
                // TODO: REPEAT FUNCTION
                //  repeatState:
                //  0 -> off
                //  1 -> repeat playlist
                //  2 -> repeat song
                val repeatBtn = binding.btnRepeat

                when (repeatState) {
                    0 -> {
                        repeatState = 1
                        repeatBtn.setImageResource(R.drawable.ic_action_repeat_on)
                    }
                    1 -> {
                        repeatState = 2
                        repeatBtn.setImageResource(R.drawable.ic_action_repeat_one_on)
                    }
                    2 -> {
                        repeatState = 0
                        repeatBtn.setImageResource(R.drawable.ic_action_repeat)
                    }
                }
            }
        }
        updateFragment()
    }

    fun updateFragment() {
        val song = mainActivity.getCurrentSong()

        if (song != null) {
            binding.tvSongTitle.text = song.title
            binding.tvSongArtist.text = song.artist

            if (song.thumbnail != null) {
                binding.ivSongThumbnail.setImageBitmap(song.thumbnail)
            } else {
                binding.ivSongThumbnail.setImageResource(R.drawable.ic_action_song)
            }

            if (song.favorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_action_favorite_on)
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_action_favorite)
            }
        }
    }

    private fun showHidePlaylist() {
        if (currentSong != null) {
            if (songListShown) {
                binding.ivSongThumbnail.visibility = View.VISIBLE
                binding.rvSongsPlayer.visibility = View.GONE
                songListShown = false

            } else {
                binding.ivSongThumbnail.visibility = View.INVISIBLE
                binding.rvSongsPlayer.visibility = View.VISIBLE
                setUpRecycler()
                songListShown = true
            }
        }
    }

    private fun setUpRecycler() {
        mAdapter = SongsAdapter(playingSongList, requireContext(), this@PlayerFragment)
        binding.rvSongsPlayer.adapter = mAdapter

        binding.rvSongsPlayer.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onSongClick(s: Song) {
        mListener?.onSongSelected(s, playingSongList)
    }

    override fun onSongLongClick(position: Int) {
        // Not used
    }

    override fun onFavoriteSong(favoriteBtn: ImageView, song: Song) {
        mListener?.onFavoriteSong(favoriteBtn, song)
        if (::mAdapter.isInitialized) {
            mAdapter.notifyDataSetChanged()
        }
        updateFragment()
    }
}