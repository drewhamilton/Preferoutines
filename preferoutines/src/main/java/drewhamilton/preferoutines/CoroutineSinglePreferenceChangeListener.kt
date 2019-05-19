package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

internal class CoroutineSinglePreferenceChangeListener<T> constructor(
    key: String,
    channel: SendChannel<T>,
    private val defaultValue: T,
    private inline val getPreference: SharedPreferences.(String, T) -> T
) : CoroutineSinglePreferenceListener<T>(key, channel) {

    override fun SharedPreferences.getCurrentValue() = getPreference(key, defaultValue)
}
