package es.vmy.musicapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.FragmentLoginBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG

class LoginFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var mListener: LoginFragmentListener? = null

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
                mListener?.btnLogin()
            }
            R.id.btn_to_register -> {
                mListener?.btnToRegister()
            }
        }
    }

    interface LoginFragmentListener {
        fun btnLogin()
        fun btnToRegister()
    }
}