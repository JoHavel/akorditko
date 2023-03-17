package cz.moznabude.akorditko

import androidx.compose.runtime.*
import cz.moznabude.akorditko.theory.string2Key
import cz.moznabude.akorditko.theory.string2KeyWithH
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.CSSAutoKeyword
import org.jetbrains.compose.web.dom.*

/**
 * Main class of GUI.
 */
@Composable
fun App() {
    var fingerings by remember { mutableStateOf(emptyList<List<Int>>()) }
    var parsed by remember { mutableStateOf("_") }
    var text by remember { mutableStateOf("") }
    var tuning by remember { mutableStateOf(standardGuitarTuning) }
    var tuningString by remember { mutableStateOf(tuning.joinToString(", ")) }
    var s2k by remember { mutableStateOf(string2Key) }
    var custom by remember { mutableStateOf(false) }

    fun parse() {
        if (text.isNotEmpty()) {
            try {
                val data = parseFull(text, s2k)
                parsed = data.second
                fingerings = FretEngine(tuning).getFrets(data.first)
            } catch (_: Exception) {

            }
        }
    }

    Div({ id("akorditkoSettings") }) {
        Div({ attr("style", "float:left") }) {
            Text("Tunning: ")
            if (custom) {
                Input(InputType.Text) {
                    title("0 = Middle C;    ±1 = ±semi-tone")
                    value(tuningString)
                    onInput { str ->
                        tuningString = str.value
                        try {
                            tuning = str.value.splitToSequence(",").map { it.trim().toInt() }.toList()
                            parse()
                        } catch (_: Exception) {

                        }
                    }
                }
                Span({ style { width(0.5.em); display(DisplayStyle.InlineBlock) } })
                Button({ onClick { custom = false } }) { Text("Back") }
            } else {
                Button({ onClick { tuning = standardGuitarTuning; parse() } }) { Text("Guitar") }
                Span({ style { width(0.5.em); display(DisplayStyle.InlineBlock) } })
                Button({ onClick { tuning = standardUkuleleTuning; parse() } }) { Text("Ukulele") }
                Span({ style { width(0.5.em); display(DisplayStyle.InlineBlock) } })
                Button({ onClick { tuningString = tuning.joinToString(", "); custom = true } }) { Text("Other") }
            }
        }

        Div({ attr("style", "float:right") }) {
            Text("A## = ")
            Select({ onChange { s2k = if (it.value == "B") string2Key else string2KeyWithH; parse() } }) {
                Option("B") { Text("B") }
                Option("H") { Text("H") }
            }
        }
    }

    // Ends floating
    Div({ attr("style", "clear:both") }) {}

    Div({ style { textAlign("center") } }) {
        Input(InputType.Text) {
            placeholder("Input a chord")
            onInput {
                text = it.value
                parse()
            }
        }

        Div({ style { marginTop(4.px) } }) { Text("Showing:") }

        Div({ style { fontSize(2.em) } }) { Text(parsed) }
    }

    Div({ style { width(CSSKeywordValue("min-content").unsafeCast<CSSAutoKeyword>()); margin("auto".asDynamic()) } }) {
        for (frets in fingerings) {
            Fingering(frets, tuning.size, FingeringStyle.defaultFingeringSettings)
        }
    }
}

/**
 * Draw fingering chart from given [frets] on instrument with [nOfStrings] strings. Styled with [style].
 */
@Composable
fun Fingering(frets: List<Int>, nOfStrings: Int, style: FingeringStyle) {
    val upper = frets.max()
    val position = if (upper <= style.nOfFrets) 1 else frets.filter { it != 0 }.min()

    val emptyStringN = nOfStrings - frets.size

    // Width of
    val width = nOfStrings * (style.stringWidth + style.spaceWidth) - style.spaceWidth

    // lineHeight=0 in this div removes vertical spaces, see https://stackoverflow.com/a/38523642
    // whitespace=nowrap in this div prevents text breaking, see https://stackoverflow.com/a/2359459, which
    //         shifts number to the right (otherwise it goes on new line)
    Div({ style { lineHeight(0.px); textAlign("left"); width(width); whiteSpace("nowrap") } }) {
        Span({ style { display(DisplayStyle.Block);height(style.firstFretHeight); width(width); background(style.firstFretColor); } }) {}
        for (j in 0 until style.nOfFrets) {
            for (i in 0 until nOfStrings) {
                Span({ style { display(DisplayStyle.InlineBlock);height(style.spaceHeight); width(style.stringWidth); background(if (i >= emptyStringN) style.activeStringColor else style.emptyStringColor); } }) {}
                if (i >= emptyStringN && frets[i - emptyStringN] - position == j) {
                    Span({
                        style {
                            display(DisplayStyle.InlineBlock)
                            height(2 * style.dotRadius)
                            width(2 * style.dotRadius)
                            marginLeft((-1 * style.stringWidth) / 2 - style.dotRadius)
                            marginRight((1 * style.stringWidth) / 2 - style.dotRadius)
                            marginBottom(style.spaceHeight / 2 - style.dotRadius)
                            background(style.dotColor)
                            borderRadius(style.dotRadius)
                        }
                    }) {}
                }
                Span({ style { display(DisplayStyle.InlineBlock);width(style.spaceWidth) } }) {}
            }
            if (j == 0 && position > 1) Text(position.toString())
            Span({ style { display(DisplayStyle.Block);height(style.otherFretHeight); width(width); background(style.otherFretColor); } }) {}
        }
        Span({ style { display(DisplayStyle.InlineBlock);height(style.interFingeringSpace) } }) {}
    }
}

/**
 * Style for [Fingering].
 */
data class FingeringStyle(
    val nOfFrets: Int = 4,
    val firstFretHeight: CSSSizeValue<*> = 2.px,
    val firstFretColor: String = "black",
    val otherFretHeight: CSSSizeValue<*> = 1.px,
    val otherFretColor: String = "lightgray",

    val activeStringColor: String = "black",
    val emptyStringColor: String = "lightgray",
    val stringWidth: CSSNumeric = 1.px,

    val spaceWidth: CSSNumeric = 10.px,
    val spaceHeight: CSSNumeric = 20.px,

    val dotRadius: CSSNumeric = 2.px,
    val dotColor: String = "black",

    val interFingeringSpace: CSSNumeric = 20.px,
) {
    companion object {
        /**
         * Default [FingeringStyle].
         */
        val defaultFingeringSettings = FingeringStyle()
    }
}