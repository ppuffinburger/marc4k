package org.marc4k.io.codec

import org.marc4k.ISO_8859_1
import org.marc4k.MarcError
import org.marc4k.MarcException
import org.marc4k.UTF_8
import org.marc4k.io.converter.CharacterConverter
import org.marc4k.io.converter.CharacterConverterResult
import org.marc4k.marc.MarcRecord
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

/**
 * Data decoder for MARC records.  This mostly simulates what MARC4J does.
 *
 * This decoder will selectively decode data in a MARC record based on the given [encoding] unless
 * a [converter] is supplied.
 */
class DefaultMarcDataDecoder(private var encoding: String = ISO_8859_1, private val converter: CharacterConverter? = null) : MarcDataDecoder() {
    init {
        encoding = parseEncoding(encoding)
    }

    override fun setApplyConverter(iso2709Record: Iso2709Record): Boolean = true

    override fun getDataAsString(fieldIndex: Int, fieldTag: String, bytes: ByteArray): String {
        if (converter != null && applyConverter) {
            return when (val converterResult = converter.convert(bytes)) {
                is CharacterConverterResult.Success -> converterResult.conversion
                is CharacterConverterResult.WithErrors -> {
                    recordErrors += MarcError.EncodingError(fieldIndex, fieldTag, converterResult.errors)
                    converterResult.conversion
                }
            }
        } else {
            return when (encoding) {
                UTF_8 -> {
                    bytes.toString(Charsets.UTF_8)
                }
                ISO_8859_1 -> {
                    bytes.toString(Charsets.ISO_8859_1)
                }
                else -> {
                    try {
                        bytes.toString(Charset.forName(encoding))
                    } catch (e: UnsupportedCharsetException) {
                        throw MarcException("Unsupported encoding", e)
                    }
                }
            }
        }
    }

    private fun parseEncoding(encoding: String): String {
        return when (encoding.uppercase()) {
            "ISO-8859-1", "ISO8859_1", "ISO_8859_1" -> ISO_8859_1
            "UTF8", "UTF-8" -> UTF_8
            else -> encoding
        }
    }
}

/**
 * Data encoder for MARC records.  This mostly simulates what MARC4J does.
 *
 * This encoder will selectively encode data in a MARC record based on the given [encoding] unless
 * a [converter] is supplied.
 */
class DefaultMarcDataEncoder(private var encoding: String = ISO_8859_1, private val converter: CharacterConverter? = null) : MarcDataEncoder() {
    init {
        encoding = parseEncoding(encoding)
    }

    override fun setApplyConverter(marcRecord: MarcRecord): Boolean = true

    override fun getDataAsBytes(data: String): ByteArray {
        if (converter != null && applyConverter) {
            return when (val converterResult = converter.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion.toByteArray(Charset.forName(encoding))
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } else {
            return when (encoding) {
                UTF_8 -> {
                    data.toByteArray(Charsets.UTF_8)
                }
                ISO_8859_1 -> {
                    data.toByteArray(Charsets.ISO_8859_1)
                }
                else -> {
                    try {
                        data.toByteArray(Charset.forName(encoding))
                    } catch (e: UnsupportedCharsetException) {
                        throw MarcException("Unsupported encoding", e)
                    }
                }
            }
        }
    }

    private fun parseEncoding(encoding: String): String {
        return when (encoding.uppercase()) {
            "ISO-8859-1", "ISO8859_1", "ISO_8859_1" -> ISO_8859_1
            "UTF8", "UTF-8" -> UTF_8
            else -> encoding
        }
    }
}