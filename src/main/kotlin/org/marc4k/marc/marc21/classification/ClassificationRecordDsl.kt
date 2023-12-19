package org.marc4k.marc.marc21.classification

import org.marc4k.marc.*
import org.marc4k.marc.marc21.CharacterCodingScheme

fun classificationRecord(block: ClassificationRecordBuilder.() -> Unit): ClassificationRecord = ClassificationRecordBuilder().apply(block).build()

@MarcRecordDsl
class ClassificationRecordBuilder {
    private val leader: ClassificationLeader = ClassificationLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: ClassificationLeaderBuilder.() -> Unit) {
        leader.setData(ClassificationLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): ClassificationRecord = ClassificationRecord().apply {
        leader.setData(this@ClassificationRecordBuilder.leader.getData())
        controlFields.addAll(this@ClassificationRecordBuilder.controlFields)
        dataFields.addAll(this@ClassificationRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class ClassificationLeaderBuilder {
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
    private var undefinedPosition18 = ' '
    var undefinedPosition19 = ' '
    var lengthOfTheLengthOfFieldPortion = '4'
    var lengthOfTheStartingCharacterPositionPortion = '5'
    var lengthOfTheImplementationDefinedPortion = '0'
    var undefinedPosition23 = '0'

    fun build() = ClassificationLeader().apply {
        recordLength = this@ClassificationLeaderBuilder.recordLength
        recordStatus = this@ClassificationLeaderBuilder.recordStatus
        typeOfRecord = this@ClassificationLeaderBuilder.typeOfRecord
        undefinedPosition7 = this@ClassificationLeaderBuilder.undefinedPosition7
        undefinedPosition8 = this@ClassificationLeaderBuilder.undefinedPosition8
        characterCodingScheme = this@ClassificationLeaderBuilder.characterCodingScheme
        indicatorCount = this@ClassificationLeaderBuilder.indicatorCount
        subfieldCodeCount = this@ClassificationLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@ClassificationLeaderBuilder.baseAddressOfData
        encodingLevel = this@ClassificationLeaderBuilder.encodingLevel
        undefinedPosition18 = this@ClassificationLeaderBuilder.undefinedPosition18
        undefinedPosition19 = this@ClassificationLeaderBuilder.undefinedPosition19
        lengthOfTheLengthOfFieldPortion = this@ClassificationLeaderBuilder.lengthOfTheLengthOfFieldPortion
        lengthOfTheStartingCharacterPositionPortion = this@ClassificationLeaderBuilder.lengthOfTheStartingCharacterPositionPortion
        lengthOfTheImplementationDefinedPortion = this@ClassificationLeaderBuilder.lengthOfTheImplementationDefinedPortion
        undefinedPosition23 = this@ClassificationLeaderBuilder.undefinedPosition23
    }
}