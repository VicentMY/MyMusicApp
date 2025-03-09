package es.vmy.musicapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.AppDB
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.concurrent.schedule

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE
        checkStoragePermission()
    }
    private fun checkStoragePermission() {
        val musicPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, musicPerm) == PackageManager.PERMISSION_GRANTED) {
            storeSongsInDB()
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(musicPerm, Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(musicPerm),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                // If more than one permission is requested, checks if the second one is granted
                (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                storeSongsInDB()
            } else {
                Toast.makeText(this, getString(R.string.perm_denied_msg), Toast.LENGTH_LONG).show()
                Timer().schedule(2000) {
                    finish()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showNextActivity() {
        binding.progressBar.visibility = View.GONE
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    private fun storeSongsInDB() {
        lifecycleScope.launch(Dispatchers.IO) {

            val db = AppDB.getInstance(this@SplashActivity).SongDAO()
            // Retrieves the songs stored in the database
            val songs = db.getAll()

            if (songs.isEmpty()) {
                // If the database is empty, retrieves the songs from the device and stores them
                db.insertMany( getSongsFromDevice() )
            }

            withContext(Dispatchers.Main) {
                showNextActivity()
            }
        }
    }

    private fun getSongsFromDevice(): MutableList<Song> {
        val songList: MutableList<Song> = mutableListOf()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // Gets the path to the root of the SD card
        var sdRoot = getExternalFilesDirs(null) // "/storage/SD_UUID"
            .getOrNull(1)?.absolutePath

        if (sdRoot != null) {
            sdRoot = sdRoot.split("/Android/data/es.vmy.musicapp/files") [0]
        }

        // Gets the path to the root of the internal storage
        val phoneRoot = "/storage/emulated/0"

        this.contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            // Gets all needed data from each song in the device
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            val placeHolderBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_action_song)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)

                // Ensures that the song is located in the Music or Download folder on the phone or SD card
                if (
                    path.startsWith("$phoneRoot/Music/") ||
                    path.startsWith("$phoneRoot/Download/") ||
                    // If the device has an SD card, checks if the song is located in the Music or Download folder
                    (sdRoot != null && (
                        path.startsWith("$sdRoot/Music/") ||
                        path.startsWith("$sdRoot/Download/")
                    ))
                ) {

                    val duration = cursor.getLong(durationColumn)
                    val title = cursor.getString(titleColumn)

                    val artist = if (cursor.getString(artistColumn) == "<unknown>") {
                        getString(R.string.unknown_artist)
                    } else {
                        cursor.getString(artistColumn)
                    }
                    val songArt = getAlbumArt(path)
                    // Creates a Bitmap of the song's thumbnail and stores it into 'thumbnail'
                    val thumbnail = if (songArt != null) {
                        BitmapFactory.decodeByteArray(songArt, 0, songArt.size)
                    } else {
                        placeHolderBitmap
                    }

                    // Creates a Song object with the retrieved data and adds it to the list
                    songList.add( Song(title, thumbnail, artist, duration, path) )
                }
            }
        }
        return songList
    }

    private fun getAlbumArt(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        // Retrieves the thumbnail of the song
        retriever.setDataSource(path)

        return retriever.embeddedPicture
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 10002
    }
}