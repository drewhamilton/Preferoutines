package drewhamilton.preferoutines.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.newSingleThreadContext
import org.junit.After
import org.junit.Before
import kotlin.coroutines.CoroutineContext

abstract class CoroutineTest {

    private lateinit var testContext: CoroutineContext
    protected lateinit var testScope: CoroutineScope
        private set

    private val testCollectors: MutableCollection<TestCollector<*>> = mutableSetOf()

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @Before
    fun setUpTestScope() {
        testContext = newSingleThreadContext("Test context")
        testScope = CoroutineScope(testContext)
    }

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @After
    fun cancelAllJobs() {
        for (testCollector in testCollectors) {
            testCollector.deferred.cancel()
        }
        testContext.cancelChildren()
        testContext.cancel()
    }

    @FlowPreview
    @UseExperimental(ObsoleteCoroutinesApi::class)
    protected fun <T> Flow<T>.test(): TestCollector<T> {
        val testCollector = TestCollector<T>()
        testCollector.deferred = testScope.async(testContext) {
            collect { value ->
                testCollector.values.add(value)
            }
        }
        testCollectors.add(testCollector)
        return testCollector
    }
}
