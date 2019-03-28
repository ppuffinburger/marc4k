package org.marc4k.io

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.marc4k.MarcException
import org.marc4k.marc.marc21.authority.AuthorityRecord
import org.marc4k.marc.marc21.bibliographic.BibliographicRecord
import org.marc4k.marc.marc21.classification.ClassificationRecord
import org.marc4k.marc.marc21.community.CommunityRecord
import org.marc4k.marc.marc21.holdings.HoldingsRecord
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class Marc21StreamReaderTest {
    private val testMarc21TypeData = listOf(
        'a' to BibliographicRecord::class,
        'c' to BibliographicRecord::class,
        'd' to BibliographicRecord::class,
        'e' to BibliographicRecord::class,
        'f' to BibliographicRecord::class,
        'g' to BibliographicRecord::class,
        'i' to BibliographicRecord::class,
        'j' to BibliographicRecord::class,
        'k' to BibliographicRecord::class,
        'm' to BibliographicRecord::class,
        'n' to BibliographicRecord::class,
        'o' to BibliographicRecord::class,
        'p' to BibliographicRecord::class,
        'r' to BibliographicRecord::class,
        't' to BibliographicRecord::class,
        'u' to HoldingsRecord::class,
        'v' to HoldingsRecord::class,
        'x' to HoldingsRecord::class,
        'y' to HoldingsRecord::class,
        'z' to AuthorityRecord::class,
        'w' to ClassificationRecord::class,
        'q' to CommunityRecord::class
    )

    @TestFactory
    fun `test valid Marc21 record types`() = testMarc21TypeData.map { (typeOfRecord, expected) ->
        Marc21StreamReader(MarcStreamReader(createByteStream(typeOfRecord))).use {
            DynamicTest.dynamicTest("when TypeOfRecord is '$typeOfRecord' then expect a '${expected.simpleName}") {
                assertThat(it.next()).isInstanceOf(expected.java)
            }
        }
    }

    @TestFactory
    fun `test invalid Marc21 record types`() = listOf('b', 'h', 'l', 's').map { typeOfRecord ->
        Marc21StreamReader(MarcStreamReader(createByteStream(typeOfRecord))).use {
            DynamicTest.dynamicTest("when TypeOfRecord is '$typeOfRecord' then throw an exception") {
                assertThatExceptionOfType(MarcException::class.java).isThrownBy { it.next() }
            }
        }
    }

    private fun createByteStream(typeOfRecord: Char): InputStream {
        val recordBytes = "00053n   a2200037    450001001500000\u001Econtrol_number\u001E\u001D".toByteArray()
        recordBytes[6] = typeOfRecord.toByte()
        return ByteArrayInputStream(recordBytes)
    }
}