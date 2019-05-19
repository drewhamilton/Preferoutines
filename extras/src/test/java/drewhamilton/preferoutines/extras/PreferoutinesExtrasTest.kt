package drewhamilton.preferoutines.extras

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PreferoutinesExtrasTest {

    @Mock private lateinit var mockSharedPreferences: SharedPreferences

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
}
