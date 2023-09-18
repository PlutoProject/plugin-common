package club.plutomc.plutoproject.apiutils.data

import java.util.concurrent.ConcurrentHashMap

fun <K, V> concurrentHashMapOf(): MutableMap<K, V> = ConcurrentHashMap<K, V>()

fun <K, V> concurrentHashMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> = ConcurrentHashMap<K, V>().apply { putAll(pairs) }