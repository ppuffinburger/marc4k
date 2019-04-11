package org.marc4k


typealias IsoCode = Int
typealias Marc8Code = Int

internal fun marc8CodeToHex(marc8Character: Char): String = marc8CodeToHex(marc8Character.toInt())
internal fun marc8CodeToHex(marc8Code: Marc8Code): String = String.format(MARC8_CODE_HEX_PATTERN, marc8Code)

internal fun unicodeToHex(character: Char): String = "<U+${String.format("%04x", character.toInt())}>"