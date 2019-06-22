package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.marc.Record

class MarcMultiplexReader(readers: List<MarcReader>) : MarcReader {
    private val readersIterator = readers.iterator()
    private var currentReader: MarcReader? = getNextMarcReader()

    override fun hasNext(): Boolean {
        if (currentReader == null || (currentReader?.hasNext() == false)) {
            currentReader = getNextMarcReader()
        }
        return currentReader?.hasNext() ?: false
    }

    override fun next(): Record {
        return currentReader?.next() ?: throw MarcException("Current file reader became null before call to next()")
    }

    override fun close() {
        currentReader?.close()
    }

    private fun getNextMarcReader(): MarcReader? {
        currentReader?.close()

        return if (readersIterator.hasNext()) {
            readersIterator.next()
        } else {
            null
        }
    }
}