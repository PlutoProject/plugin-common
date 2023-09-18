package club.plutomc.plutoproject.connector.api

interface Connection<T> {

    fun get(): T

    fun close()

}