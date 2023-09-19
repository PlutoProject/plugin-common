package club.plutomc.plutoproject.messaging.api

object MessagingApiProvider {

    private var _messenger: Messenger? = null

    /**
     * 对应平台的消息发送器实例。
     */
    var messenger: Messenger
        get() {
            return checkNotNull(_messenger)
        }
        set(value) {
            if (_messenger != null) {
                return
            }

            _messenger = value
        }

}