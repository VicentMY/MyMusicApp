package es.vmy.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Playlist
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.ActivityMainBinding
import es.vmy.musicapp.dialogs.BackConfirmDialog
import es.vmy.musicapp.fragments.AddSongPlaylistFragment
import es.vmy.musicapp.fragments.ChatFragment
import es.vmy.musicapp.fragments.InPlaylistFragment
import es.vmy.musicapp.fragments.PlayerFragment
import es.vmy.musicapp.fragments.PlaylistsFragment
import es.vmy.musicapp.fragments.SettingsFragment
import es.vmy.musicapp.fragments.SongsFragment
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.FAVORITE_SONGS_LIST_ID
import es.vmy.musicapp.utils.LAST_PLAYLIST_KEY
import es.vmy.musicapp.utils.LAST_SONG_KEY
import es.vmy.musicapp.utils.LOG_TAG
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.formatTime
import es.vmy.musicapp.utils.idToSongList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    PlayerFragment.PlayerFragmentListener,
    SongsFragment.SongsFragmentListener,
    PlaylistsFragment.PlaylistsFragmentListener,
    InPlaylistFragment.InPlaylistFragmentListener,
    AddSongPlaylistFragment.AddSongPlaylistFragmentListener {

    private lateinit var binding: ActivityMainBinding

    // Player variables
    private lateinit var music: MediaPlayer
    private var shuffleOn: Boolean = false
    private var repeatState: Int = 0
    //

    private var songs: MutableList<Song> = mutableListOf()
    private lateinit var originalSongs : MutableList<Song>
    private var currentSong: Song? = null
    private lateinit var selectedPlaylist: Playlist

    private val db by lazy { AppDB.getInstance(this@MainActivity) }
    private val prefs by lazy { getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE) }

    // Runnable that updates the SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            // Checks if the MediaPlayer is initialized and playing
            if (::music.isInitialized && music.isPlaying) {
                val currentTimePosition = music.currentPosition.toLong()
                val duration = music.duration.toLong()

                val progress = ((music.currentPosition * 100) / duration).toInt()
                val currentTime = formatTime(currentTimePosition)
                val totalTime = formatTime(duration)

                // Updates the SeekBar and the TextViews in the PlayerFragment
                val playerFragment = supportFragmentManager.findFragmentById(R.id.mainContView) as PlayerFragment
                playerFragment.updateSeekBarAndTimers(progress, currentTime, totalTime)

                // Checks if the song has finished playing and skips to the next song
                // Sometimes the MediaPlayer doesn't reach the end of the song, so it's set to -1000
                if (currentTimePosition >= duration -1000) {
                    when (repeatState) {
                        0 -> skipPrevNext(true, null) // No repeat
                        1 -> { //repeat playlist
                            val currentPosition = songs.indexOf(currentSong)
                            currentSong = if (currentPosition == songs.size -1) {
                                songs[0]
                            } else {
                                songs[currentPosition +1]
                            }
                            playCurrentSong()
                        }
                        2 -> {// repeat current song
                            music.seekTo(0)
                            music.start()
                        }

                    }
                    playerFragment.updateFragment()
                }
                // Updates the SeekBar every second
                handler.postDelayed(this, 1000)
            }
        }
    }
    //

    // Helper method to play the current song
    private fun playCurrentSong() {
        if (::music.isInitialized && music.isPlaying) {
            music.stop()
            handler.removeCallbacks(updateSeekBarRunnable)
        }
        if (currentSong != null) {
            music = MediaPlayer.create(this@MainActivity, Uri.parse(currentSong!!.path))
            music.start()
            handler.post(updateSeekBarRunnable)
        } else {
            music = MediaPlayer()
        }
    }

    // Gives access to player variables from other fragments
    fun getSongs(): MutableList<Song> {
        return songs
    }

    fun getCurrentSong(): Song? {
        return currentSong
    }

    fun getSelectedPlaylist(): Playlist {
        return selectedPlaylist
    }

    fun getMusic(): MediaPlayer {
        return music
    }

    fun getShuffleState(): Boolean {
        return shuffleOn
    }

    fun getRepeatState(): Int {
        return repeatState
    }
    fun setRepeatState(state: Int) {
        repeatState = state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)
        setUpNavigationDrawer()
        setUpExitDialog()

        binding.musicProgressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val songsDB = db.SongDAO().getAll()

            // Loads the last played song...
            val lastSongId = prefs.getLong(LAST_SONG_KEY, 1)
            // Loads the last selected playlist...
            val lastPlaylistId = prefs.getLong(LAST_PLAYLIST_KEY, -1L)
            if (lastPlaylistId != -1L) {
                selectedPlaylist = db.PlaylistDAO().findById(lastPlaylistId)!!
                songs = idToSongList(selectedPlaylist.songs, songsDB)

            } else {
                songs = songsDB
            }

            //  ...when the app was closed

            // Loads the list of songs

            originalSongs = songs.toMutableList()

            // Sets the last played song as the current song
            songs.forEach {
                if (!File(it.path).exists()) {
                    Log.w(LOG_TAG, "Invalid path for song: ${it.title}")
                    db.SongDAO().delete(it)

                } else if (it.id == lastSongId) {
                    currentSong = it
                }
            }

            withContext(Dispatchers.Main) {
                music = if (currentSong != null) {
                    MediaPlayer.create(this@MainActivity, Uri.parse(currentSong!!.path))
                } else {
                    MediaPlayer()
                }

                binding.musicProgressBar.visibility = View.GONE

                supportFragmentManager.commit {
                    replace<PlayerFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (currentSong != null) {
            // Stores the last played song in the SharedPreferences before closing the MainActivity
            with(prefs.edit()) {
                putLong(LAST_SONG_KEY, currentSong!!.id)
                apply()
            }
        }

        // Stops the Runnable that updates the SeekBar, stops the MediaPlayer and releases it before closing the MainActivity
        handler.removeCallbacks(updateSeekBarRunnable)
        music.stop()
        music.release()
    }

    // OnBackPressed
    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setUpExitDialog() {
        onBackPressedDispatcher.addCallback(this, object:
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                BackConfirmDialog().show(supportFragmentManager,
                    "CONFIRM DIALOG")
            }
        })
    }
    //

    // Navigation
    private fun setUpNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        // Synchronizes the state of the DrawerLayout with the state of the ActionBarDrawerToggle
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)

        // Selects player as default
        val menuItem: MenuItem = binding.navigationView.menu.getItem(0)
        menuItem.isChecked = true
    }

    private fun changeMenuSelection(item: MenuItem) {
        val menu = binding.navigationView.menu
        val subMenu = menu.getItem(3).subMenu!!

        // Unchecks all items in the menu and submenu and checks the selected item
        menu.forEach {
            it.isChecked = false
        }
        subMenu.forEach {
            it.isChecked = false
        }
        item.isChecked = true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)

        // Stops the Runnable that updates the SeekBar when changing fragments
        handler.removeCallbacks(updateSeekBarRunnable)

        return when (item.itemId) {
            R.id.nav_player -> {
                supportFragmentManager.commit {
                    replace<PlayerFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                    changeMenuSelection(item)
                }
                // Starts the Runnable that updates the SeekBar
                handler.post(updateSeekBarRunnable)
                true
            }
            R.id.nav_songs -> {
                supportFragmentManager.commit {
                    replace<SongsFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                    changeMenuSelection(item)
                }
                true
            }
            R.id.nav_playlists -> {
                supportFragmentManager.commit {
                    replace<PlaylistsFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                    changeMenuSelection(item)
                }
                true
            }
            R.id.nav_settings -> {
                supportFragmentManager.commit {
                    replace<SettingsFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                    changeMenuSelection(item)
                }
                true
            }
            R.id.nav_chat -> {
                val user = AuthManager().getCurrentUser()
                // If the user is not logged in, then goes to LoginActivity first
                if (user == null) {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                } else {
                    // Then opens the ChatFragment
                    supportFragmentManager.commit {
                        replace<ChatFragment>(R.id.mainContView)
                        setReorderingAllowed(true)
                        changeMenuSelection(item)
                    }
                }
                true
            }
            else -> true
        }
    }
    //

    // Player
    override fun playPause(fab: FloatingActionButton) {
        if (music.isPlaying) {
            fab.setImageResource(R.drawable.ic_action_play)
            music.pause()
            // Stops the Runnable that updates the SeekBar
            handler.removeCallbacks(updateSeekBarRunnable)
        } else {
            fab.setImageResource(R.drawable.ic_action_pause)
            music.start()
            // Starts the Runnable that updates the SeekBar
            handler.post(updateSeekBarRunnable)
        }
    }

    override fun skipPrevNext(forward: Boolean, fab: FloatingActionButton?) {
        var lastSongEndNoRepeat = false
        // If there are no songs in the device
        if (songs.isEmpty()) {
            // Depending if we're skipping forward or backward
            val msg = if (forward) {
                getString(R.string.already_last_msg)
            } else {
                getString(R.string.already_first_msg)
            }
            // Alert the user that there is no next / previous track
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()

        // If there are songs
        } else {
            val currentPosition = songs.indexOf(currentSong)

            // If we're skipping backward and passed the first 5 seconds of the song
            if (!forward && music.currentPosition.toLong() > 5000L) {
                // Play the current song from the beginning
                music.seekTo(0)
                music.start()

            } else {
                // If we're on the first song and skipping backward
                currentSong = if (currentPosition == 0 && !forward) {
                    songs[songs.size -1] // currentSong is the last song in the list

                // If we're on the last song and skipping forward
                } else if (currentPosition == songs.size -1 && forward) {
                    if (fab == null && repeatState == 0) {
                        lastSongEndNoRepeat = true
                        music.stop()
                        handler.removeCallbacks(updateSeekBarRunnable)
                        currentSong
                    } else {
                        songs[0] // currentSong is the first song in the list
                    }
                // If not just skip
                } else {
                    if (forward) {
                        songs[currentPosition +1]
                    } else {
                        songs[currentPosition -1]
                    }
                }
                // If the MediaPlayer already exists, stops it and releases it
                if (::music.isInitialized && music.isPlaying) {
                    music.stop()
                    handler.removeCallbacks(updateSeekBarRunnable)
                    music.release()
                }

                // Creates a new MediaPlayer with the selected song and starts playing it
                music = MediaPlayer.create(this@MainActivity, Uri.parse(currentSong!!.path))
                music.seekTo(0)
                val playerFragment = supportFragmentManager.findFragmentById(R.id.mainContView) as PlayerFragment
                playerFragment.updateFragment()

                if (lastSongEndNoRepeat) {
                    // Sets the play icon to the Play/Pause button
                    fab?.setImageResource(R.drawable.ic_action_play)
                } else {
                    music.start()
                    handler.post(updateSeekBarRunnable)
                    // Sets the pause icon to the Play/Pause button
                    fab?.setImageResource(R.drawable.ic_action_pause)
                }
            }
        }
    }

    override fun onSeekBarChange(progress: Int) {
        // Calculates the position in the song to skip to and skips to it
        val seekToPosition = (music.duration * progress) / 100
        music.seekTo(seekToPosition)
    }

    override fun onFavoriteSong(favoriteBtn: ImageView, song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            val favoritePlaylist = db.PlaylistDAO().findById(FAVORITE_SONGS_LIST_ID)!!

            song.favorite = !song.favorite
            db.SongDAO().update(song)

            if (song.favorite) {
                favoritePlaylist.songs.add(song.id)
            } else {
                favoritePlaylist.songs.remove(song.id)
            }

            db.PlaylistDAO().update(favoritePlaylist)

            withContext(Dispatchers.Main) {
                if (song.favorite) {
                    favoriteBtn.setImageResource(R.drawable.ic_action_favorite_on)
                } else {
                    favoriteBtn.setImageResource(R.drawable.ic_action_favorite)
                }
            }
        }
    }

    override fun onShuffle() {
        shuffleOn = !shuffleOn
        if (shuffleOn) {
            originalSongs = songs.toMutableList()
            songs.shuffle()
        } else {
            songs = originalSongs.toMutableList()
        }
    }
    //

    // Songs
    override fun onSongSelected(song: Song, songList: MutableList<Song>) {
        // Sets the selected song as the current song
        currentSong = song
        songs = songList
        // Stops the previous song and releases the MediaPlayer
        music.stop()
        music.release()
        // Creates a new MediaPlayer with the selected song and starts playing it
        music = MediaPlayer.create(this, Uri.parse(song.path))
        music.start()

        // Changes to the PlayerFragment
        supportFragmentManager.commit {
            replace<PlayerFragment>(R.id.mainContView)
            setReorderingAllowed(true)
            changeMenuSelection(binding.navigationView.menu.getItem(0))
        }
        // Starts the Runnable that updates the SeekBar
        handler.post(updateSeekBarRunnable)
    }
    //

    // Playlists
    override fun onPlaylistSelected(p: Playlist) {

        lifecycleScope.launch(Dispatchers.IO) {

            selectedPlaylist = db.PlaylistDAO().findById(p.id)!!
            with(prefs.edit()) {
                putLong(LAST_PLAYLIST_KEY, p.id)
                apply()
            }

            withContext(Dispatchers.Main) {
                supportFragmentManager.commit {
                    replace<InPlaylistFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                }
            }
        }
    }
    //

    // In Playlist
    override fun onBackBtnPressed() {
        supportFragmentManager.commit {
            replace<PlaylistsFragment>(R.id.mainContView)
            setReorderingAllowed(true)
        }
    }

    override fun onSongAddFab() {
        supportFragmentManager.commit {
            replace<AddSongPlaylistFragment>(R.id.mainContView)
            setReorderingAllowed(true)
        }
    }

    override fun onAccept(songSelection: MutableList<Song>) {
        lifecycleScope.launch(Dispatchers.IO) {

            selectedPlaylist.songs.addAll(songSelection.map { it.id }.toMutableList())

            val tmpList = idToSongList(selectedPlaylist.songs, songs)

            if (tmpList.isNotEmpty()) {
                selectedPlaylist.thumbnail = tmpList[0].thumbnail
            }

            db.PlaylistDAO().update(selectedPlaylist)

            withContext(Dispatchers.Main) {
                supportFragmentManager.commit {
                    replace<InPlaylistFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                }
            }
        }
    }

    override fun onCancel() {
        supportFragmentManager.commit {
            replace<InPlaylistFragment>(R.id.mainContView)
            setReorderingAllowed(true)
        }
    }
    //
}