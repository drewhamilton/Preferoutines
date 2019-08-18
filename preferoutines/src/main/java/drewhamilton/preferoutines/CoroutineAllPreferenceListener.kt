package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

@ExperimentalCoroutinesApi
internal class CoroutineAllPreferenceListener(
    override val channel: ProducerScope<Map<String, *>>
) : CoroutinePreferenceListener<Map<String, *>> {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        channel.offer(sharedPreferences.all)
    }
}
