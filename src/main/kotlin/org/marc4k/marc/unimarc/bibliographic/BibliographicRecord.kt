package org.marc4k.marc.unimarc.bibliographic

import org.marc4k.marc.DataField
import org.marc4k.marc.unimarc.UnimarcRecord

class BibliographicRecord : UnimarcRecord() {
    override val leader = BibliographicLeader()

    fun getTitleField(): DataField? = dataFields.firstOrNull { it.tag == "200" }

    fun getMainEntryField(): DataField? = dataFields.firstOrNull { MAIN_ENTRY_FIELDS.contains(it.tag) }

    companion object {
        private val MAIN_ENTRY_FIELDS = listOf("700", "710", "720")
    }
}