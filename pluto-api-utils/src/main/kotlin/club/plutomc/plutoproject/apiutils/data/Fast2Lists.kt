package club.plutomc.plutoproject.apiutils.data

import java.util.concurrent.CopyOnWriteArrayList

fun <E> copyOnWriteArrayListOf(): MutableList<E> = CopyOnWriteArrayList()

fun <E> copyOnWriteArrayListOf(vararg elements: E): MutableList<E> = elements.toCollection(CopyOnWriteArrayList())