package org.marc4k.marc.marc21

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CharacterCodingSchemeTest {

    @Test
    fun `test fromValue()`() {
        assertThat(CharacterCodingScheme.fromValue(' ')).isEqualTo(CharacterCodingScheme.MARC8)
        assertThat(CharacterCodingScheme.fromValue('a')).isEqualTo(CharacterCodingScheme.UNICODE)
        assertThat(CharacterCodingScheme.fromValue('#')).isEqualTo(CharacterCodingScheme.INVALID)
    }
}