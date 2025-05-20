package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.DynamicTileType
import model.MapData
import model.StaticTileType

/**
 * Draws a single cell in the map grid
 */
@Composable
fun MapCell(
    x: Int,
    y: Int,
    size: Int,
    editorState: EditorState,
    onClick: () -> Unit
) {
    val tile = editorState.mapData.getTile(x, y)
    
    // Determine colors based on tile types
    val backgroundColor = when (tile.staticType) {
        StaticTileType.EMPTY -> Color.Black
        StaticTileType.WALL -> Color.DarkGray
        StaticTileType.FLOOR -> Color.LightGray
    }
    
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(backgroundColor)
            .border(0.5.dp, Color.Gray)
            .clickable { onClick() }
    ) {
        // Draw dynamic elements if they exist
        if (tile.dynamicType != DynamicTileType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val radius = this.size.width / 3
                
                when (tile.dynamicType) {
                    DynamicTileType.PLAYER_START -> {
                        // Green circle with P
                        drawCircle(Color.Green, radius, center)
                        drawCircle(Color.Black, radius, center, style = Stroke(2f))
                    }
                    DynamicTileType.ENEMY -> {
                        // Red circle with E
                        drawCircle(Color.Red, radius, center)
                        drawCircle(Color.Black, radius, center, style = Stroke(2f))
                    }
                    DynamicTileType.PICKUP -> {
                        // Yellow circle
                        drawCircle(Color.Yellow, radius / 1.5f, center)
                    }
                    DynamicTileType.DOOR -> {
                        // Brown rectangle
                        drawRect(
                            Color(0xFF8B4513), // Brown
                            Offset(center.x - radius, center.y - radius / 2),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius)
                        )
                    }
                    DynamicTileType.SECRET_WALL -> {
                        // Dotted outline
                        drawCircle(Color.Cyan, radius, center, style = Stroke(2f))
                    }
                    else -> { /* Nothing to draw */ }
                }
            }
            
            // Add text indicator for the dynamic element type
            when (tile.dynamicType) {
                DynamicTileType.PLAYER_START -> {
                    Text(
                        "P",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                DynamicTileType.ENEMY -> {
                    Text(
                        "E",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                else -> { /* No text indicator */ }
            }
        }
    }
}

/**
 * Displays the 64x64 map grid
 */
@Composable
fun MapGrid(editorState: EditorState, modifier: Modifier = Modifier) {
    // Calculate cell size based on zoom level
    val baseCellSize = 12 // Base size in dp
    val cellSize = (baseCellSize * editorState.zoomLevel).toInt()
    
    // Observe the map version to trigger recomposition
    val mapVersion = editorState.mapData.version
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(editorState.mapData.width),
        modifier = modifier
            .padding(8.dp)
            .border(1.dp, Color.Gray)
    ) {
        items(editorState.mapData.width * editorState.mapData.height) { index ->
            val x = index % editorState.mapData.width
            val y = index / editorState.mapData.width
            
            MapCell(
                x = x,
                y = y,
                size = cellSize,
                editorState = editorState,
                onClick = { editorState.placeTile(x, y) }
            )
        }
    }
}
