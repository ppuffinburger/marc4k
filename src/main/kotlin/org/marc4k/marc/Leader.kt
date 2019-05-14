package org.marc4k.marc

@Suppress("PropertyName")
abstract class Leader {
    protected var _recordLength = 0
    protected var _recordStatus = ' '
    protected var _typeOfRecord = ' '
    protected var _implementationDefined1 = charArrayOf(' ', ' ', ' ')
    protected var _indicatorCount = 0
    protected var _subfieldCodeCount = 0
    protected var _baseAddressOfData = 0
    protected var _implementationDefined2 = charArrayOf(' ', ' ', ' ')
    protected var _entryMap = charArrayOf(' ', ' ', ' ', ' ')

    abstract fun setData(data: String)

    fun getData(): String {
        with(StringBuilder()) {
            append(_recordLength.toString().padStart(5, '0'))
            append(_recordStatus)
            append(_typeOfRecord)
            append(_implementationDefined1)
            append(_indicatorCount)
            append(_subfieldCodeCount)
            append(_baseAddressOfData.toString().padStart(5, '0'))
            append(_implementationDefined2)
            append(_entryMap)
            return toString()
        }
    }

    override fun toString(): String {
        return "LEADER " + getData()
    }
}