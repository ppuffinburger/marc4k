package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.marc4k.marc.marc21.CharacterCodingScheme

internal class CommunityLeaderTest {

    @Test
    fun `test constructor()`() {
        val given = CommunityLeader()
        assertThat(given.getData()).isEqualTo("00000     2200000   4500")
    }

    @Test
    fun `test constructor(String)`() {
        val given = CommunityLeader("12345nqn a2254321   4500")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo(RecordStatus.NEW) },
            { assertThat(given.typeOfRecord).isEqualTo(TypeOfRecord.COMMUNITY_INFORMATION) },
            { assertThat(given.kindOfData).isEqualTo(KindOfData.INDIVIDUAL) },
            { assertThat(given.undefinedPosition8).isEqualTo(' ') },
            { assertThat(given.characterCodingScheme).isEqualTo(CharacterCodingScheme.UNICODE) },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.undefinedPosition17).isEqualTo(' ') },
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
        assertThrows<IllegalArgumentException> { CommunityLeader("12345nqn a2254321   4500X") }
    }

    @Test
    fun `test constructor(AuthorityLeader)`() {
        val leaderOriginal = CommunityLeader("12345nqn a2254321   4500")
        val leaderCopy = CommunityLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).isEqualToComparingFieldByFieldRecursively(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = CommunityLeader("12345nqn a2254321   4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nqn a2254321   4500")
    }
}