package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.DynamicTileType
import model.MapData
import model.StaticTileType
import java.io.File

/**
 * Represents the current editing mode
 */
enum class EditMode {
    STATIC, // Editing walls, floors, etc.
    DYNAMIC // Editing player start, enemies, etc.
}

/**
 * Manages the state of the map editor
 */
class EditorState {
    // The current map being edited
    var mapData by mutableStateOf(MapData())

    // Current edit mode
    var editMode by mutableStateOf(EditMode.STATIC)

    // Currently selected tile types
    var selectedStaticTile by mutableStateOf(StaticTileType.WALL)
    var selectedDynamicTile by mutableStateOf(DynamicTileType.NONE)

    // Zoom level for the map view
    var zoomLevel by mutableStateOf(1.0f)
    var minZoom = 0.5f
    var maxZoom = 2.0f

    // Map file path
    var currentFilePath by mutableStateOf<String?>(null)

    // Flag to show if the map has unsaved changes
    var hasUnsavedChanges by mutableStateOf(false)
    
    // Flag to control preview window
    var showPreviewWindow by mutableStateOf(false)
    
    /**
     * Gets the window title including unsaved status
     */
    fun getWindowTitle(): String {
        val baseName = "Wolfenstein 3D Map Editor"
        val filePart = currentFilePath?.let { " - ${it.substringAfterLast('/')}" } ?: ""
        val unsavedPart = if (hasUnsavedChanges) " *" else ""
        
        return baseName + filePart + unsavedPart
    }

    // Place a tile at the specified position
    fun placeTile(x: Int, y: Int) {
        when (editMode) {
            EditMode.STATIC -> {
                mapData.setStaticTile(x, y, selectedStaticTile)
                hasUnsavedChanges = true
            }
            EditMode.DYNAMIC -> {
                // Ensure only one player start position
                if (selectedDynamicTile == DynamicTileType.PLAYER_START) {
                    mapData.clearPlayerStart()
                }
                mapData.setDynamicTile(x, y, selectedDynamicTile)
                hasUnsavedChanges = true
            }
        }
    }

    // Create a new map
    fun newMap() {
        mapData = MapData()
        currentFilePath = null
        hasUnsavedChanges = false
    }

    // Increase zoom level
    fun zoomIn() {
        zoomLevel = (zoomLevel + 0.1f).coerceAtMost(maxZoom)
    }

    // Decrease zoom level
    fun zoomOut() {
        zoomLevel = (zoomLevel - 0.1f).coerceAtLeast(minZoom)
    }


}

