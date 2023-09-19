package club.plutomc.plutoproject.messaging.api

import club.plutomc.plutoproject.messaging.impl.Constants
import club.plutomc.plutoproject.messaging.impl.simple.SimpleContents
import java.lang.reflect.Type

interface Contents {

    companion object {
        fun of(vararg args: Pair<String, Any>): Contents {
            return SimpleContents(*args)
        }
    }

    /**
     * 获取指定键的值。
     * @param key 参数名称。
     * @return 值内容。
     */
    fun get(key: String): Any

    /**
     * 获取默认键值。
     * @return 值内容。
     */
    fun getDefault(): Any {
        return get(Constants.DEFAULT_KEY)
    }

    /**
     * 获取指定键值并转换为指定类型。
     * @param T 指定类型。
     * @param key 键名称。
     * @return 值内容。
     */
    fun <T> getWithCast(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return get(key) as T
    }

    /**
     * 获取默认键值并转换为指定类型。
     * @param T 指定类型。
     * @return 值内容。
     */
    fun <T> getDefaultWithCast(): T {
        return getWithCast<T>(Constants.DEFAULT_KEY)
    }

    /**
     * 获取某个键的属性类型。
     * @return 类型。
     */
    fun getType(key: String): Type

    /**
     * 如果与指定的键值匹配，则进行操作，
     * @param keyValue 键与值。
     * @param action 进行的操作。
     * @return 对象本身，用于链式操作。
     */
    fun withKeyValueThen(vararg keyValue: Pair<String, Any>, action: (contents: Contents) -> Unit): Contents

}