package drewhamilton.preferoutines.extras

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import drewhamilton.preferoutines.extras.test.TestEnum
import drewhamilton.preferoutines.test.BasePreferoutinesTest
import kotlinx.coroutines.FlowPreview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class PreferoutinesExtrasTest : BasePreferoutinesTest() {

    @Before
    fun setUp() {
        setUpMockEditor()
        whenever(mockSharedPreferencesEditor.putInt(any(), any())).thenReturn(mockSharedPreferencesEditor)
    }

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

    //region Flow
    @FlowPreview
    @Test fun `getNonNullStringFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getString, SharedPreferences::getNonNullStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test fun `getNonNullStringFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getString, SharedPreferences::getNonNullStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test fun `getNonNullStringFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getString, SharedPreferences::getNonNullStringFlow,
            testValue = "Test value", testDefault = "Test default"
        )
    }

    @FlowPreview
    @Test fun `getNonNullStringSetFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getStringSet, SharedPreferences::getNonNullStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test fun `getNonNullStringSetFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getStringSet, SharedPreferences::getNonNullStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test fun `getNonNullStringSetFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getStringSet, SharedPreferences::getNonNullStringSetFlow,
            testValue = setOf("Test value 1", "Test value 2"), testDefault = setOf("Test default 1", "Test default 2")
        )
    }

    @FlowPreview
    @Test fun `getEnumFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect<TestEnum?, String?>(
            SharedPreferences::getString, { key, defaultValue -> getEnumFlow(key, defaultValue) },
            asPreferenceValue = { this?.name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }

    @FlowPreview
    @Test fun `getEnumFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate<TestEnum?, String?>(
            SharedPreferences::getString, { key, defaultValue -> getEnumFlow(key, defaultValue) },
            asPreferenceValue = { this?.name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }

    @FlowPreview
    @Test fun `getEnumFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel<TestEnum?, String?>(
            SharedPreferences::getString, { key, defaultValue -> getEnumFlow(key, defaultValue) },
            asPreferenceValue = { this?.name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }

    @FlowPreview
    @Test fun `getNonNullEnumFlow emits current value on collect`() {
        testGetPreferenceFlow_emitsCurrentValueOnCollect(
            SharedPreferences::getString, SharedPreferences::getNonNullEnumFlow,
            asPreferenceValue = { name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }

    @FlowPreview
    @Test fun `getNonNullEnumFlow emits on listener update`() {
        testGetPreferenceFlow_emitsOnListenerUpdate(
            SharedPreferences::getString, SharedPreferences::getNonNullEnumFlow,
            asPreferenceValue = { name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }

    @FlowPreview
    @Test fun `getNonNullEnumFlow unregisters listener on cancel`() {
        testGetPreferenceFlow_unregistersListenerOnCancel(
            SharedPreferences::getString, SharedPreferences::getNonNullEnumFlow,
            asPreferenceValue = { name },
            testValue = TestEnum.A, testDefault = TestEnum.C
        )
    }
    //endregion

    //region Edit
    @Test fun `putEnum saves enum name as string preference`() {
        val testKey = "Test enum key"
        val testValue = TestEnum.A
        whenever(mockSharedPreferencesEditor.putString(testKey, testValue.name))
            .thenReturn(mockSharedPreferencesEditor)

        val editor = mockSharedPreferencesEditor.putEnum(testKey, testValue)
        verify(mockSharedPreferencesEditor).putString(testKey, testValue.name)
        verifyNoMoreInteractions(mockSharedPreferencesEditor)

        assertSame(mockSharedPreferencesEditor, editor)
    }

    @Test fun `putEnum given null saves null as string preference`() {
        val testKey = "Test enum key"
        val testValue: TestEnum? = null
        whenever(mockSharedPreferencesEditor.putString(testKey, null))
            .thenReturn(mockSharedPreferencesEditor)

        val editor = mockSharedPreferencesEditor.putEnum(testKey, testValue)
        verify(mockSharedPreferencesEditor).putString(testKey, testValue?.name)
        verifyNoMoreInteractions(mockSharedPreferencesEditor)

        assertSame(mockSharedPreferencesEditor, editor)
    }
    //endregion
}
