package org.marc4k.marc

import java.text.DecimalFormat
import java.text.FieldPosition

internal class CustomDecimalFormat(numberOfDigits: Int, private val overflowRepresentation: OverflowRepresentation = OverflowRepresentation.ALL_NINES) : DecimalFormat(FORMAT_STRING.substring(0, numberOfDigits)) {
    private val maximumValue: Long = MAX_STRING.substring(0, numberOfDigits).toLong()

    init {
        maximumIntegerDigits = numberOfDigits
    }

    override fun format(number: Double, result: StringBuffer?, fieldPosition: FieldPosition?): StringBuffer {
        return format(number.toLong(), result, fieldPosition)
    }

    override fun format(number: Long, result: StringBuffer?, fieldPosition: FieldPosition?): StringBuffer {
        return super.format(if (number > maximumValue) getOverflowRepresentation(number) else number, result, fieldPosition)
    }

    private fun getOverflowRepresentation(number: Long): Long {
        return when (overflowRepresentation) {
            OverflowRepresentation.ALL_ZEROS -> return 0
            OverflowRepresentation.TRUNCATE -> number % (maximumValue + 1)
            else -> maximumValue
        }
    }

    companion object {
        private const val FORMAT_STRING = "00000000000000000000"
        private const val MAX_STRING = "99999999999999999999"
    }
}

enum class OverflowRepresentation {
    ALL_ZEROS,
    ALL_NINES,
    TRUNCATE
}
