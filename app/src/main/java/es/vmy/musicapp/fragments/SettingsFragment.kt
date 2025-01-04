package es.vmy.musicapp.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import es.vmy.musicapp.R
import es.vmy.musicapp.dialogs.OfflineModeDialog
import es.vmy.musicapp.utils.LOG_TAG
import es.vmy.musicapp.utils.OFFLINE_MODE_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // SettingsFragment does not inherit from Fragment so it can't have view.binding
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Obtains the offline mode preference
        val offlineModePreference: Preference? = findPreference("offline_mode")

        offlineModePreference?.setOnPreferenceClickListener {
            // Obtains the current value of the offline mode preference
            val prefs = requireActivity().getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
            val offlineMode = prefs.getBoolean(OFFLINE_MODE_KEY, false)

            // If offline mode is not enabled, show a custom dialog
            if (offlineMode) {
                OfflineModeDialog().show(parentFragmentManager, "OFFLINE MODE DIALOG")

                // Stores the offline mode preference immediately so that the dialog is not shown again
                with(prefs.edit()) {
                    putBoolean(OFFLINE_MODE_KEY, true)
                    apply()
                }
            }
            // Returns true to indicate that the preference click was handled
            true
        }
    }
}