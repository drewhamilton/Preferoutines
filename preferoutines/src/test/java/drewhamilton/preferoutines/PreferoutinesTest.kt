package drewhamilton.preferoutines

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PreferoutinesTest {

    @Mock private lateinit var mockSharedPreferences: SharedPreferences
    @Mock private lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    @InjectMocks private lateinit var preferoutines: Preferoutines

    @Before
    fun setUp() {
//        whenever(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
//        whenever(mockSharedPreferencesEditor.commit()).thenReturn(true)
    }

    //region Suspend functions
    @Test
    fun `getAll returns map from internal preferences`() {
        val testMap = mapOf(Pair("Made up map key", 23498))
        whenever(mockSharedPreferences.all).thenReturn(testMap)

        runBlocking { assertEquals(testMap, preferoutines.getAll()) }
    }

    @Test
    fun `getString returns value from internal preferences`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = "Test default"
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getString(testKey, testDefault)) }
    }

    @Test
    fun `getString returns null from internal preferences`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = null
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testDefault)

        runBlocking { assertNull(preferoutines.getString(testKey, testDefault)) }
    }

    @Test
    fun `getStringSet returns value from internal preferences`() {
        val testKey = "Test string set key"
        val testValue = setOf("Test value 1", "Test value 2")
        val testDefault = setOf("Test default 1")
        whenever(mockSharedPreferences.getStringSet(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getStringSet(testKey, testDefault)) }
    }

    @Test
    fun `getStringSet returns null from internal preferences`() {
        val testKey = "Test string set key"
        val testValue = setOf("Test value 1", "Test value 2")
        val testDefault = null
        whenever(mockSharedPreferences.getStringSet(testKey, testDefault)).thenReturn(testDefault)

        runBlocking { assertNull(preferoutines.getStringSet(testKey, testDefault)) }
    }

    @Test
    fun `getInt returns value from internal preferences`() {
        val testKey = "Test int key"
        val testValue = 2332
        val testDefault = -987
        whenever(mockSharedPreferences.getInt(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getInt(testKey, testDefault)) }
    }

    @Test
    fun `getLong returns value from internal preferences`() {
        val testKey = "Test long key"
        val testValue = 342342342343L
        val testDefault = -38948985934859L
        whenever(mockSharedPreferences.getLong(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getLong(testKey, testDefault)) }
    }

    @Test
    fun `getFloat returns value from internal preferences`() {
        val testKey = "Test float key"
        val testValue = 234.432f
        val testDefault = -987.654f
        whenever(mockSharedPreferences.getFloat(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getFloat(testKey, testDefault)) }
    }

    @Test
    fun `getBoolean returns value from internal preferences`() {
        val testKey = "Test boolean key"
        val testValue = true
        val testDefault = false
        whenever(mockSharedPreferences.getBoolean(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, preferoutines.getBoolean(testKey, testDefault)) }
    }
    //endregion
}
