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

//region Suspend
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

private suspend fun <T> SharedPreferences.awaitPreference(
    getPreference: SharedPreferences.(String, T) -> T,
    key: String,
    defaultValue: T
): T = suspendCoroutine { continuation ->
    continuation.resume(getPreference(key, defaultValue))
}
//endregion

//region Flow
@FlowPreview
fun SharedPreferences.getAllFlow(): Flow<Map<String, *>> = flowViaChannel(CONFLATED) { channel ->
    channel.offer(all)

    val listener = CoroutineAllPreferenceListener(channel)
    registerCoroutinePreferenceListener(listener)
}

@FlowPreview
fun SharedPreferences.getStringFlow(key: String, defaultValue: String?) =
    getPreferenceFlow(SharedPreferences::getString, key, defaultValue)

@FlowPreview
fun SharedPreferences.getStringSetFlow(key: String, defaultValue: Set<String>?) =
    getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue)

@FlowPreview
fun SharedPreferences.getIntFlow(key: String, defaultValue: Int) =
    getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)

@FlowPreview
fun SharedPreferences.getLongFlow(key: String, defaultValue: Long) =
    getPreferenceFlow(SharedPreferences::getLong, key, defaultValue)

@FlowPreview
fun SharedPreferences.getFloatFlow(key: String, defaultValue: Float) =
    getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)

@FlowPreview
fun SharedPreferences.getBooleanFlow(key: String, defaultValue: Boolean) =
    getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

@FlowPreview
fun SharedPreferences.getContainsFlow(key: String): Flow<Boolean> = flowViaChannel(CONFLATED) { channel ->
    channel.offer(contains(key))

    val listener = CoroutineSinglePreferenceContainsListener(key, channel)
    registerCoroutinePreferenceListener(listener)
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
//endregion
