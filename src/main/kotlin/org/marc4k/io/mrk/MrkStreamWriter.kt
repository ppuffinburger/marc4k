package org.marc4k.io.mrk

import org.marc4k.MarcException
import org.marc4k.io.MarcWriter
import org.marc4k.io.converter.CharacterConverterResult
import org.marc4k.io.converter.marc8.UnicodeToMarc8
import org.marc4k.marc.Record
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/**
 * A [MarcWriter] that writes a [Record] in MARC text format to an [OutputStream].
 *
 * @param[output] the [OutputStream] to write the MARC data to.
 * @property[outputUnicode] true if writing Unicode to output.  Defaults to true.
 */
class MrkStreamWriter(output: OutputStream, private val outputUnicode: Boolean = true) : MarcWriter {
    private val writer: PrintWriter = PrintWriter(OutputStreamWriter(output, StandardCharsets.UTF_8))
    private val converter: UnicodeToMarc8 by lazy { UnicodeToMarc8() }

    /**
     * Writes a [Record] to the underlying [PrintWriter].
     *
     * @throws[MarcException] if an outputting MARC8 and the conversion failed.
     */
    override fun write(record: Record) {
        val recordString = with(StringBuilder()) {
            append("=LDR  ${record.leader.getData()}$MRK_LINE_SEPARATOR")

            for (controlField in record.controlFields) {
                append("=${controlField.tag}  ${controlField.data.replace(' ', '\\')}$MRK_LINE_SEPARATOR")
            }

            for (dataField in record.dataFields) {
                val indicator1 = if (dataField.indicator1 == ' ') '\\' else dataField.indicator1
                val indicator2 = if (dataField.indicator2 == ' ') '\\' else dataField.indicator2
                append("=${dataField.tag}  $indicator1$indicator2")

                for (subfield in dataField.subfields) {
                    val data = if (outputUnicode) {
                        MrkTransliterator.toMrk(subfield.data)
                    } else {
                        when (val converterResult = converter.convert(subfield.data)) {
                            is CharacterConverterResult.Success -> MrkTransliterator.toMrk(converterResult.conversion, false)
                            is CharacterConverterResult.WithErrors -> throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                        }
                    }
                    append("\$${subfield.name}$data")
                }
                append(MRK_LINE_SEPARATOR)
            }
            append(MRK_LINE_SEPARATOR)

            toString()
        }
        writer.append(recordString)
        writer.flush()
    }

    /**
     * Closes the underlying [PrintWriter].
     */
    override fun close() {
        writer.flush()
        writer.close()
    }

    companion object {
        private const val MRK_LINE_SEPARATOR = "\r\n"
    }
}