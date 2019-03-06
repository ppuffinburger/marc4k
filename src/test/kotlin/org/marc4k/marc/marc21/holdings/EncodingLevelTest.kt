package org.marc4k.marc.marc21.holdings

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncodingLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(EncodingLevel.fromValue('1')).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_1)
        assertThat(EncodingLevel.fromValue('2')).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_2)
        assertThat(EncodingLevel.fromValue('3')).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_3)
        assertThat(EncodingLevel.fromValue('4')).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_4)
        assertThat(EncodingLevel.fromValue('5')).isEqualTo(EncodingLevel.HOLDINGS_LEVEL_4_WITH_PIECE_DESIGNATION)
        assertThat(EncodingLevel.fromValue('m')).isEqualTo(EncodingLevel.MIXED_LEVEL)
        assertThat(EncodingLevel.fromValue('u')).isEqualTo(EncodingLevel.UNKNOWN)
        assertThat(EncodingLevel.fromValue('z')).isEqualTo(EncodingLevel.OTHER_LEVEL)
        assertThat(EncodingLevel.fromValue('#')).isEqualTo(EncodingLevel.INVALID)
    }
}