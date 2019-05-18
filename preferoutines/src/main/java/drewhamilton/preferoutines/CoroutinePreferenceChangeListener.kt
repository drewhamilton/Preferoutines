package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

@FlowPreview
internal class CoroutinePreferenceChangeListener<T> constructor(
    key: String,
    channel: SendChannel<T>,
    private val defaultValue: T,
    private inline val getPreference: SharedPreferences.(String, T) -> T
) : CoroutinePreferenceListener<T>(key, channel) {

    override fun SharedPreferences.getCurrentValue() = getPreference(key, defaultValue)
}
