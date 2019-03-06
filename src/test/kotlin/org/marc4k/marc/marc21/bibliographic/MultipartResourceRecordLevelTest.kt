package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MultipartResourceRecordLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(MultipartResourceRecordLevel.fromValue(' ')).isEqualTo(MultipartResourceRecordLevel.NOT_SPECIFIED_OR_NOT_APPLICABLE)
        assertThat(MultipartResourceRecordLevel.fromValue('a')).isEqualTo(MultipartResourceRecordLevel.SET)
        assertThat(MultipartResourceRecordLevel.fromValue('b')).isEqualTo(MultipartResourceRecordLevel.PART_WITH_INDEPENDENT_TITLE)
        assertThat(MultipartResourceRecordLevel.fromValue('c')).isEqualTo(MultipartResourceRecordLevel.PART_WITH_DEPENDENT_TITLE)
        assertThat(MultipartResourceRecordLevel.fromValue('#')).isEqualTo(MultipartResourceRecordLevel.INVALID)
    }
}