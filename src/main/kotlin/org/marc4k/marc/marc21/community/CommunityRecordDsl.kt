package org.marc4k.marc.marc21.community

import org.marc4k.marc.*
import org.marc4k.marc.marc21.CharacterCodingScheme

fun communityRecord(block: CommunityRecordBuilder.() -> Unit): CommunityRecord = CommunityRecordBuilder().apply(block).build()

@MarcRecordDsl
class CommunityRecordBuilder {
    private val leader: CommunityLeader = CommunityLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: CommunityLeaderBuilder.() -> Unit) {
        leader.setData(CommunityLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): CommunityRecord = CommunityRecord().apply {
        leader.setData(this@CommunityRecordBuilder.leader.getData())
        controlFields.addAll(this@CommunityRecordBuilder.controlFields)
        dataFields.addAll(this@CommunityRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class CommunityLeaderBuilder {
    var recordLength = 0
    var recordStatus = RecordStatus.INVALID
    var typeOfRecord = TypeOfRecord.INVALID
    var kindOfData = KindOfData.INVALID
    var undefinedPosition8 = ' '
    var characterCodingScheme = CharacterCodingScheme.INVALID
    var indicatorCount = 2
    var subfieldCodeCount = 2
    var baseAddressOfData = 0
    var undefinedPosition17 = ' '
    var undefinedPosition18 = ' '
    var undefinedPosition19 = ' '
    var lengthOfTheLengthOfFieldPortion = '4'
    var lengthOfTheStartingCharacterPositionPortion = '5'
    var lengthOfTheImplementationDefinedPortion = '0'
    var undefinedPosition23 = '0'

    fun build() = CommunityLeader().apply {
        recordLength = this@CommunityLeaderBuilder.recordLength
        recordStatus = this@CommunityLeaderBuilder.recordStatus
        typeOfRecord = this@CommunityLeaderBuilder.typeOfRecord
        kindOfData = this@CommunityLeaderBuilder.kindOfData
        undefinedPosition8 = this@CommunityLeaderBuilder.undefinedPosition8
        characterCodingScheme = this@CommunityLeaderBuilder.characterCodingScheme
        indicatorCount = this@CommunityLeaderBuilder.indicatorCount
        subfieldCodeCount = this@CommunityLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@CommunityLeaderBuilder.baseAddressOfData
        undefinedPosition17 = this@CommunityLeaderBuilder.undefinedPosition17
        undefinedPosition18 = this@CommunityLeaderBuilder.undefinedPosition18
        undefinedPosition19 = this@CommunityLeaderBuilder.undefinedPosition19
        lengthOfTheLengthOfFieldPortion = this@CommunityLeaderBuilder.lengthOfTheLengthOfFieldPortion
        lengthOfTheStartingCharacterPositionPortion = this@CommunityLeaderBuilder.lengthOfTheStartingCharacterPositionPortion
        lengthOfTheImplementationDefinedPortion = this@CommunityLeaderBuilder.lengthOfTheImplementationDefinedPortion
        undefinedPosition23 = this@CommunityLeaderBuilder.undefinedPosition23
    }
}