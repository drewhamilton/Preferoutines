package drewhamilton.preferoutines.test

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
import org.junit.Assert
import org.mockito.Mock

@Suppress("FunctionName")
abstract class BasePreferoutinesTest : FlowTest() {

    @Mock protected lateinit var mockSharedPreferences: SharedPreferences
    @Mock protected lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    //region Suspend
    protected fun <T> testAwaitPreference_returnsCorrespondingPreference(
        getPreference: SharedPreferences.(String, T) -> T,
        awaitPreference: suspend SharedPreferences.(String, T) -> T,
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault)).thenReturn(testValue)

        runBlocking { Assert.assertEquals(testValue, mockSharedPreferences.awaitPreference(testKey, testDefault)) }
    }
    //endregion

    @FlowPreview
    protected fun <T> testGetPreferenceFlow_emitsCurrentValueOnCollect(
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
    protected fun <T> testGetPreferenceFlow_emitsOnListenerUpdate(
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
    protected fun <T> testGetPreferenceFlow_unregistersListenerOnCancel(
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
}
