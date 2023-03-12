package cz.moznabude.akorditko

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Entrypoint of GUI of desktop app.
 */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Akordítko") {
        App()
    }
}
