package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('q')).isEqualTo(TypeOfRecord.COMMUNITY_INFORMATION)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}