package drewhamilton.preferoutines

import android.content.SharedPreferences
import kotlin.coroutines.suspendCoroutine

class Preferoutines(
    private val preferences: SharedPreferences
) {

    suspend fun getAll(): Map<String, *> {
        return suspendCoroutine { preferences.all }
    }
}
