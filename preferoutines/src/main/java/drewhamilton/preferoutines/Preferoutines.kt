package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlin.coroutines.suspendCoroutine

class Preferoutines(
    private val preferences: SharedPreferences
) {

    // TODO: Provide this as a property somehow?
    suspend fun getAll(): Map<String, *> {
        return suspendCoroutine { preferences.all }
    }
}
