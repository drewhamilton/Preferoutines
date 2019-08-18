package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

internal interface CoroutinePreferenceListener<T> : SharedPreferences.OnSharedPreferenceChangeListener {

    @ExperimentalCoroutinesApi
    val channel: ProducerScope<T>
}
