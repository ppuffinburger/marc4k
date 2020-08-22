package org.marc4k.marc.marc21.authority

import org.marc4k.marc.DataField
import org.marc4k.marc.marc21.Marc21Record

class AuthorityRecord : Marc21Record() {
    override val leader = AuthorityLeader()

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 40 }?.data

    fun getAuthorizedHeadingField(): DataField? {
        return dataFields.firstOrNull { it.tag in HEADINGS_TAGS }
    }

    companion object {
        private val HEADINGS_TAGS = listOf("100", "110", "111", "130", "147", "148", "150", "151", "155", "162", "180", "181", "182", "185")
    }
}
