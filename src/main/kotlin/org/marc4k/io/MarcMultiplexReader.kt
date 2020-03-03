package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.marc.MarcRecord

/**
 * A [MarcReader] that reads records as it iterates through a list of MarcReaders.
 *
 * @param[readers] the list of MarcReaders to be used.
 */
class MarcMultiplexReader(readers: List<MarcReader>) : MarcReader {
    private val readersIterator = readers.iterator()
    private var currentReader: MarcReader? = getNextMarcReader()

    /**
     * Returns true if there could be another record.
     */
    override fun hasNext(): Boolean {
        if (currentReader == null || (currentReader?.hasNext() == false)) {
            currentReader = getNextMarcReader()
        }
        return currentReader?.hasNext() ?: false
    }

    /**
     * Returns the next [MarcRecord].
     *
     * @throws[MarcException] if an [Exception] occurs or the current file read became null.
     */
    override fun next(): MarcRecord {
        return currentReader?.next() as MarcRecord? ?: throw MarcException("Current file reader became null before call to next()")
    }

    /**
     * Closes the underlying [currentReader].
     */
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