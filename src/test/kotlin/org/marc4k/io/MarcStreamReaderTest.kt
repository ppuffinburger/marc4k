package org.marc4k.io

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class MarcStreamReaderTest {
    @Test
    fun `test with ISO-8859-5 encoding`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/ISO-8859-5_bib_record.mrc"), "ISO-8859-5").use { reader ->
            val record = reader.next()
            val author = record.dataFields.first { it.tag == "100" }
            assertEquals("100 1  ‡aБуйда, Юрий.", author.toString())
        }
    }

    @Test
    fun `test with MARC8 encoding`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_records.mrc"), "MARC8").use { reader ->
            val record = reader.next()
            val author100 = record.dataFields.first { it.tag == "100" }
            val author880 = record.dataFields.first { field -> field.tag == "880" && field.subfields.any { subfield -> subfield.name == '6' && subfield.data.startsWith("100-01/") } }
            assertEquals("100 1  ‡6880-01‡aBuĭda, I͡U͡riĭ.", author100.toString())
            assertEquals("880 1  ‡6100-01/(N‡aБуйда, Юрий.", author880.toString())
        }
    }

    @Test
    fun `test with explicit UTF8 encoding`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), "UTF-8").use { reader ->
            val record = reader.next()
            assertNotNull(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" })
        }
    }

    @Test
    fun `test with implicit UTF8 encoding`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc")).use { reader ->
            val record = reader.next()
            assertNotNull(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" })
        }
    }

    @Test
    fun `test with MARC8 encoding (CJK)`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_auth_record.mrc"), "MARC-8").use { reader ->
            val record = reader.next()
            assertNotNull(record.dataFields.find { it.tag == "400" && it.toString() == "400 0  ‡aテンジン·ギャツォ,‡cダライ·ラマ14世,‡d1935-" })
        }
    }

    @Test
    fun `test with iterator`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_records.mrc"), "MARC8").use { reader ->
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertEquals("100 1  ‡6880-01‡aBuĭda, I͡U͡riĭ.", record.dataFields.first { it.tag == "100" }.toString())
                        count++
                    }
                    1 -> {
                        assertEquals("100 1  ‡6880-03‡aRubina, Dina.", record.dataFields.first { it.tag == "100" }.toString())
                        count++
                    }
                }
            }
            assertEquals(2, count)
        }
    }

    @Test
    fun `test with unordered directory entries`() {
        MarcStreamReader(javaClass.getResourceAsStream("/records/unordered_directory_entries.mrc")).use { reader ->
            val record = reader.next()
            assertEquals("001", record.controlFields[0].tag)
        }
    }
}