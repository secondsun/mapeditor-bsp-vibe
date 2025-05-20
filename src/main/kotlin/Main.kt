import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ui.*

@Composable
@Preview
fun App(window: ComposeWindow) {
    // Create and remember editor state
    val editorState = remember { EditorState() }
    
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal toolbar at the top
            HorizontalToolbar(
                editorState = editorState,
                onNewMap = {
                    // Check for unsaved changes before creating a new map
                    if (FileOperations.checkUnsavedChanges(window, editorState)) {
                        editorState.newMap()
                    }
                },
                onSaveMap = {
                    FileOperations.saveMap(window, editorState)
                },
                onLoadMap = {
                    // Check for unsaved changes before loading a map
                    if (FileOperations.checkUnsavedChanges(window, editorState)) {
                        FileOperations.loadMap(window, editorState)
                    }
                }
            )
            
            // Vertical layout with toolbar on left and map grid on right
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Vertical toolbar (tile selectors only)
                Toolbar(
                    editorState = editorState,
                    onNewMap = {
                        if (FileOperations.checkUnsavedChanges(window, editorState)) {
                            editorState.newMap()
                        }
                    },
                    onSaveMap = {
                        FileOperations.saveMap(window, editorState)
                    },
                    onLoadMap = {
                        if (FileOperations.checkUnsavedChanges(window, editorState)) {
                            FileOperations.loadMap(window, editorState)
                        }
                    }
                )
                
                // Map grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    MapGrid(
                        editorState = editorState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Show preview window if requested
        if (editorState.showPreviewWindow) {
            PlayPreview(
                editorState = editorState,
                onClose = { editorState.showPreviewWindow = false }
            )
        }
    }
}

fun main() = application {
    val editorState = remember { EditorState() }
    var _window: ComposeWindow? = null

    val windowState = rememberWindowState(
        size = DpSize(1200.dp, 800.dp),
        position = WindowPosition(Alignment.Center)
    )
    
    val title by remember(editorState.currentFilePath, editorState.hasUnsavedChanges) { 
        mutableStateOf(editorState.getWindowTitle())
    }
    
    Window(
        onCloseRequest = {
            // Check for unsaved changes before closing
            val window = _window
            when(window) {
                null -> exitApplication()
                else -> {if (FileOperations.checkUnsavedChanges(window, editorState)) {
                    exitApplication()
                }
                }
            }
        },
        title = title,
        state = windowState
    ) {
        _window=window

        App(window)
    }
}
