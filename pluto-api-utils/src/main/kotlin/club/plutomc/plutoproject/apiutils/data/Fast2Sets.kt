package club.plutomc.plutoproject.apiutils.data

import java.util.concurrent.CopyOnWriteArraySet

fun <E> copyOnWriteArraySetOf(): MutableSet<E> = CopyOnWriteArraySet()

fun <E> copyOnWriteArraySetOf(vararg elements: E): MutableSet<E> = elements.toCollection(CopyOnWriteArraySet())