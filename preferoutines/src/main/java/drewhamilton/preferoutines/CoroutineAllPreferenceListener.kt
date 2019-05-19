package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.channels.SendChannel

internal class CoroutineAllPreferenceListener(
    private val channel: SendChannel<Map<String, *>>
) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        channel.offer(sharedPreferences.all)
    }
}
