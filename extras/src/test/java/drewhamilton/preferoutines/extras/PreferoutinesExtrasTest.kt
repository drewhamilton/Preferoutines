package drewhamilton.preferoutines.extras

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.whenever
import drewhamilton.preferoutines.test.BasePreferoutinesTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreferoutinesExtrasTest : BasePreferoutinesTest() {

    //region Synchronous
    @Test
    fun `getEnum returns enum by name`() {
        val testKey = "Test enum key"
        val testValue = TestEnum.A
        val testDefault = TestEnum.C
        whenever(mockSharedPreferences.getString(testKey, testDefault.name)).thenReturn(testValue.name)

        assertEquals(testValue, mockSharedPreferences.getEnum(testKey, testDefault))
    }

    @Test
    fun `getEnum returns null default`() {
        val testKey = "Test enum key"
        val testDefault: TestEnum? = null
        whenever(mockSharedPreferences.getString(testKey, testDefault?.name)).thenReturn(testDefault?.name)

        assertNull(mockSharedPreferences.getEnum(testKey, testDefault))
    }
    //endregion

    //region Suspend
    @Test
    fun `awaitNonNullString returns value from getString`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getString, SharedPreferences::awaitNonNullString,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @Test
    fun `awaitNonNullStringSet returns value from getStringSet`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getStringSet, SharedPreferences::awaitNonNullStringSet,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1")
        )
    }

    @Test
    fun `awaitEnum returns value from getEnum`() {
        testAwaitPreference_returnsCorrespondingPreference<String?, TestEnum?>(
            SharedPreferences::getString,
            { key, defaultValue ->
                awaitEnum(key, defaultValue)
            },
            asPreferenceValue = { this?.name },
            testValue = TestEnum.A, testDefault = TestEnum.B
        )
    }

    @Test
    fun `awaitEnum returns null from getEnum`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getString, { key, defaultValue: TestEnum? ->
                awaitEnum(key, defaultValue)
            },
            asPreferenceValue = { this?.name },
            testValue = null, testDefault = null
        )
    }

    @Test
    fun `awaitNonNullEnum returns value from getEnum`() {
        testAwaitPreference_returnsCorrespondingPreference<String, TestEnum>(
            SharedPreferences::getString, SharedPreferences::awaitNonNullEnum,
            asPreferenceValue = { name },
            testValue = TestEnum.A, testDefault = TestEnum.B
        )
    }
    //endregion
}
