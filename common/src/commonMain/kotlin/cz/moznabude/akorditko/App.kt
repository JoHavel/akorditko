package cz.moznabude.akorditko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import cz.moznabude.akorditko.theory.string2Key
import cz.moznabude.akorditko.theory.string2KeyWithH

/**
 * Main class of GUI. (Currently only for Android and desktop.)
 */
@Composable
fun App() {
    var fingerings by remember { mutableStateOf(emptyList<Fingering>()) }
    var parsed by remember { mutableStateOf("_") }
    var text by remember { mutableStateOf("") }
    var tuning by remember { mutableStateOf(standardGuitarTuning) }
    var tuningString by remember { mutableStateOf(tuning.joinToString(", ")) }
    var s2k by remember { mutableStateOf(string2Key) }
    var BIsH by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }

    fun parse() {
        if (text.isNotEmpty()) {
            try {
                val data = parseFull(text, s2k)
                parsed = data.second
                fingerings = FretEngine(tuning).getFingerings(data.first)
            } catch (_: Exception) {

            }
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(0.75F), horizontalArrangement = Arrangement.Start) {
                Text("Tunning: ", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 10.sp)
                if (custom) {
                    TextField(
                        tuningString,
                        onValueChange = { str ->
                            tuningString = str
                            try {
                                tuning = str.splitToSequence(",").map { it.trim().toInt() }.toList()
                                parse()
                            } catch (_: Exception) {

                            }
                        },
                        label = { Text("0 = Middle C;       ±1 = ±semi-tone", fontSize = 10.sp) },
                        textStyle = TextStyle(fontSize = 10.sp),
                        modifier = Modifier.fillMaxWidth(0.6F),
                    )
                    Spacer(Modifier.width(20.dp))
                    Button(
                        onClick = { custom = false },
                    ) { Text("Back", fontSize = 10.sp) }
                } else {
                    Button(
                        onClick = { tuning = standardGuitarTuning; parse() },
                    ) { Text("Guitar", fontSize = 10.sp) }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { tuning = standardUkuleleTuning; parse() },
                    ) { Text("Ukulele", fontSize = 10.sp) }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { tuningString = tuning.joinToString(", "); custom = true },
                    ) { Text("Other", fontSize = 10.sp) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("A## =")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("B")
                    RadioButton(
                        !BIsH,
                        onClick = { BIsH = false; s2k = string2Key; parse() },
                        modifier = Modifier.size(25.dp)
                    )
                }
                Text("/")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("H")
                    RadioButton(
                        BIsH,
                        onClick = { BIsH = true; s2k = string2KeyWithH; parse() },
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(text, label = { Text("Input a chord:") }, onValueChange = {
                text = it
                parse()
            })

            Spacer(Modifier.height(5.dp))

            Text("Showing:")
            Text(parsed, fontSize = 25.sp)

            Spacer(Modifier.height(5.dp))

            LazyColumn {
                for (fingering in fingerings) {
                    item {
                        ShowFingering(fingering, tuning.size, FingeringStyle.defaultFingeringSettings)
                    }
                }
            }
        }
    }
}

/**
 * Draw fingering chart from given [fingering] on instrument with [nOfStrings] strings. Styled with [style].
 */
@Composable
fun ShowFingering(fingering: Fingering, nOfStrings: Int, style: FingeringStyle) {
    val upper = fingering.max()
    val position = if (upper <= style.nOfFrets) 1 else fingering.minFret()

    val emptyStringN = nOfStrings - fingering.frets.size

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
                        if (i >= emptyStringN && fingering.frets[i - emptyStringN] - position == j) {
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
                    if (fingering.barre != null && fingering.barre.at == j + position) {
                        Divider(
                            modifier = Modifier
                                .height(style.dotRadius)
                                .width((nOfStrings - fingering.barre.from) * (style.spaceWidth + style.stringWidth) + style.spaceWidth)
                                .offset(
                                    -(nOfStrings - fingering.barre.from) * (style.spaceWidth + style.stringWidth) - 1.5 * style.spaceWidth,
                                    style.spaceHeight / 2 - style.dotRadius / 2
                                ),
                            color = style.dotColor
                        )
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
 * Style for [ShowFingering].
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