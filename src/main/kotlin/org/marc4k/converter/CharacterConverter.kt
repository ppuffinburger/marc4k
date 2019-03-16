package org.marc4k.converter

abstract class CharacterConverter {
    abstract fun convert(data: CharArray): String

    fun convert(data: ByteArray): String = convert(data.map { (if (it >= 0) it.toChar() else (256 + it).toChar()) }.toCharArray())
    fun convert(data: String): String = convert(data.toByteArray())

    open fun outputsUnicode(): Boolean = false
}