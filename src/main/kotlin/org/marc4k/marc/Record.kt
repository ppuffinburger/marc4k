package org.marc4k.marc

import org.marc4k.MarcError
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder

abstract class Record {
    abstract val leader: Leader

    val controlFields = mutableListOf<ControlField>()
    val dataFields = mutableListOf<DataField>()
    val errors = mutableListOf<MarcError>()

    fun getControlNumber() = controlFields.firstOrNull { it.tag == "001" }?.data
    fun getDateOfLatestTransaction() = controlFields.firstOrNull { it.tag == "005" }?.let { LocalDateTime.parse(it.data, dateTimeFormatter) }

    fun copyFrom(record: Record) {
        leader.setData(record.leader.getData())
        controlFields.apply {
            addAll(record.controlFields.map { it.copy() })
        }
        dataFields.apply {
            addAll(record.dataFields.map { it.copy() })
        }
    }

    override fun toString(): String {
        with(StringBuilder()) {
            appendLine(leader)
            controlFields.forEach { appendLine(it.toString()) }
            dataFields.forEach { appendLine(it.toString()) }
            return toString()
        }
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("yyyyMMddHHmmss.0")
            .toFormatter()
    }
}