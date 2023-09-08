package club.plutomc.plutoproject.apiutils.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.launchWithPluto(block: suspend CoroutineScope.() -> Unit): Job = launch(coroutineContext + PlutoDispatcher()) { block() }