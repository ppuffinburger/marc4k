package org.marc4k.io.codec

import org.marc4k.marc.MarcRecord

abstract class MarcDataEncoder {
    protected var applyConverter = false

    protected abstract fun setApplyConverter(marcRecord: MarcRecord): Boolean
    protected abstract fun getDataAsBytes(data: String): ByteArray

    fun createIso2709Record(marcRecord: MarcRecord): Iso2709Record {
        applyConverter = setApplyConverter(marcRecord)

        val iso2709Record = Iso2709Record(marcRecord.leader.getData())

        iso2709Record.controlFields +=
            marcRecord.controlFields.map { field ->
                Iso2709ControlField(field.tag, getDataAsBytes(field.data))
            }

        iso2709Record.dataFields +=
            marcRecord.dataFields.map { field ->
                Iso2709DataField(field.tag, field.indicator1, field.indicator2).apply {
                    subfields += field.subfields.map { subfield ->
                        Iso2709Subfield(subfield.name, getDataAsBytes(subfield.data))
                    }
                }
            }

        return iso2709Record
    }
}