package es.vmy.musicapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import es.vmy.musicapp.R
import es.vmy.musicapp.classes.Song
import es.vmy.musicapp.utils.bytesToFormattedMBString
import es.vmy.musicapp.utils.formatTime
import es.vmy.musicapp.utils.summarizeSongPath

class TrackInfoDialog(private val track: Song): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = it.layoutInflater.inflate(R.layout.dialog_track_info, null)

            val thumbnail = view.findViewById<ImageView>(R.id.iv_thumbnail_tr_in)
            val title = view.findViewById<TextView>(R.id.tv_title_tr_in)
            val artist = view.findViewById<TextView>(R.id.tv_artist_tr_in)
            val album = view.findViewById<TextView>(R.id.tv_album_tr_in)
            val duration = view.findViewById<TextView>(R.id.tv_duration_tr_in)
            val trackNum = view.findViewById<TextView>(R.id.tv_track_num_tr_in)
            val size = view.findViewById<TextView>(R.id.tv_size_tr_in)
            val path = view.findViewById<TextView>(R.id.tv_path_tr_in)

            if (track.thumbnail != null) {
                thumbnail.setImageBitmap(track.thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_action_song)
            }

            title.text = track.title
            artist.text = track.artist
            album.text = track.album
            duration.text = formatTime(track.duration)
            trackNum.text = track.track.toString()
            size.text = bytesToFormattedMBString(track.size)
            path.text = summarizeSongPath(it, track.path)

            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setTitle(getString(R.string.track_info))
                .setNeutralButton(getString(R.string.dialog_ok)) { dialog, id ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}