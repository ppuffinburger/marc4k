package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RecordStatusTest {

    @Test
    fun `test fromValue()`() {
        assertThat(RecordStatus.fromValue('c')).isEqualTo(RecordStatus.CORRECTED)
        assertThat(RecordStatus.fromValue('d')).isEqualTo(RecordStatus.DELETED)
        assertThat(RecordStatus.fromValue('n')).isEqualTo(RecordStatus.NEW)
        assertThat(RecordStatus.fromValue('o')).isEqualTo(RecordStatus.PREVIOUSLY_ISSUED_HIGHER_LEVEL_RECORD)
        assertThat(RecordStatus.fromValue('p')).isEqualTo(RecordStatus.PREVIOUSLY_ISSUED_AS_AN_INCOMPLETE_PREPUBLICATION_RECORD)
        assertThat(RecordStatus.fromValue('#')).isEqualTo(RecordStatus.INVALID)
    }
}