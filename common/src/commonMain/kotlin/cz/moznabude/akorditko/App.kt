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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
fun App() {
    var fingerings by remember { mutableStateOf(emptyList<List<Int>>()) }
    var parsed by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(text, onValueChange = {
            text = it
            if (it.isNotEmpty()) {
                try {
                    val data = parseFull(it)
                    parsed = data.second
                    fingerings = FretEngine(standardGuitarTuning).getFrets(data.first)
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
                    Fingering(frets)
                }
            }
        }
    }
}

@Composable
fun Fingering(frets: List<Int>) {
    val upper = frets.max()
    val position = if (upper < 5) 1 else frets.filter { it != 0 }.min()

    val emptyStringN = 6 - frets.size

    Row {
        Column {
            Divider(Modifier.height(2.dp).width(56.dp), Color.Black)
            for (j in 0..3) {
                Row {
                    for (i in 0..5) {
                        Divider(
                            Modifier.height(20.dp).width(1.dp),
                            if (i >= emptyStringN) Color.Black else Color.LightGray
                        )
                        if (i >= emptyStringN && frets[i - emptyStringN] - position == j) {
                            Box(
                                Modifier
                                    .offset((-2.5).dp, 8.dp)
                                    .width(4.dp)
                                    .height(4.dp)
                                    .background(Color.Black, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Spacer(modifier = Modifier.width(10.dp))

                        }
                    }
                }
                Divider(Modifier.height(1.dp).width(56.dp), Color.LightGray)
            }
        }
        if (position > 1) Text(position.toString())
    }
    Spacer(modifier = Modifier.height(20.dp))
}