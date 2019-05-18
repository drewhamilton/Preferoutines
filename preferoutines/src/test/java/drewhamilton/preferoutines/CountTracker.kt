package drewhamilton.preferoutines

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class CountTracker {
    @Volatile var count = 0
}

@FlowPreview
suspend fun <T> Flow<T>.trackCount(countTracker: CountTracker) = collect {
    ++countTracker.count
}
