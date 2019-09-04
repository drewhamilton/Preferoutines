package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

@ExperimentalCoroutinesApi
internal class CoroutineSinglePreferenceChangeListener<T> constructor(
    key: String,
    channel: ProducerScope<T>,
    private val defaultValue: T,
    private inline val getPreference: SharedPreferences.(String, T) -> T
) : CoroutineSinglePreferenceListener<T>(key, channel) {

    override fun SharedPreferences.getCurrentValue() = getPreference(key, defaultValue)
}
