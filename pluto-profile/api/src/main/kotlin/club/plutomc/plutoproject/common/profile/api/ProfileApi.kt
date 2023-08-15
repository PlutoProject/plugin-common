package club.plutomc.plutoproject.common.profile.api

object ProfileApi {

    private var _managerProvider: ProfileManagerProvider? = null

    var managerProvider: ProfileManagerProvider
        get() {
            return checkNotNull(_managerProvider)
        }
        set(value) {
            if (_managerProvider != null) {
                return
            }

            _managerProvider = value
        }

}