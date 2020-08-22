package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class BibliographicLeaderTest {
    @Test
    fun `test constructor()`() {
        val given = BibliographicLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   450 ")
    }

    @Test
    fun `test constructor(String)`() {
        val given = BibliographicLeader("12345nam  2254321 x 450 ")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.LANGUAGE_MATERIAL) },
            { assertThat(given.bibliographicLevel).isEqualTo(BibliographicLevel.MONOGRAPH) },
            { assertThat(given.hierarchicalLevelCode).isEqualTo(HierarchicalLevelCode.HIERARCHICAL_RELATIONSHIP_UNDEFINED) },
            { assertThat(given.typeOfControl).isEqualTo(TypeOfControl.NO_SPECIFIED_TYPE) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.encodingLevel).isEqualTo(EncodingLevel.FULL_LEVEL) },
            { assertThat(given.descriptiveCatalogingForm).isEqualTo(DescriptiveCatalogingForm.ISBD_NOT_APPLICABLE) },
            { assertThat(given.undefinedPosition19).isEqualTo(' ') },
            { assertThat(given.lengthOfTheLengthOfFieldPortion).isEqualTo('4') },
            { assertThat(given.lengthOfTheStartingCharacterPositionPortion).isEqualTo('5') },
            { assertThat(given.lengthOfTheImplementationDefinedPortion).isEqualTo('0') },
            { assertThat(given.undefinedPosition23).isEqualTo(' ') }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThatIllegalArgumentException().isThrownBy { BibliographicLeader("01234nam a2200321 a 4500X") }
    }

    @Test
    fun `test constructor(BibliographicLeader)`() {
        val leaderOriginal = BibliographicLeader("12345nam a2254321 a 450 ")
        val leaderCopy = BibliographicLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).usingRecursiveComparison().isEqualTo(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = BibliographicLeader("12345nam  2254321 x 450 ")
        assertThat(given.toString()).isEqualTo("LEADER 12345nam  2254321 x 450 ")
    }
}