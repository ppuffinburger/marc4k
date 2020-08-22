package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncodingLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(EncodingLevel.fromValue(' ')).isEqualTo(EncodingLevel.FULL_LEVEL)
        assertThat(EncodingLevel.fromValue('3')).isEqualTo(EncodingLevel.PARTIAL)
        assertThat(EncodingLevel.fromValue('#')).isEqualTo(EncodingLevel.INVALID)
    }
}