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
inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E?): E? {
    val name = getString(key, defaultValue?.name)
    return name?.let { enumValueOf<E>(it) }
}
//endregion

//region Suspend
suspend fun SharedPreferences.awaitNonNullString(key: String, defaultValue: String): String =
    awaitString(key, defaultValue)!!

suspend fun SharedPreferences.awaitNonNullStringSet(key: String, defaultValue: Set<String>): Set<String> =
    awaitStringSet(key, defaultValue)!!

suspend inline fun <reified E : Enum<E>> SharedPreferences.awaitEnum(key: String, defaultValue: E?): E? {
    val name = awaitString(key, defaultValue?.name)
    return name?.let { enumValueOf<E>(it) }
}

suspend inline fun <reified E : Enum<E>> SharedPreferences.awaitNonNullEnum(key: String, defaultValue: E): E =
    awaitEnum(key, defaultValue)!!
//endregion

//region Flow
@FlowPreview
fun SharedPreferences.getNonNullStringFlow(key: String, defaultValue: String): Flow<String> =
    getStringFlow(key, defaultValue)
        .map { it!! }

@FlowPreview
fun SharedPreferences.getNonNullStringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>> =
    getStringSetFlow(key, defaultValue)
        .map { it!! }

@FlowPreview
inline fun <reified E : Enum<E>> SharedPreferences.getEnumFlow(key: String, defaultValue: E?): Flow<E?> =
    getStringFlow(key, defaultValue?.name)
        .map { name ->
            name?.let { enumValueOf<E>(it) }
        }

@FlowPreview
inline fun <reified E : Enum<E>> SharedPreferences.getNonNullEnumFlow(key: String, defaultValue: E): Flow<E> =
    getEnumFlow(key, defaultValue)
        .map { it!! }
//endregion

//region Edit
suspend inline fun SharedPreferences.awaitEdits(edits: SharedPreferences.Editor.() -> SharedPreferences.Editor) =
    edits.invoke(this.edit()).awaitCommit()

inline fun <reified E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E?): SharedPreferences.Editor =
    putString(key, value?.name)
//endregion
