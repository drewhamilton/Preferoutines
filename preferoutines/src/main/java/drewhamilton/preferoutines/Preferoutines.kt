package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowViaChannel

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
