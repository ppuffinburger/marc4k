package org.marc4k.io.converter.marc8

import org.marc4k.ESCAPE_CHARACTER
import org.marc4k.IsoCode
import org.marc4k.MarcException

/**
 * A class to generate a MARC8 escape sequence.
 */
internal class Marc8EscapeSequenceGenerator {
    /**
     * Returns the appropriate MARC8 escape sequence based on [toCharacterSet] and the given [tracker].
     *
     * @throws[MarcException] if an invalid [toCharacterSet] is passed in.
     */
    fun generate(toCharacterSet: IsoCode, tracker: CodeTableTracker): String {
        return with(StringBuilder()) {
            if (needsToEscape(toCharacterSet, tracker)) {
                append(ESCAPE_CHARACTER)
                when(toCharacterSet) {
                    SUBSCRIPT_GRAPHIC_ISO_CODE,
                    GREEK_SYMBOLS_GRAPHIC_ISO_CODE,
                    SUPERSCRIPT_GRAPHIC_ISO_CODE -> {
                        tracker.g0 = toCharacterSet
                    }
                    BASIC_ARABIC_GRAPHIC_ISO_CODE,
                    BASIC_LATIN_GRAPHIC_ISO_CODE,
                    BASIC_CYRILLIC_GRAPHIC_ISO_CODE,
                    BASIC_GREEK_GRAPHIC_ISO_CODE,
                    BASIC_HEBREW_GRAPHIC_ISO_CODE -> {
                        append(SINGLE_BYTE_G0_INTERMEDIATE)
                        tracker.g0 = toCharacterSet
                    }
                    EXTENDED_ARABIC_GRAPHIC_ISO_CODE,
                    EXTENDED_CYRILLIC_GRAPHIC_ISO_CODE,
                    EXTENDED_LATIN_GRAPHIC_ISO_CODE -> {
                        append(SINGLE_BYTE_G1_INTERMEDIATE)
                        if (toCharacterSet == EXTENDED_LATIN_GRAPHIC_ISO_CODE) {
                            append(EXTENDED_LATIN_SECOND_INTERMEDIATE)
                        }
                        tracker.g1 = toCharacterSet
                    }
                    CJK_GRAPHIC_ISO_CODE -> {
                        append(MULTI_BYTE_INTERMEDIATE)
                        tracker.g0 = toCharacterSet
                    }
                    else -> throw MarcException("Invalid character set")
                }
                append(toCharacterSet.toChar())
            }

            toString()
        }
    }

    private fun needsToEscape(toCharacterSet: IsoCode, tracker: CodeTableTracker): Boolean = (toCharacterSet != tracker.g0) && (toCharacterSet != tracker.g1)
}