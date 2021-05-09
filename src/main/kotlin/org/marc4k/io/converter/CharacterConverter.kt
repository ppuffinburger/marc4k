package org.marc4k.io.converter

abstract class CharacterConverter {
    abstract fun convert(data: CharArray): CharacterConverterResult

    fun convert(data: ByteArray) = convert(data.map { (if (it >= 0) Char(it.toInt()) else (256 + it).toChar()) }.toCharArray())
    fun convert(data: String) = convert(data.toCharArray())

    open fun outputsUnicode(): Boolean = false
}

data class ConversionError(val reason: String, val enclosingData: String) {
    override fun toString(): String {
        return "reason: $reason, enclosing: $enclosingData)"
    }
}

sealed class CharacterConverterResult {
    data class Success(val conversion: String) : CharacterConverterResult()
    data class WithErrors(val conversion: String, val errors: List<ConversionError>) :
        CharacterConverterResult()
}
