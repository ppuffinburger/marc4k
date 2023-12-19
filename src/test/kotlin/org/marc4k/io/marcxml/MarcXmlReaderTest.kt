package org.marc4k.io.marcxml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamSource

internal class MarcXmlReaderTest {
    @Test
    fun `test valid MARCXML`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00714cam a2200205 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    12883376") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aSummerland /‡cMichael Chabon.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(12) },
                { assertThat(record.errors).isEmpty() }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML that contains no collection element`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_no_collection_element.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00757cam a22002055a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    121564") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aCinema and architecture : from historical to digital /‡cedited by Francois Penz and Maureen Thomas.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(12) },
                { assertThat(record.errors.size).isEqualTo(2) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML that contains no collection element with comment`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_no_collection_element_with_comment.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00757cam a22002055a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    121564") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aCinema and architecture : from historical to digital /‡cedited by Francois Penz and Maureen Thomas.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(12) },
                { assertThat(record.errors.size).isEqualTo(2) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with field missing indicator`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_field_missing_indicator.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00714cam a2200205 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    12883376") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aSummerland /‡cMichael Chabon.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(12) },
                { assertThat(record.errors.size).isEqualTo(1) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with subfields element`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_subfields_element.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00759cam a2200229 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    11939876") },
                { assertThat(record.dataFields.find { it.tag == "020" }?.toString()).isEqualTo("020    ") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 14 ‡aThe amazing adventures of Kavalier and Clay :‡ba novel /‡cMichael Chabon.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(14) },
                { assertThat(record.errors.size).isEqualTo(1) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with missing subfield name`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_missing_subfield_name.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00759cam a2200229 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    11939876") },
                { assertThat(record.dataFields.find { it.tag == "020" }?.toString()).isEqualTo("020    ") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 14 ‡aThe amazing adventures of Kavalier and Clay :‡ba novel /‡cMichael Chabon.") },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(14) },
                { assertThat(record.errors.size).isEqualTo(1) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with missing control field tag`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_missing_control_field_tag.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00759cam a2200229 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    11939876") },
                { assertThat(record.controlFields.none { it.tag == "005" }).isTrue() },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 14 ‡aThe amazing adventures of Kavalier and Clay :‡ba novel /‡cMichael Chabon.") },
                { assertThat(record.controlFields.size).isEqualTo(2) },
                { assertThat(record.dataFields.size).isEqualTo(14) },
                { assertThat(record.errors.size).isEqualTo(1) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with missing data field tag`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_record_missing_data_field_tag.xml")!!).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00759cam a2200229 a 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    11939876") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 14 ‡aThe amazing adventures of Kavalier and Clay :‡ba novel /‡cMichael Chabon.") },
                { assertThat(record.dataFields.count { it.tag == "655" }).isEqualTo(1) },
                { assertThat(record.controlFields.size).isEqualTo(3) },
                { assertThat(record.dataFields.size).isEqualTo(13) },
                { assertThat(record.errors.size).isEqualTo(1) }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    fun `test MARCXML with iterator`() {
        MarcXmlReader(javaClass.getResourceAsStream("/records/MARCXML_bib_records.xml")!!).use { reader ->
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(record.dataFields.first { it.tag == "245" }.toString()).isEqualTo("245 14 ‡aThe amazing adventures of Kavalier and Clay :‡ba novel /‡cMichael Chabon.")
                        count++
                    }
                    1 -> {
                        assertThat(record.dataFields.first { it.tag == "245" }.toString()).isEqualTo("245 10 ‡aSummerland /‡cMichael Chabon.")
                        count++
                    }
                }
            }
            assertThat(count).isEqualTo(2)
        }
    }

    @Test
    @Disabled("Disabled until I have time to look into why this fails")
    fun `test MODS transformation using String`() {
        // loc.gov pages are on Cloudflare and require the header User-Agent.  So far setting the property is the only way I found to get around it
        System.setProperty("http.agent", "MARC4K")

        val styleSheet = "https://www.loc.gov/standards/mods/v3/MODS3-4_MARC21slim_XSLT2-0.xsl"
        MarcXmlReader(javaClass.getResourceAsStream("/records/MODS_bib_record.xml")!!, styleSheet).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00000nkm  2200000uu 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    archives/cushman/P07803") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aTelescope Peak from Zabriskie Point") },
                { assertThat(record.controlFields.size).isEqualTo(2) },
                { assertThat(record.dataFields.size).isEqualTo(17) },
                { assertThat(record.errors).isEmpty() }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    @Disabled("Disabled until I have time to look into why this fails")
    fun `test MODS transformation using Source`() {
        // loc.gov pages are on Cloudflare and require the header User-Agent.  So far setting the property is the only way I found to get around it
        System.setProperty("http.agent", "MARC4K")

        val styleSheet = "https://www.loc.gov/standards/mods/v3/MODS3-4_MARC21slim_XSLT2-0.xsl"
        MarcXmlReader(javaClass.getResourceAsStream("/records/MODS_bib_record.xml")!!, StreamSource(styleSheet)).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00000nkm  2200000uu 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    archives/cushman/P07803") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aTelescope Peak from Zabriskie Point") },
                { assertThat(record.controlFields.size).isEqualTo(2) },
                { assertThat(record.dataFields.size).isEqualTo(17) },
                { assertThat(record.errors).isEmpty() }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }

    @Test
    @Disabled("Disabled until I have time to look into why this fails")
    fun `test MODS transformation using TransformerHandler`() {
        // loc.gov pages are on Cloudflare and require the header User-Agent.  So far setting the property is the only way I found to get around it
        System.setProperty("http.agent", "MARC4K")

        val styleSheet = "https://www.loc.gov/standards/mods/v3/MODS3-4_MARC21slim_XSLT2-0.xsl"
        val transformerHandler = (SAXTransformerFactory.newInstance() as SAXTransformerFactory).newTransformerHandler(StreamSource(styleSheet))
        MarcXmlReader(javaClass.getResourceAsStream("/records/MODS_bib_record.xml")!!, transformerHandler).use { reader ->
            val record = reader.next()
            assertAll(
                { assertThat(record.leader.toString()).isEqualTo("LEADER 00000nkm  2200000uu 4500") },
                { assertThat(record.controlFields.find { it.tag == "001" }?.toString()).isEqualTo("001    archives/cushman/P07803") },
                { assertThat(record.dataFields.find { it.tag == "245" }?.toString()).isEqualTo("245 10 ‡aTelescope Peak from Zabriskie Point") },
                { assertThat(record.controlFields.size).isEqualTo(2) },
                { assertThat(record.dataFields.size).isEqualTo(17) },
                { assertThat(record.errors).isEmpty() }
            )
            assertThat(reader.hasNext()).isFalse
        }
    }
}