package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowViaChannel
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//region Suspend

/**
 * Retrieve all values from the preferences.
 * 
 * @return a map containing a list of pairs key/value representing the preferences.
 */
suspend fun SharedPreferences.awaitAll(): Map<String, *> = suspendCoroutine { continuation ->
    continuation.resume(all)
}

/**
 * Retrieve a string value from the preferences.
 * 
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
suspend fun SharedPreferences.awaitString(key: String, defaultValue: String?) =
    awaitPreference(SharedPreferences::getString, key, defaultValue)

/**
 * Retrieve a set of String values from the preferences.
 *
 * Retrieve the set associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a {@link Set}.
 */
suspend fun SharedPreferences.awaitStringSet(key: String, defaultValue: Set<String>?) =
    awaitPreference(SharedPreferences::getStringSet, key, defaultValue)

/**
 * Retrieve an int value from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not an int.
 */
suspend fun SharedPreferences.awaitInt(key: String, defaultValue: Int) =
    awaitPreference(SharedPreferences::getInt, key, defaultValue)

/**
 * Retrieve a long value from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a long.
 */
suspend fun SharedPreferences.awaitLong(key: String, defaultValue: Long) =
    awaitPreference(SharedPreferences::getLong, key, defaultValue)

/**
 * Retrieve a float value from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a float.
 */
suspend fun SharedPreferences.awaitFloat(key: String, defaultValue: Float) =
    awaitPreference(SharedPreferences::getFloat, key, defaultValue)

/**
 * Retrieve a boolean value from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a boolean.
 */
suspend fun SharedPreferences.awaitBoolean(key: String, defaultValue: Boolean) =
    awaitPreference(SharedPreferences::getBoolean, key, defaultValue)

/**
 * Check whether the preferences contains a value for the given [key].
 */
suspend fun SharedPreferences.awaitContains(key: String): Boolean = suspendCoroutine { continuation ->
    continuation.resume(contains(key))
}

private suspend fun <T> SharedPreferences.awaitPreference(
    getPreference: SharedPreferences.(String, T) -> T,
    key: String,
    defaultValue: T
): T = withContext(Dispatchers.IO) {
    suspendCoroutine<T> {
        Thread.sleep(5000)
        it.resume(getPreference(key, defaultValue))
    }
}
//endregion

//region Flow

/**
 * Continuously receive all values from the preferences.
 * 
 * @return a [Flow] that emits a map containing a list of key/value pairs representing the preferences each time any of
 * the preferences change.
 */
@FlowPreview
fun SharedPreferences.getAllFlow(): Flow<Map<String, *>> = flowViaChannel(CONFLATED) { channel ->
    channel.offer(all)

    val listener = CoroutineAllPreferenceListener(channel)
    registerCoroutinePreferenceListener(listener)
}

/**
 * Continuously receive the string value associated with the given [key].
 *
 * @return a [Flow] for the string stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
@FlowPreview
fun SharedPreferences.getStringFlow(key: String, defaultValue: String?) =
    getPreferenceFlow(SharedPreferences::getString, key, defaultValue)

/**
 * Continuously receive a string set associated with the given [key].
 *
 * @return a [Flow] for the set stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a {@link Set}.
 */
@FlowPreview
fun SharedPreferences.getStringSetFlow(key: String, defaultValue: Set<String>?) =
    getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue)

/**
 * Continuously receive an int value associated with the given [key].
 *
 * @return a [Flow] for the int stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not an int.
 */
@FlowPreview
fun SharedPreferences.getIntFlow(key: String, defaultValue: Int) =
    getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)

/**
 * Continuously receive a long value associated with the given [key].
 *
 * @return a [Flow] for the long stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a long.
 */
@FlowPreview
fun SharedPreferences.getLongFlow(key: String, defaultValue: Long) =
    getPreferenceFlow(SharedPreferences::getLong, key, defaultValue)

/**
 * Continuously receive a float value associated with the given [key].
 *
 * @return a [Flow] for the float stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a float.
 */
@FlowPreview
fun SharedPreferences.getFloatFlow(key: String, defaultValue: Float) =
    getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)

/**
 * Continuously receive a boolean value associated with the given [key].
 *
 * @return a [Flow] for the boolean stored with the given [key]. Emits the current value upon subscription, and emits
 * the new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a boolean.
 */
@FlowPreview
fun SharedPreferences.getBooleanFlow(key: String, defaultValue: Boolean) =
    getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

/**
 * Continuously receive whether the preferences contains a value associated with the given [key].
 *
 * @return a [Flow] that emits whether the preference with the given [key] exists. Emits upon subscription as well as
 * each time the preference is changed.
 * @throws ClassCastException if there is a preference with this name that is not a long.
 */
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

//region Edit

/**
 * Commit your preferences changes in a suspending fashion. This atomically performs the requested modifications,
 * replacing whatever is currently in the preferences.
 * 
 * Note that when two editors are modifying preferences at the same time, the last one to call commit wins.
 * @return true if the new values were successfully written to persistent storage.
 */
suspend fun SharedPreferences.Editor.awaitCommit(): Boolean = suspendCoroutine { continuation ->
    continuation.resume(commit())
}

//endregion
