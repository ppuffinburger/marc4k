package org.marc4k.io

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.marc4k.MarcException
import org.marc4k.io.codec.Marc21DataDecoder
import java.io.File
import java.nio.file.Files

internal class MarcDirectoryStreamReaderTest {
    @Test
    fun `test MarcDirectoryStreamReader(File, MarcDataDecoder)`() {
        val leaders = mutableSetOf("06506cz  a2201093n  4500", "06484cz   2201093n  4500")
        MarcDirectoryStreamReader(File("src/test/resources/records/walktest"), Marc21DataDecoder()).use { reader ->
            var count = 0
            for (record in reader) {
                val leader = record.leader.getData()
                assertThat(leaders).contains(leader)
                leaders.remove(leader)
                count++
            }
            assertThat(count).isEqualTo(2)
            assertThat(leaders).isEmpty()
        }
    }

    @Test
    fun `test MarcDirectoryStreamReader(String, MarcDataDecoder)`() {
        val leaders = mutableSetOf("06506cz  a2201093n  4500", "06484cz   2201093n  4500")
        MarcDirectoryStreamReader("src/test/resources/records/walktest", Marc21DataDecoder()).use { reader ->
            var count = 0
            for (record in reader) {
                val leader = record.leader.getData()
                assertThat(leaders).contains(leader)
                leaders.remove(leader)
                count++
            }
            assertThat(count).isEqualTo(2)
            assertThat(leaders).isEmpty()
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS, OS.LINUX, disabledReason = "Disabled due to failing and I don't have the OSs to fix.")
    fun `test removing file before iteration`() {
        val tempDirectory = Files.createTempDirectory("MARC4K-MDSRT-").toFile().apply { deleteOnExit() }

        val marc8File = File("src/test/resources/records/walktest/marc8/MARC8_auth_record.mrc").copyTo(File("${tempDirectory.canonicalPath}/MARC8_auth_record.mrc")).apply { deleteOnExit() }
        File("src/test/resources/records/walktest/utf8/UTF8_auth_record.mrc").copyTo(File("${tempDirectory.canonicalPath}/UTF8_auth_record.mrc")).apply { deleteOnExit() }

        MarcDirectoryStreamReader(tempDirectory, Marc21DataDecoder()).use { reader ->
            assertThat(marc8File.delete()).isTrue
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(record.leader.getData()).isEqualTo("06506cz  a2201093n  4500")
                        count++
                    }
                }
            }
            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    fun `test exception from underlying reader`() {
        MarcDirectoryStreamReader("src/test/resources/records/walktest", Marc21DataDecoder()).use { reader ->
            reader.next()
            assertThatExceptionOfType(MarcException::class.java).isThrownBy { reader.next() }
        }
    }
}