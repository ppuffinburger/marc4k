package org.marc4k.marc.marc21.holdings

import org.marc4k.marc.Leader
import org.marc4k.marc.marc21.CharacterCodingScheme
import kotlin.properties.Delegates

class HoldingsLeader : Leader {
    // @formatter:off
    var recordLength by Delegates.observable(_recordLength) { _, _, newValue -> _recordLength = newValue }
    var recordStatus by Delegates.observable(RecordStatus.fromValue(_recordStatus)) { _, _, newValue -> _recordStatus = newValue.value }
    var typeOfRecord by Delegates.observable(TypeOfRecord.fromValue(_typeOfRecord)) { _, _, newValue -> _typeOfRecord = newValue.value }
    var undefinedPosition7 by Delegates.observable(_implementationDefined1[0]) { _, _, newValue -> _implementationDefined1[0] = newValue }
    var undefinedPosition8 by Delegates.observable(_implementationDefined1[1]) { _, _, newValue -> _implementationDefined1[1] = newValue }
    var characterCodingScheme by Delegates.observable(CharacterCodingScheme.fromValue(_characterCodingScheme)) { _, _, newValue -> _characterCodingScheme = newValue.value }
    var indicatorCount by Delegates.observable(_indicatorCount) { _, _, newValue -> _indicatorCount = newValue }
    var subfieldCodeCount by Delegates.observable(_subfieldCodeCount) { _, _, newValue -> _subfieldCodeCount = newValue }
    var baseAddressOfData by Delegates.observable(_baseAddressOfData) { _, _, newValue -> _baseAddressOfData = newValue }
    var encodingLevel by Delegates.observable(EncodingLevel.fromValue(_implementationDefined2[0])) { _, _, newValue -> _implementationDefined2[0] = newValue.value }
    var itemInformationInRecord by Delegates.observable(ItemInformationInRecord.fromValue(_implementationDefined2[1])) { _, _, newValue -> _implementationDefined2[1] = newValue.value }
    var undefinedPosition19 by Delegates.observable(_implementationDefined2[2]) { _, _, newValue -> _implementationDefined2[2] = newValue }
    var lengthOfTheLengthOfFieldPortion by Delegates.observable(_entryMap[0]) { _, _, newValue -> _entryMap[0] = newValue }
    var lengthOfTheStartingCharacterPositionPortion by Delegates.observable(_entryMap[1]) { _, _, newValue -> _entryMap[1] = newValue }
    var lengthOfTheImplementationDefinedPortion by Delegates.observable(_entryMap[2]) { _, _, newValue -> _entryMap[2] = newValue }
    var undefinedPosition23 by Delegates.observable(_entryMap[3]) { _, _, newValue -> _entryMap[3] = newValue }
    // @formatter:on

    constructor() {
        undefinedPosition7 = ' '
        undefinedPosition8 = ' '
        indicatorCount = 2
        subfieldCodeCount = 2
        undefinedPosition19 = ' '
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
//        data[7] - Undefined Position 7
//        data[8] - Undefined Position 8
        characterCodingScheme = CharacterCodingScheme.fromValue(data[9])
//        data[10] - Indicator Count
//        data[11] - Subfield Code Count
        baseAddressOfData = data.substring(12, 17).toInt()
        encodingLevel = EncodingLevel.fromValue(data[17])
        itemInformationInRecord =
            ItemInformationInRecord.fromValue(data[18])
//        data[19] - Undefined Position 19
//        data[20] - Length of Length of Field Portion
//        data[21] - Length of the starting Character Position Portion
//        data[22] - Length of the Implementation Defined Portion
//        data[23] - Undefined Position
    }
}

enum class RecordStatus(val value: Char) {
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INVALID('\u0000');

    companion object {
        private val map = RecordStatus.values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class TypeOfRecord(val value: Char) {
    UNKNOWN('u'),
    MULTIPART_ITEM_HOLDINGS('v'),
    SINGLE_PART_ITEM_HOLDINGS('x'),
    SERIAL_ITEM_HOLDINGS('y'),
    INVALID('\u0000');

    companion object {
        private val map = TypeOfRecord.values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class EncodingLevel(val value: Char) {
    HOLDINGS_LEVEL_1('1'),
    HOLDINGS_LEVEL_2('2'),
    HOLDINGS_LEVEL_3('3'),
    HOLDINGS_LEVEL_4('4'),
    HOLDINGS_LEVEL_4_WITH_PIECE_DESIGNATION('5'),
    MIXED_LEVEL('m'),
    UNKNOWN('u'),
    OTHER_LEVEL('z'),
    INVALID('\u0000');

    companion object {
        private val map = EncodingLevel.values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}

enum class ItemInformationInRecord(val value: Char) {
    ITEM_INFORMATION('i'),
    NO_ITEM_INFORMATION('n'),
    INVALID('\u0000');

    companion object {
        private val map = ItemInformationInRecord.values().associateBy { it.value }
        fun fromValue(value: Char) = map[value] ?: INVALID
    }
}