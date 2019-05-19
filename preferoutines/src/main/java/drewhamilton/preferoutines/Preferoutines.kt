package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowViaChannel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun SharedPreferences.awaitAll(): Map<String, *> = suspendCoroutine { continuation ->
    continuation.resume(all)
}

suspend fun SharedPreferences.awaitString(key: String, defaultValue: String?) =
    awaitPreference(SharedPreferences::getString, key, defaultValue)

suspend fun SharedPreferences.awaitStringSet(key: String, defaultValue: Set<String>?) =
    awaitPreference(SharedPreferences::getStringSet, key, defaultValue)

suspend fun SharedPreferences.awaitInt(key: String, defaultValue: Int) =
    awaitPreference(SharedPreferences::getInt, key, defaultValue)

suspend fun SharedPreferences.awaitLong(key: String, defaultValue: Long) =
    awaitPreference(SharedPreferences::getLong, key, defaultValue)

suspend fun SharedPreferences.awaitFloat(key: String, defaultValue: Float) =
    awaitPreference(SharedPreferences::getFloat, key, defaultValue)

suspend fun SharedPreferences.awaitBoolean(key: String, defaultValue: Boolean) =
    awaitPreference(SharedPreferences::getBoolean, key, defaultValue)

suspend fun SharedPreferences.awaitContains(key: String): Boolean = suspendCoroutine { continuation ->
    continuation.resume(contains(key))
}

@Deprecated("Use extensions")
class Preferoutines(
    private val preferences: SharedPreferences
) {

    @FlowPreview fun getAllFlow(): Flow<Map<String, *>> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.all)

        val listener = CoroutineAllPreferenceListener(channel)
        preferences.registerCoroutinePreferenceListener(listener)
    }

    @FlowPreview fun getStringFlow(key: String, defaultValue: String?) =
        getPreferenceFlow(SharedPreferences::getString, key, defaultValue)

    @FlowPreview fun getStringSetFlow(key: String, defaultValue: Set<String>?) =
        getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue)

    @FlowPreview fun getIntFlow(key: String, defaultValue: Int) =
        getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)

    @FlowPreview fun getLongFlow(key: String, defaultValue: Long) =
        getPreferenceFlow(SharedPreferences::getLong, key, defaultValue)

    @FlowPreview fun getFloatFlow(key: String, defaultValue: Float) =
        getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)

    @FlowPreview fun getBooleanFlow(key: String, defaultValue: Boolean) =
        getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

    @FlowPreview fun getContainsFlow(key: String): Flow<Boolean> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.contains(key))

        val listener = CoroutineSinglePreferenceContainsListener(key, channel)
        preferences.registerCoroutinePreferenceListener(listener)
    }

    @FlowPreview private fun <T> getPreferenceFlow(
        getPreference: SharedPreferences.(String, T) -> T,
        key: String,
        defaultValue: T
    ): Flow<T> = flowViaChannel(CONFLATED) { channel ->
        channel.offer(preferences.getPreference(key, defaultValue))

        val listener = CoroutineSinglePreferenceChangeListener(key, channel, defaultValue, getPreference)
        preferences.registerCoroutinePreferenceListener(listener)
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    private fun <T> SharedPreferences.registerCoroutinePreferenceListener(listener: CoroutinePreferenceListener<T>) {
        registerOnSharedPreferenceChangeListener(listener)
        listener.channel.invokeOnClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}

private suspend fun <T> SharedPreferences.awaitPreference(
    getPreference: SharedPreferences.(String, T) -> T,
    key: String,
    defaultValue: T
): T = suspendCoroutine { continuation ->
    continuation.resume(getPreference(key, defaultValue))
}

@FlowPreview
private fun <T> SharedPreferences.getPreferenceFlow(
    getPreference: SharedPreferences.(String, T) -> T,
    key: String,
    defaultValue: T
): Flow<T> = flowViaChannel(CONFLATED) { channel ->
    channel.offer(getPreference(key, defaultValue))

    val listener = CoroutineSinglePreferenceChangeListener(key, channel, defaultValue, getPreference)
    registerCoroutinePreferenceListener(listener)
}

@UseExperimental(ExperimentalCoroutinesApi::class)
private fun <T> SharedPreferences.registerCoroutinePreferenceListener(listener: CoroutinePreferenceListener<T>) {
    registerOnSharedPreferenceChangeListener(listener)
    listener.channel.invokeOnClose {
        unregisterOnSharedPreferenceChangeListener(listener)
    }
}
