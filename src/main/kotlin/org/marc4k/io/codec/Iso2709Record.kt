package org.marc4k.io.codec


data class Iso2709Record(val leader: String, val controlFields: MutableList<Iso2709ControlField> = mutableListOf(), val dataFields: MutableList<Iso2709DataField> = mutableListOf())

@Suppress("ArrayInDataClass")
data class Iso2709ControlField(val tag: String, val data: ByteArray)

data class Iso2709DataField(val tag: String, val indicator1: Char, val indicator2: Char, val subfields: MutableList<Iso2709Subfield> = mutableListOf())

@Suppress("ArrayInDataClass")
data class Iso2709Subfield(val name: Char, val data: ByteArray)
