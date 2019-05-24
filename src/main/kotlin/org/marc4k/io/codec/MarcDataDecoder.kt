package org.marc4k.io.codec

import org.marc4k.MarcError
import org.marc4k.marc.ControlField
import org.marc4k.marc.DataField
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Subfield

abstract class MarcDataDecoder {
    protected var applyConverter = false
    protected val recordErrors = mutableListOf<MarcError>()

    protected abstract fun setApplyConverter(iso2709Record: Iso2709Record): Boolean
    protected abstract fun getDataAsString(fieldIndex: Int, fieldTag: String, bytes: ByteArray): String

    fun createMarcRecord(iso2709Record: Iso2709Record): MarcRecord {
        recordErrors.clear()

        applyConverter = setApplyConverter(iso2709Record)

        return MarcRecord().apply {
            leader.setData(iso2709Record.leader)
            controlFields +=
                iso2709Record.controlFields.mapIndexed { index, field ->
                    ControlField(field.tag, getDataAsString(index, field.tag, field.data))
                }
            dataFields +=
                iso2709Record.dataFields.mapIndexed { index, field ->
                    DataField(field.tag, field.indicator1, field.indicator2).apply {
                        subfields += field.subfields.map { subfield ->
                            Subfield(subfield.name, getDataAsString(index + iso2709Record.controlFields.size, field.tag, subfield.data))
                        }
                    }
                }

            if (recordErrors.isNotEmpty()) {
                errors += recordErrors
            }
        }
    }
}