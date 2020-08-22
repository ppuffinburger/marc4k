package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

internal class BibliographicRecordDslTest {
    @Test
    fun `test bibliographic record DSL`() {
        val given = bibliographicRecord {
            leader {
                recordLength = 123
                recordStatus = RecordStatus.NEW
                typeOfRecord = TypeOfRecord.LANGUAGE_MATERIAL
                bibliographicLevel = BibliographicLevel.MONOGRAPH
                hierarchicalLevelCode = HierarchicalLevelCode.NO_HIERARCHICAL_RELATIONSHIP
                typeOfControl = TypeOfControl.NO_SPECIFIED_TYPE
                baseAddressOfData = 321
                encodingLevel =  EncodingLevel.FULL_LEVEL
                descriptiveCatalogingForm = DescriptiveCatalogingForm.ISBD_NOT_APPLICABLE
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
                    tag = "700"
                    indicator2 = '1'
                    subfields {
                        subfield {
                            name = 'a'
                            data = "Shakespeare"
                        }
                        subfield {
                            name = 'b'
                            data = "William"
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
            { assertThat(given.leader.toString()).isEqualTo("LEADER 00123nam0 2200321 x 450 ") },
            { assertThat(given.getControlNumber()).isEqualTo("control_number") },
            { assertThat(given.getDateOfLatestTransaction()).isEqualTo(LocalDateTime.of(2020, 3, 6, 12, 34, 56)) },
            { assertThat(given.getMainEntryField().toString()).isEqualTo("700  1 ‡aShakespeare‡bWilliam‡f1564-1616") }
        )
    }
}