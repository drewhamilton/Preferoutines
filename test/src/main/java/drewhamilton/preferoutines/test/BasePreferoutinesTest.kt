package drewhamilton.preferoutines.test

import android.annotation.SuppressLint
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
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@Suppress("FunctionName")
@RunWith(MockitoJUnitRunner::class)
abstract class BasePreferoutinesTest : FlowTest() {

    @Mock protected lateinit var mockSharedPreferences: SharedPreferences
    @Mock protected lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    @SuppressLint("CommitPrefEdits")
    protected fun setUpMockEditor() {
        whenever(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
        whenever(mockSharedPreferencesEditor.commit()).thenReturn(true)
    }

    @FlowPreview
    protected fun <T, P> testGetPreferenceFlow_emitsCurrentValueOnCollect(
        getPreference: SharedPreferences.(String, P) -> P,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        asPreferenceValue: T.() -> P = @Suppress("UNCHECKED_CAST") { this as P },
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault.asPreferenceValue()))
            .thenReturn(testValue.asPreferenceValue())

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        // Verify method call with timeout to allow flow initialization to complete:
        verify(mockSharedPreferences, timeout(500)).registerOnSharedPreferenceChangeListener(any())
        verify(mockSharedPreferences).getPreference(testKey, testDefault.asPreferenceValue())

        testCollector.assert {
            valueCount(1)
            values(testValue)
        }
    }

    @FlowPreview
    protected fun <T, P> testGetPreferenceFlow_emitsOnListenerUpdate(
        getPreference: SharedPreferences.(String, P) -> P,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        asPreferenceValue: T.() -> P = @Suppress("UNCHECKED_CAST") { this as P },
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault.asPreferenceValue()))
            .thenReturn(testValue.asPreferenceValue())

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getPreference(testKey, testDefault.asPreferenceValue())

        testCollector.assert { valueCount(1) }

        listenerCaptor.lastValue.onSharedPreferenceChanged(mockSharedPreferences, testKey)

        verify(mockSharedPreferences, timeout(100).times(2)).getPreference(testKey, testDefault.asPreferenceValue())
        testCollector.assert { valueCount(2) }
    }

    @FlowPreview
    protected fun <T, P> testGetPreferenceFlow_unregistersListenerOnCancel(
        getPreference: SharedPreferences.(String, P) -> P,
        getPreferenceFlow: SharedPreferences.(String, T) -> Flow<T>,
        asPreferenceValue: T.() -> P = @Suppress("UNCHECKED_CAST") { this as P },
        testValue: T,
        testDefault: T,
        testKey: String = "Test key"
    ) {
        whenever(mockSharedPreferences.getPreference(testKey, testDefault.asPreferenceValue()))
            .thenReturn(testValue.asPreferenceValue())

        val testCollector = mockSharedPreferences.getPreferenceFlow(testKey, testDefault).test()

        val listenerCaptor: KArgumentCaptor<SharedPreferences.OnSharedPreferenceChangeListener> = argumentCaptor()
        verify(mockSharedPreferences, timeout(100)).registerOnSharedPreferenceChangeListener(listenerCaptor.capture())
        verify(mockSharedPreferences).getPreference(testKey, testDefault.asPreferenceValue())
        verify(mockSharedPreferences, never()).unregisterOnSharedPreferenceChangeListener(any())

        testCollector.assert { valueCount(1) }
        testCollector.deferred.cancel()

        verify(mockSharedPreferences, timeout(100)).unregisterOnSharedPreferenceChangeListener(listenerCaptor.lastValue)
    }
}
