package org.marc4k.converter.marc8

import org.marc4k.IsoCode
import org.marc4k.MARC8_CODE_HEX_PATTERN
import java.util.*

internal class Marc8Tracker(data: CharArray, var g0: IsoCode = BASIC_LATIN_GRAPHIC_ISO_CODE, var g1: IsoCode = EXTENDED_LATIN_GRAPHIC_ISO_CODE) {
    private val buffer = ArrayDeque(data.toList())
    private val rollback = ArrayDeque<Char>()
    private val committed = ArrayDeque<Char>()

    val offset: Int
        get() = committed.size + rollback.size

    fun isEACC() = g0 == 0x31 || g1 == 0x31

    fun isEmpty() = buffer.isEmpty()

    fun peek(): Char? = buffer.peek()

    fun pop(): Char? {
        if (buffer.isEmpty()) {
            return null
        }
        rollback.push(buffer.pop())
        return rollback.peek()
    }

    fun undo() = rollback.peek()?.let { buffer.push(rollback.pop()) }

    fun rollback() {
        while(rollback.isNotEmpty()) {
            buffer.push(rollback.pop())
        }
    }

    fun commit() {
        for (character in rollback.descendingIterator()) {
            committed.push(character)
        }
        rollback.clear()
    }

    fun getEnclosingData(): String {
        val characters = with(mutableListOf<Char>()) {
            rollback.take(10 - size).forEach {
                add(0, it)
            }

            committed.take(10 - size).forEach {
                add(0, it)
            }

            buffer.take(20 - size).forEach {
                add(it)
            }

            toList()
        }

        return "Hex: (${characters.joinToString(" ") { String.format(MARC8_CODE_HEX_PATTERN, it.toInt()) }}) ASCII: (${characters.joinToString("") { if (isControlCharacter(it)) "?" else it.toString() }})"
    }

    private fun isControlCharacter(character: Char): Boolean {
        return when(character) {
            in C0_CONTROL_CHARACTER_RANGE -> true
            in C1_CONTROL_CHARACTER_RANGE -> true
            else -> false
        }
    }
}