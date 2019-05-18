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
    fun getStringFlow(key: String, defaultValue: String?) =
        getPreferenceFlow(SharedPreferences::getString, key, defaultValue)

    suspend fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? = suspendCoroutine {
        it.resume(preferences.getStringSet(key, defaultValue))
    }

    @FlowPreview
    fun getStringSetFlow(key: String, defaultValue: Set<String>?) =
        getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue)

    suspend fun getInt(key: String, defaultValue: Int): Int = suspendCoroutine {
        it.resume(preferences.getInt(key, defaultValue))
    }

    @FlowPreview
    fun getIntFlow(key: String, defaultValue: Int) =
        getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)

    suspend fun getLong(key: String, defaultValue: Long): Long = suspendCoroutine {
        it.resume(preferences.getLong(key, defaultValue))
    }

    @FlowPreview
    fun getLongFlow(key: String, defaultValue: Long) =
        getPreferenceFlow(SharedPreferences::getLong, key, defaultValue)

    suspend fun getFloat(key: String, defaultValue: Float): Float = suspendCoroutine {
        it.resume(preferences.getFloat(key, defaultValue))
    }

    @FlowPreview
    fun getFloatFlow(key: String, defaultValue: Float) =
        getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)

    suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = suspendCoroutine {
        it.resume(preferences.getBoolean(key, defaultValue))
    }

    @FlowPreview
    fun getBooleanFlow(key: String, defaultValue: Boolean) =
        getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

    suspend fun contains(key: String): Boolean = suspendCoroutine {
        it.resume(preferences.contains(key))
    }

    @FlowPreview
    @UseExperimental(ExperimentalCoroutinesApi::class)
    private fun <T> getPreferenceFlow(
        getPreference: SharedPreferences.(String, T) -> T,
        key: String,
        defaultValue: T
    ): Flow<T> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.getPreference(key, defaultValue))

        val listener = CoroutinePreferenceChangeListener(key, channel, defaultValue, getPreference)
        preferences.registerOnSharedPreferenceChangeListener(listener)
        channel.invokeOnClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
