package club.plutomc.plutoproject.apiutils.data

fun <T : Any> T?.nonnull(): T {
    return checkNotNull(this)
}