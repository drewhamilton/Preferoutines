package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowViaChannel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Preferoutines(
    private val preferences: SharedPreferences
) {

    suspend fun getAll(): Map<String, *> = suspendCoroutine {
        it.resume(preferences.all)
    }

    suspend fun getString(key: String, defaultValue: String?): String? = suspendCoroutine {
        it.resume(preferences.getString(key, defaultValue))
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun getStringFlow(key: String, defaultValue: String?): Flow<String?> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.getString(key, defaultValue))

        val listener = CoroutinePreferenceChangeListener(key, channel, defaultValue, SharedPreferences::getString)
        preferences.registerOnSharedPreferenceChangeListener(listener)
        channel.invokeOnClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    suspend fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? = suspendCoroutine {
        it.resume(preferences.getStringSet(key, defaultValue))
    }

    suspend fun getInt(key: String, defaultValue: Int): Int = suspendCoroutine {
        it.resume(preferences.getInt(key, defaultValue))
    }

    suspend fun getLong(key: String, defaultValue: Long): Long = suspendCoroutine {
        it.resume(preferences.getLong(key, defaultValue))
    }

    suspend fun getFloat(key: String, defaultValue: Float): Float = suspendCoroutine {
        it.resume(preferences.getFloat(key, defaultValue))
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = suspendCoroutine {
        it.resume(preferences.getBoolean(key, defaultValue))
    }

    suspend fun contains(key: String): Boolean = suspendCoroutine {
        it.resume(preferences.contains(key))
    }
}
