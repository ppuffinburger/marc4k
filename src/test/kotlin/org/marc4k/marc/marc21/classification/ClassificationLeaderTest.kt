package org.marc4k.marc.marc21.classification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.marc4k.marc.marc21.CharacterCodingScheme

internal class ClassificationLeaderTest {

    @Test
    fun `test constructor()`() {
        val given = ClassificationLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   4500")
    }

    @Test
    fun `test constructor(String)`() {
        val given = ClassificationLeader("12345nw  a2254321n  4500")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.CLASSIFICATION_DATA) },
            { assertThat(given.undefinedPosition7).isEqualTo(' ') },
            { assertThat(given.undefinedPosition8).isEqualTo(' ') },
            { assertThat(given.characterCodingScheme).isEqualTo(CharacterCodingScheme.UNICODE) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.encodingLevel).isEqualTo(EncodingLevel.COMPLETE_CLASSIFICATION_RECORD) },
            { assertThat(given.undefinedPosition18).isEqualTo(' ') },
            { assertThat(given.undefinedPosition19).isEqualTo(' ') },
            { assertThat(given.lengthOfTheLengthOfFieldPortion).isEqualTo('4') },
            { assertThat(given.lengthOfTheStartingCharacterPositionPortion).isEqualTo('5') },
            { assertThat(given.lengthOfTheImplementationDefinedPortion).isEqualTo('0') },
            { assertThat(given.undefinedPosition23).isEqualTo('0') }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThrows<IllegalArgumentException> { ClassificationLeader("12345nw  a2254321n  4500X") }
    }

    @Test
    fun `test constructor(AuthorityLeader)`() {
        val leaderOriginal = ClassificationLeader("12345nw  a2254321n  4500")
        val leaderCopy = ClassificationLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).isEqualToComparingFieldByFieldRecursively(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = ClassificationLeader("12345nw  a2254321n  4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nw  a2254321n  4500")
    }
}