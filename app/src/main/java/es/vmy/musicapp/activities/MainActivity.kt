package es.vmy.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
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
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.ActivityMainBinding
import es.vmy.musicapp.dialogs.BackConfirmDialog
import es.vmy.musicapp.fragments.ChatFragment
import es.vmy.musicapp.fragments.PlayerFragment
import es.vmy.musicapp.fragments.PlaylistsFragment
import es.vmy.musicapp.fragments.SettingsFragment
import es.vmy.musicapp.fragments.SongsFragment
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.LAST_SONG_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    PlayerFragment.PlayerFragmentListener,
    SongsFragment.SongsFragmentListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var music: MediaPlayer
    private var songs: MutableList<Song> = mutableListOf()
    private lateinit var currentSong: Song

    // Runnable that updates the SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {

        override fun run() {
            // Checks if the MediaPlayer is initialized and playing
            if (::music.isInitialized && music.isPlaying) {
                val currentTimePosition = music.currentPosition
                val duration = music.duration

                val progress = (music.currentPosition * 100) / duration
                val currentTime = formatTime(currentTimePosition)
                val totalTime = formatTime(duration)

                // Updates the SeekBar and the TextViews in the PlayerFragment
                val playerFragment = supportFragmentManager.findFragmentById(R.id.mainContView) as PlayerFragment
                playerFragment.updateSeekBarAndTimers(progress, currentTime, totalTime)

                // Checks if the song has finished playing and skips to the next song
                // Sometimes the MediaPlayer doesn't reach the end of the song, so it's set to -1000
                if (currentTimePosition >= duration -1000) {
                    skipPrevNext(true, null)
                    playerFragment.updateFragment()
                }

                // Updates the SeekBar every second
                handler.postDelayed(this, 1000)
            }
        }
    }
    //

    // Gives access to player variables from other fragments
    fun getSongs(): MutableList<Song> {
        return songs
    }

    fun getCurrentSong(): Song {
        return currentSong
    }

    fun getMusic(): MediaPlayer {
        return music
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

            val db = AppDB.getInstance(this@MainActivity).SongDAO()
            // Loads the list of songs
            songs = db.getAll()

            // Loads the last played song when the app was closed
            val prefs = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
            val lastSongId = prefs.getLong(LAST_SONG_KEY, 1)

            // Sets the last played song as the current song
            songs.forEach {
                if (it.id == lastSongId) {
                    currentSong = it
                }
            }

            withContext(Dispatchers.Main) {
                binding.musicProgressBar.visibility = View.GONE
                supportFragmentManager.commit {
                    replace<PlayerFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                }
                music = MediaPlayer.create(this@MainActivity, Uri.parse(currentSong.path))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stores the last played song in the SharedPreferences before closing the MainActivity
        val prefs = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
        with(prefs.edit()) {
            putLong(LAST_SONG_KEY, currentSong.id)
            apply()
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
            R.id.nav_chat -> {
                supportFragmentManager.commit {
                    replace<ChatFragment>(R.id.mainContView)
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
            R.id.nav_logout -> {
                changeMenuSelection(item)
                // Closes the Firebase session
                AuthManager().logOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
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
        val currentPosition = songs.indexOf(currentSong)
        var err = false

        // Checks if the current song is the first or last song in the list
        if (currentPosition == 0 && !forward) {
            err = true
        }
        if (currentPosition == songs.size -1 && forward) {
            err = true
        }

        // Shows a Snackbar if the current song is the first or last song in the list
        if (err) {
            val msg = if (forward) {
                getString(R.string.already_last_msg)
            } else {
                getString(R.string.already_first_msg)
            }
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        } else {
            // Set the next or previous song as the current song depending on the 'forward' boolean
            currentSong = if (forward) {
                songs[currentPosition +1]
            } else {
                songs[currentPosition -1]
            }

            // If the MediaPlayer already exists, stops it and releases it
            if (::music.isInitialized && music.isPlaying) {
                music.stop()
                handler.removeCallbacks(updateSeekBarRunnable)
                music.release()
            }
            // Creates a new MediaPlayer with the selected song and starts playing it
            music = MediaPlayer.create(this, Uri.parse(currentSong.path))
            music.start()
            handler.post(updateSeekBarRunnable)

            // Sets the pause button to the Play/Pause button
            fab?.setImageResource(R.drawable.ic_action_pause)
        }
    }

    override fun onSeekBarChange(progress: Int) {
        // Calculates the position in the song to skip to and skips to it
        val seekToPosition = (music.duration * progress) / 100
        music.seekTo(seekToPosition)
    }
    //

    // Songs
    override fun onSongSelected(song: Song) {
        // Sets the selected song as the current song
        currentSong = song
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

    private fun formatTime(mSec: Int): String {
        // Calculates the minutes and seconds of the song
        val min = (mSec / 1000) / 60
        val sec = (mSec / 1000) % 60
        return String.format("%02d:%02d", min, sec)
    }
}