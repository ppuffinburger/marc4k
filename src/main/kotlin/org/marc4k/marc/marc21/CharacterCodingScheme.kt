package org.marc4k.marc.marc21

enum class CharacterCodingScheme(val value: Char) {
    MARC8(' '),
    UNICODE('a'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy(CharacterCodingScheme::value)
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}