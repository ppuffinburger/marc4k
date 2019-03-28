package org.marc4k.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class Marc8TrackerTest {
    @Test
    fun `test constructor(CharArray)`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertAll(
            { assertThat(tracker.g0).isEqualTo(0x42) },
            { assertThat(tracker.g1).isEqualTo(0x45) }
        )
    }

    @Test
    fun `test constructor(CharArray, IsoCode, IsoCode)`() {
        val tracker = Marc8Tracker("data".toCharArray(), 0x4E, 0x51)
        assertAll(
            { assertThat(tracker.g0).isEqualTo(0x4E) },
            { assertThat(tracker.g1).isEqualTo(0x51) }
        )
    }

    @Test
    fun `test isEACC()`() {
        assertThat(Marc8Tracker("data".toCharArray()).isEACC()).isFalse()
        assertThat(Marc8Tracker("data".toCharArray(), g0 = 0x31).isEACC()).isTrue()
        assertThat(Marc8Tracker("data".toCharArray(), g1 = 0x31).isEACC()).isTrue()
    }

    @Test
    fun `test isEmpty()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.isEmpty()).isFalse()
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.isEmpty()).isFalse()
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.isEmpty()).isFalse()
        assertThat(tracker.pop()).isEqualTo('t')
        assertThat(tracker.isEmpty()).isFalse()
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.isEmpty()).isTrue()
    }

    @Test
    fun `test peek()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.peek()).isEqualTo('d')
        assertThat(tracker.peek()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.peek()).isEqualTo('a')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isEqualTo('t')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isNull()
        assertThat(tracker.peek()).isNull()
    }

    @Test
    fun `test pop()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isEqualTo('t')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isNull()
    }

    @Test
    fun `test undo()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.peek()).isEqualTo('a')
        tracker.undo()
        assertThat(tracker.pop()).isEqualTo('d')
    }

    @Test
    fun `test rollback()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        tracker.rollback()
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('a')
        tracker.rollback()
        assertThat(tracker.pop()).isEqualTo('d')
    }

    @Test
    fun `test commit()`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('a')
        tracker.commit()
        tracker.rollback()
        assertThat(tracker.pop()).isEqualTo('t')
    }

    @Test
    fun `test offset property`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isEqualTo('t')
        assertThat(tracker.offset).isEqualTo(3)
        tracker.undo()
        assertThat(tracker.offset).isEqualTo(2)
        tracker.rollback()
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.pop()).isEqualTo('d')
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.pop()).isEqualTo('t')
        assertThat(tracker.offset).isEqualTo(3)
        tracker.commit()
        assertThat(tracker.offset).isEqualTo(3)
        assertThat(tracker.pop()).isEqualTo('a')
        assertThat(tracker.offset).isEqualTo(4)
    }

    @Test
    fun `test getEnclosingData()`() {
        val data = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val tracker = Marc8Tracker(data)
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("abcdefghijklmnopqrst") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x61 0x62 0x63 0x64 0x65 0x66 0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74") }
        )
        for (index in 1..16) {
            tracker.pop()
        }
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("ghijklmnopqrstuvwxyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74 0x75 0x76 0x77 0x78 0x79 0x7a") }
        )
        tracker.commit()
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("ghijklmnopqrstuvwxyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74 0x75 0x76 0x77 0x78 0x79 0x7a") }
        )
        for (index in 1..10) {
            tracker.pop()
        }
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("qrstuvwxyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x71 0x72 0x73 0x74 0x75 0x76 0x77 0x78 0x79 0x7a") }
        )
        tracker.commit()
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("qrstuvwxyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x71 0x72 0x73 0x74 0x75 0x76 0x77 0x78 0x79 0x7a") }
        )
    }

    @Test
    fun `test getEnclosingData() with control characters`() {
        val data = "abc\u000Aefghijklmnopqrstuv\u008Axyz".toCharArray()
        val tracker = Marc8Tracker(data)
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("abc?efghijklmnopqrst") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x61 0x62 0x63 0x0a 0x65 0x66 0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74") }
        )
        for (index in 1..16) {
            tracker.pop()
        }
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("ghijklmnopqrstuv?xyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74 0x75 0x76 0x8a 0x78 0x79 0x7a") }
        )
        tracker.commit()
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("ghijklmnopqrstuv?xyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74 0x75 0x76 0x8a 0x78 0x79 0x7a") }
        )
        for (index in 1..10) {
            tracker.pop()
        }
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("qrstuv?xyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x71 0x72 0x73 0x74 0x75 0x76 0x8a 0x78 0x79 0x7a") }
        )
        tracker.commit()
        assertAll(
            { assertThat(getAscii(tracker.getEnclosingData())).isEqualTo("qrstuv?xyz") },
            { assertThat(getHex(tracker.getEnclosingData())).isEqualTo("0x71 0x72 0x73 0x74 0x75 0x76 0x8a 0x78 0x79 0x7a") }
        )
    }

    private fun getAscii(data: String): String {
        val asciiStart = data.indexOf("ASCII: (") + 8
        val asciiEnd = data.indexOf(')', asciiStart)
        return data.substring(asciiStart, asciiEnd)
    }

    private fun getHex(data: String): String {
        val hexStart = data.indexOf("HEX: (") + 7
        val hexEnd = data.indexOf(')', hexStart)
        return data.substring(hexStart, hexEnd)
    }
}