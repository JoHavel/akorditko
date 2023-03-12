package cz.moznabude.akorditko

import org.jetbrains.compose.web.renderComposable

/**
 * Entrypoint of GUI of JS app.
 */
fun main() {
    renderComposable(rootElementId = "akorditko") {
        App()
    }
}

