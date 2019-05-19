package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.channels.SendChannel

internal interface CoroutinePreferenceListener<T> : SharedPreferences.OnSharedPreferenceChangeListener {

    val channel: SendChannel<T>
}
