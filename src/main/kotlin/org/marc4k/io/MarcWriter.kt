package org.marc4k.io

import org.marc4k.marc.Record
import java.io.Closeable

interface MarcWriter : Closeable {
    fun write(record: Record)
}