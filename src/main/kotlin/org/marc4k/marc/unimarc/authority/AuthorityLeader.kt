package org.marc4k.marc.unimarc.authority

import org.marc4k.marc.Leader
import kotlin.properties.Delegates

class AuthorityLeader : Leader {
    // @formatter:off
    var recordLength by Delegates.observable(_recordLength) { _, _, newValue -> _recordLength = newValue }
    var recordStatus by Delegates.observable(RecordStatus.fromValue(_recordStatus)) { _, _, newValue -> _recordStatus = newValue.value }
    var typeOfRecord by Delegates.observable(TypeOfRecord.fromValue(_typeOfRecord)) { _, _, newValue -> _typeOfRecord = newValue.value }
    var undefinedPosition7 by Delegates.observable(_implementationDefined1[0]) { _, _, newValue -> _implementationDefined1[0] = newValue }
    var undefinedPosition8 by Delegates.observable(_implementationDefined1[1]) { _, _, newValue -> _implementationDefined1[1] = newValue }
    var typeOfEntity by Delegates.observable(TypeOfEntity.fromValue(_implementationDefined1[2])) { _, _, newValue -> _implementationDefined1[2] = newValue.value }
    var indicatorCount by Delegates.observable(_indicatorCount) { _, _, newValue -> _indicatorCount = newValue }
    var subfieldCodeCount by Delegates.observable(_subfieldCodeCount) { _, _, newValue -> _subfieldCodeCount = newValue }
    var baseAddressOfData by Delegates.observable(_baseAddressOfData) { _, _, newValue -> _baseAddressOfData = newValue }
    var encodingLevel by Delegates.observable(EncodingLevel.fromValue(_implementationDefined2[0])) { _, _, newValue -> _implementationDefined2[0] = newValue.value }
    var undefinedPosition18 by Delegates.observable(_implementationDefined2[1]) { _, _, newValue -> _implementationDefined2[1] = newValue }
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
        undefinedPosition7 = data[7]
        undefinedPosition8 = data[8]
        typeOfEntity = TypeOfEntity.fromValue(data[9])
        baseAddressOfData = data.substring(12, 17).toInt()
        encodingLevel = EncodingLevel.fromValue(data[17])
        undefinedPosition18 = data[18]
        undefinedPosition19 = data[19]
    }
}

enum class RecordStatus(val value: Char) {
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfRecord(val value: Char) {
    AUTHORITY('x'),
    REFERENCE('y'),
    GENERAL_EXPLANATORY('z'),
    INVALID('\u0000'),;

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfEntity(val value: Char) {
    PERSONAL_NAME('a'),
    CORPORATE_NAME('b'),
    TERRITORIAL_OR_GEOGRAPHICAL_NAME('c'),
    TRADEMARK('d'),
    FAMILY_NAME('e'),
    UNIFORM_NAME('f'),
    COLLECTIVE_UNIFORM_TITLE('g'),
    NAME_TITLE('h'),
    NAME_COLLECTIVE_UNIFORM_TITLE('i'),
    TOPICAL_SUBJECT('j'),
    PLACE_ACCESS('k'),
    FORM_GENRE_OR_PHYSICAL_CHARACTERISTICS('l'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class EncodingLevel(val value: Char) {
    FULL_LEVEL(' '),
    PARTIAL('3'),
    INVALID('\u0000');

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}