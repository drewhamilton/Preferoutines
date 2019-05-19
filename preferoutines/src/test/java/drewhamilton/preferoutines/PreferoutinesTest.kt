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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PreferoutinesTest : FlowTest() {

    @Mock private lateinit var mockSharedPreferences: SharedPreferences
    @Mock private lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    //region Suspend
    @Test
    fun `awaitAll returns map from internal preferences`() {
        val testMap = mapOf(Pair("Made up map key", 23498))
        whenever(mockSharedPreferences.all).thenReturn(testMap)

        runBlocking { assertEquals(testMap, mockSharedPreferences.awaitAll()) }
    }

    @Test
    fun `awaitString returns value from internal preferences`() {
        val testKey = "Test string key"
        val testValue = "Test value"
        val testDefault = "Test default"
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitString(testKey, testDefault)) }
    }

    @Test
    fun `awaitString returns null from internal preferences`() {
        val testKey = "Test string key"
        val testDefault = null
        whenever(mockSharedPreferences.getString(testKey, testDefault)).thenReturn(testDefault)

        runBlocking { assertNull(mockSharedPreferences.awaitString(testKey, testDefault)) }
    }

    @Test
    fun `awaitStringSet returns value from internal preferences`() {
        val testKey = "Test string set key"
        val testValue = setOf("Test value 1", "Test value 2")
        val testDefault = setOf("Test default 1")
        whenever(mockSharedPreferences.getStringSet(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitStringSet(testKey, testDefault)) }
    }

    @Test
    fun `awaitStringSet returns null from internal preferences`() {
        val testKey = "Test string set key"
        val testDefault = null
        whenever(mockSharedPreferences.getStringSet(testKey, testDefault)).thenReturn(testDefault)

        runBlocking { assertNull(mockSharedPreferences.awaitStringSet(testKey, testDefault)) }
    }

    @Test
    fun `awaitInt returns value from internal preferences`() {
        val testKey = "Test int key"
        val testValue = 2332
        val testDefault = -987
        whenever(mockSharedPreferences.getInt(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitInt(testKey, testDefault)) }
    }

    @Test
    fun `awaitLong returns value from internal preferences`() {
        val testKey = "Test long key"
        val testValue = 342342342343L
        val testDefault = -38948985934859L
        whenever(mockSharedPreferences.getLong(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitLong(testKey, testDefault)) }
    }

    @Test
    fun `awaitFloat returns value from internal preferences`() {
        val testKey = "Test float key"
        val testValue = 234.432f
        val testDefault = -987.654f
        whenever(mockSharedPreferences.getFloat(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitFloat(testKey, testDefault)) }
    }

    @Test
    fun `awaitBoolean returns value from internal preferences`() {
        val testKey = "Test boolean key"
        val testValue = true
        val testDefault = false
        whenever(mockSharedPreferences.getBoolean(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitBoolean(testKey, testDefault)) }
    }

    @Test
    fun `awaitContains returns value from internal preferences`() {
        val testKey = "Test key"
        val testValue = true
        whenever(mockSharedPreferences.contains(testKey)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitContains(testKey)) }
    }
    //endregion

    //region Flow
    @FlowPreview
    @Test
    fun `getAllFlow emits current value on collect`() {
        val testValue = mapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
        whenever(mockSharedPreferences.all).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getAllFlow().test()

        // Verify method call with timeout to allow flow initialization to complete:
        verify(mockSharedPreferences, timeout(500)).registerOnSharedPreferenceChangeListener(any())
        verify(mockSharedPreferences).all

        testCollector.assert {
            valueCount(1)
            values(testValue)
        }
    }

    @FlowPreview
    @Test
    fun `getAllFlow emits on listener update`() {
        val testValue = mapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
        whenever(mockSharedPreferences.all).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getAllFlow().test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).all

        testCollector.assert { valueCount(1) }

        listenerCaptor.lastValue.onSharedPreferenceChanged(mockSharedPreferences, "Any key")

        verify(mockSharedPreferences, timeout(100).times(2)).all
        testCollector.assert { valueCount(2) }
    }

    @FlowPreview
    @Test
    fun `getAllFlow unregisters listener on cancel`() {
        val testValue = mapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
        whenever(mockSharedPreferences.all).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getAllFlow().test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).all
        verify(mockSharedPreferences, never()).unregisterOnSharedPreferenceChangeListener(any())

        testCollector.assert { valueCount(1) }
        testCollector.deferred.cancel()

        verify(mockSharedPreferences, timeout(100)).unregisterOnSharedPreferenceChangeListener(listenerCaptor.lastValue)
    }

    @FlowPreview
    @Test
    fun `getStringFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getString,
            SharedPreferences::getStringFlow,
            "Test value",
            "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getString,
            SharedPreferences::getStringFlow,
            "Test value",
            "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getString,
            SharedPreferences::getStringFlow,
            "Test value",
            "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getStringSet,
            SharedPreferences::getStringSetFlow,
            setOf("Test value 1", "Test value 2"),
            setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getStringSet,
            SharedPreferences::getStringSetFlow,
            setOf("Test value 1", "Test value 2"),
            setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getStringSet,
            SharedPreferences::getStringSetFlow,
            setOf("Test value 1", "Test value 2"),
            setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getInt,
            SharedPreferences::getIntFlow,
            123,
            -321
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getInt,
            SharedPreferences::getIntFlow,
            123,
            -321
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getInt,
            SharedPreferences::getIntFlow,
            123,
            -321
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getLong,
            SharedPreferences::getLongFlow,
            12345678900,
            -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getLong,
            SharedPreferences::getLongFlow,
            12345678900,
            -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getLong,
            SharedPreferences::getLongFlow,
            12345678900,
            -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getFloat,
            SharedPreferences::getFloatFlow,
            123.456f,
            -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getFloat,
            SharedPreferences::getFloatFlow,
            123.456f,
            -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getFloat,
            SharedPreferences::getFloatFlow,
            123.456f,
            -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getBoolean,
            SharedPreferences::getBooleanFlow,
            testValue = true,
            testDefault = false
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getBoolean,
            SharedPreferences::getBooleanFlow,
            testValue = true,
            testDefault = false
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getBoolean,
            SharedPreferences::getBooleanFlow,
            testValue = true,
            testDefault = false
        )
    }

    @FlowPreview
    @Test
    fun `getContainsFlow emits current value on collect`() {
        val testKey = "Test key"
        val testValue = true
        whenever(mockSharedPreferences.contains(testKey)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getContainsFlow(testKey).test()

        // Verify method call with timeout to allow flow initialization to complete:
        verify(mockSharedPreferences, timeout(500)).registerOnSharedPreferenceChangeListener(any())
        verify(mockSharedPreferences).contains(testKey)

        testCollector.assert {
            valueCount(1)
            values(testValue)
        }
    }

    @FlowPreview
    @Test
    fun `getContainsFlow emits on listener update`() {
        val testKey = "Test key"
        val testValue = true
        whenever(mockSharedPreferences.contains(testKey)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getContainsFlow(testKey).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).contains(testKey)

        testCollector.assert { valueCount(1) }

        listenerCaptor.lastValue.onSharedPreferenceChanged(mockSharedPreferences, testKey)

        verify(mockSharedPreferences, timeout(100).times(2)).contains(testKey)
        testCollector.assert { valueCount(2) }
    }

    @FlowPreview
    @Test
    fun `getContainsFlow unregisters listener on cancel`() {
        val testKey = "Test key"
        val testValue = true
        whenever(mockSharedPreferences.contains(testKey)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getContainsFlow(testKey).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).contains(testKey)
        verify(mockSharedPreferences, never()).unregisterOnSharedPreferenceChangeListener(any())

        testCollector.assert { valueCount(1) }
        testCollector.deferred.cancel()

        verify(mockSharedPreferences, timeout(100)).unregisterOnSharedPreferenceChangeListener(listenerCaptor.lastValue)
    }

    @FlowPreview
    private fun <T> testGetPreferenceFlow_emitsCurrentValueOnCollect(
        getPreference: SharedPreferences.(String, T) -> T,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        // Verify method call with timeout to allow flow initialization to complete:
        verify(mockSharedPreferences, timeout(500)).registerOnSharedPreferenceChangeListener(any())
        verify(mockSharedPreferences).getPreference(testKey, testDefault)

        testCollector.assert {
            valueCount(1)
            values(testValue)
        }
    }

    @FlowPreview
    private fun <T> testGetPreferenceFlow_emitsOnListenerUpdate(
        getPreference: SharedPreferences.(String, T) -> T,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getPreference(testKey, testDefault)

        testCollector.assert { valueCount(1) }

        listenerCaptor.lastValue.onSharedPreferenceChanged(mockSharedPreferences, testKey)

        verify(mockSharedPreferences, timeout(100).times(2)).getPreference(testKey, testDefault)
        testCollector.assert { valueCount(2) }
    }

    @FlowPreview
    private inline fun <T> testGetPreferenceFlow_unregistersListenerOnCancel(
        getPreference: SharedPreferences.(String, T) -> T,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault)).thenReturn(testValue)

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getPreference(testKey, testDefault)
        verify(mockSharedPreferences, never()).unregisterOnSharedPreferenceChangeListener(any())

        testCollector.assert { valueCount(1) }
        testCollector.deferred.cancel()

        verify(mockSharedPreferences, timeout(100)).unregisterOnSharedPreferenceChangeListener(listenerCaptor.lastValue)
    }
    //endregion

    //region Edit
    @Test
    fun `awaitCommit returns value from commit`() {
        whenever(mockSharedPreferencesEditor.commit()).thenReturn(true)

        runBlocking { assertTrue(mockSharedPreferencesEditor.awaitCommit()) }
    }
    //endregion
}
