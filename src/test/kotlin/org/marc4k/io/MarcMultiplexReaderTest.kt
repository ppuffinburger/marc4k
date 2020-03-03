package org.marc4k.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.marc4k.io.converter.marc8.Marc8ToUnicode
import org.marc4k.io.codec.DefaultMarcDataDecoder
import java.io.FileInputStream

internal class MarcMultiplexReaderTest {
    @Test
    fun `test MarcMultiplexReader(List)`() {
        val readers = listOf(
            NewMarcStreamReader(FileInputStream("src/test/resources/records/MARC8_auth_record.mrc"), DefaultMarcDataDecoder(converter = Marc8ToUnicode())),
            NewMarcStreamReader(FileInputStream("src/test/resources/records/UTF8_auth_record.mrc"), DefaultMarcDataDecoder("UTF-8"))
        )

        MarcMultiplexReader(readers).use { reader ->
            var count = 0
            for ((index, record) in reader.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(record.leader.getData()).isEqualTo("06484cz   2201093n  4500")
                        count++
                    }
                    1 -> {
                        assertThat(record.leader.getData()).isEqualTo("06506cz  a2201093n  4500")
                        count++
                    }
                }
            }
            assertThat(count).isEqualTo(2)
        }
    }
}