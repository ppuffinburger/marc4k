package org.marc4k.marc

import kotlin.properties.Delegates

class MarcLeader : Leader {
    var recordLength by Delegates.observable(_recordLength) {
            _, _, newValue -> _recordLength = newValue
    }
    var recordStatus by Delegates.observable(_recordStatus) {
            _, _, newValue -> _recordStatus = newValue
    }
    var typeOfRecord by Delegates.observable(_typeOfRecord) {
            _, _, newValue -> _typeOfRecord = newValue
    }
    var implementationDefined1 by Delegates.observable(_implementationDefined1) {
            _, _, newValue -> _implementationDefined1 = newValue
    }
    var characterCodingScheme by Delegates.observable(_characterCodingScheme) {
            _, _, newValue -> _characterCodingScheme = newValue
    }
    var indicatorCount by Delegates.observable(_indicatorCount) {
            _, _, newValue -> _indicatorCount = newValue
    }
    var subfieldCodeCount by Delegates.observable(_subfieldCodeCount) {
            _, _, newValue -> _subfieldCodeCount = newValue
    }
    var baseAddressOfData by Delegates.observable(_baseAddressOfData) {
            _, _, newValue -> _baseAddressOfData = newValue
    }
    var implementationDefined2 by Delegates.observable(_implementationDefined2) {
            _, _, newValue -> _implementationDefined2 = newValue
    }
    var entryMap by Delegates.observable(_entryMap) {
            _, _, newValue -> _entryMap = newValue
    }

    constructor()

    constructor(data: String) : this() {
        setData(data)
    }

    constructor(leader: Leader) : this(leader.getData())

    override fun setData(data: String) {
        require(data.length == 24) { "Leader is not the correct length." }

        recordLength = data.substring(0..4).toIntOrNull() ?: 0
        recordStatus = data[5]
        typeOfRecord = data[6]
        implementationDefined1 = data.substring(7..8).toCharArray()
        characterCodingScheme = data[9]
        indicatorCount = if (data[10].isDigit()) data[10].toString().toInt() else 2
        subfieldCodeCount = if (data[11].isDigit()) data[11].toString().toInt() else 2
        baseAddressOfData = data.substring(12..16).toIntOrNull() ?: 0
        implementationDefined2 = data.substring(17..19).toCharArray()
        entryMap = data.substring(20..23).toCharArray()
    }
}