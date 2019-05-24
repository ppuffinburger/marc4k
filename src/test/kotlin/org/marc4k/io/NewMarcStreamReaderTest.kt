package org.marc4k.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.converter.marc8.Marc8ToUnicode
import org.marc4k.io.codec.DefaultMarcDataDecoder
import org.marc4k.io.codec.Marc21DataDecoder

internal class NewMarcStreamReaderTest {
    @Test
    fun `test with ISO-8859-5 encoding using default decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/ISO-8859-5_bib_record.mrc"), DefaultMarcDataDecoder("ISO-8859-5")).use { reader ->
            val record = reader.next()
            val author = record.dataFields.first { it.tag == "100" }
            assertThat(author.toString()).isEqualTo("100 1  ‡aБуйда, Юрий.")
        }
    }

    @Test
    fun `test with MARC8 encoding using default decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_records.mrc"), DefaultMarcDataDecoder(converter = Marc8ToUnicode())).use { reader ->
            val record = reader.next()
            val author100 = record.dataFields.first { it.tag == "100" }
            val author880 = record.dataFields.first { field -> field.tag == "880" && field.subfields.any { subfield -> subfield.name == '6' && subfield.data.startsWith("100-01/") } }
            assertAll(
                { assertThat(author100.toString()).isEqualTo("100 1  ‡6880-01‡aBuĭda, I͡Uriĭ.") },
                { assertThat(author880.toString()).isEqualTo("880 1  ‡6100-01/(N‡aБуйда, Юрий.") }
            )
        }
    }

    @Test
    fun `test with explicit UTF8 encoding using default decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), DefaultMarcDataDecoder("UTF-8")).use { reader ->
            val record = reader.next()
            assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
        }
    }

    @Test
    fun `test with MARC8 encoding (CJK) using default decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_auth_record.mrc"), DefaultMarcDataDecoder(converter = Marc8ToUnicode())).use { reader ->
            val record = reader.next()
            assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
        }
    }

    @Test
    fun `test with MARC8 encoding using Marc21 decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_records.mrc"), Marc21DataDecoder()).use { reader ->
            val record = reader.next()
            val author100 = record.dataFields.first { it.tag == "100" }
            val author880 = record.dataFields.first { field -> field.tag == "880" && field.subfields.any { subfield -> subfield.name == '6' && subfield.data.startsWith("100-01/") } }
            assertAll(
                { assertThat(author100.toString()).isEqualTo("100 1  ‡6880-01‡aBuĭda, I͡Uriĭ.") },
                { assertThat(author880.toString()).isEqualTo("880 1  ‡6100-01/(N‡aБуйда, Юрий.") }
            )
        }
    }

    @Test
    fun `test with UTF8 encoding using Marc21 decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), Marc21DataDecoder()).use { reader ->
            val record = reader.next()
            assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
        }
    }

    @Test
    fun `test with MARC8 encoding (CJK) using Marc21 decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_auth_record.mrc"), Marc21DataDecoder()).use { reader ->
            val record = reader.next()
            assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
        }
    }

    @Test
    fun `test with MIXED encodings in file using Marc21 decoder`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MIXED_auth_records.mrc"), Marc21DataDecoder()).use { reader ->
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
                        count++
                    }
                    1 -> {
                        assertThat(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" }).isNotNull
                        count++
                    }
                }
            }
            assertThat(count).isEqualTo(2)
        }
    }

    @Test
    fun `test with iterator`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_records.mrc"), DefaultMarcDataDecoder(converter = Marc8ToUnicode())).use { reader ->
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(record.dataFields.first { it.tag == "100" }.toString()).isEqualTo("100 1  ‡6880-01‡aBuĭda, I͡Uriĭ.")
                        count++
                    }
                    1 -> {
                        assertThat(record.dataFields.first { it.tag == "100" }.toString()).isEqualTo("100 1  ‡6880-03‡aRubina, Dina.")
                        count++
                    }
                }
            }
            assertThat(count).isEqualTo(2)
        }
    }

    @Test
    fun `test with unordered directory entries`() {
        NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_record_unordered_directory_entries.mrc"), DefaultMarcDataDecoder(converter = Marc8ToUnicode())).use { reader ->
            val record = reader.next()
            assertThat(record.controlFields[0].tag).isEqualTo("001")
        }
    }
}