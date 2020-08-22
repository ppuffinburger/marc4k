package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfControlTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfControl.fromValue(' ')).isEqualTo(TypeOfControl.NO_SPECIFIED_TYPE)
        assertThat(TypeOfControl.fromValue('a')).isEqualTo(TypeOfControl.ARCHIVAL)
        assertThat(TypeOfControl.fromValue('m')).isEqualTo(TypeOfControl.MUSEUM)
        assertThat(TypeOfControl.fromValue('#')).isEqualTo(TypeOfControl.INVALID)
    }
}