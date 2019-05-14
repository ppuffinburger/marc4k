package org.marc4k.marc.marc21

import org.marc4k.marc.Record

abstract class Marc21Record : Record() {
    abstract fun getValid008Data(): String?
}