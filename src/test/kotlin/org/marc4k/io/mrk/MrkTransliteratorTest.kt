package org.marc4k.io.mrk

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.marc4k.converter.CharacterConverterResult
import org.marc4k.converter.marc8.Marc8ToUnicode
import org.marc4k.converter.marc8.UnicodeToMarc8

internal class MrkTransliteratorTest {
    private val marc8ToUnicode = Marc8ToUnicode()
    private val unicodeToMarc8 = UnicodeToMarc8()

    private val testMrkData = listOf(
        Triple("the uppercase Polish L", " in {Lstrok}{acute}od{acute}z", " in Łódź"),
        Triple("the uppercase Scandinavia O", " in {Ostrok}st", " in Øst"),
        Triple("the uppercase D with crossbar", " in {Dstrok}uro", " in Đuro"),
        Triple("the uppercase Icelandic thorn", " in {THORN}ann", " in Þann"),
        Triple("the uppercase digraph AE", " in {AElig}gir", " in Ægir"),
        Triple("the uppercase digraph OE", " in {OElig}uvres", " in Œuvres"),
        Triple("the soft sign", " in rech{softsign}", " in rechʹ"),
        Triple("the middle dot", " in col{middot}lecci{acute}o", " in col·lecció"),
        Triple("the musical flat", " in F{flat}", " in F♭"),
        Triple("the patent mark", " in Frizbee{reg}", " in Frizbee®"),
        Triple("the plus or minus sign", " in {plusmn}54%", " in ±54%"),
        Triple("the uppercase O-hook", " in B{Ohorn}", " in BƠ"),
        Triple("the uppercase U-hook", " in X{Uhorn}A", " in XƯA"),
        Triple("the alif", " in mas{mlrhring}alah", " in masʼalah"),
        Triple("the ayn", " in {mllhring}arab", " in ʻarab"),
        Triple("the lowercase Polish l", " in W{lstrok}oc{lstrok}aw", " in Włocław"),
        Triple("the lowercase Scandinavian o", " in K{ostrok}benhavn", " in København"),
        Triple("the lowercase d with crossbar", " in {dstrok}avola", " in đavola"),
        Triple("the lowercase Icelandic thorn", " in {thorn}ann", " in þann"),
        Triple("the lowercase digraph ae", " in v{aelig}re", " in være"),
        Triple("the lowercase digraph oe", " in c{oelig}ur", " in cœur"),
        Triple("the lowercase hardsign", " in s{hardsign}ezd", " in sʺezd"),
        Triple("the Turkish dotless i", " in masal{inodot}", " in masalı"),
        Triple("the British pound sign", " in {pound}5.95", " in £5.95"),
        Triple("the lowercase eth", " in ver{eth}ur", " in verður"),
        Triple("the lowercase o-hook (with pseudo question mark)", " in S{hooka}{ohorn}", " in Sở"),
        Triple("the lowercase u-hook", " in T{uhorn} D{uhorn}c", " in Tư Dưc"),
        Triple("the pseudo question mark", " in c{hooka}ui", " in củi"),
        Triple("the grave accent", " in tr{grave}es", " in très"),
        Triple("the acute accent", " in d{acute}esir{acute}ee", " in désirée"),
        Triple("the circumflex", " in c{circ}ote", " in côte"),
        Triple("the tilde", " in ma{tilde}nana", " in mañana"),
        Triple("the macron", " in T{macr}okyo", " in Tōkyo"),
        Triple("the breve", " in russki{breve}i", " in russkiĭ"),
        Triple("the dot above", " in {dot}zaba", " in żaba"),
        Triple("the dieresis (umlaut)", " in L{uml}owenbr{uml}au", " in Löwenbräu"),
        Triple("the caron (hachek)", " in {caron}crny", " in črny"),
        Triple("the circle above (angstrom)", " in {ring}arbok", " in årbok"),
        Triple("the ligature first and second halves", " in d{llig}i{rlig}ad{llig}i{rlig}a", " in di͡adi͡a"),
        Triple("the high comma off center", " in rozdel{rcommaa}ovac", " in rozdelo̕vac"),
        Triple("the double acute", " in id{dblac}oszaki", " in időszaki"),
        Triple("the candrabindu (breve with dot above)", " in Ali{candra}iev", " in Alii̐ev"),
        Triple("the cedilla", " in {cedil}ca va comme {cedil}ca", " in ça va comme ça"),
        Triple("the right hook", " in viet{ogon}a", " in vietą"),
        Triple("the dot below", " in te{dotb}da", " in teḍa"),
        Triple("the double dot below", " in {under}k{under}hu{dbldotb}tbah", " in k̲h̲ut̤bah"),
        Triple("the circle below", " in Sa{dotb}msk{ringb}rta", " in Saṃskr̥ta"),
        Triple("the double underscore", " in {dblunder}Ghulam", " in G̳hulam"),
        Triple("the left hook", " in Lech Wa{lstrok}{commab}esa", " in Lech Wałe̦sa"),
        Triple("the right cedilla (comma below)", " in kh{rcedil}ong", " in kho̜ng"),
        Triple("the upadhmaniya (half circle below)", " in {breveb}humantu{caron}s", " in ḫumantuš"),
        Triple("the double tilde, first and second halves", " in {ldbltil}n{rdbltil}galan", " in n͠galan"),
        Triple("the high comma (centered)", " in g{commaa}eotermika", " in ge̓otermika"),
        Triple("the opening and closing curly brackets", " in {lcub}text{rcub}", " in {text}"),
        Triple("the degree sign", " in 98.6{deg}", " in 98.6°"),
        Triple("the small script l", " in 45{scriptl}", " in 45ℓ"),
        Triple("the phono copyright mark", " in {phono}1994", " in ℗1994"),
        Triple("the copyright mark", " in {copy}1955", " in ©1955"),
        Triple("the musical sharp in concerto", " in F{sharp} major", " in F♯ major"),
        Triple("the inverted question mark", " in {iquest}Que pas{acute}o?", " in ¿Que pasó?"),
        Triple("the inverted exclamation mark", " in {iexcl}Ay caramba!", " in ¡Ay caramba!"),
        Triple("the dollar sign", " in {dollar}24.99", " in $24.99"),
        Triple("an empty input", "", "")
    )

    @TestFactory
    fun `test fromMrk()`() = testMrkData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = marc8ToUnicode.convert(MrkTransliterator.fromMrk(data))
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }

    @TestFactory
    fun `test toMrk`() = testMrkData.map { (description, expected, data) ->
        DynamicTest.dynamicTest(description) {
            val givenConversionResult = unicodeToMarc8.convert(data)
            assertThat(givenConversionResult).isInstanceOf(CharacterConverterResult.Success::class.java)
            val given = MrkTransliterator.toMrk((givenConversionResult as CharacterConverterResult.Success).conversion, false)
            assertThat(given).isEqualTo(expected)
        }
    }

    private val testMrkMiscellaneousData = listOf(
        Triple("the spacing circumflex", " in 3{spcirc}5", " in 3^5"),
        Triple("the spacing underscore", " in file{spundscr}name", " in file_name"),
        Triple("the spacing grave", " in Philip{spgrave}s", " in Philip`s"),
        Triple("the spacing tilde", " in x {sptilde} y", " in x ~ y"),
        Triple("an unknown mnemonic", " in {zilch}", " in &zilch;")
    )

    @TestFactory
    fun `test fromMrk() with misc data`() = testMrkMiscellaneousData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = marc8ToUnicode.convert(MrkTransliterator.fromMrk(data))
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }
}