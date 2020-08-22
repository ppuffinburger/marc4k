package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('x')).isEqualTo(TypeOfRecord.AUTHORITY)
        assertThat(TypeOfRecord.fromValue('y')).isEqualTo(TypeOfRecord.REFERENCE)
        assertThat(TypeOfRecord.fromValue('z')).isEqualTo(TypeOfRecord.GENERAL_EXPLANATORY)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}