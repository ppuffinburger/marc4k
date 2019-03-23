package org.marc4k.converter.marc8

import org.marc4k.converter.*
import org.marc4k.converter.marc8.CombiningDoubleInvertedBreveParser.Companion.COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF
import org.marc4k.converter.marc8.CombiningDoubleInvertedBreveParser.Companion.COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF
import org.marc4k.converter.marc8.CombiningDoubleTildeParser.Companion.COMBINING_DOUBLE_TILDE_FIRST_HALF
import org.marc4k.converter.marc8.CombiningDoubleTildeParser.Companion.COMBINING_DOUBLE_TILDE_SECOND_HALF
import org.marc4k.marc.ESCAPE_CHARACTER
import org.marc4k.marc.SPACE_CHARACTER
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class Marc8ToUnicode : CharacterConverter {
    private val translateNcr: Boolean
    private val ncrParser = NcrParser()
    private val escapeSequenceParser = Marc8EscapeSequenceParser()

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
            val diacritics = ArrayDeque<Char>()
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
                            errors.add(createConversionError("C0 control character found (${String.format("0x%02x", it.toInt())}), which is invalid, deleting it.", tracker))
                        }
                        tracker.commit()
                        continue@loop
                    }
                    in C1_CONTROL_CHARACTER_RANGE -> {
                        if (peeked !in NON_SORT_BEGIN_CHARACTER..NON_SORT_END_CHARACTER && peeked !in JOINER_CHARACTER..NON_JOINER_CHARACTER) {
                            tracker.pop()?.let {
                                errors.add(createConversionError("C1 control character found (${String.format("0x%02x", it.toInt())}), which is invalid, deleting it.", tracker))
                            }
                            tracker.commit()
                            continue@loop
                        }
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
                                    errors.add(createConversionError("Unknown MARC8 character found: ${String.format("0x%02x", marc8.toInt())}", tracker))
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
                            when(val result = CombiningDoubleInvertedBreveParser().parse(tracker)) {
                                is ParsedData -> { this.append(result.parsedData) }
                                is Error -> {
                                    errors.add(createConversionError("Unable to parse Combining Double Inverted Breve", tracker))
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
                            when(val result = CombiningDoubleTildeParser().parse(tracker)) {
                                is ParsedData -> { this.append(result.parsedData) }
                                is Error -> {
                                    errors.add(createConversionError("Unable to parse Combining Double Tilde", tracker))
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
                                errors.add(createConversionError("Orphaned diacritics found: ${diacritics.joinToString { String.format("U+%04x", it.toInt()) }}", tracker))
                                // TODO : do I want to replace instead of discarding?
                                tracker.commit()
                                continue@loop
                            }
                        }
                    }
                } else {
                    tracker.pop()?.let { marc8 ->
                        getChar(marc8.toInt(), tracker).let { character ->
                            val toAppend = character ?: Fixes.replaceKnownMarc8EncodingIssues(marc8)

                            if (toAppend == null) {
                                errors.add(createConversionError("Unknown MARC8 character found: ${String.format("0x%02x", marc8.toInt())}", tracker))
                                // TODO : do I want to replace instead of discarding?
                                tracker.commit()
                            } else {
                                append(toAppend)
                                tracker.commit()

                                while (diacritics.isNotEmpty()) {
                                    append(diacritics.pop())
                                }
                            }
                        }
                    }
                }
            }

            toString()
        }

        val conversion = if (translateNcr) ncrParser.parse(convertedString) else convertedString

        return if (errors.isEmpty()) {
            NoErrors(conversion)
        } else {
            WithErrors(conversion, errors)
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

    private fun isStartOfDiacritics(tracker: Marc8Tracker): Boolean {
        return tracker.peek()?.let { isCombining(it.toInt(), tracker) } ?: false
    }

    private fun parseDiacritics(tracker: Marc8Tracker, diacritics: ArrayDeque<Char>): Boolean {
        diacritics.clear()

        do {
            var readDiacritic = false
            tracker.peek()?.let { peekedMarc8 ->
                if (isCombining(peekedMarc8.toInt(), tracker)) {
                    tracker.pop()?.let { poppedMarc8 ->
                        getChar(poppedMarc8.toInt(), tracker)?.let { character ->
                            diacritics.push(character)
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
        return if (marc8Code <= 0x7E) {
            codeTable.getChar(marc8Code, tracker.g0)
        } else {
            codeTable.getChar(marc8Code, tracker.g1)
        }
    }

    private fun loadMultiByte() {
        codeTable = CodeTable(javaClass.getResourceAsStream("/codetables.xml"))
        loadedMultiByteCodeTable = true
    }

    private fun getMultiByteCharacter(first: Char, second: Char, third: Char): Char? {
        val marc8Code = String.format("%02X%02X%02X", first.toInt(), second.toInt(), third.toInt()).toInt(16)
        return codeTable.getChar(marc8Code, CJK_ISO_CODE)
    }

    private fun createConversionError(reason: String, tracker: Marc8Tracker): ConversionError {
        return ConversionError(reason, tracker.getSurroundingData())
    }

    companion object {
        private const val CJK_ISO_CODE = 0x31
        private const val NON_SORT_BEGIN_CHARACTER = '\u0088'
        private const val NON_SORT_END_CHARACTER = '\u0089'
        private const val JOINER_CHARACTER = '\u008D'
        private const val NON_JOINER_CHARACTER = '\u008E'
        private val C0_CONTROL_CHARACTER_RANGE = '\u0000'..'\u001F'
        private val C1_CONTROL_CHARACTER_RANGE = '\u0080'..'\u009F'
    }
}