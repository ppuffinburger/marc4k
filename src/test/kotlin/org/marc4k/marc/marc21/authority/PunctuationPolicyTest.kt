package org.marc4k.marc.marc21.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PunctuationPolicyTest {

    @Test
    fun `test fromValue()`() {
        assertThat(PunctuationPolicy.fromValue(' ')).isEqualTo(PunctuationPolicy.NO_INFORMATION_PROVIDED)
        assertThat(PunctuationPolicy.fromValue('c')).isEqualTo(PunctuationPolicy.PUNCTUATION_OMITTED)
        assertThat(PunctuationPolicy.fromValue('i')).isEqualTo(PunctuationPolicy.PUNCTUATION_INCLUDED)
        assertThat(PunctuationPolicy.fromValue('u')).isEqualTo(PunctuationPolicy.UNKNOWN)
        assertThat(PunctuationPolicy.fromValue('#')).isEqualTo(PunctuationPolicy.INVALID)
    }
}