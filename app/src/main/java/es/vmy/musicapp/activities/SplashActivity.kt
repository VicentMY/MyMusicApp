package es.vmy.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import es.vmy.musicapp.R
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

        Timer().schedule(3000){
//            binding.progressBar.visibility = View.GONE
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()

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
        }
    }
}