package org.marc4k.marc.marc21.holdings

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.marc21.CharacterCodingScheme

internal class HoldingsLeaderTest {

    @Test
    fun `test constructor()`() {
        val given = HoldingsLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   4500")
    }

    @Test
    fun `test constructor(String)`() {
        val given = HoldingsLeader("12345nx  a22543211i 4500")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.SINGLE_PART_ITEM_HOLDINGS) },
            { assertThat(given.undefinedPosition7).isEqualTo(' ') },
            { assertThat(given.undefinedPosition8).isEqualTo(' ') },
            { assertThat(given.characterCodingScheme).isEqualTo(CharacterCodingScheme.UNICODE) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.encodingLevel).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_1) },
            { assertThat(given.itemInformationInRecord).isEqualTo(ItemInformationInRecord.ITEM_INFORMATION) },
            { assertThat(given.undefinedPosition19).isEqualTo(' ') },
            { assertThat(given.lengthOfTheLengthOfFieldPortion).isEqualTo('4') },
            { assertThat(given.lengthOfTheStartingCharacterPositionPortion).isEqualTo('5') },
            { assertThat(given.lengthOfTheImplementationDefinedPortion).isEqualTo('0') },
            { assertThat(given.undefinedPosition23).isEqualTo('0') }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThatIllegalArgumentException().isThrownBy { HoldingsLeader("12345nx  a22543211i 4500X") }
    }

    @Test
    fun `test constructor(AuthorityLeader)`() {
        val leaderOriginal = HoldingsLeader("12345nx  a22543211i 4500")
        val leaderCopy = HoldingsLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).isEqualToComparingFieldByFieldRecursively(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = HoldingsLeader("12345nx  a22543211i 4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nx  a22543211i 4500")
    }
}