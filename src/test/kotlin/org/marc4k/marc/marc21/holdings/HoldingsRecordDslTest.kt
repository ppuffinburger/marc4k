package org.marc4k.marc.marc21.holdings

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.marc21.CharacterCodingScheme
import java.time.LocalDateTime

internal class HoldingsRecordDslTest {
    @Test
    fun `test holdings record DSL`() {
        val given = holdingsRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.SERIAL_ITEM_HOLDINGS
                characterCodingScheme = CharacterCodingScheme.UNICODE
                baseAddressOfData = 321
                encodingLevel = EncodingLevel.HOLDINGS_LEVEL_1
                itemInformationInRecord = ItemInformationInRecord.NO_ITEM_INFORMATION
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
                    tag = "852"
                    indicator1 = '0'
                    indicator2 = '1'
                    subfields {
                        subfield {
                            name = 'a'
                            data = "MnRM"
                        }
                        subfield {
                            name = 'h'
                            data = "QH511"
                        }
                        subfield {
                            name = 'i'
                            data = ".A1J68"
                        }
                    }
                }
            }
        }

        assertAll(
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123ny  a22003211n 4500") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.dataFields.first { it.tag == "852" }.toString()).isEqualTo("852 01 ‡aMnRM‡hQH511‡i.A1J68") }
        )
    }
}