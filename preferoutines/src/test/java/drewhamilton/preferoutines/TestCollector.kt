package drewhamilton.preferoutines

import kotlinx.coroutines.Deferred

class TestCollector<T> {
    val values = mutableListOf<T>()
    lateinit var deferred: Deferred<Unit>
}
