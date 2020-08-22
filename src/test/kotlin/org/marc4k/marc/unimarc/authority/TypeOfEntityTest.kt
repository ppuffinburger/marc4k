package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfEntityTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfEntity.fromValue('a')).isEqualTo(TypeOfEntity.PERSONAL_NAME)
        assertThat(TypeOfEntity.fromValue('b')).isEqualTo(TypeOfEntity.CORPORATE_NAME)
        assertThat(TypeOfEntity.fromValue('c')).isEqualTo(TypeOfEntity.TERRITORIAL_OR_GEOGRAPHICAL_NAME)
        assertThat(TypeOfEntity.fromValue('d')).isEqualTo(TypeOfEntity.TRADEMARK)
        assertThat(TypeOfEntity.fromValue('e')).isEqualTo(TypeOfEntity.FAMILY_NAME)
        assertThat(TypeOfEntity.fromValue('f')).isEqualTo(TypeOfEntity.UNIFORM_NAME)
        assertThat(TypeOfEntity.fromValue('g')).isEqualTo(TypeOfEntity.COLLECTIVE_UNIFORM_TITLE)
        assertThat(TypeOfEntity.fromValue('h')).isEqualTo(TypeOfEntity.NAME_TITLE)
        assertThat(TypeOfEntity.fromValue('i')).isEqualTo(TypeOfEntity.NAME_COLLECTIVE_UNIFORM_TITLE)
        assertThat(TypeOfEntity.fromValue('j')).isEqualTo(TypeOfEntity.TOPICAL_SUBJECT)
        assertThat(TypeOfEntity.fromValue('k')).isEqualTo(TypeOfEntity.PLACE_ACCESS)
        assertThat(TypeOfEntity.fromValue('l')).isEqualTo(TypeOfEntity.FORM_GENRE_OR_PHYSICAL_CHARACTERISTICS)
        assertThat(TypeOfEntity.fromValue('#')).isEqualTo(TypeOfEntity.INVALID)
    }
}