package drewhamilton.preferoutines.test

import android.content.SharedPreferences

/**
 * An implementation of [SharedPreferences] that only retains data in memory, typically for tests.
 */
class InMemorySharedPreferences : SharedPreferences {

    private var preferences = mutableMapOf<String, Any>()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    inner class Editor(
        private val editingPreferences: MutableMap<String, Any>
    ) : SharedPreferences.Editor {

        private val changedKeys = mutableSetOf<String>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor = put(key, value)
        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = put(key, values)
        override fun putInt(key: String, value: Int): SharedPreferences.Editor  = put(key, value)
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = put(key, value)
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = put(key, value)
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = put(key, value)

        private fun put(key: String, value: Any?): SharedPreferences.Editor {
            if (value == null)
                editingPreferences.remove(key)
            else
                editingPreferences[key] = value
            changedKeys.add(key)
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            editingPreferences.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            editingPreferences.clear()
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            preferences = editingPreferences
            listeners.forEach { listener ->
                changedKeys.forEach { changedKey ->
                    listener.onSharedPreferenceChanged(this@InMemorySharedPreferences, changedKey)
                }
            }
            changedKeys.clear()
        }
    }

    override fun getAll(): MutableMap<String, *> = preferences

    override fun getString(key: String, defValue: String?): String? = (preferences[key] ?: defValue) as String?
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
        (preferences[key] ?: defValues) as MutableSet<String>?
    override fun getInt(key: String, defValue: Int): Int = (preferences[key] ?: defValue) as Int
    override fun getLong(key: String, defValue: Long): Long = (preferences[key] ?: defValue) as Long
    override fun getFloat(key: String, defValue: Float): Float = (preferences[key] ?: defValue) as Float
    override fun getBoolean(key: String, defValue: Boolean): Boolean = (preferences[key] ?: defValue) as Boolean

    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun edit(): SharedPreferences.Editor = Editor(preferences.toMutableMap())

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners.remove(listener)
    }
}
