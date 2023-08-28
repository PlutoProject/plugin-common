package club.plutomc.plutoproject.messaging.impl

import java.util.logging.Logger

internal object ImplUtils {

    private val logger = Logger.getLogger("connector-internal")

    private fun isDebugLogEnabled(): Boolean {
        if (!System.getProperties().contains("connectorDebugLog")) {
            return false
        }

        if (!System.getProperty("connectorDebugLog").toBoolean()) {
            return false
        }

        return true
    }

    fun debugLogInfo(message: String) {
        if (!isDebugLogEnabled()) {
            return
        }

        logger.info(message)
    }

    fun debugLogWarn(message: String) {
        if (!isDebugLogEnabled()) {
            return
        }

        logger.warning(message)
    }

    fun debugLogError(message: String) {
        if (!isDebugLogEnabled()) {
            return
        }

        logger.severe(message)
    }

}