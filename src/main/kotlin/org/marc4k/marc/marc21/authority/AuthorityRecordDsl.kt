package org.marc4k.marc.marc21.authority

import org.marc4k.marc.*
import org.marc4k.marc.marc21.CharacterCodingScheme

fun authorityRecord(block: AuthorityRecordBuilder.() -> Unit): AuthorityRecord = AuthorityRecordBuilder().apply(block).build()

@MarcRecordDsl
class AuthorityRecordBuilder {
    private val leader: AuthorityLeader = AuthorityLeader()
    private val controlFields: MutableList<ControlField> = mutableListOf()
    private val dataFields: MutableList<DataField> = mutableListOf()

    fun leader(block: AuthorityLeaderBuilder.() -> Unit) {
        leader.setData(AuthorityLeaderBuilder().apply(block).build().getData())
    }

    fun controlFields(block: CONTROLFIELDS.() -> Unit) {
        controlFields.addAll(CONTROLFIELDS().apply(block))
    }

    fun dataFields(block: DATAFIELDS.() -> Unit) {
        dataFields.addAll(DATAFIELDS().apply(block))
    }

    fun build(): AuthorityRecord = AuthorityRecord().apply {
        leader.setData(this@AuthorityRecordBuilder.leader.getData())
        controlFields.addAll(this@AuthorityRecordBuilder.controlFields)
        dataFields.addAll(this@AuthorityRecordBuilder.dataFields)
    }
}

@MarcRecordDsl
class AuthorityLeaderBuilder {
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
    var punctuationPolicy = PunctuationPolicy.INVALID
    var undefinedPosition19 = ' '
    var lengthOfTheLengthOfFieldPortion = '4'
    var lengthOfTheStartingCharacterPositionPortion = '5'
    var lengthOfTheImplementationDefinedPortion = '0'
    var undefinedPosition23 = '0'

    fun build() = AuthorityLeader().apply {
        recordLength = this@AuthorityLeaderBuilder.recordLength
        recordStatus = this@AuthorityLeaderBuilder.recordStatus
        typeOfRecord = this@AuthorityLeaderBuilder.typeOfRecord
        undefinedPosition7 = this@AuthorityLeaderBuilder.undefinedPosition7
        undefinedPosition8 = this@AuthorityLeaderBuilder.undefinedPosition8
        characterCodingScheme = this@AuthorityLeaderBuilder.characterCodingScheme
        indicatorCount = this@AuthorityLeaderBuilder.indicatorCount
        subfieldCodeCount = this@AuthorityLeaderBuilder.subfieldCodeCount
        baseAddressOfData = this@AuthorityLeaderBuilder.baseAddressOfData
        encodingLevel = this@AuthorityLeaderBuilder.encodingLevel
        punctuationPolicy = this@AuthorityLeaderBuilder.punctuationPolicy
        undefinedPosition19 = this@AuthorityLeaderBuilder.undefinedPosition19
        lengthOfTheLengthOfFieldPortion = this@AuthorityLeaderBuilder.lengthOfTheLengthOfFieldPortion
        lengthOfTheStartingCharacterPositionPortion = this@AuthorityLeaderBuilder.lengthOfTheStartingCharacterPositionPortion
        lengthOfTheImplementationDefinedPortion = this@AuthorityLeaderBuilder.lengthOfTheImplementationDefinedPortion
        undefinedPosition23 = this@AuthorityLeaderBuilder.undefinedPosition23
    }
}
