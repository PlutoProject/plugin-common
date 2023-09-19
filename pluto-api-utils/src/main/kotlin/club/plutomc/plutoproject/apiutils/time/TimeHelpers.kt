package club.plutomc.plutoproject.apiutils.time

fun unixTimestamp(): Long {
    return System.currentTimeMillis() / 1000
}