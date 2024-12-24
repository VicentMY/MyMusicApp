package es.vmy.musicapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.ActivityLoginBinding
import es.vmy.musicapp.fragments.LoginFragment
import es.vmy.musicapp.fragments.RegisterFragment
import es.vmy.musicapp.utils.LOG_TAG

class LoginActivity : AppCompatActivity(), LoginFragment.LoginFragmentListener, RegisterFragment.RegisterFragmentListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun btnLogin() {
        //TODO: Implementar Login en la app
        Log.d(LOG_TAG, "LOGIN")

        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    override fun btnToRegister() {
        supportFragmentManager.commit {
            replace<RegisterFragment>(R.id.loginContView)
            setReorderingAllowed(true)
        }
    }

    override fun btnRegister() {
        //TODO: Implementar Registro en la app
        Log.d(LOG_TAG, "REGISTER")
    }

    override fun btnBackToLogin() {
        supportFragmentManager.commit {
            replace<LoginFragment>(R.id.loginContView)
            setReorderingAllowed(true)
        }
    }
}