package es.vmy.musicapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.ActivityLoginBinding
import es.vmy.musicapp.fragments.LoginFragment
import es.vmy.musicapp.fragments.PassRecoveryFragment
import es.vmy.musicapp.fragments.RegisterFragment
import es.vmy.musicapp.utils.AuthManager

class LoginActivity : AppCompatActivity(),
    LoginFragment.LoginFragmentListener,
    RegisterFragment.RegisterFragmentListener,
    PassRecoveryFragment.PassRecoveryFragmentListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = AuthManager().getCurrentUser()
        // If the user is already logged in jumps straight to MainActivity
        if (user != null) {
            onLogin()
        }
    }

    // Login Fragment
    override fun onLogin() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    override fun toRegister() {
        supportFragmentManager.commit {
            replace<RegisterFragment>(R.id.loginContView)
            setReorderingAllowed(true)
        }
    }

    override fun toPassRecovery() {
        supportFragmentManager.commit {
            replace<PassRecoveryFragment>(R.id.loginContView)
            setReorderingAllowed(true)
        }
    }
    //

    // Register Fragment
    override fun onRegistered() {
        onLogin()
    }

    override fun backToLoginRegister() {
        toLoginFragment()
    }
    //

    // PassRecovery Fragment
    override fun onPassRecoveryMailSent() {
        toLoginFragment()
    }

    override fun backToLoginRecovery() {
        toLoginFragment()
    }
    //

    // Prevents code duplication
    private fun toLoginFragment() {
        supportFragmentManager.commit {
            replace<LoginFragment>(R.id.loginContView)
            setReorderingAllowed(true)
        }
    }
}