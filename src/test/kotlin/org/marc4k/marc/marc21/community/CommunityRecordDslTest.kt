package org.marc4k.marc.marc21.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.marc21.CharacterCodingScheme
import java.time.LocalDateTime

internal class CommunityRecordDslTest {
    @Test
    fun `test community record DSL`() {
        val given = communityRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.COMMUNITY_INFORMATION
                kindOfData = KindOfData.ORGANIZATION
                characterCodingScheme = CharacterCodingScheme.UNICODE
                baseAddressOfData = 321
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
                    tag = "110"
                    indicator1 = '2'
                    subfields {
                        subfield {
                            name = 'a'
                            data = "Haven House."
                        }
                    }
                }
            }
        }

        assertAll(
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123nqo a2200321   4500") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.dataFields.first { it.tag == "110" }.toString()).isEqualTo("110 2  ‡aHaven House.") }
        )
    }
}