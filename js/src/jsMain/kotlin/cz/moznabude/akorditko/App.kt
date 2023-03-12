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

    Div({ style { textAlign("center") } }) {
        Input(InputType.Text) {
            onInput {
                if (it.value.isNotEmpty()) {
                    try {
                        val data = parseFull(it.value)
                        parsed = data.second
                        fingerings = FretEngine(standardGuitarTuning).getFrets(data.first)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        Div({ style { fontSize(2.em) } }) { Text(parsed) }
    }

    Div({ style { width(CSSKeywordValue("min-content").unsafeCast<CSSAutoKeyword>()); margin("auto".asDynamic()) } }) {
        for (frets in fingerings) {
            Fingering(frets)
        }
    }
}

/**
 * Draw fingering chart from given [frets].
 */
@Composable
fun Fingering(frets: List<Int>) {
    val upper = frets.max()
    val position = if (upper < 5) 1 else frets.filter { it != 0 }.min()

    val emptyStringN = 6 - frets.size

    // lineHeight=0 in this div removes vertical spaces, see https://stackoverflow.com/a/38523642
    Div({ style { lineHeight(0.px); textAlign("left"); width(56.px + 3.em) } }) {
        Span({ style { display(DisplayStyle.Block);height(2.px); width(56.px); background("black"); } }) {}
        for (j in 0..3) {
            for (i in 0..5) {
                Span({ style { display(DisplayStyle.InlineBlock);height(20.px); width(1.px); background(if (i >= emptyStringN) "black" else "lightgray"); } }) {}
                if (i >= emptyStringN && frets[i - emptyStringN] - position == j) {
                    Span({
                        style {
                            display(DisplayStyle.InlineBlock);height(4.px); width(4.px); marginLeft((-2.5).px); marginRight(
                            (-1.5).px
                        ); marginBottom(8.px); background("black"); borderRadius(2.px)
                        }
                    }) {}
                }
                Span({ style { display(DisplayStyle.InlineBlock);width(10.px) } }) {}
            }
            if (j == 0 && position > 1) Text(position.toString())
            Span({ style { display(DisplayStyle.Block);height(1.px); width(56.px); background("lightgray"); } }) {}
        }
        Span({ style { display(DisplayStyle.InlineBlock);height(20.px) } }) {}
    }
}