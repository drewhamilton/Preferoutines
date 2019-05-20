package drewhamilton.preferoutines.extras

import android.content.SharedPreferences
import drewhamilton.preferoutines.awaitCommit
import drewhamilton.preferoutines.awaitString
import drewhamilton.preferoutines.awaitStringSet
import drewhamilton.preferoutines.getStringFlow
import drewhamilton.preferoutines.getStringSetFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//region Synchronous

/**
 * Retrieve an enum value by [Enum.name] from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws [ClassCastException] if there is a preference with this key that is not
 * a string
 * @throws [IllegalArgumentException] if the stored string does not resolve to a valid
 * name for a value of type [E].
 */
inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E?): E? {
    val name = getString(key, defaultValue?.name)
    return name?.let { enumValueOf<E>(it) }
}
//endregion

//region Suspend

/**
 * Retrieve a string value from the preferences. Since [defaultValue] is not null, the return value won't be null.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
suspend fun SharedPreferences.awaitNonNullString(key: String, defaultValue: String): String =
    awaitString(key, defaultValue)!!

/**
 * Retrieve a set of String values from the preferences. Since [defaultValue] is not null, the returned set won't be
 * null.
 *
 * Retrieve the set associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a {@link Set}.
 */
suspend fun SharedPreferences.awaitNonNullStringSet(key: String, defaultValue: Set<String>): Set<String> =
    awaitStringSet(key, defaultValue)!!

/**
 * Retrieve an enum value by [Enum.name] from the preferences.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
suspend inline fun <reified E : Enum<E>> SharedPreferences.awaitEnum(key: String, defaultValue: E?): E? {
    val name = awaitString(key, defaultValue?.name)
    return name?.let { enumValueOf<E>(it) }
}

/**
 * Retrieve an enum value by [Enum.name] from the preferences. Since [defaultValue] is not null, the return value won't
 * be null.
 *
 * Retrieve the value associated with [key], if it exists.
 * @return the preference value if it exists, otherwise the given [defaultValue].
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
suspend inline fun <reified E : Enum<E>> SharedPreferences.awaitNonNullEnum(key: String, defaultValue: E): E =
    awaitEnum(key, defaultValue)!!
//endregion

//region Flow

/**
 * Continuously receive the string value associated with the given [key]. Since [defaultValue] is not null, the emitted
 * value will never be null.
 *
 * @return a [Flow] for the string stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
@FlowPreview
fun SharedPreferences.getNonNullStringFlow(key: String, defaultValue: String): Flow<String> =
    getStringFlow(key, defaultValue)
        .map { it!! }

/**
 * Continuously receive a string set associated with the given [key]. Since [defaultValue] is not null, the emitted set
 * will never be null.
 *
 * @return a [Flow] for the set stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a {@link Set}.
 */
@FlowPreview
fun SharedPreferences.getNonNullStringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>> =
    getStringSetFlow(key, defaultValue)
        .map { it!! }

/**
 * Continuously receive the enum value associated with the given [key] by [Enum.name].
 *
 * @return a [Flow] for the string stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
@FlowPreview
inline fun <reified E : Enum<E>> SharedPreferences.getEnumFlow(key: String, defaultValue: E?): Flow<E?> =
    getStringFlow(key, defaultValue?.name)
        .map { name ->
            name?.let { enumValueOf<E>(it) }
        }

/**
 * Continuously receive the enum value associated with the given [key] by [Enum.name]. Since [defaultValue] is not null,
 * the emitted value will never be null.
 *
 * @return a [Flow] for the string stored with the given [key]. Emits the current value upon subscription, and emits the
 * new value each time it is updated. If the value is null or is later set to null or removed, the provided
 * [defaultValue] is emitted.
 * @throws ClassCastException if there is a preference with this name that is not a String.
 */
@FlowPreview
inline fun <reified E : Enum<E>> SharedPreferences.getNonNullEnumFlow(key: String, defaultValue: E): Flow<E> =
    getEnumFlow(key, defaultValue)
        .map { it!! }

//endregion

//region Edit

/**
 * Apply a series of [edits] to the preferences and then commit them in a suspending fashiong.
 *
 * Note that calls to [SharedPreferences.Editor.remove] and [SharedPreferences.Editor.clear] are executed first,
 * regardless of what order they appear in the series of edits.
 * @return true if the new values were successfully written to persistent storage.
 */
suspend inline fun SharedPreferences.awaitEdits(edits: SharedPreferences.Editor.() -> SharedPreferences.Editor) =
    edits.invoke(this.edit()).awaitCommit()

/**
 * Set an enum value associated with [key] in the preferences editor, to be written as string [Enum.name] once
 * [SharedPreferences.Editor.commit] or [SharedPreferences.Editor.apply] is called.
 * @return a reference to the same [SharedPreferences.Editor] object, so you can chain calls together.
 */
inline fun <reified E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E?): SharedPreferences.Editor =
    putString(key, value?.name)

//endregion
