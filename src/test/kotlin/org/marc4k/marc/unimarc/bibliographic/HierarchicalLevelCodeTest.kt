package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class HierarchicalLevelCodeTest {

    @Test
    fun `test fromValue()`() {
        assertThat(HierarchicalLevelCode.fromValue(' ')).isEqualTo(HierarchicalLevelCode.HIERARCHICAL_RELATIONSHIP_UNDEFINED)
        assertThat(HierarchicalLevelCode.fromValue('0')).isEqualTo(HierarchicalLevelCode.NO_HIERARCHICAL_RELATIONSHIP)
        assertThat(HierarchicalLevelCode.fromValue('1')).isEqualTo(HierarchicalLevelCode.HIGHEST_LEVEL_RECORD)
        assertThat(HierarchicalLevelCode.fromValue('2')).isEqualTo(HierarchicalLevelCode.RECORD_BELOW_HIGHEST_LEVEL)
        assertThat(HierarchicalLevelCode.fromValue('#')).isEqualTo(HierarchicalLevelCode.INVALID)
    }
}