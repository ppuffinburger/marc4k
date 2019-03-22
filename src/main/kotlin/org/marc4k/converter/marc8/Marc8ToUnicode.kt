package org.marc4k.converter.marc8

import org.marc4k.converter.*
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
                when (tracker.peek()) {
                    ESCAPE_CHARACTER -> {
                        if (!escapeSequenceParser.parse(tracker)) {
                            errors.add(createConversionError("Unknown character set", tracker))
                            tracker.advanceToEnd()
                        }
                        continue@loop
                    }
                    SPACE_CHARACTER -> {
                        tracker.pop()?.let { append(it) }
                        tracker.commit()
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
                                    errors.add(createConversionError("Unknown MARC8 character found: ${String.format("0x%02x", marc8.toInt())}", tracker))
                                    tracker.advanceToEnd()
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
                } else if (isDiacritic(tracker)) {
                    when(tracker.peek()) {
                        COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF -> {
                            val result = CombiningDoubleInvertedBreveParser().parse(tracker)
                            when(result) {
                                is ParsedData -> { this.append(result.parsedData) }
                                is Error -> {
                                    errors.add(createConversionError("Unable to parse Combining Double Inverted Breve", tracker))
                                    tracker.advanceToEnd()
                                }
                            }
                            continue@loop
                        }
                        COMBINING_DOUBLE_TILDE_FIRST_HALF -> {
                            val result = CombiningDoubleTildeParser().parse(tracker)
                            when(result) {
                                is ParsedData -> { this.append(result.parsedData) }
                                is Error -> {
                                    errors.add(createConversionError("Unable to parse Combining Double Tilde", tracker))
                                    tracker.advanceToEnd()
                                }
                            }
                            continue@loop
                        }
                        else -> {
                            if (!parseDiacritics(tracker, diacritics)) {
                                // TODO : orphaned diacritics found
                                errors.add(createConversionError("Orphaned diacritics found", tracker))
                                tracker.advanceToEnd()
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
                                tracker.advanceToEnd()
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

    private fun createConversionError(reason: String, tracker: Marc8Tracker): ConversionError {
        return ConversionError(reason, tracker.offset, tracker.getSurroundingData())
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

    private fun isDiacritic(tracker: Marc8Tracker): Boolean {
        val peeked = tracker.peek()
        return if (peeked != null) {
            isCombining(peeked.toInt(), tracker)
        } else {
            false
        }
    }

    private fun parseDiacritics(tracker: Marc8Tracker, diacritics: ArrayDeque<Char>): Boolean {
        var valid = true

        diacritics.clear()

        var current = tracker.pop()
        while (current != null && isCombining(current.toInt(), tracker)) {
            val character = getChar(current.toInt(), tracker)

            if (character == null) {
                valid = false
                break
            }

            diacritics.push(character)
            current = tracker.pop()
        }

        if (diacritics.isNotEmpty()) {
            valid = if (current == null) {
                false
            } else {
                val character = getChar(current.toInt(), tracker)

                if (character == null) {
                    tracker.undo()
                    false
                } else {
                    tracker.undo()
                    tracker.commit()
                    true
                }
            }
        } else {
            tracker.rollback()
        }

        return valid
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

    companion object {
        private const val CJK_ISO_CODE = 0x31
        private const val COMBINING_DOUBLE_INVERTED_BREVE_FIRST_HALF = '\u00EB'
        private const val COMBINING_DOUBLE_TILDE_FIRST_HALF = '\u00FA'
    }
}