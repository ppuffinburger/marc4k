package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.marc4k.marc.marc21.CharacterCodingScheme

internal class BibliographicLeaderTest {
    @Test
    fun `test constructor()`() {
        val given = BibliographicLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   4500")
    }

    @Test
    fun `test constructor(String)`() {
        val given = BibliographicLeader("12345nam a2254321 a 4500")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.LANGUAGE_MATERIAL) },
            { assertThat(given.bibliographicLevel).isEqualTo(BibliographicLevel.MONOGRAPH_ITEM) },
            { assertThat(given.typeOfControl).isEqualTo(TypeOfControl.NO_SPECIFIC_TYPE) },
            { assertThat(given.characterCodingScheme).isEqualTo(CharacterCodingScheme.UNICODE) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.encodingLevel).isEqualTo(EncodingLevel.FULL_LEVEL) },
            { assertThat(given.descriptiveCatalogingForm).isEqualTo(DescriptiveCatalogingForm.AACR2) },
            { assertThat(given.multipartResourceRecordLevel).isEqualTo(MultipartResourceRecordLevel.NOT_SPECIFIED_OR_NOT_APPLICABLE) },
            { assertThat(given.lengthOfTheLengthOfFieldPortion).isEqualTo('4') },
            { assertThat(given.lengthOfTheStartingCharacterPositionPortion).isEqualTo('5') },
            { assertThat(given.lengthOfTheImplementationDefinedPortion).isEqualTo('0') },
            { assertThat(given.undefinedPosition23).isEqualTo('0') }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThrows<IllegalArgumentException> { BibliographicLeader("01234nam a2200321 a 4500X") }
    }

    @Test
    fun `test constructor(BibliographicLeader)`() {
        val leaderOriginal = BibliographicLeader("12345nam a2254321 a 4500")
        val leaderCopy = BibliographicLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).isEqualToComparingFieldByFieldRecursively(leaderOriginal) }
        )
    }

    @Test
    fun `test constructor(String) with unknown encoding level`() {
        val given = BibliographicLeader("12345nam a2254321#a 4500")
        assertThat(given.getData()).isEqualTo("12345nam a2254321\u0000a 4500")
    }

    @Test
    fun `test constructor(String) with OCLC encoding level I`() {
        val given = BibliographicLeader("12345nam a2254321Ia 4500")
        assertThat(given.getData()).isEqualTo("12345nam a2254321 a 4500")
    }

    @Test
    fun `test constructor(String) with OCLC encoding level K`() {
        val given = BibliographicLeader("12345nam a2254321Ka 4500")
        assertThat(given.getData()).isEqualTo("12345nam a22543217a 4500")
    }

    @Test
    fun `test constructor(String) with OCLC encoding level L`() {
        val given = BibliographicLeader("12345nam a2254321La 4500")
        assertThat(given.getData()).isEqualTo("12345nam a22543211a 4500")
    }

    @Test
    fun `test constructor(String) with OCLC encoding level M`() {
        val given = BibliographicLeader("12345nam a2254321Ma 4500")
        assertThat(given.getData()).isEqualTo("12345nam a22543212a 4500")
    }

    @Test
    fun `test constructor(String) with OCLC encoding level J`() {
        val given = BibliographicLeader("12345nam a2254321Ja 4500")
        assertThat(given.getData()).isEqualTo("12345nam a22543212a 4500")
    }

    @Test
    fun `test toString()`() {
        val given = BibliographicLeader("12345nam a2254321 a 4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nam a2254321 a 4500")
    }
}