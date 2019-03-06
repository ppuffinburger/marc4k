package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncodingLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(EncodingLevel.fromValue(' ')).isEqualTo(EncodingLevel.FULL_LEVEL)
        assertThat(EncodingLevel.fromValue('1')).isEqualTo(EncodingLevel.FULL_LEVEL_MATERIAL_NOT_EXAMINED)
        assertThat(EncodingLevel.fromValue('2')).isEqualTo(EncodingLevel.LESS_THAN_FULL_LEVEL_MATERIAL_NOT_EXAMINED)
        assertThat(EncodingLevel.fromValue('3')).isEqualTo(EncodingLevel.ABBREVIATED_LEVEL)
        assertThat(EncodingLevel.fromValue('4')).isEqualTo(EncodingLevel.CORE_LEVEL)
        assertThat(EncodingLevel.fromValue('5')).isEqualTo(EncodingLevel.PARTIAL_PRELIMINARY_LEVEL)
        assertThat(EncodingLevel.fromValue('7')).isEqualTo(EncodingLevel.MINIMAL_LEVEL)
        assertThat(EncodingLevel.fromValue('8')).isEqualTo(EncodingLevel.PREPUBLICATION_LEVEL)
        assertThat(EncodingLevel.fromValue('u')).isEqualTo(EncodingLevel.UNKNOWN)
        assertThat(EncodingLevel.fromValue('z')).isEqualTo(EncodingLevel.NOT_APPLICABLE)
        assertThat(EncodingLevel.fromValue('#')).isEqualTo(EncodingLevel.INVALID)
    }
}