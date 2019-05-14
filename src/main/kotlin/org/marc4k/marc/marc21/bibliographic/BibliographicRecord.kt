package org.marc4k.marc.marc21.bibliographic

import org.marc4k.SUBFIELD_DELIMITER_CHARACTER
import org.marc4k.marc.DataField
import org.marc4k.marc.marc21.Marc21Record
import java.util.*

class BibliographicRecord : Marc21Record() {
    override val leader = BibliographicLeader()

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 40 }?.data

    fun getTitleField(): DataField? = dataFields.firstOrNull { it.tag == "245" }

    fun getMainEntryField(): DataField? = dataFields.firstOrNull { MAIN_ENTRY_FIELDS.contains(it.tag) }

    fun getTargetAudience(): Char? {
        getValid008Data()?.let {
            return when (leader.typeOfRecord) {
                TypeOfRecord.LANGUAGE_MATERIAL,
                TypeOfRecord.MANUSCRIPT_LANGUAGE_MATERIAL -> {
                    if (BIBLIOGRAPHIC_LEVELS_WITH_TARGET_AUDIENCE.contains(leader.bibliographicLevel)) {
                        it[22]
                    } else {
                        null
                    }
                }
                TypeOfRecord.COMPUTER_FILE,
                TypeOfRecord.NOTATED_MUSIC,
                TypeOfRecord.MANUSCRIPT_NOTED_MUSIC,
                TypeOfRecord.PROJECTED_MEDIUM,
                TypeOfRecord.NON_MUSICAL_SOUND_RECORDING,
                TypeOfRecord.MUSICAL_SOUND_RECORDING,
                TypeOfRecord.KIT,
                TypeOfRecord.TWO_DIMENSIONAL_NON_PROJECTABLE_GRAPHIC,
                TypeOfRecord.THREE_DIMENSIONAL_ARTIFACT_OR_NATURALLY_OCCURRING_OBJECT -> {
                    it[22]
                }
                else -> null
            }
        }
        return null
    }

    fun isRda(): Boolean {
        if (leader.descriptiveCatalogingForm == DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED) {
            return dataFields.firstOrNull { it.tag == "040" }?.getData()?.contains("${SUBFIELD_DELIMITER_CHARACTER}erda", ignoreCase = false) ?: false
        }
        return false
    }

    companion object {
        private val BIBLIOGRAPHIC_LEVELS_WITH_TARGET_AUDIENCE = EnumSet.of(
            BibliographicLevel.MONOGRAPH_ITEM,
            BibliographicLevel.MONOGRAPHIC_COMPONENT_PART,
            BibliographicLevel.COLLECTION,
            BibliographicLevel.SUBUNIT
        )
        private val MAIN_ENTRY_FIELDS = listOf("100", "110", "111", "130")
    }
}