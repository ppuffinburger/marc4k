package org.marc4k.io.mrk

import org.marc4k.MarcException
import org.marc4k.converter.CharacterConverterResult
import org.marc4k.converter.marc8.Marc8ToUnicode
import org.marc4k.io.MarcReader
import org.marc4k.marc.ControlField
import org.marc4k.marc.DataField
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Subfield
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

class MrkStreamReader(input: InputStream) : MarcReader {
    private val input = Scanner(BufferedInputStream(input), StandardCharsets.UTF_8.name()).apply { useDelimiter(delimiterPattern) }
    private val converter: Marc8ToUnicode by lazy { Marc8ToUnicode() }

    override fun hasNext(): Boolean {
        while (input.hasNextLine()) {
            if (input.hasNext(leaderPattern)) {
                return true
            }
            input.nextLine()
        }
        return false
    }

    override fun next(): MarcRecord {
        var hasHighBitCharacters = false

        val lines = mutableListOf<String>(input.nextLine())

        while (input.hasNextLine()) {
            if (input.hasNext(leaderPattern)) {
                return parse(lines, hasHighBitCharacters)
            } else {
                val line = input.nextLine()
                if (line.isNotBlank()) {
                    // Normalize the line.   MARC4J and MARC4K without this do the same thing, but the record contents may not
                    // display correctly (at least on macOS).  This corrects the display.  May make it an option or if it is
                    // OS specific then check the OS and only do it on OS's that require it.
                    lines.add(Normalizer.normalize(line, Normalizer.Form.NFC))

                    if (!hasHighBitCharacters && line.any { character -> character >= '\u007F' }) {
                        hasHighBitCharacters = true
                    }
                }
            }
        }

        return parse(lines, hasHighBitCharacters)
    }

    override fun close() {
        input.close()
    }

    private fun parse(lines: List<String>, hasHighBitCharacters: Boolean): MarcRecord {
        val record = MarcRecord()

        for (line in lines) {
            if (line.trim().isEmpty()) {
                continue
            }

            val tag = line.substring(1..3)

            if (tag.equals("LDR", true) || tag == "000") {
                record.leader.setData(line.substring(6))
            } else if (tag.startsWith("00")) {
                record.controlFields.add(ControlField(tag, line.substring(6).replace('\\', ' ')))
            } else {
                val data = line.substring(6)

                val indicator1 = if (data[0] == '\\') ' ' else data[0]
                val indicator2 = if (data[1] == '\\') ' ' else data[1]

                if (!isValidIndicator(indicator1) || !isValidIndicator(indicator2)) {
                    throw MarcException("Wrong indicator format. It has to be a number or a space.")
                }

                val field = DataField(tag, indicator1, indicator2)

                for (subfield in data.substring(3).split("$")) {
                    var subfieldData = MrkTransliterator.fromMrk(subfield.substring(1))
                    if (!hasHighBitCharacters && subfieldData.any { character -> character !in '\u0020'..'\u007F' }) {
                        val converterResult = converter.convert(subfieldData)
                        subfieldData = when (converterResult) {
                            is CharacterConverterResult.Success -> converterResult.conversion
                            is CharacterConverterResult.WithErrors -> throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                        }
                    }
                    field.subfields.add(Subfield(subfield[0], subfieldData))
                }

                record.dataFields.add(field)
            }
        }

        return record
    }

    private fun isValidIndicator(indicator: Char): Boolean {
        return indicator == ' ' || indicator in '0'..'9'
    }

    companion object {
        private val delimiterPattern = Pattern.compile("^=(LDR|\\d{3}) .*(\\r\\n|[\\n\\r\u2028\u2029\u0085])$")
        private val leaderPattern = Pattern.compile("=(?:LDR|000).*", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
    }
}