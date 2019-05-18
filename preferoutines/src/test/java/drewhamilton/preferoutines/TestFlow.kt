package drewhamilton.preferoutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
val testContext: CoroutineContext = newSingleThreadContext("Test context")

@UseExperimental(ObsoleteCoroutinesApi::class)
private val testScope: CoroutineScope = CoroutineScope(testContext)

@UseExperimental(ObsoleteCoroutinesApi::class)
@FlowPreview
fun <T> Flow<T>.test(): TestCollector<T> {
    val testCollector = TestCollector<T>()
    testCollector.deferred = testScope.async(testContext) {
        collect {
            testCollector.values.add(it)
        }
    }
    return testCollector
}

class TestCollector<T> {
    val values = mutableListOf<T>()
    lateinit var deferred: Deferred<Unit>
}
