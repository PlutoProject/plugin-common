package club.plutomc.plutoproject.apiutils.data

fun <A, B, C> tripleOf(a: A, b: B, c: C): Triple<A, B, C> {
    return Triple(a, b, c)
}