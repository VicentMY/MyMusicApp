package es.vmy.musicapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.FragmentLoginBinding
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var mListener: LoginFragmentListener? = null

    private val authManager: AuthManager by lazy { AuthManager() }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is LoginFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG LoginFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener(this)
        binding.btnToRegister.setOnClickListener(this)
        binding.tvLogPassRecovery.setOnClickListener(this)

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> {
                val email = binding.etLogEmail.text.toString()
                val pass = binding.etLogPass.text.toString()

                if (email.isNotBlank() && pass.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        // Tries to log in the user with the provided email and password
                        val userLogged = authManager.login(email, pass)

                        withContext(Dispatchers.Main) {
                            if (userLogged != null) {
                                // If the login is successful, the user is taken to the main activity
                                mListener?.onLogin(email)
                            } else {
                                // In case of an error, lets the user know
                                Snackbar.make(binding.root, getString(R.string.bad_credentials), Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // In case the user did not provide an email or password lets them know
                    Snackbar.make(binding.root, getString(R.string.no_email_or_pass), Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.btn_to_register -> {
                mListener?.toRegister()
            }
            R.id.tv_log_pass_recovery -> {
                mListener?.toPassRecovery()
            }
        }
    }

    interface LoginFragmentListener {
        fun onLogin(email: String? = null)
        fun toRegister()
        fun toPassRecovery()
    }
}