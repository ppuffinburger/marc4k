package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.io.codec.DefaultMarcDataDecoder
import org.marc4k.io.codec.MarcDataDecoder
import org.marc4k.marc.MarcRecord
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * A [MarcReader] that iterates through all MARC files (.mrc) in a directory and uses a [MarcDataDecoder] to handle character conversions.
 *
 * @param[directory] a directory containing MARC records.
 * @property[decoder] the [MarcDataDecoder] used to transform the character data.  Default is a [DefaultMarcDataDecoder].
 */
class MarcDirectoryStreamReader(directory: File, private val decoder: MarcDataDecoder = DefaultMarcDataDecoder()) : MarcReader {
    private val fileIterator: Iterator<String> = directory.walk().filter { !it.isDirectory && it.extension == "mrc" }.map { it.canonicalPath }.toList().iterator()
    private var currentFileReader: NewMarcStreamReader? = getNextMarcReader()

    /**
     * Instantiates the class using the given [directory] and [decoder].
     */
    constructor(directory: String, decoder: MarcDataDecoder = DefaultMarcDataDecoder()) : this(File(directory), decoder)

    /**
     * Returns true if there could be another record.
     */
    override fun hasNext(): Boolean {
        if (currentFileReader == null || (currentFileReader?.hasNext() == false)) {
            currentFileReader = getNextMarcReader()
        }
        return currentFileReader?.hasNext() ?: false
    }

    /**
     * Returns the next [MarcRecord].
     *
     * @throws[MarcException] if an [Exception] occurs or the current file read became null.
     */
    override fun next(): MarcRecord {
        try {
            return currentFileReader?.next() ?: throw MarcException("Current file reader became null before call to next()")
        } catch (e: Exception) {
            throw MarcException("Exception caught in underlying reader", e)
        }
    }

    /**
     * Closes the underlying [currentFileReader].
     */
    override fun close() {
        currentFileReader?.close()
    }

    private fun getNextMarcReader(): NewMarcStreamReader? {
        currentFileReader?.close()

        return if (fileIterator.hasNext()) {
            try {
                NewMarcStreamReader(FileInputStream(fileIterator.next()), decoder)
            } catch (e: FileNotFoundException) {
                getNextMarcReader()
            }
        } else {
            null
        }
    }
}