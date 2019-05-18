package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

@FlowPreview
internal abstract class CoroutinePreferenceListener<T> constructor(
    key: String,
    private val channel: SendChannel<T>
) : SinglePreferenceListener(key) {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
        channel.offer(sharedPreferences.getCurrentValue())
    }

    protected abstract fun SharedPreferences.getCurrentValue(): T
}
