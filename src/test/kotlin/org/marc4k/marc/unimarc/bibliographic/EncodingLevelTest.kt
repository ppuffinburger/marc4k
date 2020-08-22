package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncodingLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(EncodingLevel.fromValue(' ')).isEqualTo(EncodingLevel.FULL_LEVEL)
        assertThat(EncodingLevel.fromValue('1')).isEqualTo(EncodingLevel.SUBLEVEL_1)
        assertThat(EncodingLevel.fromValue('2')).isEqualTo(EncodingLevel.SUBLEVEL_2)
        assertThat(EncodingLevel.fromValue('3')).isEqualTo(EncodingLevel.SUBLEVEL_3)
        assertThat(EncodingLevel.fromValue('#')).isEqualTo(EncodingLevel.INVALID)
    }
}