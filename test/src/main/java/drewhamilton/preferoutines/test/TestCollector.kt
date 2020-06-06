package drewhamilton.preferoutines.test

import kotlinx.coroutines.Deferred

class TestCollector<T> {
    val values = mutableListOf<T>()
    lateinit var deferred: Deferred<Unit>

    fun assert(assertions: Assert.() -> Unit) {
        // TODO WORKAROUND: Short sleep avoids race condition where values have not yet propagated at time of assertion
        Thread.sleep(2)
        Assert().assertions()
    }

    inner class Assert internal constructor() {

        fun valueCount(count: Int) {
            assertEquals(count, values.size)
        }

        fun values(vararg expected: T) {
            assertEquals(expected.size, values.size)
            for (i in expected.indices) {
                assertEquals(expected[i], values[i])
            }
        }

        private fun assertEquals(expected: Any?, actual: Any?) {
            org.junit.Assert.assertEquals(expected, actual)
        }

    }
}
