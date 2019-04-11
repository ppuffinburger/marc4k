package org.marc4k.converter.marc8

import org.marc4k.ESCAPE_CHARACTER
import org.marc4k.IsoCode
import org.marc4k.MarcException

class Marc8EscapeSequenceGenerator {
    fun generate(toCharacterSet: IsoCode, codeTableTracker: CodeTableTracker): String {
        return with(StringBuilder()) {
            if (needsToEscape(toCharacterSet, codeTableTracker.g0, codeTableTracker.g1)) {
                append(ESCAPE_CHARACTER)
                when(toCharacterSet) {
                    SUBSCRIPT_GRAPHIC_ISO_CODE,
                    GREEK_SYMBOLS_GRAPHIC_ISO_CODE,
                    SUPERSCRIPT_GRAPHIC_ISO_CODE -> {
                        codeTableTracker.g0 = toCharacterSet
                    }
                    BASIC_ARABIC_GRAPHIC_ISO_CODE,
                    BASIC_LATIN_GRAPHIC_ISO_CODE,
                    BASIC_CYRILLIC_GRAPHIC_ISO_CODE,
                    BASIC_GREEK_GRAPHIC_ISO_CODE,
                    BASIC_HEBREW_GRAPHIC_ISO_CODE -> {
                        append(SINGLE_BYTE_G0_INTERMEDIATE)
                        codeTableTracker.g0 = toCharacterSet
                    }
                    EXTENDED_ARABIC_GRAPHIC_ISO_CODE,
                    EXTENDED_CYRILLIC_GRAPHIC_ISO_CODE,
                    EXTENDED_LATIN_GRAPHIC_ISO_CODE -> {
                        append(SINGLE_BYTE_G1_INTERMEDIATE)
                        if (toCharacterSet == EXTENDED_LATIN_GRAPHIC_ISO_CODE) {
                            append(EXTENDED_LATIN_SECOND_INTERMEDIATE)
                        }
                        codeTableTracker.g1 = toCharacterSet
                    }
                    CJK_GRAPHIC_ISO_CODE -> {
                        append(MULTI_BYTE_INTERMEDIATE)
                        codeTableTracker.g0 = toCharacterSet
                    }
                    else -> throw MarcException("Invalid character set")
                }
                append(toCharacterSet.toChar())
            }

            toString()
        }
    }

    private fun needsToEscape(toIsoCode: IsoCode, currentG0: IsoCode, currentG1: IsoCode): Boolean = (toIsoCode != currentG0) && (toIsoCode != currentG1)
}