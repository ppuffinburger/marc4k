package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.ControlField

internal class CommunityRecordTest {

    @Test
    fun `test newly constructed CommunityRecord`() {
        val given = CommunityRecord()
        assertAll(
            { Assertions.assertThat(given.leader).isInstanceOf(CommunityLeader::class.java) },
            { Assertions.assertThat(given.controlFields).isEmpty() },
            { Assertions.assertThat(given.dataFields).isEmpty() }
        )
    }

    @Test
    fun `test getValid008Data() with no 008`() {
        val given = CommunityRecord()
        Assertions.assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with invalid length 008`() {
        val given = CommunityRecord()
            .apply { controlFields += ControlField("008", "0123") }
        Assertions.assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with 008`() {
        val given = CommunityRecord()
            .apply { controlFields += ControlField("008", "123456789012345") }
        Assertions.assertThat(given.getValid008Data()).isEqualTo("123456789012345")
    }
}