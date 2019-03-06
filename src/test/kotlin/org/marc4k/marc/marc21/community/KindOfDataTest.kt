package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KindOfDataTest {

    @Test
    fun `test fromValue()`() {
        assertThat(KindOfData.fromValue('n')).isEqualTo(KindOfData.INDIVIDUAL)
        assertThat(KindOfData.fromValue('o')).isEqualTo(KindOfData.ORGANIZATION)
        assertThat(KindOfData.fromValue('p')).isEqualTo(KindOfData.PROGRAM_OR_SERVICE)
        assertThat(KindOfData.fromValue('q')).isEqualTo(KindOfData.EVENT)
        assertThat(KindOfData.fromValue('z')).isEqualTo(KindOfData.OTHER)
        assertThat(KindOfData.fromValue('#')).isEqualTo(KindOfData.INVALID)
    }
}