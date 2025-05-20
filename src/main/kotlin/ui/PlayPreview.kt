package ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import game.GameEngine
import game.RaycastRenderer
import kotlinx.coroutines.delay

/**
 * Opens a new window with a playable 3D preview of the current map
 */
@Composable
fun PlayPreview(
    editorState: EditorState,
    onClose: () -> Unit
) {
    // Create a game engine with the current map
    val gameEngine = remember { GameEngine(editorState.mapData) }
    
    // Set up the preview window
    Window(
        onCloseRequest = onClose,
        title = "Wolfenstein 3D Preview - WASD/Arrows to Move, Space to Interact",
        state = rememberWindowState(
            size = DpSize(800.dp, 600.dp),
            position = WindowPosition(Alignment.Center)
        ),
        onKeyEvent = {
            // This helps capture key events at the window level
            // and allows them to propagate to children
            false
        }
    ) {
        MaterialTheme {

            
            // Root Box that handles keyboard input
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Fill the window with the raycasting renderer
                RaycastRenderer(
                    gameEngine = gameEngine,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
