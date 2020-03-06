package org.marc4k.marc.marc21.holdings

import org.marc4k.marc.*
import org.marc4k.marc.marc21.CharacterCodingScheme

fun holdingsRecord(block: HoldingsRecordBuilder.() -> Unit): HoldingsRecord = HoldingsRecordBuilder().apply(block).build()

@MarcRecordDsl
class HoldingsRecordBuilder {
    private val leader: HoldingsLeader = HoldingsLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: HoldingsLeaderBuilder.() -> Unit) {
        leader.setData(HoldingsLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): HoldingsRecord = HoldingsRecord().apply {
        leader.setData(this@HoldingsRecordBuilder.leader.getData())
        controlFields.addAll(this@HoldingsRecordBuilder.controlFields)
        dataFields.addAll(this@HoldingsRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class HoldingsLeaderBuilder {
    var recordLength = 0
    var recordStatus = RecordStatus.INVALID
    var typeOfRecord = TypeOfRecord.INVALID
    var undefinedPosition7 = ' '
    var undefinedPosition8 = ' '
    var characterCodingScheme = CharacterCodingScheme.INVALID
    var indicatorCount = 2
    var subfieldCodeCount = 2
    var baseAddressOfData = 0
    var encodingLevel = EncodingLevel.INVALID
    var itemInformationInRecord = ItemInformationInRecord.INVALID
    var undefinedPosition19 = ' '
    var lengthOfTheLengthOfFieldPortion = '4'
    var lengthOfTheStartingCharacterPositionPortion = '5'
    var lengthOfTheImplementationDefinedPortion = '0'
    var undefinedPosition23 = '0'

    fun build() = HoldingsLeader().apply {
        recordLength = this@HoldingsLeaderBuilder.recordLength
        recordStatus = this@HoldingsLeaderBuilder.recordStatus
        typeOfRecord = this@HoldingsLeaderBuilder.typeOfRecord
        undefinedPosition7 = this@HoldingsLeaderBuilder.undefinedPosition7
        undefinedPosition8 = this@HoldingsLeaderBuilder.undefinedPosition8
        characterCodingScheme = this@HoldingsLeaderBuilder.characterCodingScheme
        indicatorCount = this@HoldingsLeaderBuilder.indicatorCount
        subfieldCodeCount = this@HoldingsLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@HoldingsLeaderBuilder.baseAddressOfData
        encodingLevel = this@HoldingsLeaderBuilder.encodingLevel
        itemInformationInRecord = this@HoldingsLeaderBuilder.itemInformationInRecord
        undefinedPosition19 = this@HoldingsLeaderBuilder.undefinedPosition19
        lengthOfTheLengthOfFieldPortion = this@HoldingsLeaderBuilder.lengthOfTheLengthOfFieldPortion
        lengthOfTheStartingCharacterPositionPortion = this@HoldingsLeaderBuilder.lengthOfTheStartingCharacterPositionPortion
        lengthOfTheImplementationDefinedPortion = this@HoldingsLeaderBuilder.lengthOfTheImplementationDefinedPortion
        undefinedPosition23 = this@HoldingsLeaderBuilder.undefinedPosition23
    }
}
