package org.marc4k.converter.marc8

import org.marc4k.marc.ESCAPE_CHARACTER

internal class Marc8EscapeSequenceParser {
    fun parse(tracker: Marc8Tracker) = parseTechnique1EscapeSequence(tracker) || parseTechnique2EscapeSequence(tracker)

    private fun parseTechnique1EscapeSequence(tracker: Marc8Tracker): Boolean {
        if (tracker.pop() == ESCAPE_CHARACTER) {
            tracker.pop()?.let { characterSet ->
                if (characterSet in TECHNIQUE_1_CHARACTER_SETS) {
                    tracker.g0 = if (characterSet == ASCII_DEFAULT_GRAPHIC_CHARACTER) BASIC_LATIN_ISO_CODE else characterSet.toInt()
                    tracker.commit()
                    return true
                }
            }
        }
        tracker.rollback()
        return false
    }

    private fun parseTechnique2EscapeSequence(tracker: Marc8Tracker) = parseTechnique2SingleByteEscapeSequence(tracker) || parseTechnique2MultiByteEscapeSequence(tracker)

    private fun parseTechnique2SingleByteEscapeSequence(tracker: Marc8Tracker): Boolean {
        if (tracker.pop() == ESCAPE_CHARACTER) {
            tracker.pop()?.let { intermediate ->
                if (intermediate in SINGLE_BYTE_INTERMEDIATES) {
                    if (tracker.peek() == EXTENDED_LATIN_SECOND_INTERMEDIATE) {
                        tracker.pop()
                        if (tracker.peek() != EXTENDED_LATIN_GRAPHIC_CHARACTER) {
                            tracker.undo()
                        }
                    }
                    tracker.pop()?.let { characterSet ->
                        if (characterSet in TECHNIQUE_2_SINGLE_BYTE_CHARACTER_SETS) {
                            when (intermediate) {
                                SINGLE_BYTE_G0_INTERMEDIATE, SINGLE_BYTE_G0_ALTERNATE_INTERMEDIATE -> {
                                    tracker.g0 = characterSet.toInt()
                                }
                                SINGLE_BYTE_G1_INTERMEDIATE, SINGLE_BYTE_G1_ALTERNATE_INTERMEDIATE -> {
                                    tracker.g1 = characterSet.toInt()
                                }
                            }
                            tracker.commit()
                            return true
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return false
    }

    private fun parseTechnique2MultiByteEscapeSequence(tracker: Marc8Tracker): Boolean {
        if (tracker.pop() == ESCAPE_CHARACTER) {
            tracker.pop()?.let { intermediate ->
                if (intermediate == MULTI_BYTE_INTERMEDIATE) {
                    tracker.peek()?.let { secondIntermediate ->
                        if (secondIntermediate in MULTI_BYTE_SECOND_INTERMEDIATES) {
                            tracker.pop()
                        }
                        tracker.pop()?.let { characterSet ->
                            if (characterSet == CJK_GRAPHIC_CHARACTER) {
                                if (secondIntermediate in MULTI_BYTE_G1_INTERMEDIATES) {
                                    tracker.g1 = characterSet.toInt()
                                } else {
                                    tracker.g0 = characterSet.toInt()
                                }
                                tracker.commit()
                                return true
                            }
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return false
    }

    companion object {
        private const val SUBSCRIPT_GRAPHIC_CHARACTER = '\u0062'
        private const val GREEK_SYMBOLS_GRAPHIC_CHARACTER = '\u0067'
        private const val SUPERSCRIPT_GRAPHIC_CHARACTER = '\u0070'
        private const val ASCII_DEFAULT_GRAPHIC_CHARACTER = '\u0073'
        private const val BASIC_ARABIC_GRAPHIC_CHARACTER = '\u0033'
        private const val EXTENDED_ARABIC_GRAPHIC_CHARACTER = '\u0034'
        private const val BASIC_LATIN_GRAPHIC_CHARACTER = '\u0042'
        private const val EXTENDED_LATIN_GRAPHIC_CHARACTER = '\u0045'
        private const val BASIC_CYRILLIC_GRAPHIC_CHARACTER = '\u004E'
        private const val EXTENDED_CYRILLIC_GRAPHIC_CHARACTER = '\u0051'
        private const val BASIC_GREEK_GRAPHIC_CHARACTER = '\u0053'
        private const val BASIC_HEBREW_GRAPHIC_CHARACTER = '\u0032'
        private const val CJK_GRAPHIC_CHARACTER = '\u0031'

        private const val SINGLE_BYTE_G0_INTERMEDIATE = '\u0028'
        private const val SINGLE_BYTE_G0_ALTERNATE_INTERMEDIATE = '\u002C'
        private const val SINGLE_BYTE_G1_INTERMEDIATE = '\u0029'
        private const val SINGLE_BYTE_G1_ALTERNATE_INTERMEDIATE = '\u002D'

        private const val EXTENDED_LATIN_SECOND_INTERMEDIATE = '\u0021'

        private const val MULTI_BYTE_INTERMEDIATE = '\u0024'
        private const val MULTI_BYTE_G0_SECOND_INTERMEDIATE = '\u002C'
        private const val MULTI_BYTE_G1_SECOND_INTERMEDIATE = '\u0029'
        private const val MULTI_BYTE_G1_ALTERNATE_SECOND_INTERMEDIATE = '\u002D'

        private const val BASIC_LATIN_ISO_CODE = 0x42

        private val TECHNIQUE_1_CHARACTER_SETS = listOf(
            SUBSCRIPT_GRAPHIC_CHARACTER,
            GREEK_SYMBOLS_GRAPHIC_CHARACTER,
            SUPERSCRIPT_GRAPHIC_CHARACTER,
            ASCII_DEFAULT_GRAPHIC_CHARACTER
        )

        private val SINGLE_BYTE_INTERMEDIATES = listOf(
            SINGLE_BYTE_G0_INTERMEDIATE,
            SINGLE_BYTE_G0_ALTERNATE_INTERMEDIATE,
            SINGLE_BYTE_G1_INTERMEDIATE,
            SINGLE_BYTE_G1_ALTERNATE_INTERMEDIATE
        )

        private val TECHNIQUE_2_SINGLE_BYTE_CHARACTER_SETS = listOf(
            BASIC_ARABIC_GRAPHIC_CHARACTER,
            EXTENDED_ARABIC_GRAPHIC_CHARACTER,
            BASIC_LATIN_GRAPHIC_CHARACTER,
            EXTENDED_LATIN_GRAPHIC_CHARACTER,
            BASIC_CYRILLIC_GRAPHIC_CHARACTER,
            EXTENDED_CYRILLIC_GRAPHIC_CHARACTER,
            BASIC_GREEK_GRAPHIC_CHARACTER,
            BASIC_HEBREW_GRAPHIC_CHARACTER
        )

        private val MULTI_BYTE_SECOND_INTERMEDIATES = listOf(
            MULTI_BYTE_G0_SECOND_INTERMEDIATE,
            MULTI_BYTE_G1_SECOND_INTERMEDIATE,
            MULTI_BYTE_G1_ALTERNATE_SECOND_INTERMEDIATE
        )

        private val MULTI_BYTE_G1_INTERMEDIATES = listOf(
            MULTI_BYTE_G1_SECOND_INTERMEDIATE,
            MULTI_BYTE_G1_ALTERNATE_SECOND_INTERMEDIATE
        )
    }
}

internal class CombiningDoubleInvertedBreveParser {
    fun parse(tracker: Marc8Tracker): CombiningParserResult {
        tracker.pop()?.let { firstHalf ->
            if (firstHalf == COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF) {
                tracker.pop()?.let { firstLatin ->
                    if (firstLatin.isLetterOrDigit()) {
                        tracker.pop()?.let { secondHalf ->
                            if (secondHalf == COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF) {
                                tracker.pop()?.let { secondLatin ->
                                    if (secondLatin.isLetterOrDigit()) {
                                        tracker.commit()
                                        return ParsedData("$firstLatin\u0361$secondLatin")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return Error("Unable to parse Double Inverted Breve")
    }

    companion object {
        const val COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF = '\u00EB'
        const val COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF = '\u00EC'
    }
}

internal class CombiningDoubleTildeParser {
    fun parse(tracker: Marc8Tracker): CombiningParserResult {
        tracker.pop()?.let { firstHalf ->
            if (firstHalf == COMBINING_DOUBLE_TILDE_FIRST_HALF) {
                tracker.pop()?.let { firstLatin ->
                    if (firstLatin in 'a'..'z' || firstLatin in 'A'..'Z') {
                        tracker.pop()?.let { secondHalf ->
                            if (secondHalf == COMBINING_DOUBLE_TILDE_SECOND_HALF) {
                                tracker.pop()?.let { secondLatin ->
                                    if (secondLatin in 'a'..'z' || secondLatin in 'A'..'Z') {
                                        tracker.commit()
                                        return ParsedData("$firstLatin\u0360$secondLatin")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return Error("Unable to parse Double Tilde")
    }

    companion object {
        const val COMBINING_DOUBLE_TILDE_FIRST_HALF = '\u00FA'
        const val COMBINING_DOUBLE_TILDE_SECOND_HALF = '\u00FB'
    }
}

sealed class CombiningParserResult
data class ParsedData(val parsedData: String) : CombiningParserResult()
data class Error(val message: String) : CombiningParserResult()
