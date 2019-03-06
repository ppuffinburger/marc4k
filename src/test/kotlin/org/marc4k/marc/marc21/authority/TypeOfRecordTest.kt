package org.marc4k.marc.marc21.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('z')).isEqualTo(TypeOfRecord.AUTHORITY_DATA)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}