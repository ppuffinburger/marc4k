package org.marc4k.marc.marc21.classification

import org.marc4k.marc.Leader
import org.marc4k.marc.Record
import org.marc4k.marc.marc21.Marc21Record

class ClassificationRecord : Record, Marc21Record {
    override val leader: Leader = ClassificationLeader()

    constructor()

    constructor(record: Record) : this() {
        leader.setData(record.leader.getData())
        controlFields.apply {
            addAll(record.controlFields.map { it.copy() })
        }
        dataFields.apply {
            clear()
            addAll(record.dataFields.map { it.copy() })
        }
    }

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 14 }?.data
}