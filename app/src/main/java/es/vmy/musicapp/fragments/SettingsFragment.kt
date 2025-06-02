package es.vmy.musicapp.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import es.vmy.musicapp.R
import es.vmy.musicapp.dialogs.OfflineModeDialog
import es.vmy.musicapp.utils.AuthManager
import es.vmy.musicapp.utils.CHAT_COLOR_OTHER_KEY
import es.vmy.musicapp.utils.CHAT_COLOR_SELF_KEY
import es.vmy.musicapp.utils.CHAT_LOGOUT_KEY
import es.vmy.musicapp.utils.CHAT_USERNAME_KEY
import es.vmy.musicapp.utils.OFFLINE_MODE_KEY
import es.vmy.musicapp.utils.PREFERENCES_FILE
import es.vmy.musicapp.utils.USER_EMAIL_KEY

class SettingsFragment : PreferenceFragmentCompat() {

    private val prefs by lazy { requireActivity().getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // SettingsFragment does not inherit from Fragment so it can't have view.binding
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Obtains the offline mode preference
        val offlineModePreference: Preference? = findPreference(OFFLINE_MODE_KEY)

        offlineModePreference?.setOnPreferenceClickListener {
            // Obtains the current value of the offline mode preference
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

        val chatLogOutPreference: Preference? = findPreference(CHAT_LOGOUT_KEY)

        chatLogOutPreference?.setOnPreferenceClickListener {
            val chatLogOut = prefs.getBoolean(CHAT_LOGOUT_KEY, false)

            if (chatLogOut) {
                // If the user is logged in, then logs out, if not nothing happens
                val currentUser = AuthManager().getCurrentUser()
                if (currentUser != null) {
                    // Closes the Firebase session
                    AuthManager().logOut()
                    // Removes the username and colors from the SharedPreferences
                    with(prefs.edit()) {
                        remove(CHAT_USERNAME_KEY)
                        remove(CHAT_COLOR_SELF_KEY)
                        remove(CHAT_COLOR_OTHER_KEY)
                        apply()
                    }

                    val user = prefs.getString(USER_EMAIL_KEY, "") ?: ""
                    Toast.makeText(requireActivity(), getString(R.string.logout_message) + user, Toast.LENGTH_SHORT).show()
                }

                with(prefs.edit()) {
                    putBoolean(CHAT_LOGOUT_KEY, false)
                    apply()
                }
            }

            true
        }
    }
}