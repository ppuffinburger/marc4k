package org.marc4k.io.converter.marc8

import org.marc4k.*
import org.marc4k.io.converter.*
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

/**
 * A [CharacterConverter] that converts MARC8 to Unicode.
 */
class Marc8ToUnicode : CharacterConverter {
    private val translateNcr: Boolean
    private val ncrTranslator: NcrTranslator by lazy { NcrTranslator() }
    private val escapeSequenceParser: Marc8EscapeSequenceParser by lazy { Marc8EscapeSequenceParser() }
    private val combiningDoubleInvertedBreveParser: CombiningDoubleInvertedBreveParser by lazy { CombiningDoubleInvertedBreveParser() }
    private val combiningDoubleTildeParser: CombiningDoubleTildeParser by lazy { CombiningDoubleTildeParser() }

    private var codeTable: CodeTable
    private var loadedMultiByteCodeTable = false

    /**
     * Instantiates class using one of the internal MARC8 code tables based on [loadMultiByteCodeTable].
     *
     * @property[translateNcr] true if translating NCR sequences.  Defaults to false.
     */
    constructor(loadMultiByteCodeTable: Boolean = false, translateNcr: Boolean = false) : this(
        inputStream = if (loadMultiByteCodeTable) Marc8ToUnicode::class.java.getResourceAsStream("/codetables.xml") else Marc8ToUnicode::class.java.getResourceAsStream("/codetablesnocjk.xml"),
        translateNcr = translateNcr
    ) {
        loadedMultiByteCodeTable = loadMultiByteCodeTable
    }

    /**
     * Instantiates class using the given code table at [filename].
     *
     * @property[translateNcr] true if translating NCR sequences.  Defaults to false.
     */
    constructor(filename: String, translateNcr: Boolean = false) : this(FileInputStream(filename), translateNcr)

    /**
     * Instantiates class using the given code table in [inputStream].
     *
     * @property[translateNcr] true if translating NCR sequences.  Defaults to false.
     */
    constructor(inputStream: InputStream, translateNcr: Boolean = false) {
        codeTable = CodeTable(inputStream)
        this.translateNcr = translateNcr
        loadedMultiByteCodeTable = true
    }

    override fun outputsUnicode() = true

    /**
     * Returns a [CharacterConverterResult] for the given [data].
     */
    override fun convert(data: CharArray): CharacterConverterResult {
        val errors = mutableListOf<ConversionError>()
        val convertedString = with(StringBuilder()) {
            val diacritics = ArrayDeque<Pair<MarcCode, Char>>()
            val tracker = CodeDataTracker(data)

            loop@ while (!tracker.isEmpty()) {
                when (val peeked = tracker.peek()) {
                    ESCAPE_CHARACTER -> {
                        if (!escapeSequenceParser.parse(tracker)) {
                            errors.add(createConversionError("Incomplete or invalid escape sequence.  Discarding escape character.", tracker))
                            tracker.pop()
                            tracker.commit()
                        }
                        continue@loop
                    }
                    SPACE_CHARACTER -> {
                        tracker.pop()?.let { append(it) }
                        tracker.commit()
                        continue@loop
                    }
                    in C0_CONTROL_CHARACTER_RANGE -> {
                        tracker.pop()?.let {
                            errors.add(createConversionError("C0 control character found (${marc8CodeToHex(it)}), which is invalid, deleting it.", tracker))
                        }
                        tracker.commit()
                        continue@loop
                    }
                    in C1_CONTROL_CHARACTER_RANGE -> {
                        when(peeked) {
                            NON_SORT_BEGIN_CHARACTER -> {
                                tracker.pop()
                                append(START_OF_STRING_CHARACTER)
                            }
                            NON_SORT_END_CHARACTER -> {
                                tracker.pop()
                                append(STRING_TERMINATOR_CHARACTER)
                            }
                            JOINER_CHARACTER -> {
                                tracker.pop()
                                append(ZERO_WIDTH_JOINER_CHARACTER)
                            }
                            NON_JOINER_CHARACTER -> {
                                tracker.pop()
                                append(ZERO_WIDTH_NON_JOINER_CHARACTER)
                            }
                            else -> {
                                tracker.pop()?.let {
                                    errors.add(createConversionError("C1 control character found (${marc8CodeToHex(it)}), which is invalid, deleting it.", tracker))
                                }
                            }
                        }
                        continue@loop
                    }
                }

                when {
                    tracker.isEACC() && parseEACC(tracker, this) -> {}
                    isStartOfDiacritics(tracker) -> {
                        when(tracker.peek()) {
                            COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF -> {
                                when(val result = combiningDoubleInvertedBreveParser.parse(tracker)) {
                                    is CombiningParserResult.Success -> { this.append(result.result) }
                                    is CombiningParserResult.Failure -> {
                                        errors.add(createConversionError(result.error, tracker))
                                        tracker.pop()
                                        tracker.commit()
                                    }
                                }
                                continue@loop
                            }
                            COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF -> {
                                // We found this by itself or there was another problem when parsing above and we popped the first half
                                errors.add(createConversionError("Unable to parse Combining Double Inverted Breve Second Half", tracker))
                                tracker.pop()
                                tracker.commit()
                            }
                            COMBINING_DOUBLE_TILDE_FIRST_HALF -> {
                                when(val result = combiningDoubleTildeParser.parse(tracker)) {
                                    is CombiningParserResult.Success -> { this.append(result.result) }
                                    is CombiningParserResult.Failure -> {
                                        errors.add(createConversionError(result.error, tracker))
                                        tracker.pop()
                                        tracker.commit()
                                    }
                                }
                                continue@loop
                            }
                            COMBINING_DOUBLE_TILDE_SECOND_HALF -> {
                                // We found this by itself or there was another problem when parsing above and we popped the first half
                                errors.add(createConversionError("Unable to parse Combining Double Tilde Second Half", tracker))
                                tracker.pop()
                                tracker.commit()
                            }
                            else -> {
                                if (!parseDiacritics(tracker, diacritics)) {
                                    errors.add(createConversionError("Orphaned diacritics found: ${diacritics.joinToString { marc8CodeToHex(it.first) }}", tracker))
                                    tracker.commit()
                                    continue@loop
                                }
                            }
                        }
                    }
                    else -> {
                        tracker.pop()?.let { marc8 ->
                            getChar(marc8.toInt(), tracker).let { character ->
                                val toAppend = character ?: Marc8Fixes.replaceKnownMarc8EncodingIssues(marc8)

                                if (toAppend == null) {
                                    errors.add(createConversionError("Unknown MARC8 character found: ${marc8CodeToHex(marc8)}", tracker))
                                    tracker.commit()
                                } else {
                                    append(toAppend)
                                    tracker.commit()

                                    while (diacritics.isNotEmpty()) {
                                        append(diacritics.pop().second)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            toString()
        }

        val conversion = if (translateNcr) ncrTranslator.parse(convertedString) else convertedString

        return if (errors.isEmpty()) {
            CharacterConverterResult.Success(conversion)
        } else {
            CharacterConverterResult.WithErrors(conversion, errors)
        }
    }

    private fun parseEACC(tracker: CodeDataTracker, convertedBuilder: StringBuilder): Boolean {
        if (!loadedMultiByteCodeTable) {
            loadMultiByte()
        }

        tracker.pop()?.let { eacc1 ->
            tracker.pop()?.let { eacc2 ->
                tracker.pop()?.let { eacc3 ->
                    getMultiByteCharacter(eacc1, eacc2, eacc3)?.let {
                        convertedBuilder.append(it)
                        tracker.commit()
                        return true
                    }
                }
            }
        }
        tracker.rollback()
        return false
    }

    private fun isStartOfDiacritics(tracker: CodeDataTracker) = tracker.peek()?.let { isCombining(it.toInt(), tracker) } ?: false

    private fun parseDiacritics(tracker: CodeDataTracker, diacritics: ArrayDeque<Pair<MarcCode, Char>>): Boolean {
        diacritics.clear()

        do {
            var readDiacritic = false
            tracker.peek()?.let { peekedMarc8 ->
                if (isCombining(peekedMarc8.toInt(), tracker)) {
                    tracker.pop()?.let { poppedMarc8 ->
                        val poppedMarc8Code = poppedMarc8.toInt()
                        getChar(poppedMarc8Code, tracker)?.let { character ->
                            diacritics.push(poppedMarc8Code to character)
                            readDiacritic = true
                        }
                    }
                }
            } ?: break
        } while (readDiacritic)

        if (diacritics.isNotEmpty()) {
            if (tracker.peek() == ESCAPE_CHARACTER) {
                val currentTracker = tracker.getTrackerWithCurrentBuffer()
                if (escapeSequenceParser.parse(currentTracker)) {
                    currentTracker.peek()?.let { marc8 ->
                        getChar(marc8.toInt(), currentTracker)?.let {
                            // We have diacritics and a valid character after an ESC sequence following them
                            tracker.commit()
                            return true
                        }
                    }
                }
            } else {
                tracker.peek()?.let { marc8 ->
                    getChar(marc8.toInt(), tracker)?.let {
                        // We have diacritics and a valid character following them
                        tracker.commit()
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun isCombining(marcCode: MarcCode, tracker: CodeDataTracker): Boolean {
        return codeTable.isCombining(marcCode, tracker.g0, tracker.g1)
    }

    private fun getChar(marcCode: MarcCode, tracker: CodeDataTracker): Char? {
        return when(marcCode) {
            in 0x20..0x7E -> codeTable.getChar(marcCode, tracker.g0)
            in 0xA0..0xFE -> codeTable.getChar(marcCode, tracker.g1)
            else -> null
        }
    }

    private fun loadMultiByte() {
        codeTable = CodeTable(javaClass.getResourceAsStream("/codetables.xml"))
        loadedMultiByteCodeTable = true
    }

    private fun getMultiByteCharacter(first: Char, second: Char, third: Char): Char? {
        val marc8Code = String.format("%02X%02X%02X", first.toInt(), second.toInt(), third.toInt()).toInt(16)
        return codeTable.getChar(marc8Code, CJK_GRAPHIC_ISO_CODE)
    }

    private fun createConversionError(reason: String, tracker: CodeDataTracker): ConversionError {
        return ConversionError(reason, tracker.getEnclosingData())
    }
}