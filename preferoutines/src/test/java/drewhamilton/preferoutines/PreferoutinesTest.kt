package drewhamilton.preferoutines

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import drewhamilton.preferoutines.test.FlowTest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
    fun `awaitAll returns map from getAll`() {
        val testMap = mapOf(Pair("Made up map key", 23498))
        whenever(mockSharedPreferences.all).thenReturn(testMap)

        runBlocking { assertEquals(testMap, mockSharedPreferences.awaitAll()) }
    }

    @Test
    fun `awaitString returns value from getString`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getString, SharedPreferences::awaitString,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @Test
    fun `awaitString returns null from getString`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getString, SharedPreferences::awaitString,
            testValue = null, testDefault = null
        )
    }

    @Test
    fun `awaitStringSet returns value from getStringSet`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getStringSet, SharedPreferences::awaitStringSet,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1")
        )
    }

    @Test
    fun `awaitStringSet returns null from getStringSet`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getStringSet, SharedPreferences::awaitStringSet,
            testValue = null, testDefault = null
        )
    }

    @Test
    fun `awaitInt returns value from getInt`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getInt, SharedPreferences::awaitInt,
            testValue = 2332, testDefault = -987
        )
    }

    @Test
    fun `awaitLong returns value from getLong`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getLong, SharedPreferences::awaitLong,
            testValue = 342342342343L, testDefault = -38948985934859L
        )
    }

    @Test
    fun `awaitFloat returns value from getFloat`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getFloat, SharedPreferences::awaitFloat,
            testValue = 234.432f, testDefault = -987.654f
        )
    }

    @Test
    fun `awaitBoolean returns value from getBoolean`() {
        testAwaitPreference_returnsCorrespondingPreference(
            SharedPreferences::getBoolean, SharedPreferences::awaitBoolean,
            testValue = true, testDefault = false
        )
    }

    @Test
    fun `awaitContains returns value from contains`() {
        val testKey = "Test key"
        val testValue = true
        whenever(mockSharedPreferences.contains(testKey)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitContains(testKey)) }
    }

    private fun <T> testAwaitPreference_returnsCorrespondingPreference(
        getPreference: SharedPreferences.(String, T) -> T,
        awaitPreference: suspend SharedPreferences.(String, T) -> T,
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault)).thenReturn(testValue)

        runBlocking { assertEquals(testValue, mockSharedPreferences.awaitPreference(testKey, testDefault)) }
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
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getStringSetFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @FlowPreview
    @Test
    fun `getIntFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getLongFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getFloatFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
        )
    }

    @FlowPreview
    @Test
    fun `getBooleanFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
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
