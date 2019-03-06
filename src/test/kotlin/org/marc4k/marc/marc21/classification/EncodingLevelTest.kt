package org.marc4k.marc.marc21.classification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EncodingLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(EncodingLevel.fromValue('n')).isEqualTo(EncodingLevel.COMPLETE_CLASSIFICATION_RECORD)
        assertThat(EncodingLevel.fromValue('o')).isEqualTo(EncodingLevel.INCOMPLETE_CLASSIFICATION_RECORD)
        assertThat(EncodingLevel.fromValue('#')).isEqualTo(EncodingLevel.INVALID)
    }
}