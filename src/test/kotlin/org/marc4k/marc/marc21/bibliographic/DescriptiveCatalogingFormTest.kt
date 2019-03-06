package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DescriptiveCatalogingFormTest {

    @Test
    fun `test fromValue()`() {
        assertThat(DescriptiveCatalogingForm.fromValue(' ')).isEqualTo(DescriptiveCatalogingForm.NON_ISBD)
        assertThat(DescriptiveCatalogingForm.fromValue('a')).isEqualTo(DescriptiveCatalogingForm.AACR2)
        assertThat(DescriptiveCatalogingForm.fromValue('c')).isEqualTo(DescriptiveCatalogingForm.ISBD_PUNCTUATION_OMITTED)
        assertThat(DescriptiveCatalogingForm.fromValue('i')).isEqualTo(DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED)
        assertThat(DescriptiveCatalogingForm.fromValue('n')).isEqualTo(DescriptiveCatalogingForm.NON_ISBD_PUNCTUATION_OMITTED)
        assertThat(DescriptiveCatalogingForm.fromValue('u')).isEqualTo(DescriptiveCatalogingForm.UNKNOWN)
        assertThat(DescriptiveCatalogingForm.fromValue('#')).isEqualTo(DescriptiveCatalogingForm.INVALID)
    }
}