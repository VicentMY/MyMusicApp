package es.vmy.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.ActivityMainBinding
import es.vmy.musicapp.databinding.FragmentPlayerBinding
import es.vmy.musicapp.dialogs.BackConfirmDialog
import es.vmy.musicapp.fragments.ChatFragment
import es.vmy.musicapp.fragments.PlayerFragment
import es.vmy.musicapp.fragments.PlaylistsFragment
import es.vmy.musicapp.fragments.SettingsFragment
import es.vmy.musicapp.fragments.SongsFragment
import es.vmy.musicapp.utils.LOG_TAG

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, PlayerFragment.PlayerFragmentListener {

    private lateinit var binding: ActivityMainBinding

    // Player
    private lateinit var music: MediaPlayer
    //

    private lateinit var songs: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        setUpNavigationDrawer()

        songs = getSongList()

        music = MediaPlayer.create(this, R.raw.cereal_killa)

        setUpExitDialog()
    }

    // OnBackPressed
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
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)

        // Selects song list as default
        val menuItem: MenuItem = binding.navigationView.menu.getItem(1)
        menuItem.isChecked = true
    }

    private fun changeMenuSelection(item: MenuItem) {
        val menu = binding.navigationView.menu
        val subMenu = menu.getItem(3).subMenu!!

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

        return when (item.itemId) {
            R.id.nav_player -> {
                supportFragmentManager.commit {
                    replace<PlayerFragment>(R.id.mainContView)
                    setReorderingAllowed(true)
                    changeMenuSelection(item)
                }
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
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> true
        }
    }
    //

    override fun playPause(fab: FloatingActionButton) {
        if (music.isPlaying) {
            fab.setImageResource(R.drawable.ic_action_play)
            music.pause()
        } else {
            fab.setImageResource(R.drawable.ic_action_pause)
            music.start()
        }
    }

    override fun skipPrevious() {
        // TODO: "Not yet implemented"
    }

    override fun skipNext() {
        // TODO: "Not yet implemented"
    }

    private fun getSongList(): MutableList<Song> {
        val songList: MutableList<Song> = mutableListOf()
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        var song: Song

        this.contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                val path = dataColumn.let { cursor.getString(it) }
                val duration = durationColumn.let { cursor.getLong(it) }

                song = Song(path.toString(), duration, path.toString())
                songList.add(song)
                Log.d(LOG_TAG, path)
            }
        }
        return songList
    }
}