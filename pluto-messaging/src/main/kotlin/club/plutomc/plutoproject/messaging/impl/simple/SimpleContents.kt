package club.plutomc.plutoproject.messaging.impl.simple

import club.plutomc.plutoproject.apiutils.data.concurrentHashMapOf
import club.plutomc.plutoproject.apiutils.data.nonnull
import club.plutomc.plutoproject.apiutils.json.GsonProvider
import club.plutomc.plutoproject.apiutils.json.toJsonString
import club.plutomc.plutoproject.messaging.api.Contents
import java.lang.reflect.Type

open class SimpleContents(vararg args: Pair<String, Any>) : Contents {

    private val typeMap = concurrentHashMapOf<String, String>()
    private val valueMap = concurrentHashMapOf<String, Any>()

    init {
        typeMap.putAll(args.associate { it.first to it.second::class.java.typeName })
        valueMap.putAll(args.associate { it.first to it.second })
    }

    override fun get(key: String): Any {
        val value = valueMap[key].nonnull().toJsonString()
        val type = Class.forName(typeMap[key])

        return GsonProvider.gson.fromJson(value, type)
    }

    override fun getType(key: String): Type {
        return Class.forName(key)
    }

    override fun withKeyValueThen(vararg keyValue: Pair<String, Any>, action: (contents: Contents) -> Unit): Contents {
        keyValue.forEach {
            if (valueMap.containsKey(it.first) && valueMap[it.first].nonnull() == it.second) {
                action(this)
            }
        }

        return this
    }

}