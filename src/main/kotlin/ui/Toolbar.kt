package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.secondsun.map_editor_bsp_vibe.generated.resources.Res
import dev.secondsun.map_editor_bsp_vibe.generated.resources.ic_folder
import dev.secondsun.map_editor_bsp_vibe.generated.resources.ic_remove
import dev.secondsun.map_editor_bsp_vibe.generated.resources.ic_save
import model.DynamicTileType
import model.StaticTileType


/**
 * Toolbar component for the map editor
 */
@Composable
fun Toolbar(editorState: EditorState, onNewMap: () -> Unit, onSaveMap: () -> Unit, onLoadMap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .padding(8.dp)
            .background(Color.LightGray.copy(alpha = 0.2f))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        // Title
        Text(
            "Wolfenstein Map Editor",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // File operations
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Use a fixed button width to ensure consistency
            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            
            Button(
                onClick = onNewMap,
                modifier = buttonModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Map")
                    Spacer(Modifier.width(8.dp))
                    Text("New Map")
                }
            }
            
            Button(
                onClick = onSaveMap,
                modifier = buttonModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(painterResource(Res.drawable.ic_save), contentDescription = "Save Map")
                    Spacer(Modifier.width(8.dp))
                    Text("Save Map")
                }
            }
        
            Button(
                onClick = onLoadMap,
                modifier = buttonModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(painterResource(Res.drawable.ic_folder), contentDescription = "Load Map")
                    Spacer(Modifier.width(8.dp))
                    Text("Open Map")
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Edit mode selection
        Text(
            "Edit Mode",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = editorState.editMode == EditMode.STATIC,
                onClick = { editorState.editMode = EditMode.STATIC },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
            )
            Text(
                "Static (Walls, Floors)",
                modifier = Modifier
                    .clickable { editorState.editMode = EditMode.STATIC }
                    .align(Alignment.CenterVertically)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = editorState.editMode == EditMode.DYNAMIC,
                onClick = { editorState.editMode = EditMode.DYNAMIC },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
            )
            Text(
                "Dynamic (Entities)",
                modifier = Modifier
                    .clickable { editorState.editMode = EditMode.DYNAMIC }
                    .align(Alignment.CenterVertically)
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Tile type selection
        if (editorState.editMode == EditMode.STATIC) {
            StaticTileSelector(editorState)
        } else {
            DynamicTileSelector(editorState)
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Zoom controls
        Text(
            "Zoom: ${(editorState.zoomLevel * 100).toInt()}%",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { editorState.zoomOut() }) {
                Icon(painterResource(Res.drawable.ic_remove), contentDescription = "Zoom Out")
            }
            
            Button(onClick = { editorState.zoomIn() }) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
        }
        
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Play button
                    Text(
            "Preview",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Button(
            onClick = { editorState.showPreviewWindow = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play Preview")
                Spacer(Modifier.width(8.dp))
                Text("Play Preview")
            }
                    }
                    
        Spacer(modifier = Modifier.weight(1f))
        
        // Status
        Text(
            if (editorState.hasUnsavedChanges) "Unsaved Changes" else "No Changes",
            style = MaterialTheme.typography.caption,
            color = if (editorState.hasUnsavedChanges) Color.Red else Color.Gray
        )
    }
}

/**
 * Selector for static tile types
 */
@Composable
fun StaticTileSelector(editorState: EditorState) {
    Text(
        "Static Tile Type",
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    
    StaticTileType.values().forEach { tileType ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = editorState.selectedStaticTile == tileType,
                onClick = { editorState.selectedStaticTile = tileType },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
            )
            
            // Preview of the tile
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        when (tileType) {
                            StaticTileType.EMPTY -> Color.Black
                            StaticTileType.WALL -> Color.DarkGray
                            StaticTileType.FLOOR -> Color.LightGray
                        }
                    )
                    .border(1.dp, Color.Gray)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                StaticTileType.getDisplayName(tileType),
                modifier = Modifier.clickable { editorState.selectedStaticTile = tileType }
            )
        }
    }
}

/**
 * Selector for dynamic tile types
 */
@Composable
fun DynamicTileSelector(editorState: EditorState) {
    Text(
        "Dynamic Tile Type",
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    
    DynamicTileType.values().forEach { tileType ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = editorState.selectedDynamicTile == tileType,
                onClick = { editorState.selectedDynamicTile = tileType },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
            )
            
            // Preview of the dynamic element
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray)
                    .border(1.dp, Color.Gray)
            ) {
                when (tileType) {
                    DynamicTileType.PLAYER_START -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                                .background(Color.Green)
                        )
                    }
                    DynamicTileType.ENEMY -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                                .background(Color.Red)
                        )
                    }
                    DynamicTileType.PICKUP -> {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.Center)
                                .background(Color.Yellow)
                        )
                    }
                    DynamicTileType.DOOR -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp, 8.dp)
                                .align(Alignment.Center)
                                .background(Color(0xFF8B4513)) // Brown
                        )
                    }
                    DynamicTileType.SECRET_WALL -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                                .border(1.dp, Color.Cyan)
                        )
                    }
                    else -> { /* Empty */ }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                DynamicTileType.getDisplayName(tileType),
                modifier = Modifier.clickable { editorState.selectedDynamicTile = tileType }
            )
        }
    }
}
