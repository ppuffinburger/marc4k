package org.marc4k

import org.marc4k.io.converter.ConversionError

sealed class MarcError {
    data class EncodingError(val fieldIndex: Int, val tag: String, val errors: List<ConversionError>) : MarcError() {
        override fun toString() =
            "Field Index: $fieldIndex Tag: $tag${System.lineSeparator()}${errors.joinToString(System.lineSeparator()) { "\t$it" }}"
    }
    data class StructuralError(val message: String) : MarcError()
}

class MarcException : RuntimeException {
    constructor(message: String?, cause: Exception?): super(message, cause)
    constructor(message: String?): super(message)
    constructor(cause: Exception): super(cause)
}