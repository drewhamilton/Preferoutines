package drewhamilton.preferoutines

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

abstract class FlowTest {

    private lateinit var testContext: CoroutineContext
    private lateinit var testScope: CoroutineScope

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @Before
    fun setUpTestScope() {
        testContext = newSingleThreadContext("Test context")
        testScope = CoroutineScope(testContext)
    }

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @After
    fun tearDownTestContext() {
        testContext.cancelChildren()
        testContext.cancel()
    }

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @FlowPreview
    protected fun <T> Flow<T>.test(): TestCollector<T> {
        val testCollector = TestCollector<T>()
        testCollector.deferred = testScope.async(testContext) {
            collect {
                testCollector.values.add(it)
            }
        }
        return testCollector
    }

}
