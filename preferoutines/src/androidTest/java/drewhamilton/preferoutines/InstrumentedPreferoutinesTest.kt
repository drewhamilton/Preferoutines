package drewhamilton.preferoutines

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstrumentedPreferoutinesTest {

    private val sharedPreferences: SharedPreferences =
        InstrumentationRegistry.getInstrumentation().context.getSharedPreferences(
            "InstrumentedPreferoutinesTest",
            Context.MODE_PRIVATE
        )

    private val testCollectors: MutableCollection<TestCollector<*>> = mutableSetOf()
    private var deferred: Deferred<Unit>? = null

    @After fun clearTestPreferences() {
        val cleared = sharedPreferences.edit().clear().commit()
        if (!cleared) fail("Failed to clear preferences after test")
    }

    @After fun cancelCoroutines() {
        testCollectors.forEach { testCollector ->
            testCollector.deferred.cancel()
        }
        testCollectors.clear()
    }

    @Test fun getStringFlow_emitsCurrentValueOnCollect() {
        val testKey = "Test string"
        sharedPreferences.edit()
            .putString(testKey, "Value")
            .commit()

        val testCollector = sharedPreferences.getStringFlow(testKey, null).test()
        testCollector.assert {
            valueCount(1)
            values("Value")
        }
    }

    @Test fun getStringFlow_emitsOnListenerUpdate() = runBlockingTest {
        val testKey = "Test string"
        sharedPreferences.edit()
            .putString(testKey, "Value")
            .commit()

        val testCollector = sharedPreferences.getStringFlow(testKey, null).test()

        testCollector.assert {
            valueCount(1)
        }

        sharedPreferences.edit()
            .putString(testKey, "Another")
            .commit()

        testCollector.assert {
            valueCount(2)
            values("Value", "Another")
        }
    }

    @Test fun getStringFlow_unregistersListenerOnCancel() {
        val testKey = "Test string"
        sharedPreferences.edit()
            .putString(testKey, "Value")
            .commit()

        val testCollector = sharedPreferences.getStringFlow(testKey, null).test()

        testCollector.assert {
            valueCount(1)
        }

        testCollector.deferred.cancel()

        sharedPreferences.edit()
            .putString(testKey, "Another")
            .commit()

        testCollector.assert {
            valueCount(1)
        }
    }

    private fun <T> Flow<T>.test(): TestCollector<T> {
        val testCollector = TestCollector<T>()
        testCollector.deferred = TestCoroutineScope().async {
            collect { testCollector.values.add(it) }
        }
        testCollectors.add(testCollector)
        return testCollector
    }

    private class TestCollector<T> {
        val values = mutableListOf<T>()

        lateinit var deferred: Deferred<Unit>

        fun assert(assertions: Assert.() -> Unit) {
            Assert().assertions()
        }

        inner class Assert internal constructor() {

            fun valueCount(count: Int) {
                assertEquals(count, values.size)
            }

            fun values(vararg expected: T) {
                assertEquals(expected.size, values.size)
                for (i in 0 until expected.size) {
                    assertEquals(expected[i], values[i])
                }
            }

            private fun assertEquals(expected: Any?, actual: Any?) {
                org.junit.Assert.assertEquals(expected, actual)
            }

        }
    }
}
