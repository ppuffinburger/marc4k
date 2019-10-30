package org.marc4k.converter.marc8

import org.marc4k.IsoCode
import org.marc4k.MARC8_CODE_HEX_PATTERN
import java.util.*

internal class CodeDataTracker(data: CharArray, var g0: IsoCode = BASIC_LATIN_GRAPHIC_ISO_CODE, var g1: IsoCode = EXTENDED_LATIN_GRAPHIC_ISO_CODE) {
    private val stack = ArrayDeque(data.toList())
    private val undo = ArrayDeque<Char>(data.size)
    private val history = ArrayDeque<Char>(data.size)

    val offset: Int
        get() = history.size + undo.size

    fun isEACC() = g0 == CJK_GRAPHIC_ISO_CODE || g1 == CJK_GRAPHIC_ISO_CODE

    fun isEmpty() = stack.isEmpty()

    fun peek(): Char? = stack.peek()

    fun pop(): Char? {
        if (stack.isEmpty()) {
            return null
        }
        undo.push(stack.pop())
        return undo.peek()
    }

    fun undo() = undo.peek()?.let { stack.push(undo.pop()) }

    fun rollback() {
        while(canUndo()) {
            undo()
        }
    }

    fun commit() {
        for (character in undo.descendingIterator()) {
            history.push(character)
        }
        undo.clear()
    }

    fun getEnclosingData(): String {
        val characters = with(mutableListOf<Char>()) {
            undo.take(10 - size).forEach {
                add(0, it)
            }

            history.take(10 - size).forEach {
                add(0, it)
            }

            stack.take(20 - size).forEach {
                add(it)
            }

            toList()
        }

        return "Hex: (${characters.joinToString(" ") { String.format(MARC8_CODE_HEX_PATTERN, it.toInt()) }}) ASCII: (${characters.joinToString("") { if (isControlCharacter(it)) "?" else it.toString() }})"
    }

    fun getTrackerWithCurrentBuffer() = CodeDataTracker(stack.toCharArray(), g0, g1)

    private fun canUndo() = undo.isNotEmpty()

    private fun isControlCharacter(character: Char): Boolean {
        return when(character) {
            in C0_CONTROL_CHARACTER_RANGE -> true
            in C1_CONTROL_CHARACTER_RANGE -> true
            else -> false
        }
    }
}