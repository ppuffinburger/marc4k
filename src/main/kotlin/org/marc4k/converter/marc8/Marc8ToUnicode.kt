package org.marc4k.converter.marc8

import org.marc4k.*
import org.marc4k.converter.*
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class Marc8ToUnicode : CharacterConverter {
    private val translateNcr: Boolean
    private val ncrTranslator = NcrTranslator()
    private val escapeSequenceParser = Marc8EscapeSequenceParser()
    private val combiningDoubleInvertedBreveParser = CombiningDoubleInvertedBreveParser()
    private val combiningDoubleTildeParser = CombiningDoubleTildeParser()

    private var codeTable: CodeTable
    private var loadedMultiByteCodeTable = false

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

    override fun outputsUnicode() = true

    override fun convert(data: CharArray): CharacterConverterResult {
        val errors = mutableListOf<ConversionError>()
        val convertedString = with(StringBuilder()) {
            val diacritics = ArrayDeque<Pair<Marc8Code, Char>>()
            val tracker = Marc8Tracker(data)

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

                if (tracker.isEACC()) {
                    if (!loadedMultiByteCodeTable) {
                        loadMultiByte()
                    }

                    if (!parseEACC(tracker, this)) {
                        tracker.pop()?.let { marc8 ->
                            getChar(marc8.toInt(), tracker).let { character ->
                                if (character == null) {
                                    errors.add(createConversionError("Unknown MARC8 character found: ${marc8CodeToHex(marc8)}", tracker))
                                    // TODO : do I want to replace instead of discarding?
                                    tracker.commit()
                                } else {
                                    append(character)
                                    tracker.commit()

                                    while (diacritics.isNotEmpty()) {
                                        append(diacritics.pop())
                                    }
                                }
                            }
                        }
                    }
                } else if (isStartOfDiacritics(tracker)) {
                    when(tracker.peek()) {
                        COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF -> {
                            when(val result = combiningDoubleInvertedBreveParser.parse(tracker)) {
                                is CombiningParserResult.Success -> { this.append(result.result) }
                                is CombiningParserResult.Failure -> {
                                    errors.add(createConversionError(result.error, tracker))
                                    // TODO : should I just replace or try to fix instead of discarding?
                                    tracker.pop()
                                    tracker.commit()
                                }
                            }
                            continue@loop
                        }
                        COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF -> {
                            // We found this by itself or there was another problem when parsing above and we popped the first half
                            errors.add(createConversionError("Unable to parse Combining Double Inverted Breve Second Half", tracker))
                            // TODO : do I want to replace instead of discarding?
                            tracker.pop()
                            tracker.commit()
                        }
                        COMBINING_DOUBLE_TILDE_FIRST_HALF -> {
                            when(val result = combiningDoubleTildeParser.parse(tracker)) {
                                is CombiningParserResult.Success -> { this.append(result.result) }
                                is CombiningParserResult.Failure -> {
                                    errors.add(createConversionError(result.error, tracker))
                                    // TODO : should I just replace or try to fix instead of discarding?
                                    tracker.pop()
                                    tracker.commit()
                                }
                            }
                            continue@loop
                        }
                        COMBINING_DOUBLE_TILDE_SECOND_HALF -> {
                            // We found this by itself or there was another problem when parsing above and we popped the first half
                            errors.add(createConversionError("Unable to parse Combining Double Tilde Second Half", tracker))
                            // TODO : do I want to replace instead of discarding?
                            tracker.pop()
                            tracker.commit()
                        }
                        else -> {
                            if (!parseDiacritics(tracker, diacritics)) {
                                errors.add(createConversionError("Orphaned diacritics found: ${diacritics.joinToString { marc8CodeToHex(it.first) }}", tracker))
                                // TODO : do I want to replace instead of discarding?
                                tracker.commit()
                                continue@loop
                            }
                        }
                    }
                } else {
                    tracker.pop()?.let { marc8 ->
                        getChar(marc8.toInt(), tracker).let { character ->
                            val toAppend = character ?: Marc8Fixes.replaceKnownMarc8EncodingIssues(marc8)

                            if (toAppend == null) {
                                errors.add(createConversionError("Unknown MARC8 character found: ${marc8CodeToHex(marc8)}", tracker))
                                // TODO : do I want to replace instead of discarding?
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

            toString()
        }

        val conversion = if (translateNcr) ncrTranslator.parse(convertedString) else convertedString

        return if (errors.isEmpty()) {
            CharacterConverterResult.Success(conversion)
        } else {
            CharacterConverterResult.WithErrors(conversion, errors)
        }
    }

    private fun parseEACC(tracker: Marc8Tracker, convertedBuilder: StringBuilder): Boolean {
        tracker.pop()?.let { eacc1 ->
            tracker.pop()?.let { eacc2 ->
                tracker.pop()?.let { eacc3 ->
                    getMultiByteCharacter(eacc1, eacc2, eacc3).let {
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

    private fun isStartOfDiacritics(tracker: Marc8Tracker) = tracker.peek()?.let { isCombining(it.toInt(), tracker) } ?: false

    private fun parseDiacritics(tracker: Marc8Tracker, diacritics: ArrayDeque<Pair<Marc8Code, Char>>): Boolean {
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
            tracker.peek()?.let { marc8 ->
                getChar(marc8.toInt(), tracker)?.let {
                    // We have diacritics and a valid character following them
                    tracker.commit()
                    return true
                }
            }
        }

        return false
    }

    private fun isCombining(marc8Code: Marc8Code, tracker: Marc8Tracker): Boolean {
        return codeTable.isCombining(marc8Code, tracker.g0, tracker.g1)
    }

    private fun getChar(marc8Code: Marc8Code, tracker: Marc8Tracker): Char? {
        return when(marc8Code) {
            in 0x20..0x7E -> codeTable.getChar(marc8Code, tracker.g0)
            in 0xA0..0xFE -> codeTable.getChar(marc8Code, tracker.g1)
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

    private fun createConversionError(reason: String, tracker: Marc8Tracker): ConversionError {
        return ConversionError(reason, tracker.getEnclosingData())
    }
}