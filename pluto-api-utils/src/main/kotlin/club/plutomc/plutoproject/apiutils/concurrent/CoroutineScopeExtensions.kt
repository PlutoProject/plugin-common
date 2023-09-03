package club.plutomc.plutoproject.apiutils.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.launchWithPluto(block: CoroutineScope.() -> Unit): Job = launch(PlutoDispatcher()) { block() }