package club.plutomc.plutoproject.apiutils.data

fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> {
    return this as MutableMap<K, V>
}