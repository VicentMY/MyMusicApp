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
import es.vmy.musicapp.databinding.FragmentPassRecoveryBinding
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PassRecoveryFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPassRecoveryBinding? = null
    private val binding get() = _binding!!
    private  var mListener: PassRecoveryFragmentListener? = null

    private val authManager: AuthManager by lazy { AuthManager() }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PassRecoveryFragmentListener) {
            mListener = context
        } else {
            throw Exception("$LISTENER_EX_MSG FragmentPassRecoveryListener")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassRecoveryBinding.inflate(inflater, container, false)

        binding.btnSendEmail.setOnClickListener(this)
        binding.btnPassRecReturn.setOnClickListener(this)

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
            R.id.btn_send_email -> {
                val email = binding.etPassRecEmail.text.toString()

                if (email.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val mailSent = authManager.resetPassword(email)

                        withContext(Dispatchers.Main) {
                            if (mailSent) {
                                // Lets the user know the recovery email was sent and goes back to the login screen
                                Snackbar.make(binding.root, getString(R.string.rec_mail_sent), Snackbar.LENGTH_SHORT).show()
                                mListener?.onPassRecoveryMailSent()
                            } else {
                                // Lets the user know the recovery email was not sent due to an error
                                Snackbar.make(binding.root, getString(R.string.rec_mail_error), Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Lets the user know a recovery email was not provided
                    Snackbar.make(binding.root, getString(R.string.rec_no_mail_error), Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.btn_pass_rec_return -> {
                mListener?.backToLoginRecovery()
            }
        }
    }

    interface PassRecoveryFragmentListener {
        fun onPassRecoveryMailSent()
        fun backToLoginRecovery()
    }

}