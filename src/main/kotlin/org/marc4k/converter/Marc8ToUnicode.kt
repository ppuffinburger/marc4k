package org.marc4k.converter

import org.marc4k.MarcException
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class Marc8ToUnicode : CharacterConverter {
    private val translateNcr: Boolean
    private val ncrParser = NcrParser()

    private var codeTable: CodeTable
    private var loadedMultiByteCodeTable = false

    // TODO - eventually make a MarcPermissiveReader
    private var currentReader: String? = null

    constructor(loadMultiByteCodeTable: Boolean = false, translateNcr: Boolean = false)
            : this(if (loadMultiByteCodeTable) Marc8ToUnicode::class.java.getResourceAsStream("/codetables.xml") else Marc8ToUnicode::class.java.getResourceAsStream("/codetablesnocjk.xml"), translateNcr) {
        loadedMultiByteCodeTable = loadMultiByteCodeTable
    }

    constructor(filename: String, translateNcr: Boolean = false) : this(FileInputStream(filename), translateNcr)

    constructor(inputStream: InputStream, translateNcr: Boolean = false) {
        codeTable = CodeTable(inputStream)
        this.translateNcr = translateNcr
        loadedMultiByteCodeTable = true
    }

    override fun convert(data: CharArray): String {
        val convertedString = with(StringBuilder()) {
            val tracker = CodeDataTracker()

            checkMode(data, tracker)

            val diacritics = ArrayDeque<Char>()

            while (tracker.offset < data.size) {
                if (codeTable.isCombining(data[tracker.offset].toInt(), tracker.g0, tracker.g1) && hasNext(tracker.offset, data.size)) {
                    while (tracker.offset < data.size && codeTable.isCombining(
                            data[tracker.offset].toInt(),
                            tracker.g0,
                            tracker.g1
                        ) && hasNext(tracker.offset, data.size)
                    ) {
                        val character = getCharCDT(data, tracker)
                        if (character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                            diacritics.add(character)
                        }
                        checkMode(data, tracker)
                    }

                    if (tracker.offset >= data.size) {
                        currentReader?.let { currentReader?.toInt() }
                        break
                    }

                    val character2 = getCharCDT(data, tracker)

                    checkMode(data, tracker)

                    if (character2 != CODE_TABLE_CHARACTER_NOT_FOUND) {
                        append(character2)
                    }

                    while (diacritics.isNotEmpty()) {
                        append(diacritics.pop())
                    }
                } else if (tracker.multiByte) {
                    append(convertMultiByte(data, tracker))
                } else {
                    val offset = tracker.offset
                    val characterAtOffset = data[offset]

                    var character = getCharCDT(data, tracker)

                    var greekErrorFixed = false

                    if (character == '\r' || character == '\n') {
                        currentReader?.let { currentReader?.toInt() }
                        character = ' '
                    }

                    if (currentReader != null && tracker.g0 == BASIC_GREEK_ISO_CODE && data[offset] in ' '..'@') {
                        if (character == CODE_TABLE_CHARACTER_NOT_FOUND && data[offset] in ' '..'@') {
                            currentReader?.let { currentReader?.toInt() }
                            tracker.g0 = BASIC_LATIN_ISO_CODE

                            character = getChar(data[offset].toInt(), tracker.g0, tracker.g1)

                            if (character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                                append(character)
                                greekErrorFixed = true
                            }
                        } else if (offset + 1 < data.size && data[offset].isDigit() && data[offset + 1].isDigit()) {
                            currentReader?.let { currentReader?.toInt() }
                            tracker.g0 = BASIC_LATIN_ISO_CODE

                            val character1 = getChar(data[offset].toInt(), tracker.g0, tracker.g1)

                            if (character1 != CODE_TABLE_CHARACTER_NOT_FOUND) {
                                append(character1)
                                greekErrorFixed = true
                            }
                        }
                    }

                    if (!greekErrorFixed && character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                        append(character)
                    } else if (!greekErrorFixed && character == CODE_TABLE_CHARACTER_NOT_FOUND) {
                        append("<U+${Integer.toHexString(characterAtOffset.toInt()).padStart(4, '0')}>")
                        currentReader?.let { currentReader?.toInt() }
                    }
                }

                if (hasNext(tracker.offset, data.size)) {
                    checkMode(data, tracker)
                }
            }

            toString()
        }

        return if (translateNcr) ncrParser.parse(convertedString) else convertedString
    }

    override fun outputsUnicode() = true

    private fun checkMode(data: CharArray, tracker: CodeDataTracker) {
        var extra = 0
        var extra2 = 0

        while (tracker.offset + extra + extra2 < data.size && isEscape(data[tracker.offset])) {
            if (tracker.offset + extra + extra2 + 1 == data.size) {
                tracker.offset++
                currentReader?.let { currentReader?.toInt() } ?: throw MarcException("Escape character found at end of field.")
                break
            }

            // TODO : toInt() and magic numbers
            when (data[tracker.offset + 1 + extra].toInt()) {
                0x28, 0x2C -> updateCodeDateTracker(tracker, 0, data, 2 + extra, false) // '(', ','
                0x29, 0x2D -> updateCodeDateTracker(tracker, 1, data, 2+ extra, false) // ')', '-'
                0x24 -> {
                    if (!loadedMultiByteCodeTable) { loadMultiByte() }

                    var switchOffset = tracker.offset + 2 + extra + extra2
                    if (switchOffset >= data.size) {
                        tracker.offset++
                        currentReader?.let { currentReader?.toInt() ?: throw MarcException("Incomplete character set code found following escape character.") }
                    } else {
                        when (data[switchOffset].toInt()) {
                            0x29, 0x2D -> { // ')', '-'
                                val newOffset = 3 + extra + extra2
                                if (tracker.offset + newOffset >= data.size) {
                                    tracker.offset++
                                    currentReader?.let { currentReader?.toInt() ?: throw MarcException("Incomplete character set code found following escape character.") }
                                } else {
                                    updateCodeDateTracker(tracker, 1, data, newOffset, true)
                                }
                            }
                            0x2C -> { // ','
                                val newOffset = 3 + extra + extra2
                                if (tracker.offset + newOffset >= data.size) {
                                    tracker.offset++
                                    currentReader?.let { currentReader?.toInt() ?: throw MarcException("Incomplete character set code found following escape character.") }
                                } else {
                                    updateCodeDateTracker(tracker, 0, data, newOffset, true)
                                }
                            }
                            CJK_ISO_CODE -> { // '1'
                                tracker.g0 = data[tracker.offset + 2 + extra + extra2].toInt()
                                tracker.offset += 3 + extra + extra2
                                tracker.multiByte = true
                            }
                            SPACE_CODE -> { // ' '
                                extra2++
                            }
                            else -> {
                                tracker.offset++
                                currentReader?.let { currentReader?.toInt() ?: throw MarcException("Unknown character set code found following escape character.") }
                            }
                        }
                    }
                }
                GREEK_SYMBOLS_ISO_CODE, SUBSCRIPT_ISO_CODE, SUPERSCRIPT_ISO_CODE -> { // 'g', 'b', 'p'
                    tracker.g0 = data[tracker.offset + 1 + extra].toInt()
                    tracker.offset += 2 + extra
                    tracker.multiByte = false
                }
                ASCII_DEFAULT_ISO_CODE -> { // 's'
                    tracker.g0 = BASIC_LATIN_ISO_CODE
                    tracker.offset += 2 + extra
                    tracker.multiByte = false
                }
                SPACE_CODE -> { // ' '
                    if (currentReader == null) {
                        throw  MarcException("Extraneous space character found within MARC8 character set escape sequence.")
                    }
                    extra++
                }
                else -> { // unknown code character found: discard escape sequence and return
                    tracker.offset++
                    currentReader?.let { currentReader?.toInt() ?: throw MarcException("Unknown character set code found following escape character.") }
                }
            }
        }

        if (currentReader != null && (extra != 0 || extra2 != 0)) {
            currentReader?.toInt()
        }
    }

    private fun updateCodeDateTracker(tracker: CodeDataTracker, g0Org1: Int, data: CharArray, additionalOffset: Int, multiByte: Boolean) {
        var currentAdditionalOffset = additionalOffset
        if (data[tracker.offset + currentAdditionalOffset] == '!' && data[tracker.offset + currentAdditionalOffset + 1] == 'E') {
            currentAdditionalOffset++
        } else if (data[tracker.offset + currentAdditionalOffset] == ' ') {
            currentReader?.let { currentReader?.toInt() ?: throw MarcException("Extraneous space character found within MARC8 character set escape sequence.") }
            currentAdditionalOffset++
        } else if ("(,)-\$!".indexOf(data[tracker.offset + currentAdditionalOffset]) != -1) {
            currentReader?.let { currentReader?.toInt() ?: throw MarcException("Extraneous intermediate character found following escape character.") }
            currentAdditionalOffset++
        }

        if ("34BE1NQS2".indexOf(data[tracker.offset + currentAdditionalOffset]) == -1) {
            tracker.offset++
            tracker.multiByte = false

            currentReader?.let { currentReader?.toInt() ?: throw MarcException("Unknown character set code found following escape character.") }
        } else {
            if (g0Org1 == 0) {
                tracker.g0 = data[tracker.offset + currentAdditionalOffset].toInt()
            } else {
                tracker.g1 = data[tracker.offset + currentAdditionalOffset].toInt()
            }

            tracker.offset += currentAdditionalOffset + 1
            tracker.multiByte = multiByte
        }
    }

    // TODO - magic number and toInt() call
    private fun isEscape(c: Char): Boolean {
        return c.toInt() == 0x1B
    }

    private fun hasNext(position: Int, length: Int) = position < length -1

    // TODO - magic String
    private fun loadMultiByte() {
        codeTable = CodeTable(javaClass.getResourceAsStream("/codetables.xml"))
        loadedMultiByteCodeTable = true
    }

    private fun getCharCDT(data: CharArray, tracker: CodeDataTracker): Char {
        var character = getChar(data[tracker.offset].toInt(), tracker.g0, tracker.g1)

        if (translateNcr && character == '&' && data.size >= tracker.offset + 5 ) { // if we're translating NCRs and have enough data the check integrity
            if (data[tracker.offset + 1] == '#' && data[tracker.offset + 2] == 'x') {
                var length = 0
                while (tracker.offset + 3 + length < data.size) {
                    val c1 = data[tracker.offset + 3 + length]
                    if ("0123456789ABCDEFabcdef".contains(c1)) {
                        length++
                        continue
                    } else if (length >= 1 && c1 == ';') {
                        character = getCharFromCodePoint(String(data, tracker.offset + 3, length))
                        tracker.offset += length + 4
                        if (character == '\r' || character == '\n') {
                            currentReader?.let { currentReader?.toInt() }
                            return character
                        }
                    } else if (length == 0 && c1 == ';') {
                        currentReader?.let { currentReader?.toInt() }
                        tracker.offset += 4
                        character = getCharCDT(data, tracker)
                        return character
                    } else if (length >= 1
                        && c1 == '%'
                        && data.size > tracker.offset + length + 4
                        && data[tracker.offset + 3 + length + 1] == 'x'
                        && (data.size == tracker.offset + length + 5 || data[tracker.offset + 3 + length + 2] == ';')
                    ) {
                        character = getCharFromCodePoint(String(data, tracker.offset + 3, length))
                        currentReader?.let { currentReader?.toInt() }
                        tracker.offset += length + 5
                        return character
                    } else if (length >= 1
                        && c1 == '%'
                        && data.size > tracker.offset + length + 5
                        && data[tracker.offset + 3 + length + 1] == 'x'
                        && data[tracker.offset + 3 + length + 2] == ';'
                    ) {
                        character = getCharFromCodePoint(String(data, tracker.offset + 3, length))
                        currentReader?.let { currentReader?.toInt() }
                        tracker.offset += length + 6
                        return character
                    } else {
                        currentReader?.let { currentReader?.toInt() }
                        tracker.offset++
                        return character
                    }
                    length++
                }
                currentReader?.let { currentReader?.toInt() }
                character = getCharFromCodePoint(String(data, tracker.offset + 3, length))
                tracker.offset + 3 + length
                return character
            } else {
                tracker.offset++
            }
        } else {
            tracker.offset++
        }

        return character
    }

    private fun getChar(marc8Code: Marc8Code, g0: Int, g1: Int): Char {
        return if (marc8Code <= 0x7E) {
            codeTable.getChar(marc8Code, g0)
        } else {
            codeTable.getChar(marc8Code, g1)
        }
    }

    private fun getCharFromCodePoint(charCodePoint: String) = charCodePoint.toInt(16).toChar()

    private fun convertMultiByte(data: CharArray, tracker: CodeDataTracker): String {
        return with(StringBuilder()) {
            var offset = tracker.offset

            while (offset < data.size && data[offset].toInt() != 0x1B) {
                val length = getRawMultiByteLength(data, offset)
                val spaces = getNumberOfSpacesInMultiByteLength(data, offset)

                var errorsPresent = false

                if ((length - spaces) % 3 != 0) {
                    errorsPresent = true
                }

                if (data[offset].toInt() == 0x20) {
                    append(' ')
                    offset++
                } else if (data[offset].toInt() >= 0x80) {
                    val character2 = getChar(data[offset].toInt(), tracker.g0, tracker.g1)
                    append(character2)
                    offset++
                } else if (currentReader == null) {
                    if (offset + 3 <= data.size) {
                        val character = getMultiByteCharacter(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt()))

                        if (character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                            append(character)
                            offset += 3
                        } else {
                            append(data[offset])
                            append(data[offset + 1])
                            append(data[offset + 2])
                            offset += 3
                        }
                    } else {
                        while (offset < data.size) {
                            append(data[offset++])
                        }
                    }
                } else if (!errorsPresent
                    && offset + 3 <= data.size
                    && (currentReader == null || data[offset + 1].toInt() != 0x20 && data[offset + 2].toInt() != 0x20)
                    && getMultiByteCharacter(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt())) != CODE_TABLE_CHARACTER_NOT_FOUND) {
                    val character = getMultiByteCharacter(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt()))

                    if (currentReader == null && character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                        append(character)
                        offset += 3
                    }
                } else if (offset + 6 <= data.size
                    && noneEquals(data, offset, offset + 3, '\u0020')
                    && (getMultiByteCharacter(makeMultiByte(data[offset + 0].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt())) == CODE_TABLE_CHARACTER_NOT_FOUND || getMultiByteCharacter(makeMultiByte(data[offset + 3].toInt(), data[offset + 4].toInt(), data[offset + 5].toInt())) == CODE_TABLE_CHARACTER_NOT_FOUND)
                    && getMultiByteCharacter(makeMultiByte(data[offset + 2].toInt(), data[offset + 3].toInt(), data[offset + 4].toInt())) != CODE_TABLE_CHARACTER_NOT_FOUND
                    && noneEquals(data, offset, offset + 5, '\u001B')
                    && noneInRange(data, offset, offset + 5, '\u0080', '\u00FF')
                    && !nextEscapeIsMultiByte(data, offset, data.size)) {
                    val multiByteString = getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), '['.toInt(), data[offset + 1].toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), ']'.toInt(), data[offset + 1].toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), '['.toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), ']'.toInt()))

                    if (multiByteString.length == 1) {
                        currentReader?.let { currentReader?.toInt() } // "Missing square brace character in MARC8 multibyte character, inserting one to create the only valid option"
                        append(multiByteString)
                        offset += 2
                    } else if (multiByteString.length > 1) {
                        currentReader?.let { currentReader?.toInt() } // "Missing square brace character in MARC8 multibyte character, inserting one to create a randomly chosen valid option"
                        append(multiByteString.substring(0, 1))
                        offset += 2
                    } else if (multiByteString.length == 0) {
                        currentReader?.let { currentReader?.toInt() } // "Erroneous MARC8 multibyte character, Discarding bad character and continuing reading Multibyte characters"
                        append("[?]")
                        offset += 2
                    }
                } else if (offset + 7 <= data.size
                    && noneEquals(data, offset, offset + 3, '\u0020')
                    && (getMultiByteCharacter(makeMultiByte(data[offset + 0].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt())) == CODE_TABLE_CHARACTER_NOT_FOUND || getMultiByteCharacter(makeMultiByte(data[offset + 3].toInt(), data[offset + 4].toInt(), data[offset + 5].toInt())) == CODE_TABLE_CHARACTER_NOT_FOUND)
                    && getMultiByteCharacter(makeMultiByte(data[offset + 4].toInt(), data[offset + 5].toInt(), data[offset + 6].toInt())) != CODE_TABLE_CHARACTER_NOT_FOUND
                    && noneEquals(data, offset, offset + 6, '\u001B')
                    && noneInRange(data, offset, offset + 6, '\u0080', '\u00FF')
                    && !nextEscapeIsMultiByte(data, offset, data.size)) {
                    val multiByteString = getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), '['.toInt(), data[offset + 1].toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), ']'.toInt(), data[offset + 1].toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), '['.toInt())) +
                            getMultiByteCharacterString(makeMultiByte(data[offset].toInt(), data[offset + 1].toInt(), ']'.toInt()))

                    if (multiByteString.length == 1) {
                        currentReader?.let { currentReader?.toInt() } // "Missing square brace character in MARC8 multibyte character, inserting one to create the only valid option"
                        append(multiByteString)
                        offset += 2
                    } else if (multiByteString.length > 1) {
                        currentReader?.let { currentReader?.toInt() } // "Missing square brace character in MARC8 multibyte character, inserting one to create a randomly chosen valid option"
                        append(multiByteString.substring(0, 1))
                        offset += 2
                    } else if (multiByteString.length == 0) {
                        currentReader?.let { currentReader?.toInt() } // "Erroneous MARC8 multibyte character, Discarding bad character and continuing reading Multibyte characters"
                        append("[?]")
                        offset += 2
                    }
                } else if (offset + 4 <= data.size && data[offset].toInt() > 0x7F && getMultiByteCharacter(makeMultiByte(data[offset + 1].toInt(), data[offset + 2].toInt(), data[offset + 3].toInt())) != CODE_TABLE_CHARACTER_NOT_FOUND) {
                    currentReader?.let { currentReader?.toInt() } // "Erroneous character in MARC8 multibyte character, Copying bad character and continuing reading Multibyte characters"
                    append(getChar(data[offset].toInt(), BASIC_LATIN_ISO_CODE, EXTENDED_LATIN_ISO_CODE))
                    offset++
                } else if (currentReader != null && offset + 4 <= data.size && (data[offset + 1].toInt() == 0x20 || data[offset + 2].toInt() == 0x20)) {
                    val multiByte = makeMultiByte(data[offset].toInt(), if (data[offset + 1].toInt() != 0x20) data[offset + 1].toInt() else data[offset + 2].toInt(), data[offset + 3].toInt())
                    val character = getMultiByteCharacter(multiByte)
                    if (character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                        currentReader?.let { currentReader?.toInt() } // "Extraneous space found within MARC8 multibyte character"
                        append(character)
                        append(' ')
                        offset += 4
                    } else {
                        currentReader?.let { currentReader?.toInt() } // "Erroneous MARC8 multibyte character, inserting change to default character set"
                        tracker.multiByte = false
                        tracker.g0 = BASIC_LATIN_ISO_CODE
                        tracker.g1 = EXTENDED_LATIN_ISO_CODE
                        break
                    }
                } else if (offset + 3 > data.size || offset + 3 == data.size && (data[offset + 1].toInt() == 0x20 || data[offset + 2].toInt() == 0x20)) {
                    currentReader?.let { currentReader?.toInt() } // "Partial MARC8 multibyte character, inserting change to default character set"
                    tracker.multiByte = false
                    tracker.g0 = BASIC_LATIN_ISO_CODE
                    tracker.g1 = EXTENDED_LATIN_ISO_CODE
                    break
                } else if (offset + 3 <= data.size && getMultiByteCharacter(makeMultiByte(data[offset + 0].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt())) != CODE_TABLE_CHARACTER_NOT_FOUND) {
                    val character = getMultiByteCharacter(makeMultiByte(data[offset + 0].toInt(), data[offset + 1].toInt(), data[offset + 2].toInt()))
                    if (currentReader == null || character != CODE_TABLE_CHARACTER_NOT_FOUND) {
                        append(character)
                        offset += 3
                    }
                } else {
                    currentReader?.let { currentReader?.toInt() } // "Erroneous MARC8 multibyte character, inserting change to default character set"
                    tracker.multiByte = false
                    tracker.g0 = BASIC_LATIN_ISO_CODE
                    tracker.g1 = EXTENDED_LATIN_ISO_CODE
                    break
                }
            }

            tracker.offset = offset

            toString()
        }
    }

    private fun getRawMultiByteLength(data: CharArray, originalOffset: Int): Int {
        var offset = originalOffset
        var length = 0

        while (offset < data.size && data[offset].toInt() != 0x1b) {
            offset++
            length++
        }
        return length
    }

    private fun getNumberOfSpacesInMultiByteLength(data: CharArray, originalOffset: Int): Int {
        var offset = originalOffset
        var count = 0

        while (offset < data.size && data[offset] != '\u001B') {
            if (data[offset] == ' ') {
                count++
            }
            offset++
        }
        return count
    }

    private fun makeMultiByte(first: Int, second: Int, third: Int): Int {
        return String.format("%02X%02X%02X", first, second, third).toInt(16)
    }

    private fun getMultiByteCharacter(characterValue: Int): Char {
        return codeTable.getChar(characterValue, CJK_ISO_CODE)
    }

    private fun noneEquals(data: CharArray, start: Int, end: Int, value: Char): Boolean {
        for (offset in start..end) {
            if (data[offset] == value) {
                return false
            }
        }
        return true
    }

    private fun allCharactersInRangeAreASCII(data: CharArray, start: Int, end: Int): Boolean {
        return data.copyOfRange(start, end).all { it in '\u0000'..'\u007F' }
    }

    private fun noneInRange(data: CharArray, start: Int, end: Int, lowValue: Char, highValue: Char): Boolean {
        for (offset in start..end) {
            if (data[offset] in lowValue..highValue) {
                return false
            }
        }
        return true
    }

    private fun nextEscapeIsMultiByte(data: CharArray, start: Int, length: Int): Boolean {
        for (offset in start until length - 1) {
            if (data[offset] == 0x1b.toChar()) {
                return if (data[offset + 1] == '$') {
                    true
                } else {
                    break
                }
            }
        }
        return false
    }

    private fun getMultiByteCharacterString(marc8Code: Marc8Code): String {
        val character = codeTable.getChar(marc8Code, CJK_ISO_CODE)
        return when (character == CODE_TABLE_CHARACTER_NOT_FOUND) {
            true -> ""
            false -> character.toString()
        }
    }

    // TODO - magic numbers
    private class CodeDataTracker {
        var offset = 0
        var g0 = BASIC_LATIN_ISO_CODE
        var g1 = EXTENDED_LATIN_ISO_CODE
        var multiByte = false

        override fun toString(): String {
            return "Offset: " + offset + " G0: " + Integer.toHexString(g0) + " G1: " + Integer.toHexString(g1) + " MultiByte: " + multiByte;
        }
    }

    companion object {
        private const val BASIC_ARABIC_ISO_CODE = 0x33
        private const val EXTENDED_ARABIC_ISO_CODE = 0x34
        private const val BASIC_LATIN_ISO_CODE = 0x42
        private const val EXTENDED_LATIN_ISO_CODE = 0x45
        private const val CJK_ISO_CODE = 0x31
        private const val BASIC_CYRILLIC_ISO_CODE = 0x4E
        private const val EXTENDED_CYRILLIC_ISO_CODE = 0x51
        private const val BASIC_GREEK_ISO_CODE = 0x53
        private const val BASIC_HEBREW_ISO_CODE = 0x32
        private const val SUBSCRIPT_ISO_CODE = 0x62
        private const val GREEK_SYMBOLS_ISO_CODE = 0x67
        private const val SUPERSCRIPT_ISO_CODE = 0x70
        private const val ASCII_DEFAULT_ISO_CODE = 0x73
        private const val SPACE_CODE = 0x20
        private const val SPACE_CHAR = SPACE_CODE.toChar()

    }
}