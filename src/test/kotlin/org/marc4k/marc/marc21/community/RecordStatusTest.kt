package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RecordStatusTest {

    @Test
    fun `test fromValue()`() {
        assertThat(RecordStatus.fromValue('c')).isEqualTo(RecordStatus.CORRECTED_OR_REVISED)
        assertThat(RecordStatus.fromValue('d')).isEqualTo(RecordStatus.DELETED)
        assertThat(RecordStatus.fromValue('n')).isEqualTo(RecordStatus.NEW)
        assertThat(RecordStatus.fromValue('#')).isEqualTo(RecordStatus.INVALID)
    }
}