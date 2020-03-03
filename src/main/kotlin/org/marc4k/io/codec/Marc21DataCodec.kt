package org.marc4k.io.codec

import org.marc4k.MarcError
import org.marc4k.MarcException
import org.marc4k.io.converter.CharacterConverterResult
import org.marc4k.io.converter.marc8.Marc8ToUnicode
import org.marc4k.io.converter.marc8.UnicodeToMarc8
import org.marc4k.marc.MarcRecord

/**
 * Data decoder for MARC21 records.
 *
 * This decoder will selectively decode data in a MARC record based on the Leader.
 *
 * Leader/09 = ' ' all data gets put through the [converter].
 * Leader/09 = 'a' all data gets converted using the UTF-8 character set.
 */
class Marc21DataDecoder(private val converter: Marc8ToUnicode = Marc8ToUnicode()) : MarcDataDecoder() {
    override fun setApplyConverter(iso2709Record: Iso2709Record): Boolean {
        return iso2709Record.leader[CHARACTER_CODING_SCHEME_POSITION] == MARC8_SCHEME_CHARACTER
    }

    override fun getDataAsString(fieldIndex: Int, fieldTag: String, bytes: ByteArray): String {
        return if (applyConverter) {
            when (val converterResult = converter.convert(bytes)) {
                is CharacterConverterResult.Success -> converterResult.conversion
                is CharacterConverterResult.WithErrors -> {
                    recordErrors += MarcError.EncodingError(fieldIndex, fieldTag, converterResult.errors)
                    converterResult.conversion
                }
            }
        } else {
            bytes.toString(Charsets.UTF_8)
        }
    }

    companion object {
        private const val CHARACTER_CODING_SCHEME_POSITION = 9
        private const val MARC8_SCHEME_CHARACTER = ' '
    }
}

/**
 * Data encoder for MARC21 records.
 *
 * This encoder will selectively encode data in a MARC record based on the Leader.
 *
 * ImplementationDefined1/02 = ' ' all data get put through the [converter].
 * ImplementationDefined1/02 = 'a' all data gets converted using the UTF-8 character set.
 */
class Marc21DataEncoder(private val converter: UnicodeToMarc8 = UnicodeToMarc8()) : MarcDataEncoder() {
    override fun setApplyConverter(marcRecord: MarcRecord): Boolean {
        return marcRecord.leader.implementationDefined1[2] == MARC8_SCHEME_CHARACTER
    }

    override fun getDataAsBytes(data: String): ByteArray {
        return if (applyConverter) {
            when (val converterResult = converter.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion.toByteArray(Charsets.ISO_8859_1)
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } else {
            data.toByteArray(Charsets.UTF_8)
        }
    }

    companion object {
        private const val MARC8_SCHEME_CHARACTER = ' '
    }
}