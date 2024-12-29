package es.vmy.musicapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.databinding.ActivitySplashBinding
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


//        Timer().schedule(3000){
//            val prefs = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
//            val autoLogin = prefs.getBoolean("remember_login", false)

//            if (autoLogin) {
//                val intent = Intent(this@SplashActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
    }
    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            showMainActivity()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                PERMISSION_REQUEST_CODE
            )
            Toast.makeText(this, "Request permission!", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMainActivity()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                Timer().schedule(1000) {
                    finish()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showMainActivity() {
        binding.progressBar.visibility = View.GONE
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        finish()
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 10002
    }
}