package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

@ExperimentalCoroutinesApi
internal class CoroutineSinglePreferenceContainsListener constructor(
    key: String,
    channel: ProducerScope<Boolean>
) : CoroutineSinglePreferenceListener<Boolean>(key, channel) {

    override fun SharedPreferences.getCurrentValue() = contains(key)
}
