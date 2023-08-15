package club.plutomc.plutoproject.common.profile.impl

import club.plutomc.plutoproject.common.profile.api.Profile
import dev.morphia.annotations.*
import org.bson.types.ObjectId
import java.util.*

@Entity("profile_profiles")
class BasicProfile constructor() : Profile {

    @Id
    private var id: ObjectId? = null

    @Property("uuid")
    private var _uuid: UUID

    @Indexed(options = IndexOptions(name = "name_index"))
    @Property("name")
    private var _name = "<undefined>"

    @Property("created_time")
    private var _createdTime = System.currentTimeMillis() / 1000

    @Property("properties")
    private var _properties: HashMap<String, Any> = hashMapOf()

    constructor(uuid: UUID) : this() {
        this._uuid = uuid
    }

    init {
        this._uuid = UUID.randomUUID()
    }

    override val uuid: UUID
        get() = _uuid
    override var name: String
        get() = _name
        set(value) {
            _name = value
        }
    override val createdTime: Long
        get() = _createdTime
    override val properties: MutableMap<String, Any>
        get() = _properties

    override fun isInitialized(): Boolean = name != "<undefined>"

}