package org.marc4k.converter.marc8

import org.marc4k.COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER
import org.marc4k.COMBINING_DOUBLE_TILDE_CHARACTER
import org.marc4k.ESCAPE_CHARACTER

internal class Marc8EscapeSequenceParser {
    fun parse(tracker: Marc8Tracker) = parseTechnique1EscapeSequence(tracker) || parseTechnique2EscapeSequence(tracker)

    private fun parseTechnique1EscapeSequence(tracker: Marc8Tracker): Boolean {
        if (tracker.pop() == ESCAPE_CHARACTER) {
            tracker.pop()?.let { characterSet ->
                if (characterSet in TECHNIQUE_1_CHARACTER_SETS) {
                    tracker.g0 = if (characterSet == ASCII_DEFAULT_GRAPHIC_CHARACTER) BASIC_LATIN_GRAPHIC_ISO_CODE else characterSet.toInt()
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
                tracker.pop()?.let { firstCharacter ->
                    if (firstCharacter.isLetterOrDigit()) {
                        tracker.pop()?.let { secondHalf ->
                            if (secondHalf == COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF) {
                                tracker.pop()?.let { secondCharacter ->
                                    if (secondCharacter.isLetterOrDigit()) {
                                        tracker.commit()
                                        return CombiningParserResult.Success("$firstCharacter$COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER$secondCharacter")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return CombiningParserResult.Failure("Unable to parse Double Inverted Breve")
    }
}

internal class CombiningDoubleTildeParser {
    fun parse(tracker: Marc8Tracker): CombiningParserResult {
        tracker.pop()?.let { firstHalf ->
            if (firstHalf == COMBINING_DOUBLE_TILDE_FIRST_HALF) {
                tracker.pop()?.let { firstCharacter ->
                    if (firstCharacter.isLetterOrDigit()) {
                        tracker.pop()?.let { secondHalf ->
                            if (secondHalf == COMBINING_DOUBLE_TILDE_SECOND_HALF) {
                                tracker.pop()?.let { secondCharacter ->
                                    if (secondCharacter.isLetterOrDigit()) {
                                        tracker.commit()
                                        return CombiningParserResult.Success("$firstCharacter$COMBINING_DOUBLE_TILDE_CHARACTER$secondCharacter")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tracker.rollback()
        return CombiningParserResult.Failure("Unable to parse Double Tilde")
    }
}

internal sealed class CombiningParserResult {
    data class Success(val result: String) : CombiningParserResult()
    data class Failure(val error: String) : CombiningParserResult()
}
