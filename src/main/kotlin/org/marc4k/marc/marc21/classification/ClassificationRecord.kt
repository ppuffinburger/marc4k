package org.marc4k.marc.marc21.classification

import org.marc4k.marc.marc21.Marc21Record

class ClassificationRecord : Marc21Record() {
    override val leader = ClassificationLeader()

    override fun getValid008Data(): String? = controlFields.firstOrNull { it.tag == "008" && it.data.length == 14 }?.data
}