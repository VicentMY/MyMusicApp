package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import es.vmy.musicapp.R
import es.vmy.musicapp.utils.FireStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteMessageDialog(private val fireStoreManager: FireStoreManager, private val msgId: String, private val mContext: Context): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(getString(R.string.dialog_delete_msg_title))
                .setMessage(getString(R.string.dialog_delete_msg_message))
                .setPositiveButton(getString(R.string.dialog_confirm)) { dialog, id ->

                    lifecycleScope.launch(Dispatchers.IO) {
                        val result = fireStoreManager.removeMessage(msgId)
                        if (result) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(mContext, getString(R.string.msg_deleted_correctly), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(mContext, getString(R.string.err_removing_msg), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, id ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}