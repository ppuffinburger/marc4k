package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('a')).isEqualTo(TypeOfRecord.LANGUAGE_MATERIAL)
        assertThat(TypeOfRecord.fromValue('b')).isEqualTo(TypeOfRecord.MANUSCRIPT_LANGUAGE_MATERIAL)
        assertThat(TypeOfRecord.fromValue('c')).isEqualTo(TypeOfRecord.NOTATED_MUSIC)
        assertThat(TypeOfRecord.fromValue('d')).isEqualTo(TypeOfRecord.MANUSCRIPT_NOTATED_MUSIC)
        assertThat(TypeOfRecord.fromValue('e')).isEqualTo(TypeOfRecord.CARTOGRAPHIC_MATERIAL)
        assertThat(TypeOfRecord.fromValue('f')).isEqualTo(TypeOfRecord.MANUSCRIPT_CARTOGRAPHIC_MATERIAL)
        assertThat(TypeOfRecord.fromValue('g')).isEqualTo(TypeOfRecord.PROJECTED_AND_VIDEO_MATERIAL)
        assertThat(TypeOfRecord.fromValue('i')).isEqualTo(TypeOfRecord.NON_MUSICAL_SOUND_RECORDING)
        assertThat(TypeOfRecord.fromValue('j')).isEqualTo(TypeOfRecord.MUSICAL_SOUND_RECORDING)
        assertThat(TypeOfRecord.fromValue('k')).isEqualTo(TypeOfRecord.TWO_DIMENSIONAL_GRAPHIC)
        assertThat(TypeOfRecord.fromValue('l')).isEqualTo(TypeOfRecord.ELECTRONIC_RESOURCE)
        assertThat(TypeOfRecord.fromValue('m')).isEqualTo(TypeOfRecord.MULTIMEDIA)
        assertThat(TypeOfRecord.fromValue('r')).isEqualTo(TypeOfRecord.THREE_DIMENSIONAL_ARTEFACT)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}