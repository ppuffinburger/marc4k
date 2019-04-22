package org.marc4k.io.marcxml

import org.marc4k.MarcException
import org.marc4k.marc.Leader
import org.marc4k.marc.Record
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.InputStream
import java.util.concurrent.BlockingQueue
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.TransformerHandler

class MarcXmlParser(private val recordQueue: BlockingQueue<Record>) : MarcXmlHandlerCallback {
    override fun recordRead(record: Record) {
        recordQueue.put(record)
    }

    override fun parseComplete() {
        recordQueue.put(DummyRecord())
    }

    fun parse(inputStream: InputStream, transformerHandler: TransformerHandler?) {
        val reader = try {
            SAXParserFactory.newInstance().newSAXParser().xmlReader.apply {
                setFeature("http://xml.org/sax/features/namespaces", true)
                setFeature("http://xml.org/sax/features/namespace-prefixes", true)
            }
        } catch (e: SAXException) {
            throw MarcException("Failure to create XMLReader", e)
        }

        val marcXmlHandler = MarcXmlHandler(this)

        reader.contentHandler = transformerHandler?.apply { setResult(SAXResult(marcXmlHandler)) } ?: marcXmlHandler

        try {
            reader.parse(InputSource(inputStream))
        } catch (e: Exception) {
            throw MarcException("Failure to parse input", e)
        }
    }
}

internal class DummyLeader : Leader() { override fun setData(data: String) {} }
internal class DummyRecord(override val leader: Leader = DummyLeader()) : Record()