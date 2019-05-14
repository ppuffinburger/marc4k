package org.marc4k.marc.marc21.community

import org.marc4k.marc.marc21.Marc21Record

class CommunityRecord : Marc21Record() {
    override val leader = CommunityLeader()

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 15 }?.data
}