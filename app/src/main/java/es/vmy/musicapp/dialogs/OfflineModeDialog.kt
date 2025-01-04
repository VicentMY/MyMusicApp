package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import es.vmy.musicapp.R

class OfflineModeDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = it.layoutInflater.inflate(R.layout.dialog_offline_mode, null)

            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setTitle(getString(R.string.dialog_offline_title))
                .setNeutralButton(getString(R.string.dialog_ok)) { dialog, id ->
                    dismiss()
                }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}