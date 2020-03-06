package org.marc4k.marc.marc21.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.marc.marc21.CharacterCodingScheme
import java.time.LocalDateTime

internal class AuthorityRecordDslTest {
    @Test
    fun `test authority record DSL`() {
        val given = authorityRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.AUTHORITY_DATA
                characterCodingScheme = CharacterCodingScheme.UNICODE
                baseAddressOfData = 321
                encodingLevel = EncodingLevel.COMPLETE_AUTHORITY_RECORD
                punctuationPolicy = PunctuationPolicy.PUNCTUATION_OMITTED
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

        assertAll(
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123nz  a2200321nc 4500") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.getAuthorizedHeadingField().toString()).isEqualTo("100  1 ‡aShakespeare, William,‡d1564-1616") }
        )
    }
}