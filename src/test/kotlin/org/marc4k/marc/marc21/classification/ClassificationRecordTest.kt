package org.marc4k.marc.marc21.classification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.ControlField

internal class ClassificationRecordTest {

    @Test
    fun `test newly constructed ClassificationRecord`() {
        val given = ClassificationRecord()
        assertAll(
            { assertThat(given.leader).isInstanceOf(ClassificationLeader::class.java) },
            { assertThat(given.controlFields).isEmpty() },
            { assertThat(given.dataFields).isEmpty() }
        )
    }

    @Test
    fun `test getValid008Data() with no 008`() {
        val given = ClassificationRecord()
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with invalid length 008`() {
        val given = ClassificationRecord()
            .apply { controlFields += ControlField("008", "0123") }
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with 008`() {
        val given = ClassificationRecord()
            .apply { controlFields += ControlField("008", "12345678901234") }
        assertThat(given.getValid008Data()).isEqualTo("12345678901234")
    }
}