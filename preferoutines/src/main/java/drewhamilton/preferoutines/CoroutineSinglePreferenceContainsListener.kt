package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

internal class CoroutineSinglePreferenceContainsListener constructor(
    key: String,
    channel: SendChannel<Boolean>
) : CoroutineSinglePreferenceListener<Boolean>(key, channel) {

    override fun SharedPreferences.getCurrentValue() = contains(key)
}
