package club.plutomc.plutoproject.common.connector.api

object ConnectorApiProvider {

    private var _connector: Connector? = null
    var connector: Connector
        get() {
            return checkNotNull(_connector)
        }
        set(value) {
            if  (_connector != null) {
                return
            }

            _connector = value
        }

}