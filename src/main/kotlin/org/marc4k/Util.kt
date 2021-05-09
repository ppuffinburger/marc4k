package org.marc4k


internal typealias IsoCode = Int
internal typealias MarcCode = Int

internal fun marc8CodeToHex(marc8Character: Char): String = marc8CodeToHex(marc8Character.code)
internal fun marc8CodeToHex(marc8Code: MarcCode): String = String.format(MARC8_CODE_HEX_PATTERN, marc8Code)

internal fun unicodeToHex(character: Char): String = "<U+${String.format("%04x", character.code)}>"