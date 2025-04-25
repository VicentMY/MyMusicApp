package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import es.vmy.musicapp.R

class RenamePlaylistDialog(private val mListener: RenamePlaylistDialogListener, private val currentName: String): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = it.layoutInflater.inflate(R.layout.dialog_create_playlist, null)
            val editText = view.findViewById<TextInputEditText>(R.id.et_dialog_playlist_name)

            editText.setText(currentName)

            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setTitle(getString(R.string.dialog_create_playlist_title))
                .setPositiveButton(getString(R.string.dialog_rename)) { dialog, id ->
                    val playlistName = editText.text.toString()
                    mListener.onRenamePlaylist(playlistName)
                    dismiss()
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, id ->
                    dismiss()
                }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface RenamePlaylistDialogListener {
        fun onRenamePlaylist(name: String)
    }
}