package org.marc4k.converter

class NcrGenerator {
    fun generate(character: Char) = generate(character.toInt())

    fun generate(codePoint: Int): String {
        return if (codePoint <= 0xFFFF) {
            "&#x${String.format("%04x", codePoint).toUpperCase()};"
        } else {
            "&#x${String.format("%06x", codePoint).toUpperCase()};"
        }
    }
}