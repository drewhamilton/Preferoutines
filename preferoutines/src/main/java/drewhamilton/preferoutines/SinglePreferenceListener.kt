package drewhamilton.preferoutines

import android.content.SharedPreferences

internal abstract class SinglePreferenceListener(
    protected val key: String
) : SharedPreferences.OnSharedPreferenceChangeListener {

    final override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (this.key == key)
            onSharedPreferenceChanged(sharedPreferences)
    }

    abstract fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences)
}
