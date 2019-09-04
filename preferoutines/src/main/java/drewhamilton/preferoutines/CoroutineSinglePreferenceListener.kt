package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

@ExperimentalCoroutinesApi
internal abstract class CoroutineSinglePreferenceListener<T> constructor(
    key: String,
    override val channel: ProducerScope<T>
) : SinglePreferenceListener(key), CoroutinePreferenceListener<T> {

    final override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
        channel.offer(sharedPreferences.getCurrentValue())
    }

    protected abstract fun SharedPreferences.getCurrentValue(): T
}
