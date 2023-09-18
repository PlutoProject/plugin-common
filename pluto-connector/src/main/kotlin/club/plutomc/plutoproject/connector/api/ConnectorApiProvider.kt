package club.plutomc.plutoproject.connector.api

object ConnectorApiProvider {

    private var _connectionManager: ConnectionManager? = null
    var connectionManager: ConnectionManager
        get() {
            return checkNotNull(_connectionManager)
        }
        set(value) {
            if (_connectionManager != null) {
                return
            }

            _connectionManager = value
        }

}