package org.marc4k.marc.marc21.classification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.marc21.CharacterCodingScheme
import java.time.LocalDateTime

internal class ClassificationRecordDslTest {
    @Test
    fun `test classification record DSL`() {
        val given = classificationRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.CLASSIFICATION_DATA
                characterCodingScheme = CharacterCodingScheme.UNICODE
                baseAddressOfData = 321
                encodingLevel = EncodingLevel.COMPLETE_CLASSIFICATION_RECORD
            }
            controlFields {
                controlField {
                    tag = "001"
                    data = "control_number"
                }
                controlField {
                    tag = "005"
                    data = "20200306123456.0"
                }
            }
            dataFields {
                dataField {
                    tag = "153"
                    subfields {
                        subfield {
                            name = 'a'
                            data = "600"
                        }
                        subfield {
                            name = 'j'
                            data = "Technology (Applied sciences)"
                        }
                    }
                }
            }
        }

        assertAll(
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123nw  a2200321n  4500") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.dataFields.first { it.tag == "153" }.toString()).isEqualTo("153    ‡a600‡jTechnology (Applied sciences)") }
        )
    }
}