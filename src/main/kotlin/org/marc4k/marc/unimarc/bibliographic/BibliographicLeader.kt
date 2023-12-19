package org.marc4k.marc.unimarc.bibliographic

import org.marc4k.marc.Leader
import kotlin.properties.Delegates

class BibliographicLeader : Leader {
    // @formatter:off
    var recordLength by Delegates.observable(_recordLength) { _, _, newValue -> _recordLength = newValue }
    var recordStatus by Delegates.observable(RecordStatus.fromValue(_recordStatus)) { _, _, newValue -> _recordStatus = newValue.value }
    var typeOfRecord by Delegates.observable(TypeOfRecord.fromValue(_typeOfRecord)) { _, _, newValue -> _typeOfRecord = newValue.value }
    var bibliographicLevel by Delegates.observable(BibliographicLevel.fromValue(_implementationDefined1[0])) { _, _, newValue -> _implementationDefined1[0] = newValue.value }
    var hierarchicalLevelCode by Delegates.observable(HierarchicalLevelCode.fromValue(_implementationDefined1[1])) { _, _, newValue -> _implementationDefined1[1] = newValue.value }
    var typeOfControl by Delegates.observable(TypeOfControl.fromValue(_implementationDefined1[2])) { _, _, newValue -> _implementationDefined1[2] = newValue.value }
    var indicatorCount by Delegates.observable(_indicatorCount) { _, _, newValue -> _indicatorCount = newValue }
    var subfieldCodeCount by Delegates.observable(_subfieldCodeCount) { _, _, newValue -> _subfieldCodeCount = newValue }
    var baseAddressOfData by Delegates.observable(_baseAddressOfData) { _, _, newValue -> _baseAddressOfData = newValue }
    var encodingLevel by Delegates.observable(EncodingLevel.fromValue(_implementationDefined2[0])) { _, _, newValue -> _implementationDefined2[0] = newValue.value }
    var descriptiveCatalogingForm by Delegates.observable(DescriptiveCatalogingForm.fromValue(_implementationDefined2[1])) { _, _, newValue -> _implementationDefined2[1] = newValue.value }
    var undefinedPosition19 by Delegates.observable(_implementationDefined2[2]) { _, _, newValue -> _implementationDefined2[2] = newValue }
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
        undefinedPosition23 = ' '
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
        hierarchicalLevelCode = HierarchicalLevelCode.fromValue(data[8])
        typeOfControl = TypeOfControl.fromValue(data[9])
        baseAddressOfData = data.substring(12, 17).toInt()
        encodingLevel = EncodingLevel.fromValue(data[17])
        descriptiveCatalogingForm = DescriptiveCatalogingForm.fromValue(data[18])
        undefinedPosition19 = data[19]
    }
}

enum class RecordStatus(val value: Char) {
    CORRECTED('c'),
    DELETED('d'),
    NEW('n'),
    PREVIOUSLY_ISSUED_HIGHER_LEVEL_RECORD('o'),
    PREVIOUSLY_ISSUED_AS_AN_INCOMPLETE_PREPUBLICATION_RECORD('p'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfRecord(val value: Char) {
    LANGUAGE_MATERIAL('a'),
    MANUSCRIPT_LANGUAGE_MATERIAL('b'),
    NOTATED_MUSIC('c'),
    MANUSCRIPT_NOTATED_MUSIC('d'),
    CARTOGRAPHIC_MATERIAL('e'),
    MANUSCRIPT_CARTOGRAPHIC_MATERIAL('f'),
    PROJECTED_AND_VIDEO_MATERIAL('g'),
    NON_MUSICAL_SOUND_RECORDING('i'),
    MUSICAL_SOUND_RECORDING('j'),
    TWO_DIMENSIONAL_GRAPHIC('k'),
    ELECTRONIC_RESOURCE('l'),
    MULTIMEDIA('m'),
    THREE_DIMENSIONAL_ARTEFACT('r'),
    INVALID('\u0000'),;

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class BibliographicLevel(val value: Char) {
    ANALYTIC_COMPONENT_PART('a'),
    COLLECTION('c'),
    INTEGRATING_RESOURCE('i'),
    MONOGRAPH('m'),
    SERIAL('s'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class HierarchicalLevelCode(val value: Char) {
    HIERARCHICAL_RELATIONSHIP_UNDEFINED(' '),
    NO_HIERARCHICAL_RELATIONSHIP('0'),
    HIGHEST_LEVEL_RECORD('1'),
    RECORD_BELOW_HIGHEST_LEVEL('2'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfControl(val value: Char) {
    NO_SPECIFIED_TYPE(' '),
    ARCHIVAL('a'),
    MUSEUM('m'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class EncodingLevel(val value: Char) {
    FULL_LEVEL(' '),
    SUBLEVEL_1('1'),
    SUBLEVEL_2('2'),
    SUBLEVEL_3('3'),
    INVALID('\u0000');

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class DescriptiveCatalogingForm(val value: Char) {
    FULL_ISBD(' '),
    PARTIAL_OR_INCOMPLETE_ISBD('i'),
    NON_ISBD('n'),
    ISBD_NOT_APPLICABLE('x'),
    INVALID('\u0000'),;

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}