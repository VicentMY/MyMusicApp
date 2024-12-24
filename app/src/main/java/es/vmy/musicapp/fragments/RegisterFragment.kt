package es.vmy.musicapp.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.vmy.musicapp.R
import es.vmy.musicapp.databinding.FragmentRegisterBinding
import es.vmy.musicapp.utils.LISTENER_EX_MSG
import es.vmy.musicapp.utils.LOG_TAG

class RegisterFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var mListener: RegisterFragmentListener? = null

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
                mListener?.btnRegister()
            }
            R.id.btn_login_return -> {
                mListener?.btnBackToLogin()
            }
        }
    }

    interface RegisterFragmentListener {
        fun btnRegister()
        fun btnBackToLogin()
    }
}