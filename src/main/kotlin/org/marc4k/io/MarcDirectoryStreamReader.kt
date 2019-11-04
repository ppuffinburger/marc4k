package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.io.codec.DefaultMarcDataDecoder
import org.marc4k.io.codec.MarcDataDecoder
import org.marc4k.marc.Record
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class MarcDirectoryStreamReader(directory: File, private val decoder: MarcDataDecoder = DefaultMarcDataDecoder()) : MarcReader {
    private val fileIterator: Iterator<String> = directory.walk().filter { !it.isDirectory && it.extension == "mrc" }.map { it.canonicalPath }.toList().iterator()
    private var currentFileReader: MarcReader? = getNextMarcReader()

    constructor(directory: String, decoder: MarcDataDecoder = DefaultMarcDataDecoder()) : this(File(directory), decoder)

    override fun hasNext(): Boolean {
        if (currentFileReader == null || (currentFileReader?.hasNext() == false)) {
            currentFileReader = getNextMarcReader()
        }
        return currentFileReader?.hasNext() ?: false
    }

    override fun next(): Record {
        try {
            return currentFileReader?.next() ?: throw MarcException("Current file reader became null before call to next()")
        } catch (e: Exception) {
            throw MarcException("Exception caught in underlying reader", e)
        }
    }

    override fun close() {
        currentFileReader?.close()
    }

    private fun getNextMarcReader(): MarcReader? {
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