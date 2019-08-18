package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

//region Flow

/**
 * Continuously receive all values from the preferences.
 * 
 * @return a [Flow] that emits a map containing a list of key/value pairs representing the preferences each time any of
 * the preferences change.
 */
@ExperimentalCoroutinesApi
fun SharedPreferences.getAllFlow(): Flow<Map<String, *>> = callbackFlow {
    offer(all)
    val listener = CoroutineAllPreferenceListener(this)
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
@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
fun SharedPreferences.getBooleanFlow(key: String, defaultValue: Boolean) =
    getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)

/**
 * Continuously receive whether the preferences contains a value associated with the given [key].
 *
 * @return a [Flow] that emits whether the preference with the given [key] exists. Emits upon subscription as well as
 * each time the preference is changed.
 * @throws ClassCastException if there is a preference with this name that is not a long.
 */
@ExperimentalCoroutinesApi
fun SharedPreferences.getContainsFlow(key: String): Flow<Boolean> = callbackFlow {
    offer(contains(key))

    val listener = CoroutineSinglePreferenceContainsListener(key, this)
    registerCoroutinePreferenceListener(listener)
}

@ExperimentalCoroutinesApi
private fun <T> SharedPreferences.getPreferenceFlow(
    getPreference: SharedPreferences.(String, T) -> T,
    key: String,
    defaultValue: T
): Flow<T> = callbackFlow {
    offer(getPreference(key, defaultValue))

    val listener = CoroutineSinglePreferenceChangeListener(key, this, defaultValue, getPreference)
    registerCoroutinePreferenceListener(listener)
}

@UseExperimental(ExperimentalCoroutinesApi::class)
private suspend fun <T> SharedPreferences.registerCoroutinePreferenceListener(listener: CoroutinePreferenceListener<T>) {
    registerOnSharedPreferenceChangeListener(listener)
    listener.channel.awaitClose {
        unregisterOnSharedPreferenceChangeListener(listener)
    }
}

//endregion
