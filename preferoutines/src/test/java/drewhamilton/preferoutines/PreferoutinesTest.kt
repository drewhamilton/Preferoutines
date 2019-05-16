package drewhamilton.preferoutines

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        whenever(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
        whenever(mockSharedPreferencesEditor.commit()).thenReturn(true)
    }

    //region All preferences
    @Test
    fun `getAll returns map from internal preferences`() {
        val testMap = mapOf(Pair("Made up map key", 23498))
        whenever(mockSharedPreferences.all).thenReturn(testMap)

        val deferredResult = GlobalScope.async { preferoutines.getAll() }
        assertFalse(deferredResult.isCompleted)
        assertEquals(testMap, runBlocking { deferredResult.await() })
    }
    //endregion
}
