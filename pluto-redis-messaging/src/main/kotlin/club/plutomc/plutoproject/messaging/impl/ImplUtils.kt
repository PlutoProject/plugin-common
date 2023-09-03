package club.plutomc.plutoproject.messaging.impl

internal object ImplUtils {

    fun throwClosedException() {
        throw RuntimeException("MessageManager already isClosed!")
    }

}