package org.marc4k.converter

abstract class CharacterConverter {
    abstract fun convert(data: CharArray): CharacterConverterResult

    fun convert(data: ByteArray) = convert(data.map { (if (it >= 0) it.toChar() else (256 + it).toChar()) }.toCharArray())
    fun convert(data: String) = convert(data.toByteArray())

    open fun outputsUnicode(): Boolean = false
}

data class ConversionError(val reason: String, val offset: Int, val surroundingData: String) {
    override fun toString(): String {
        return "reason: $reason, offset: $offset, surroundingData: $surroundingData)"
    }
}

sealed class CharacterConverterResult
data class NoErrors(val conversion: String) : CharacterConverterResult()
data class WithErrors(val conversion: String, val errors: List<ConversionError>) : CharacterConverterResult()
