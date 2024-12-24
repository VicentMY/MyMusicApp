package es.vmy.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.navigation.NavigationView
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.ActivityMainBinding
import es.vmy.musicapp.dialogs.BackConfirmDialog
import es.vmy.musicapp.fragments.ChatFragment
import es.vmy.musicapp.fragments.PlayerFragment
import es.vmy.musicapp.fragments.PlaylistsFragment
import es.vmy.musicapp.fragments.SongsFragment
import es.vmy.musicapp.utils.LOG_TAG

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        setUpNavigationDrawer()
        setUpExitDialog()
    }

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

        //Select the first item
        val menuItem: MenuItem = binding.navigationView.menu.getItem(0)
        Log.d(LOG_TAG, menuItem.toString())
        onNavigationItemSelected(menuItem)
        menuItem.isChecked = true
    }

    private fun changeSubmenuSelection(item: MenuItem) {
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

        val submenuItems = binding.navigationView.menu.getItem(3).subMenu!!

        val chat = submenuItems.getItem(0)
        val settings = submenuItems.getItem(1)
        val logout = submenuItems.getItem(2)

        return when (item.itemId) {
            R.id.nav_player -> {
                supportFragmentManager.commit {
                    replace<PlayerFragment>(R.id.mainContView)
                    setReorderingAllowed(true)

                    chat.isChecked = false
                }
                true
            }
            R.id.nav_songs -> {
                supportFragmentManager.commit {
                    replace<SongsFragment>(R.id.mainContView)
                    setReorderingAllowed(true)

                    chat.isChecked = false
                }
                true
            }
            R.id.nav_playlists -> {
                supportFragmentManager.commit {
                    replace<PlaylistsFragment>(R.id.mainContView)
                    setReorderingAllowed(true)

                    chat.isChecked = false
                }
                true
            }
            R.id.nav_chat -> {
                supportFragmentManager.commit {
                    replace<ChatFragment>(R.id.mainContView)
                    setReorderingAllowed(true)

                    changeSubmenuSelection(chat)
                }
                true
            }
            R.id.nav_settings -> {
                changeSubmenuSelection(settings)
                true
            }
            R.id.nav_logout -> {
                changeSubmenuSelection(logout)
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> true
        }
    }
}