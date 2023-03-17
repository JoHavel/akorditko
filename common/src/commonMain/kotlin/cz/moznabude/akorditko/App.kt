package cz.moznabude.akorditko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.times

/**
 * Main class of GUI. (Currently only for Android and desktop.)
 */
@Composable
fun App() {
    var fingerings by remember { mutableStateOf(emptyList<List<Int>>()) }
    var parsed by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var tuning by remember { mutableStateOf(standardGuitarTuning) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(text, onValueChange = {
            text = it
            if (it.isNotEmpty()) {
                try {
                    val data = parseFull(it)
                    parsed = data.second
                    fingerings = FretEngine(tuning).getFrets(data.first)
                } catch (_: Exception) {

                }
            }
        })

        Spacer(Modifier.height(5.dp))

        Text(parsed, fontSize = 2.em)

        Spacer(Modifier.height(5.dp))

        LazyColumn {
            for (frets in fingerings) {
                item {
                    Fingering(frets, tuning.size, FingeringStyle.defaultFingeringSettings)
                }
            }
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
    val width = nOfStrings * style.stringWidth + (nOfStrings - 1) * style.spaceWidth

    Row {
        Column {
            Divider(Modifier.height(style.firstFretHeight).width(width), style.firstFretColor)
            for (j in 0 until style.nOfFrets) {
                Row {
                    for (i in 0 until nOfStrings) {
                        Divider(
                            Modifier.height(style.spaceHeight).width(style.stringWidth),
                            if (i >= emptyStringN) style.activeStringColor else style.emptyStringColor
                        )
                        if (i >= emptyStringN && frets[i - emptyStringN] - position == j) {
                            Box(
                                Modifier
                                    .offset(
                                        -style.dotRadius - (style.stringWidth / 2),
                                        style.spaceHeight / 2 - style.dotRadius
                                    )
                                    .width(2 * style.dotRadius)
                                    .height(2 * style.dotRadius)
                                    .background(style.dotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(style.spaceWidth - 2 * style.dotRadius))
                        } else {
                            Spacer(modifier = Modifier.width(style.spaceWidth))

                        }
                    }
                }
                Divider(Modifier.height(style.otherFretHeight).width(width), style.otherFretColor)
            }
        }
        if (position > 1) Text(position.toString())
    }
    Spacer(modifier = Modifier.height(style.interFingeringSpace))
}

/**
 * Style for [Fingering].
 */
data class FingeringStyle(
    val nOfFrets: Int = 4,
    val firstFretHeight: Dp = 2.dp,
    val firstFretColor: Color = Color.Black,
    val otherFretHeight: Dp = 1.dp,
    val otherFretColor: Color = Color.LightGray,

    val activeStringColor: Color = Color.Black,
    val emptyStringColor: Color = Color.LightGray,
    val stringWidth: Dp = 1.dp,

    val spaceWidth: Dp = 10.dp,
    val spaceHeight: Dp = 20.dp,

    val dotRadius: Dp = 2.dp,
    val dotColor: Color = Color.Black,

    val interFingeringSpace: Dp = 20.dp,
) {
    companion object {
        /**
         * Default [FingeringStyle].
         */
        val defaultFingeringSettings = FingeringStyle()
    }
}