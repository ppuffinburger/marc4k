package org.marc4k.io.converter.marc8

import org.marc4k.IsoCode
import org.marc4k.MARC8_CODE_HEX_PATTERN
import java.util.*

/**
 * A class to track the operations on a [CharArray] by using a stack that supports undo, rollback, and commit operations.
 *
 * @param[data] the [CharArray] that is being operated on.
 * @property[g0] the current G0 as an [IsoCode].  Defaults to [BASIC_LATIN_GRAPHIC_ISO_CODE].
 * @property[g1] the current G1 as an [IsoCode].  Defaults to [EXTENDED_LATIN_GRAPHIC_ISO_CODE].
 */
internal class CodeDataTracker(data: CharArray, var g0: IsoCode = BASIC_LATIN_GRAPHIC_ISO_CODE, var g1: IsoCode = EXTENDED_LATIN_GRAPHIC_ISO_CODE) {
    private val stack = ArrayDeque(data.toList())
    private val undo = ArrayDeque<Char>(data.size)
    private val history = ArrayDeque<Char>(data.size)

    /**
     * Gets the current offset into the underlying stack.
     */
    val offset: Int
        get() = history.size + undo.size

    /**
     * Returns true if either G0 or G1 is [CJK_GRAPHIC_ISO_CODE].
     */
    fun isEACC() = g0 == CJK_GRAPHIC_ISO_CODE || g1 == CJK_GRAPHIC_ISO_CODE

    /**
     * Returns true if the stack is empty.
     */
    fun isEmpty() = stack.isEmpty()

    /**
     * Retrieves, but does not remove, the character at the head of the stack or null if there is none.
     */
    fun peek(): Char? = stack.peek()

    /**
     * Returns the character at the head of the stack or null if the is none.
     */
    fun pop(): Char? {
        if (stack.isEmpty()) {
            return null
        }
        undo.push(stack.pop())
        return undo.peek()
    }

    /**
     * Undoes the last operation on the underlying stack.
     */
    fun undo() = undo.peek()?.let { stack.push(undo.pop()) }

    /**
     * Undoes all the operations on the underlying stack that occurred after the last commit.
     */
    fun rollback() {
        while(canUndo()) {
            undo()
        }
    }

    /**
     * Commits all current operations on the underlying stack.
     */
    fun commit() {
        for (character in undo.descendingIterator()) {
            history.push(character)
        }
        undo.clear()
    }

    /**
     * Returns a representation of the data surrounding the current position in the stack in Hex and ASCII form.
     */
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

        return "Hex: (${characters.joinToString(" ") { String.format(MARC8_CODE_HEX_PATTERN, it.code) }}) ASCII: (${characters.joinToString("") { if (isControlCharacter(it)) "?" else it.toString() }})"
    }

    /**
     * Returns a new [CodeDataTracker] with the current data in the stack.
     */
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