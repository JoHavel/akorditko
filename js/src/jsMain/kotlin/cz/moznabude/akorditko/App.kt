package cz.moznabude.akorditko

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.CSSAutoKeyword
import org.jetbrains.compose.web.dom.*

/**
 * Main class of GUI.
 */
@Composable
fun App() {
    var fingerings by remember { mutableStateOf(emptyList<List<Int>>()) }
    var parsed by remember { mutableStateOf("") }
    var tuning by remember { mutableStateOf(standardGuitarTuning) }

    Div({ style { textAlign("center") } }) {
        Input(InputType.Text) {
            onInput {
                if (it.value.isNotEmpty()) {
                    try {
                        val data = parseFull(it.value)
                        parsed = data.second
                        fingerings = FretEngine(tuning).getFrets(data.first)
                    } catch (_: Exception) {
                    }
                }
            }
        }

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
    Div({ style { lineHeight(0.px); textAlign("left"); width(width + 3.em) } }) {
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