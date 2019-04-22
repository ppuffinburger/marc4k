package org.marc4k.io.marcxml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import org.marc4k.converter.marc8.Marc8ToUnicode
import org.marc4k.io.MarcStreamReader
import org.marc4k.marc.Leader
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import org.marc4k.marc.marc21.authority.AuthorityRecord
import org.marc4k.marc.marc21.bibliographic.BibliographicRecord
import org.marc4k.marc.marc21.classification.ClassificationRecord
import org.marc4k.marc.marc21.community.CommunityRecord
import org.marc4k.marc.marc21.holdings.HoldingsRecord
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.io.File
import javax.xml.transform.dom.DOMResult

internal class MarcXmlWriterTest {
    @Test
    fun `test with BibliographicRecord`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_bib_record.mrc")).use { reader ->
            record = reader.next()
        }

        val expected = File(javaClass.getResource("/records/MARCXML_bib_record.xml").toURI())

        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream, indent = true).use { writer ->
                writer.write(record)
            }

            assertThat(outputStream.toString("UTF-8")).isXmlEqualToContentOf(expected)
        }
    }

    @Test
    fun `test using converter`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_record_many_diacritics.mrc")).use { reader ->
            record = reader.next()
        }

        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream, indent = true).apply {
                converter = Marc8ToUnicode()
            }.use { writer ->
                writer.write(record)
            }

            val stringContent = outputStream.toString("UTF-8")
            assertThat(stringContent).contains(
                "the tilde in man\u0303ana",
                "the grave accent in tre\u0300s",
                "the acute accent in de\u0301sire\u0301e",
                "the circumflex in co\u0302te",
                "the macron in To\u0304kyo",
                "the breve in russkii\u0306",
                "the dot above in z\u0307aba",
                "the dieresis (umlaut) in Lo\u0308wenbra\u0308u"
            )
        }
    }

    @Test
    fun `test using converter and normalizeUnicode`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_record_many_diacritics.mrc")).use { reader ->
            record = reader.next()
        }

        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream, indent = true).apply {
                converter = Marc8ToUnicode()
            }.use { writer ->
                writer.write(record)
            }

            val stringContent = outputStream.toString("UTF-8")
            assertThat(stringContent).contains(
                "the tilde in mañana",
                "the grave accent in très",
                "the acute accent in désirée",
                "the circumflex in côte",
                "the macron in Tōkyo",
                "the breve in russkiĭ",
                "the dot above in żaba",
                "the dieresis (umlaut) in Löwenbräu"
            )
        }
    }

    @Test
    fun `test writing to a Result`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_bib_record.mrc")).use { reader ->
            record = reader.next()
        }

        val result = DOMResult()
        MarcXmlWriter(result).use { writer ->
            writer.write(record)
        }

        val document = result.node as Document
        val documentElement = document.documentElement
        val children = documentElement.childNodes
        val firstChild = children.item(0) as Element
        val leaders = firstChild.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "leader")
        val controlFields = firstChild.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "controlfield")
        val dataFields = firstChild.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "datafield")

        assertAll(
            { assertThat(documentElement.localName).isEqualTo("collection") },
            { assertThat(children.length).isEqualTo(1) },
            { assertThat(firstChild.localName).isEqualTo("record") },
            { assertThat(leaders.length).isEqualTo(1) },
            { assertThat(controlFields.length).isEqualTo(3) },
            { assertThat(dataFields.length).isEqualTo(12) }
        )
    }

    @Test
    fun `test writing to a Result with stylesheet as String`() {
        lateinit var record: Record
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_bib_record.mrc")).use {
            record = it.next()
        }

        System.setProperty("http.agent", "MARC4K")
        val stylesheetUrl = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl"

        val result = DOMResult()
        MarcXmlWriter(result, stylesheetUrl).use { writer ->
            writer.write(record)
        }

        val document = result.node as Document
        val documentElement = document.documentElement
        val children = documentElement.childNodes
        val firstChild = children.item(0) as Element
        val titles = firstChild.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "title")
        val names = firstChild.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "name")
        val physicalDescriptions = firstChild.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "physicalDescription")

        assertAll(
            { assertThat(documentElement.localName).isEqualTo("modsCollection") },
            { assertThat(children.length).isEqualTo(1) },
            { assertThat(firstChild.localName).isEqualTo("mods") },
            { assertThat(titles.length).isEqualTo(1) },
            { assertThat(names.length).isEqualTo(1) },
            { assertThat(physicalDescriptions.length).isEqualTo(1) }
        )
    }

    @Test
    fun `test writing bad characters in various fields`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_bib_record_bad_characters_in_various_fields.mrc")).use { reader ->
            record = reader.next()
        }

        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream, indent = true).apply {
                replaceNonXmlCharacters = true
            }.use { writer ->
                writer.write(record)
            }

            val stringContent = outputStream.toString("UTF-8")
            assertThat(stringContent).contains(
                """>01899cam &lt;U+0014&gt;22004458a 4500<""",
                """ind1="&lt;U+0014&gt;" ind2="&lt;U+0014&gt;"""",
                """code="&lt;U+0014&gt;">&lt;U+0014&gt; 2011035923""",
                """code="&lt;U+001f&gt;">9781410442444 (hbk.)"""
            )
        }
    }

    private val recordTypeLeaderTestData = listOf(
        'a' to "Bibliographic",
        'c' to "Bibliographic",
        'd' to "Bibliographic",
        'e' to "Bibliographic",
        'f' to "Bibliographic",
        'g' to "Bibliographic",
        'i' to "Bibliographic",
        'j' to "Bibliographic",
        'k' to "Bibliographic",
        'm' to "Bibliographic",
        'n' to "Bibliographic",
        'o' to "Bibliographic",
        'p' to "Bibliographic",
        'r' to "Bibliographic",
        't' to "Bibliographic",
        'u' to "Holdings",
        'v' to "Holdings",
        'x' to "Holdings",
        'y' to "Holdings",
        'z' to "Authority",
        'w' to "Classification",
        'q' to "Community",
        'b' to ""
    )

    @TestFactory
    fun `test record type using leader values`() = recordTypeLeaderTestData.map { (typeOfRecord, expected) ->
        val record = MarcRecord().apply {
            leader.typeOfRecord = typeOfRecord
        }
        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream).use { writer ->
                writer.write(record)
            }

            val stringContent = outputStream.toString("UTF-8")
            DynamicTest.dynamicTest("Testing with typeOfRecord of '$typeOfRecord' writes a record with ${if (expected.isEmpty()) "no type" else "type of '$expected'"}") {
                if (expected.isEmpty()) {
                    assertThat(stringContent).doesNotContain("<marc:record type=")
                } else {
                    assertThat(stringContent).contains("""<marc:record type="$expected">""")
                }
            }
        }
    }

    private val recordTypeClassTestData = listOf<Pair<Record, String>>(
        BibliographicRecord() to "Bibliographic",
        HoldingsRecord() to "Holdings",
        AuthorityRecord() to "Authority",
        ClassificationRecord() to "Classification",
        CommunityRecord() to "Community",
        TestRecord() to ""
    )

    @TestFactory
    fun `test record type using MARC21 classes`() = recordTypeClassTestData.map { (record, expected) ->
        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter(outputStream).use { writer ->
                writer.write(record)
            }

            val stringContent = outputStream.toString("UTF-8")
            DynamicTest.dynamicTest("Testing with typeOfRecord of '${record.javaClass}' writes a record with ${if (expected.isEmpty()) "no type" else "type of '$expected'"}") {
                if (expected.isEmpty()) {
                    assertThat(stringContent).doesNotContain("<marc:record type=")
                } else {
                    assertThat(stringContent).contains("""<marc:record type="$expected">""")
                }
            }
        }
    }

    @Test
    fun `test using writeSingleRecord()`() {
        lateinit var record: MarcRecord
        MarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_bib_record.mrc")).use { reader ->
            record = reader.next()
        }

        val expected = File(javaClass.getResource("/records/MARCXML_bib_record_no_collection.xml").toURI())

        ByteArrayOutputStream().use { outputStream ->
            MarcXmlWriter.writeSingleRecord(record, outputStream, indent = true)

            assertThat(outputStream.toString("UTF-8")).isXmlEqualToContentOf(expected)
        }
    }


    private class TestLeader : Leader() { override fun setData(data: String) {} }
    private class TestRecord(override val leader: Leader = TestLeader()) : Record()
}