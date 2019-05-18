package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

@UseExperimental(FlowPreview::class)
internal abstract class CoroutinePreferenceListener<T> constructor(
    protected val key: String,
    private val channel: SendChannel<T>
) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (shouldEmit(key))
            channel.offer(getCurrentValue(preferences))
    }

    protected fun shouldEmit(key: String): Boolean {
        return this.key == key
    }

    protected abstract fun getCurrentValue(preferences: SharedPreferences): T
}
