package org.marc4k.marc

@DslMarker
annotation class MarcRecordDsl

fun marcRecord(block: MarcRecordBuilder.() -> Unit): MarcRecord = MarcRecordBuilder().apply(block).build()

@MarcRecordDsl
class MarcRecordBuilder {
    private val leader: MarcLeader = MarcLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: MarcLeaderBuilder.() -> Unit) {
        leader.setData(MarcLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): MarcRecord = MarcRecord().apply {
        leader.setData(this@MarcRecordBuilder.leader.getData())
        controlFields.addAll(this@MarcRecordBuilder.controlFields)
        dataFields.addAll(this@MarcRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class MarcLeaderBuilder {
    var recordLength = 0
    var recordStatus = ' '
    var typeOfRecord = ' '
    var implementationDefined1 = charArrayOf(' ', ' ', ' ')
    var indicatorCount = 2
    var subfieldCodeCount = 2
    var baseAddressOfData = 0
    var implementationDefined2 = charArrayOf(' ', ' ', ' ')
    var entryMap = CharArray(4)

    fun build() = MarcLeader().apply {
        recordLength = this@MarcLeaderBuilder.recordLength
        recordStatus = this@MarcLeaderBuilder.recordStatus
        typeOfRecord = this@MarcLeaderBuilder.typeOfRecord
        implementationDefined1 = this@MarcLeaderBuilder.implementationDefined1
        indicatorCount = this@MarcLeaderBuilder.indicatorCount
        subfieldCodeCount = this@MarcLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@MarcLeaderBuilder.baseAddressOfData
        implementationDefined2 = this@MarcLeaderBuilder.implementationDefined2
        entryMap = this@MarcLeaderBuilder.entryMap
    }
}

@MarcRecordDsl
class ControlFieldBuilder {
    var tag = ""
    var data = ""

    fun build() = ControlField(tag, data)
}

@MarcRecordDsl
class CONTROLFIELDS: ArrayList<ControlField>() {
    fun controlField(block: ControlFieldBuilder.() -> Unit) {
        add(ControlFieldBuilder().apply(block).build())
    }
}

@MarcRecordDsl
class DataFieldBuilder {
    var tag = ""
    var indicator1 = ' '
    var indicator2 = ' '

    private val subfields = mutableListOf<Subfield>()

    fun subfields(block: SUBFIELDS.() -> Unit) {
        subfields.addAll(SUBFIELDS().apply(block))
    }

    fun build() = DataField(tag, indicator1, indicator2, subfields)
}

@MarcRecordDsl
class DATAFIELDS: ArrayList<DataField>() {
    fun dataField(block: DataFieldBuilder.() -> Unit) {
        add(DataFieldBuilder().apply(block).build())
    }
}

@MarcRecordDsl
class SubfieldBuilder {
    var name = ' '
    var data = ""

    fun build() = Subfield(name, data)
}

@MarcRecordDsl
class SUBFIELDS: ArrayList<Subfield>() {
    fun subfield(block: SubfieldBuilder.() -> Unit) {
        add(SubfieldBuilder().apply(block).build())
    }
}