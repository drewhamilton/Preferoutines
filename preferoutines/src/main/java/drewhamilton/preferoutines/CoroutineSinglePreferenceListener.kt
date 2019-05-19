package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

internal abstract class CoroutineSinglePreferenceListener<T> constructor(
    key: String,
    private val channel: SendChannel<T>
) : SinglePreferenceListener(key) {

    final override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
        channel.offer(sharedPreferences.getCurrentValue())
    }

    protected abstract fun SharedPreferences.getCurrentValue(): T
}
