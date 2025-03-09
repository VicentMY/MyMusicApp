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
import es.vmy.musicapp.databinding.FragmentRegisterBinding
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var mListener: RegisterFragmentListener? = null

    private val authManager: AuthManager by lazy { AuthManager() }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is RegisterFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG RegisterFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.btnRegister.setOnClickListener(this)
        binding.btnLoginReturn.setOnClickListener(this)

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
            R.id.btn_register -> {
                val email = binding.etRegEmail.text.toString()
                val pass = binding.etRegPass.text.toString()
                val pass2 = binding.etRegPassCon.text.toString()

                if (pass != pass2) {
                    // Lets the user know that the passwords do not match
                    Snackbar.make(binding.root, getString(R.string.error_no_same_pass), Snackbar.LENGTH_SHORT).show()
                } else {
                    if (email.isNotBlank() && pass.isNotBlank()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            // Tries to register the user
                            val userRegistered = authManager.createUser(email, pass)

                            withContext(Dispatchers.Main) {
                                if (userRegistered != null) {
                                    // if the user was registered, goes to the login screen
                                    mListener?.onRegistered(email)
                                } else {
                                    // Lets the user know an error occurred on registration
                                    Snackbar.make(binding.root, getString(R.string.bad_credentials), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // Lets the user know that the email or password was not provided
                        Snackbar.make(binding.root, getString(R.string.no_email_or_pass), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.btn_login_return -> {
                mListener?.backToLoginRegister()
            }
        }
    }

    interface RegisterFragmentListener {
        fun onRegistered(email: String)
        fun backToLoginRegister()
    }
}