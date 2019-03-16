package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.marc.Record
import org.marc4k.marc.marc21.authority.AuthorityRecord
import org.marc4k.marc.marc21.bibliographic.BibliographicRecord
import org.marc4k.marc.marc21.classification.ClassificationRecord
import org.marc4k.marc.marc21.community.CommunityRecord
import org.marc4k.marc.marc21.holdings.HoldingsRecord

class Marc21StreamReader(private val reader: MarcStreamReader) : MarcReader by reader {
    override fun next(): Record {
        val record = reader.next()

        return when (val typeOfRecord = record.leader.typeOfRecord) {
            'a', 'c', 'd', 'e', 'f', 'g', 'i', 'j', 'k', 'm', 'n', 'o', 'p', 'r', 't' -> BibliographicRecord().apply { copyFrom(record) }
            'u', 'v', 'x', 'y' -> HoldingsRecord().apply { copyFrom(record) }
            'z' -> AuthorityRecord().apply { copyFrom(record) }
            'w' -> ClassificationRecord().apply { copyFrom(record) }
            'q' -> CommunityRecord().apply { copyFrom(record) }
            else -> throw MarcException("Invalid MARC21 record type : '$typeOfRecord'")
        }
    }
}
