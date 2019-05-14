package org.marc4k.marc.marc21.bibliographic

import org.marc4k.marc.Leader
import org.marc4k.marc.marc21.CharacterCodingScheme
import kotlin.properties.Delegates

class BibliographicLeader : Leader {
    // @formatter:off
    var recordLength by Delegates.observable(_recordLength) { _, _, newValue -> _recordLength = newValue }
    var recordStatus by Delegates.observable(RecordStatus.fromValue(_recordStatus)) { _, _, newValue -> _recordStatus = newValue.value }
    var typeOfRecord by Delegates.observable(TypeOfRecord.fromValue(_typeOfRecord)) { _, _, newValue -> _typeOfRecord = newValue.value }
    var bibliographicLevel by Delegates.observable(BibliographicLevel.fromValue(_implementationDefined1[0])) { _, _, newValue -> _implementationDefined1[0] = newValue.value }
    var typeOfControl by Delegates.observable(TypeOfControl.fromValue(_implementationDefined1[1])) { _, _, newValue -> _implementationDefined1[1] = newValue.value }
    var characterCodingScheme by Delegates.observable(CharacterCodingScheme.fromValue(_implementationDefined1[2])) { _, _, newValue -> _implementationDefined1[2] = newValue.value }
    var indicatorCount by Delegates.observable(_indicatorCount) { _, _, newValue -> _indicatorCount = newValue }
    var subfieldCodeCount by Delegates.observable(_subfieldCodeCount) { _, _, newValue -> _subfieldCodeCount = newValue }
    var baseAddressOfData by Delegates.observable(_baseAddressOfData) { _, _, newValue -> _baseAddressOfData = newValue }
    var encodingLevel by Delegates.observable(EncodingLevel.fromValue(_implementationDefined2[0])) { _, _, newValue -> _implementationDefined2[0] = newValue.value }
    var descriptiveCatalogingForm by Delegates.observable(DescriptiveCatalogingForm.fromValue(_implementationDefined2[1])) { _, _, newValue -> _implementationDefined2[1] = newValue.value }
    var multipartResourceRecordLevel by Delegates.observable(MultipartResourceRecordLevel.fromValue(_implementationDefined2[2])) { _, _, newValue -> _implementationDefined2[2] = newValue.value }
    var lengthOfTheLengthOfFieldPortion by Delegates.observable(_entryMap[0]) { _, _, newValue -> _entryMap[0] = newValue }
    var lengthOfTheStartingCharacterPositionPortion by Delegates.observable(_entryMap[1]) { _, _, newValue -> _entryMap[1] = newValue }
    var lengthOfTheImplementationDefinedPortion by Delegates.observable(_entryMap[2]) { _, _, newValue -> _entryMap[2] = newValue }
    var undefinedPosition23 by Delegates.observable(_entryMap[3]) { _, _, newValue -> _entryMap[3] = newValue }
    // @formatter:on

    constructor() {
        indicatorCount = 2
        subfieldCodeCount = 2
        lengthOfTheLengthOfFieldPortion = '4'
        lengthOfTheStartingCharacterPositionPortion = '5'
        lengthOfTheImplementationDefinedPortion = '0'
        undefinedPosition23 = '0'
    }

    constructor(data: String) : this() {
        setData(data)
    }

    constructor(leader: Leader) : this(leader.getData())

    override fun setData(data: String) {
        require(data.length == 24) { "Leader length should be 24 bytes" }

        recordLength = data.substring(0, 5).toInt()
        recordStatus = RecordStatus.fromValue(data[5])
        typeOfRecord = TypeOfRecord.fromValue(data[6])
        bibliographicLevel = BibliographicLevel.fromValue(data[7])
        typeOfControl = TypeOfControl.fromValue(data[8])
        characterCodingScheme = CharacterCodingScheme.fromValue(data[9])
        baseAddressOfData = data.substring(12, 17).toInt()
        encodingLevel = getEncodingLevel(data[17])
        descriptiveCatalogingForm = DescriptiveCatalogingForm.fromValue(data[18])
        multipartResourceRecordLevel = MultipartResourceRecordLevel.fromValue(data[19])
    }

    private fun getEncodingLevel(encodingLevel: Char): EncodingLevel {
        return when(encodingLevel) {
            'I' -> EncodingLevel.FULL_LEVEL
            'K' -> EncodingLevel.MINIMAL_LEVEL
            'L' -> EncodingLevel.FULL_LEVEL_MATERIAL_NOT_EXAMINED
            'M' -> EncodingLevel.LESS_THAN_FULL_LEVEL_MATERIAL_NOT_EXAMINED
            'J' -> EncodingLevel.LESS_THAN_FULL_LEVEL_MATERIAL_NOT_EXAMINED
            else -> EncodingLevel.fromValue(encodingLevel)
        }
    }
}

enum class RecordStatus(val value: Char) {
    INCREASE_IN_ENCODING_LEVEL('a'),
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INCREASE_IN_ENCODING_LEVEL_FROM_PREPUBLICATION('p'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfRecord(val value: Char) {
    LANGUAGE_MATERIAL('a'),
    NOTATED_MUSIC('c'),
    MANUSCRIPT_NOTED_MUSIC('d'),
    CARTOGRAPHIC_MATERIAL('e'),
    MANUSCRIPT_CARTOGRAPHIC_MATERIAL('f'),
    PROJECTED_MEDIUM('g'),
    NON_MUSICAL_SOUND_RECORDING('i'),
    MUSICAL_SOUND_RECORDING('j'),
    TWO_DIMENSIONAL_NON_PROJECTABLE_GRAPHIC('k'),
    COMPUTER_FILE('m'),
    KIT('o'),
    MIXED_MATERIAL('p'),
    THREE_DIMENSIONAL_ARTIFACT_OR_NATURALLY_OCCURRING_OBJECT('r'),
    MANUSCRIPT_LANGUAGE_MATERIAL('t'),
    INVALID('\u0000'),;

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class BibliographicLevel(val value: Char) {
    MONOGRAPHIC_COMPONENT_PART('a'),
    SERIAL_COMPONENT_PART('b'),
    COLLECTION('c'),
    SUBUNIT('d'),
    INTEGRATING_RESOURCE('i'),
    MONOGRAPH_ITEM('m'),
    SERIAL('s'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfControl(val value: Char) {
    NO_SPECIFIC_TYPE(' '),
    ARCHIVAL('a'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class EncodingLevel(val value: Char) {
    FULL_LEVEL(' '),
    FULL_LEVEL_MATERIAL_NOT_EXAMINED('1'),
    LESS_THAN_FULL_LEVEL_MATERIAL_NOT_EXAMINED('2'),
    ABBREVIATED_LEVEL('3'),
    CORE_LEVEL('4'),
    PARTIAL_PRELIMINARY_LEVEL('5'),
    MINIMAL_LEVEL('7'),
    PREPUBLICATION_LEVEL('8'),
    UNKNOWN('u'),
    NOT_APPLICABLE('z'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class DescriptiveCatalogingForm(val value: Char) {
    NON_ISBD(' '),
    AACR2('a'),
    ISBD_PUNCTUATION_OMITTED('c'),
    ISBD_PUNCTUATION_INCLUDED('i'),
    NON_ISBD_PUNCTUATION_OMITTED('n'),
    UNKNOWN('u'),
    INVALID('\u0000'),;

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class MultipartResourceRecordLevel(val value: Char) {
    NOT_SPECIFIED_OR_NOT_APPLICABLE(' '),
    SET('a'),
    PART_WITH_INDEPENDENT_TITLE('b'),
    PART_WITH_DEPENDENT_TITLE('c'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}
