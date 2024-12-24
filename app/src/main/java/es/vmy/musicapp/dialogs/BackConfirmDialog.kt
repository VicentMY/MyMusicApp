package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import es.vmy.musicapp.R

class BackConfirmDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_confirm)) {dialog, id ->
                    it.finish()
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) {dialog, id ->
                    dismiss() // <- No es obligatorio
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
