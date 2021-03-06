package drewhamilton.preferoutines

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import drewhamilton.preferoutines.test.BasePreferoutinesTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

class PreferoutinesTest : BasePreferoutinesTest() {

    //region Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getAllFlow emits current value on collect`() {
        val testValue = mutableMapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getAllFlow emits on listener update`() {
        val testValue = mutableMapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getAllFlow unregisters listener on cancel`() {
        val testValue = mutableMapOf(Pair("Key 1", "Value 1"), Pair("Key 2", 3), Pair("Key 3", null))
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getString, SharedPreferences::getStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringSetFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringSetFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getStringSetFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getStringSet, SharedPreferences::getStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getIntFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getIntFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getIntFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getInt, SharedPreferences::getIntFlow,
            testValue = 123, testDefault = -321
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getLongFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getLongFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getLongFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getLong, SharedPreferences::getLongFlow,
            testValue = 12345678900, testDefault = -9876543210
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getFloatFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getFloatFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getFloatFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getFloat, SharedPreferences::getFloatFlow,
            testValue = 123.456f, testDefault = -321.987f
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getBooleanFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getBooleanFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getBooleanFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getBoolean, SharedPreferences::getBooleanFlow,
            testValue = true, testDefault = false
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getContainsFlow emits current value on collect`() {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getContainsFlow emits on listener update`() {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `getContainsFlow unregisters listener on cancel`() {
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
    //endregion
}
