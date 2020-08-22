package org.marc4k.marc.unimarc.authority

import org.marc4k.marc.DataField
import org.marc4k.marc.unimarc.UnimarcRecord

class AuthorityRecord : UnimarcRecord() {
    override val leader = AuthorityLeader()

    fun getAuthorizedHeadingField(): DataField? {
        return dataFields.firstOrNull { it.tag in HEADINGS_TAGS }
    }

    companion object {
        private val HEADINGS_TAGS = listOf("200", "210", "215", "216", "220", "230", "235", "240", "245", "250", "260", "280")
    }
}