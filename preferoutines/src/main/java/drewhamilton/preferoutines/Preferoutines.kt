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

    suspend fun getAll(): Map<String, *> = suspendCoroutine { continuation ->
        continuation.resume(preferences.all)
    }

    @FlowPreview
    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun getAllFlow(): Flow<Map<String, *>> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.all)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            channel.offer(sharedPreferences.all)
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        channel.invokeOnClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    suspend fun getString(key: String, defaultValue: String?) =
        getSuspendedPreference(SharedPreferences::getString, key, defaultValue)

    @FlowPreview
    fun getStringFlow(key: String, defaultValue: String?) =
        getPreferenceFlow(SharedPreferences::getString, key, defaultValue)

    suspend fun getStringSet(key: String, defaultValue: Set<String>?) =
        getSuspendedPreference(SharedPreferences::getStringSet, key, defaultValue)

    @FlowPreview
    fun getStringSetFlow(key: String, defaultValue: Set<String>?) =
        getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue)

    suspend fun getInt(key: String, defaultValue: Int) =
        getSuspendedPreference(SharedPreferences::getInt, key, defaultValue)

    @FlowPreview
    fun getIntFlow(key: String, defaultValue: Int) =
        getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)

    suspend fun getLong(key: String, defaultValue: Long) =
        getSuspendedPreference(SharedPreferences::getLong, key, defaultValue)

    @FlowPreview
    fun getLongFlow(key: String, defaultValue: Long) =
        getPreferenceFlow(SharedPreferences::getLong, key, defaultValue)

    suspend fun getFloat(key: String, defaultValue: Float) =
        getSuspendedPreference(SharedPreferences::getFloat, key, defaultValue)

    @FlowPreview
    fun getFloatFlow(key: String, defaultValue: Float) =
        getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)

    suspend fun getBoolean(key: String, defaultValue: Boolean) =
        getSuspendedPreference(SharedPreferences::getBoolean, key, defaultValue)

    @FlowPreview
    fun getBooleanFlow(key: String, defaultValue: Boolean) =
        getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

    suspend fun contains(key: String): Boolean = suspendCoroutine { continuation ->
        continuation.resume(preferences.contains(key))
    }

    @FlowPreview
    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun getContainsFlow(key: String): Flow<Boolean> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.contains(key))

        val listener = object : SinglePreferenceListener(key) {
            override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
                channel.offer(preferences.contains(key))
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        channel.invokeOnClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private suspend fun <T> getSuspendedPreference(
        getPreference: SharedPreferences.(String, T) -> T,
        key: String,
        defaultValue: T
    ): T = suspendCoroutine { continuation ->
        continuation.resume(preferences.getPreference(key, defaultValue))
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
