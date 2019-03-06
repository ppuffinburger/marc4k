package org.marc4k.marc.marc21.classification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RecordStatusTest {

    @Test
    fun `test fromValue()`() {
        assertThat(RecordStatus.fromValue('a')).isEqualTo(RecordStatus.INCREASE_IN_ENCODING_LEVEL)
        assertThat(RecordStatus.fromValue('c')).isEqualTo(RecordStatus.CORRECTED_OR_REVISED)
        assertThat(RecordStatus.fromValue('d')).isEqualTo(RecordStatus.DELETED)
        assertThat(RecordStatus.fromValue('n')).isEqualTo(RecordStatus.NEW)
        assertThat(RecordStatus.fromValue('#')).isEqualTo(RecordStatus.INVALID)
    }
}