package org.marc4k.marc.marc21.holdings

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ItemInformationInRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(ItemInformationInRecord.fromValue('i')).isEqualTo(ItemInformationInRecord.ITEM_INFORMATION)
        assertThat(ItemInformationInRecord.fromValue('n')).isEqualTo(ItemInformationInRecord.NO_ITEM_INFORMATION)
        assertThat(ItemInformationInRecord.fromValue('#')).isEqualTo(ItemInformationInRecord.INVALID)
    }
}