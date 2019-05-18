package drewhamilton.preferoutines

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.FlowPreview
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
class PreferoutinesTest : FlowTest() {

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

    //region Flow functions
    @FlowPreview
    @Test
    fun `getStringFlow emits current value on collect`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = "Test default"
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testValue)

        val testCollector = preferoutines.getStringFlow(testKey, testDefault).test()

        // Verify method call with timeout to allow flow initialization to complete:
        verify(mockSharedPreferences, timeout(500)).registerOnSharedPreferenceChangeListener(any())
        verify(mockSharedPreferences).getString(testKey, testDefault)

        testCollector.assert {
            valueCount(1)
            values(testValue)
        }
    }

    @FlowPreview
    @Test
    fun `getStringFlow emits on listener update`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = "Test default"
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testValue)

        val testCollector = preferoutines.getStringFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getString(testKey, testDefault)

        testCollector.assert { valueCount(1) }

        listenerCaptor.lastValue.onSharedPreferenceChanged(mockSharedPreferences, testKey)

        verify(mockSharedPreferences, timeout(100).times(2)).getString(testKey, testDefault)
        testCollector.assert { valueCount(2) }
    }

    @FlowPreview
    @Test
    fun `getStringFlow unregisters listener on cancel`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = "Test default"
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testValue)

        val testCollector = preferoutines.getStringFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getString(testKey, testDefault)
        verify(mockSharedPreferences, never()).unregisterOnSharedPreferenceChangeListener(any())

        testCollector.assert { valueCount(1) }
        testCollector.deferred.cancel()

        verify(mockSharedPreferences, timeout(100)).unregisterOnSharedPreferenceChangeListener(listenerCaptor.lastValue)
    }
    //endregion
}
