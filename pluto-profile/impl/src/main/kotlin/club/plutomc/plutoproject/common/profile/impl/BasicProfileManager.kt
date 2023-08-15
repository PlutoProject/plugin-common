package club.plutomc.plutoproject.common.profile.impl

import club.plutomc.plutoproject.common.profile.api.Profile
import club.plutomc.plutoproject.common.profile.api.ProfileManager
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import dev.morphia.Datastore
import dev.morphia.Morphia
import dev.morphia.query.filters.Filters
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.ClassModel
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.*

class BasicProfileManager : ProfileManager {

    companion object {
        private fun createMongo(
            address: String,
            port: Int,
            username: String,
            database: String,
            password: String
        ): MongoClient {
            val connectionString = ConnectionString("mongodb://$address:$port")

            val credentials = MongoCredential.createCredential(
                username,
                database,
                password.toCharArray()
            )

            val profileClassModel = ClassModel.builder(Profile::class.java).enableDiscriminator(true).build()
            val basicProfileClassModel = ClassModel.builder(BasicProfile::class.java).enableDiscriminator(true).build()
            val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                    PojoCodecProvider.builder()
                        .register(profileClassModel, basicProfileClassModel)
                        .automatic(true)
                        .build()
                )
            )

            val settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecRegistry)
                .applyConnectionString(connectionString)
                .credential(credentials)
                .build()

            return MongoClients.create(settings)
        }
    }

    private val mongo: MongoClient
    private val datastore: Datastore
    private val prefix: String
    private val database: MongoDatabase
    private val cache: Cache<UUID, Profile>

    constructor(mongoClient: MongoClient, database: String, collectionPrefix: String) {
        this.mongo = mongoClient
        this.database = mongo.getDatabase(database)
        this.datastore = Morphia.createDatastore(mongo, database)
        this.datastore.mapper.map(BasicProfile::class.java)
        this.cache = Caffeine.newBuilder()
            .maximumSize(10000L)
            .build()
        this.prefix = collectionPrefix
    }

    constructor(
        address: String,
        port: Int,
        username: String,
        database: String,
        password: String,
        collectionPrefix: String
    ) : this(createMongo(address, port, username, database, password), database, collectionPrefix)

    override fun get(uuid: UUID): Profile {
        if (cache.getIfPresent(uuid) != null) {
            return cache.getIfPresent(uuid)!!
        }

        if (contains(uuid)) {
            val profiles: List<Profile> = datastore.find(BasicProfile::class.java)
                .filter(Filters.eq("uuid", uuid))
                .toList()

            return profiles[0]
        }

        val profile = BasicProfile(uuid)
        save(profile)

        return profile
    }

    override fun contains(uuid: UUID): Boolean {
        if (cache.getIfPresent(uuid) != null) {
            return true
        }

        val profiles: List<Profile> = datastore.find(BasicProfile::class.java)
            .filter(Filters.eq("uuid", uuid))
            .toList()

        return profiles.isNotEmpty()
    }

    override fun save(profile: Profile) {
        cache.put(profile.uuid, profile)
        datastore.save(profile)
    }

    override fun destroy(uuid: UUID) {
        cache.invalidate(uuid)
        datastore.find(BasicProfile::class.java)
            .filter(Filters.eq("uuid", uuid))
            .findAndDelete()
    }

    override fun addSubProfile(root: Profile, sub: Profile) {
        if (root.properties["sub_profiles"] == null) {
            root.properties["sub_profiles"] = arrayListOf<UUID>()
        }

        val any = root.properties["sub_profiles"]
        val nullableSubProfiles: MutableList<UUID>? =
            if (any is MutableList<*>) any.filterIsInstance<UUID>().toMutableList() else null
        val subProfiles = checkNotNull(nullableSubProfiles)

        subProfiles.add(sub.uuid)
        root.properties["sub_profiles"] = subProfiles

        sub.properties["is_sub_profile"] = true
        sub.properties["root_profile"] = root.uuid

        save(root)
        save(sub)
    }

    override fun addSubProfile(root: UUID, sub: Profile) {
        addSubProfile(get(root), sub)
    }

    override fun removeSubProfile(root: Profile, sub: UUID) {
        if (root.properties["sub_profiles"] == null) {
            return
        }

        val any = root.properties["sub_profiles"]
        val nullableSubProfiles: MutableList<UUID>? =
            if (any is MutableList<*>) any.filterIsInstance<UUID>().toMutableList() else null
        val subProfiles = checkNotNull(nullableSubProfiles)

        subProfiles.remove(sub)
        root.properties["sub_profiles"] = subProfiles

        destroy(sub)
        save(root)
    }

    override fun removeSubProfile(root: UUID, sub: UUID) {
        removeSubProfile(get(root), sub)
    }

    override fun isSubProfile(profile: Profile): Boolean = profile.properties.contains("is_sub_profile")
    override fun removeCache(profile: Profile) {
        removeCache(profile.uuid)
    }

    override fun removeCache(uuid: UUID) {
        if (cache.getIfPresent(uuid) != null) {
            cache.invalidate(uuid)
        }
    }

    override fun close() {
        mongo.close()
    }

}