package org.marc4k

internal const val MARC8_CODE_HEX_PATTERN = "0x%02x"

// MARC Constants
internal const val LEADER_LENGTH = 24
internal const val RECORD_TERMINATOR = 0x1D
internal const val FIELD_TERMINATOR = 0x1E
internal const val SUBFIELD_DELIMITER = 0x1F

// Unicode Constants
internal const val ESCAPE_CHARACTER = '\u001B'
internal const val SUBFIELD_DELIMITER_CHARACTER = '\u001F'
internal const val SPACE_CHARACTER = '\u0020'
internal const val START_OF_STRING_CHARACTER = '\u0098'
internal const val STRING_TERMINATOR_CHARACTER = '\u009C'
internal const val ZERO_WIDTH_JOINER_CHARACTER = '\u200D'
internal const val ZERO_WIDTH_NON_JOINER_CHARACTER = '\u200C'
internal const val COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER = '\u0361'
internal const val COMBINING_DOUBLE_TILDE_CHARACTER = '\u0360'
