package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TypeOfRecordTest {

    @Test
    fun `test fromValue()`() {
        assertThat(TypeOfRecord.fromValue('a')).isEqualTo(TypeOfRecord.LANGUAGE_MATERIAL)
        assertThat(TypeOfRecord.fromValue('c')).isEqualTo(TypeOfRecord.NOTATED_MUSIC)
        assertThat(TypeOfRecord.fromValue('d')).isEqualTo(TypeOfRecord.MANUSCRIPT_NOTED_MUSIC)
        assertThat(TypeOfRecord.fromValue('e')).isEqualTo(TypeOfRecord.CARTOGRAPHIC_MATERIAL)
        assertThat(TypeOfRecord.fromValue('f')).isEqualTo(TypeOfRecord.MANUSCRIPT_CARTOGRAPHIC_MATERIAL)
        assertThat(TypeOfRecord.fromValue('g')).isEqualTo(TypeOfRecord.PROJECTED_MEDIUM)
        assertThat(TypeOfRecord.fromValue('i')).isEqualTo(TypeOfRecord.NON_MUSICAL_SOUND_RECORDING)
        assertThat(TypeOfRecord.fromValue('j')).isEqualTo(TypeOfRecord.MUSICAL_SOUND_RECORDING)
        assertThat(TypeOfRecord.fromValue('k')).isEqualTo(TypeOfRecord.TWO_DIMENSIONAL_NON_PROJECTABLE_GRAPHIC)
        assertThat(TypeOfRecord.fromValue('m')).isEqualTo(TypeOfRecord.COMPUTER_FILE)
        assertThat(TypeOfRecord.fromValue('o')).isEqualTo(TypeOfRecord.KIT)
        assertThat(TypeOfRecord.fromValue('p')).isEqualTo(TypeOfRecord.MIXED_MATERIAL)
        assertThat(TypeOfRecord.fromValue('r')).isEqualTo(TypeOfRecord.THREE_DIMENSIONAL_ARTIFACT_OR_NATURALLY_OCCURRING_OBJECT)
        assertThat(TypeOfRecord.fromValue('t')).isEqualTo(TypeOfRecord.MANUSCRIPT_LANGUAGE_MATERIAL)
        assertThat(TypeOfRecord.fromValue('#')).isEqualTo(TypeOfRecord.INVALID)
    }
}