package org.marc4k.converter

import org.junit.jupiter.api.Test
import org.marc4k.converter.marc8.Marc8Tracker
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class Marc8TrackerTest {
    @Test
    fun `test constructor(CharArray)`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test constructor(CharArray, IsoCode, IsoCode)`() {
        val tracker = Marc8Tracker("data".toCharArray(), 0x4E, 0x51)
        assertEquals(0x4E, tracker.g0)
        assertEquals(0x51, tracker.g1)
    }

    @Test
    fun `test isEACC()`() {
        assertFalse { Marc8Tracker("data".toCharArray()).isEACC() }
        assertTrue { Marc8Tracker("data".toCharArray(), g0 = 0x31).isEACC() }
        assertTrue { Marc8Tracker("data".toCharArray(), g1 = 0x31).isEACC() }
    }

    @Test
    fun `test isEmpty()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertFalse { tracker.isEmpty() }
        assertEquals('d', tracker.pop())
        assertFalse { tracker.isEmpty() }
        assertEquals('a', tracker.pop())
        assertFalse { tracker.isEmpty() }
        assertEquals('t', tracker.pop())
        assertFalse { tracker.isEmpty() }
        assertEquals('a', tracker.pop())
        assertTrue { tracker.isEmpty() }
    }

    @Test
    fun `test peek()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals('d', tracker.peek())
        assertEquals('d', tracker.peek())
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.peek())
        assertEquals('a', tracker.pop())
        assertEquals('t', tracker.pop())
        assertEquals('a', tracker.pop())
        assertNull(tracker.pop())
        assertNull(tracker.peek())
    }

    @Test
    fun `test pop()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.pop())
        assertEquals('t', tracker.pop())
        assertEquals('a', tracker.pop())
        assertNull(tracker.pop())
    }

    @Test
    fun `test undo()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.peek())
        tracker.undo()
        assertEquals('d', tracker.pop())
    }

    @Test
    fun `test rollback()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        tracker.rollback()
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.pop())
        tracker.rollback()
        assertEquals('d', tracker.pop())
    }

    @Test
    fun `test commit()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.pop())
        tracker.commit()
        tracker.rollback()
        assertEquals('t', tracker.pop())
    }

    @Test
    fun `test offset property`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertEquals(0, tracker.offset)
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.pop())
        assertEquals('t', tracker.pop())
        assertEquals(3, tracker.offset)
        tracker.undo()
        assertEquals(2, tracker.offset)
        tracker.rollback()
        assertEquals(0, tracker.offset)
        assertEquals('d', tracker.pop())
        assertEquals('a', tracker.pop())
        assertEquals('t', tracker.pop())
        assertEquals(3, tracker.offset)
        tracker.commit()
        assertEquals(3, tracker.offset)
        assertEquals('a', tracker.pop())
        assertEquals(4, tracker.offset)
    }
}