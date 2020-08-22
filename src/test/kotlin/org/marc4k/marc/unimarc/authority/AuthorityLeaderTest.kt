package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test

internal class AuthorityLeaderTest {

    @Test
    fun `test constructor()`() {
        val given = AuthorityLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   450 ")
    }

    @Test
    fun `test constructor(String)`() {
        val given = AuthorityLeader("12345nx  a2254321   450 ")
        org.junit.jupiter.api.assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.AUTHORITY) },
            { assertThat(given.undefinedPosition7).isEqualTo(' ') },
            { assertThat(given.undefinedPosition8).isEqualTo(' ') },
            { assertThat(given.typeOfEntity).isEqualTo(TypeOfEntity.PERSONAL_NAME) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.encodingLevel).isEqualTo(EncodingLevel.FULL_LEVEL) },
            { assertThat(given.undefinedPosition18).isEqualTo(' ') },
            { assertThat(given.undefinedPosition19).isEqualTo(' ') },
            { assertThat(given.lengthOfTheLengthOfFieldPortion).isEqualTo('4') },
            { assertThat(given.lengthOfTheStartingCharacterPositionPortion).isEqualTo('5') },
            { assertThat(given.lengthOfTheImplementationDefinedPortion).isEqualTo('0') },
            { assertThat(given.undefinedPosition23).isEqualTo(' ') }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThatIllegalArgumentException().isThrownBy { AuthorityLeader("12345nz  a2254321nc 450 X") }
    }

    @Test
    fun `test constructor(AuthorityLeader)`() {
        val leaderOriginal = AuthorityLeader("12345nz  a2254321nc 450 ")
        val leaderCopy = AuthorityLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).usingRecursiveComparison().isEqualTo(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = AuthorityLeader("12345nz  a2254321   4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nz  a2254321   450 ")
    }
}