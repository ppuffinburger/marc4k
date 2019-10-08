package org.marc4k.io.marcxml

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.marc4k.MarcException
import org.marc4k.io.MarcReader
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import java.io.InputStream
import java.util.concurrent.LinkedBlockingQueue
import javax.xml.transform.Source
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamSource

class MarcXmlReader : MarcReader {
    private val recordQueue = LinkedBlockingQueue<Record>(20)

    constructor(inputStream: InputStream) {
        startParsing(inputStream)
    }

    constructor(inputStream: InputStream, stylesheet: String) : this(inputStream = inputStream, stylesheet = StreamSource(stylesheet))

    constructor(inputStream: InputStream, stylesheet: Source) {
        try {
            val transformerHandler = (SAXTransformerFactory.newInstance() as SAXTransformerFactory).newTransformerHandler(stylesheet)
            startParsing(inputStream, transformerHandler)
        } catch (e: TransformerConfigurationException) {
            throw MarcException("Failure to create TransformerHandler", e)
        }
    }

    constructor(inputStream: InputStream, transformerHandler: TransformerHandler) {
        startParsing(inputStream, transformerHandler)
    }

    override fun hasNext(): Boolean {
        loop@ while (true) {
            return when(recordQueue.peek()) {
                null -> continue@loop
                is MarcRecord -> true
                else -> false
            }
        }
    }

    override fun next(): Record {
        return recordQueue.take()
    }

    override fun close() {
        recordQueue.clear()
    }

    private fun startParsing(inputStream: InputStream, transformerHandler: TransformerHandler? = null) {
        GlobalScope.launch {
            try {
                MarcXmlParser(recordQueue).parse(inputStream, transformerHandler)
            } catch (e: Exception) {
                throw MarcException(e)
            }
        }
    }
}