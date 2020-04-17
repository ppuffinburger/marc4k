package org.marc4k.marc

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MarcRecordDslTest {
    @Test
    fun `test marc record DSL`() {
        val given = marcRecord {
            leader {
                recordLength = 123
                recordStatus = 'n'
                typeOfRecord = 'a'
                implementationDefined1[0] = 'm'
                implementationDefined1[2] = 'a'
                baseAddressOfData = 321
                implementationDefined2[0] = '7'
                implementationDefined2[1] = 'a'
                entryMap[0] = '4'
                entryMap[1] = '5'
                entryMap[2] = '0'
                entryMap[3] = '0'

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
                    tag = "100"
                    indicator2 = '1'
                    subfields {
                        subfield {
                            name = 'a'
                            data = "Shakespeare, William,"
                        }
                        subfield {
                            name = 'd'
                            data = "1564-1616"
                        }
                    }
                }
            }
        }

        SoftAssertions().apply {
            assertThat(given.leader.toString()).isEqualTo("LEADER 00123nam a22003217a 4500")
            assertThat(given.getControlNumber()).isEqualTo("control_number")
            assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56))
            assertThat(given.dataFields.first { it.tag == "100" }.toString()).isEqualTo("100  1 ‡aShakespeare, William,‡d1564-1616")
        }.assertAll()
    }
}