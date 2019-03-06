package org.marc4k.marc.marc21.holdings

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('u')).isEqualTo(TypeOfRecord.UNKNOWN)
        assertThat(TypeOfRecord.fromValue('v')).isEqualTo(TypeOfRecord.MULTIPART_ITEM_HOLDINGS)
        assertThat(TypeOfRecord.fromValue('x')).isEqualTo(TypeOfRecord.SINGLE_PART_ITEM_HOLDINGS)
        assertThat(TypeOfRecord.fromValue('y')).isEqualTo(TypeOfRecord.SERIAL_ITEM_HOLDINGS)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}