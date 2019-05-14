package org.marc4k.marc.marc21.holdings

import org.marc4k.marc.Leader
import org.marc4k.marc.marc21.Marc21Record

class HoldingsRecord : Marc21Record() {
    override val leader: Leader = HoldingsLeader()

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 32 }?.data
}