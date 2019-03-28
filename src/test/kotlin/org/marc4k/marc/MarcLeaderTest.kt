package org.marc4k.marc

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class MarcLeaderTest {

    @Test
    fun `test constructor()`() {
        val given = MarcLeader()
        assertThat(given.getData()).isEqualTo("00000     0000000       ")
    }

    @Test
    fun `test constructor(String)`() {
        val given = MarcLeader("12345nam a2254321 a 4500")
        assertAll(
            { assertThat(given.recordLength).isEqualTo(12345) },
            { assertThat(given.recordStatus).isEqualTo('n') },
            { assertThat(given.typeOfRecord).isEqualTo('a') },
            { assertThat(given.implementationDefined1).isEqualTo("m ".toCharArray()) },
            { assertThat(given.characterCodingScheme).isEqualTo('a') },
            { assertThat(given.indicatorCount).isEqualTo(2) },
            { assertThat(given.subfieldCodeCount).isEqualTo(2) },
            { assertThat(given.baseAddressOfData).isEqualTo(54321) },
            { assertThat(given.implementationDefined2).isEqualTo(" a ".toCharArray()) },
            { assertThat(given.entryMap).isEqualTo("4500".toCharArray()) }
        )
    }

    @Test
    fun `test constructor(String) throws`() {
        assertThatIllegalArgumentException().isThrownBy { MarcLeader("12345nam a2254321 a 4500X") }
    }

    @Test
    fun `test constructor(AuthorityLeader)`() {
        val leaderOriginal = MarcLeader("12345nam a2254321 a 4500")
        val leaderCopy = MarcLeader(leaderOriginal)
        assertAll(
            { assertThat(leaderCopy).isNotSameAs(leaderOriginal) },
            { assertThat(leaderCopy).isEqualToComparingFieldByFieldRecursively(leaderOriginal) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = MarcLeader("12345nam a2254321 a 4500")
        assertThat(given.toString()).isEqualTo("LEADER 12345nam a2254321 a 4500")
    }
}