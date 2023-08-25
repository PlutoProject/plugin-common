package club.plutomc.plutoproject.messaging.api

object MessagingApiProvider {

    private var _manager: MessageManager? = null
    var manager: MessageManager
        get() = checkNotNull(_manager)
        set(value) {
            if (_manager != null) {
                return
            }

            _manager = value
        }

}