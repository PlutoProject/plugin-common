package club.plutomc.plutoproject.apiutils.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchWithPluto(block: CoroutineScope.() -> Unit) {
    launch(PlutoDispatcher()) {
        block()
    }
}

object CoroutineScopeExtensions {

}