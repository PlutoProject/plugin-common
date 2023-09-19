package club.plutomc.plutoproject.apiutils.data

fun <A, B> pairOf(key: A, value: B): Pair<A, B> {
    return Pair(key, value)
}