package org.marc4k.converter

abstract class CharacterConverter {
    abstract fun convert(dataElement: CharArray): String

    fun convert(dataElement: ByteArray): String = convert(dataElement.map { (if (it >= 0) it.toChar() else (256 + it).toChar()) }.toCharArray())
    fun convert(dataElement: String): String = convert(dataElement.toByteArray())

    open fun outputsUnicode(): Boolean = false
}