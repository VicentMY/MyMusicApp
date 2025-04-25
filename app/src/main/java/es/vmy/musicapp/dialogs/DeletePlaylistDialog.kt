package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Playlist

class DeletePlaylistDialog(private val playlist: Playlist, private val mListener: DeletePlaylistDialogListener): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(getString(R.string.dialog_delete_playlist_title))
                .setMessage(getString(R.string.dialog_delete_playlist_message))
                .setPositiveButton(getString(R.string.dialog_confirm)) { dialog, id ->
                    mListener.onPlaylistRemove(playlist)
                    dismiss()
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, id ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface DeletePlaylistDialogListener {
        fun onPlaylistRemove(playlist: Playlist)
    }
}