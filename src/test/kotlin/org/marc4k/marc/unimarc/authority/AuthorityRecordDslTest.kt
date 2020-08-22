package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AuthorityRecordDslTest {
    @Test
    fun `test authority record DSL`() {
        val given = authorityRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.AUTHORITY
                typeOfEntity = TypeOfEntity.PERSONAL_NAME
                baseAddressOfData = 321
                encodingLevel = EncodingLevel.FULL_LEVEL
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
                    tag = "200"
                    indicator2 = '1'
                    subfields {
                        subfield {
                            name = 'a'
                            data = "Shakespeare,"
                        }
                        subfield {
                            name = 'b'
                            data = "William,"
                        }
                        subfield {
                            name = 'f'
                            data = "1564-1616"
                        }
                    }
                }
            }
        }

        assertAll(
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123nx  a2200321   450 ") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.getAuthorizedHeadingField().toString()).isEqualTo("200  1 ‡aShakespeare,‡bWilliam,‡f1564-1616") }
        )
    }
}