package org.marc4k.marc

import org.marc4k.SUBFIELD_DELIMITER_CHARACTER

data class DataField(
    val tag: String,
    val indicator1: Char = ' ',
    val indicator2: Char = ' ',
    val subfields: MutableList<Subfield> = mutableListOf()
) {
    fun getData() = subfields.joinToString(separator = "") { "$SUBFIELD_DELIMITER_CHARACTER${it.name}${it.data}" }

    fun setData(data: String) {
        subfields.clear()

        subfields += data.split(SUBFIELD_DELIMITER_CHARACTER)
            .asSequence()
            .filter { it.isNotBlank() && it.length > 1 }
            .map { Subfield(it[0], it.substring(1)) }
    }

    override fun toString(): String = "$tag $indicator1$indicator2 ${getData().replace(
        SUBFIELD_DELIMITER_CHARACTER,
        DOUBLE_DAGGER
    )}"

    companion object {
        private const val DOUBLE_DAGGER = '\u2021'
    }
}