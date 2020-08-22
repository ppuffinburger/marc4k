package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DescriptiveCatalogingFormTest {

    @Test
    fun `test fromValue()`() {
        assertThat(DescriptiveCatalogingForm.fromValue(' ')).isEqualTo(DescriptiveCatalogingForm.FULL_ISBD)
        assertThat(DescriptiveCatalogingForm.fromValue('i')).isEqualTo(DescriptiveCatalogingForm.PARTIAL_OR_INCOMPLETE_ISBD)
        assertThat(DescriptiveCatalogingForm.fromValue('n')).isEqualTo(DescriptiveCatalogingForm.NON_ISBD)
        assertThat(DescriptiveCatalogingForm.fromValue('x')).isEqualTo(DescriptiveCatalogingForm.ISBD_NOT_APPLICABLE)
        assertThat(DescriptiveCatalogingForm.fromValue('#')).isEqualTo(DescriptiveCatalogingForm.INVALID)
    }
}