package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.channels.SendChannel

internal abstract class CoroutineSinglePreferenceListener<T> constructor(
    key: String,
    override val channel: SendChannel<T>
) : SinglePreferenceListener(key), CoroutinePreferenceListener<T> {

    final override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
        channel.offer(sharedPreferences.getCurrentValue())
    }

    protected abstract fun SharedPreferences.getCurrentValue(): T
}
