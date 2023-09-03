package club.plutomc.plutoproject.apiutils.concurrent

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

@Suppress("FunctionName")
fun PlutoDispatcher() = CoroutineHelpers.plutoDispatcher

internal object CoroutineHelpers {

    val plutoDispatcher = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() / 2, ThreadFactoryBuilder()
            .setNameFormat("Pluto Coroutine Dispatcher Thread - %s")
            .setPriority(Thread.MAX_PRIORITY)
            .build()
    ).asCoroutineDispatcher()

}