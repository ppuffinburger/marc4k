package org.marc4k.marc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ControlFieldTest {

    @Test
    fun `test toString()`() {
        val given = ControlField("001", "control_number")
        assertThat(given.toString()).isEqualTo("001    control_number")
    }
}