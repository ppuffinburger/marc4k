package org.marc4k.marc.marc21.bibliographic

import org.marc4k.marc.*
import org.marc4k.marc.marc21.CharacterCodingScheme

fun bibliographicRecord(block: BibliographicRecordBuilder.() -> Unit): BibliographicRecord = BibliographicRecordBuilder().apply(block).build()

@MarcRecordDsl
class BibliographicRecordBuilder {
    private val leader: BibliographicLeader = BibliographicLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: BibliographicLeaderBuilder.() -> Unit) {
        leader.setData(BibliographicLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): BibliographicRecord = BibliographicRecord().apply {
        leader.setData(this@BibliographicRecordBuilder.leader.getData())
        controlFields.addAll(this@BibliographicRecordBuilder.controlFields)
        dataFields.addAll(this@BibliographicRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class BibliographicLeaderBuilder {
    var recordLength = 0
    var recordStatus = RecordStatus.INVALID
    var typeOfRecord = TypeOfRecord.INVALID
    var bibliographicLevel = BibliographicLevel.INVALID
    var typeOfControl = TypeOfControl.INVALID
    var characterCodingScheme = CharacterCodingScheme.INVALID
    var indicatorCount = 2
    var subfieldCodeCount = 2
    var baseAddressOfData = 0
    var encodingLevel = EncodingLevel.INVALID
    var descriptiveCatalogingForm = DescriptiveCatalogingForm.INVALID
    var multipartResourceRecordLevel = MultipartResourceRecordLevel.INVALID
    var lengthOfTheLengthOfFieldPortion = '4'
    var lengthOfTheStartingCharacterPositionPortion = '5'
    var lengthOfTheImplementationDefinedPortion = '0'
    var undefinedPosition23 = '0'

    fun build() = BibliographicLeader().apply {
        recordLength = this@BibliographicLeaderBuilder.recordLength
        recordStatus = this@BibliographicLeaderBuilder.recordStatus
        typeOfRecord = this@BibliographicLeaderBuilder.typeOfRecord
        bibliographicLevel = this@BibliographicLeaderBuilder.bibliographicLevel
        typeOfControl = this@BibliographicLeaderBuilder.typeOfControl
        characterCodingScheme = this@BibliographicLeaderBuilder.characterCodingScheme
        indicatorCount = this@BibliographicLeaderBuilder.indicatorCount
        subfieldCodeCount = this@BibliographicLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@BibliographicLeaderBuilder.baseAddressOfData
        encodingLevel = this@BibliographicLeaderBuilder.encodingLevel
        descriptiveCatalogingForm = this@BibliographicLeaderBuilder.descriptiveCatalogingForm
        multipartResourceRecordLevel = this@BibliographicLeaderBuilder.multipartResourceRecordLevel
        lengthOfTheLengthOfFieldPortion = this@BibliographicLeaderBuilder.lengthOfTheLengthOfFieldPortion
        lengthOfTheStartingCharacterPositionPortion = this@BibliographicLeaderBuilder.lengthOfTheStartingCharacterPositionPortion
        lengthOfTheImplementationDefinedPortion = this@BibliographicLeaderBuilder.lengthOfTheImplementationDefinedPortion
        undefinedPosition23 = this@BibliographicLeaderBuilder.undefinedPosition23
    }
}
