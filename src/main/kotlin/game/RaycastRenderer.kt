package game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import model.DynamicTileType
import model.StaticTileType
import kotlin.math.*

/**
 * Renders a first-person raycasting view of the map
 */
@Composable
fun RaycastRenderer(
    gameEngine: GameEngine,
    modifier: Modifier = Modifier
) {
    // Use a mutable state object for pressedKeys that can be updated without triggering recomposition
    val pressedKeysState = remember { mutableStateOf(setOf<Key>()) }
    
    // Remember time of last frame for framerate calculation
    val lastFrameTime = remember { mutableStateOf(0L) }
    val frameRate = remember { mutableStateOf(0) }
    val frameCount = remember { mutableStateOf(0) }
    val frameTimeSum = remember { mutableStateOf(0L) }
    
    // Manage focus with FocusRequester
    val focusRequester = remember { FocusRequester() }
    
    // Process input and rendering on each animation frame
    val isActive = remember { mutableStateOf(true) }
    
    // Game state for triggering recomposition
    val renderTrigger = remember { mutableStateOf(0) }
    
    // Separate game loop for input processing
    LaunchedEffect(Unit) {
        while (isActive.value) {
            // Process input as frequently as possible
            gameEngine.processInput(pressedKeysState.value)
            delay(5) // Small delay to prevent CPU overload
        }
    }
    
    // Dedicated rendering loop that forces recomposition
    LaunchedEffect(Unit) {
        val targetFrameTime = 16_666_667L // Target ~60 FPS (in nanoseconds)
        
        while (isActive.value) {
            val startTime = System.nanoTime()
            
            // Force recomposition by updating renderTrigger
            renderTrigger.value = (renderTrigger.value + 1) % 1000
            
            // Calculate frame rate every second
            val currentTime = System.nanoTime()
            val frameDuration = currentTime - lastFrameTime.value
            lastFrameTime.value = currentTime
            
            frameCount.value++
            frameTimeSum.value += frameDuration
            
            if (frameTimeSum.value >= 1_000_000_000L) { // 1 second in nanoseconds
                frameRate.value = frameCount.value
                frameCount.value = 0
                frameTimeSum.value = 0
            }
            
            // Ensure consistent frame rate by sleeping if needed
            val processingTime = System.nanoTime() - startTime
            val sleepTime = (targetFrameTime - processingTime).coerceAtLeast(0)
            
            if (sleepTime > 0) {
                delay(sleepTime / 1_000_000) // Convert nanoseconds to milliseconds for delay
            }
        }
    }
    
    // Clean up when the component is disposed
    DisposableEffect(Unit) {
        onDispose {
            isActive.value = false
        }
    }
    
    // Request focus when first displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Box(
        modifier = modifier
            .background(Color.Black)
            .onKeyEvent { event ->
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        val newKeys = pressedKeysState.value + event.key
                        pressedKeysState.value = newKeys
                        true
                    }
                    KeyEventType.KeyUp -> {
                        val newKeys = pressedKeysState.value - event.key
                        pressedKeysState.value = newKeys
                        true
                    }
                    else -> false
                }
            }
            .focusRequester(focusRequester)
            .focusTarget()
            .focusable(true)
    ) {
        // This hidden Text triggers recomposition based on the render trigger
        // It doesn't display anything but forces Canvas to redraw
        Text(
            text = "",
            modifier = Modifier.size(0.dp),
            color = Color.Transparent,
            // Using renderTrigger to force redraws
            fontSize = renderTrigger.value.sp
        )
        
        // Draw the 3D view
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width.toInt()
            val height = size.height.toInt()
            
            // Define colors
            val ceilingColor = Color(0xFF4444AA)
            val floorColor = Color(0xFF444444)
            val wallColor = Color(0xFF882222)
            val shadedWallColor = Color(0xFF661111)
            val doorColor = Color(0xFF8B4513)
            val secretWallColor = Color(0xFF00FFFF)
            val pickupColor = Color.Yellow
            val enemyColor = Color.Red
            
            // Draw ceiling and floor
            drawRect(ceilingColor, Offset.Zero, Size(width.toFloat(), height.toFloat() / 2))
            drawRect(floorColor, Offset(0f, height.toFloat() / 2), Size(width.toFloat(), height.toFloat() / 2))
            
            // Raycasting
            for (x in 0 until width) {
                // Calculate ray position and direction
                val cameraX = 2 * x.toDouble() / width - 1 // x-coordinate in camera space
                val rayDirX = gameEngine.player.dirX + gameEngine.player.planeX * cameraX
                val rayDirY = gameEngine.player.dirY + gameEngine.player.planeY * cameraX
                
                // Which box of the map we're in
                var mapX = gameEngine.player.x.toInt()
                var mapY = gameEngine.player.y.toInt()
                
                // Length of ray from current position to next x or y-side
                var sideDistX: Double
                var sideDistY: Double
                
                // Length of ray from one x or y-side to next x or y-side
                val deltaDistX = if (rayDirX == 0.0) Double.MAX_VALUE else abs(1 / rayDirX)
                val deltaDistY = if (rayDirY == 0.0) Double.MAX_VALUE else abs(1 / rayDirY)
                
                // What direction to step in x or y-direction (either +1 or -1)
                val stepX: Int
                val stepY: Int
                
                // Calculate step and initial sideDist
                if (rayDirX < 0) {
                    stepX = -1
                    sideDistX = (gameEngine.player.x - mapX) * deltaDistX
                } else {
                    stepX = 1
                    sideDistX = (mapX + 1.0 - gameEngine.player.x) * deltaDistX
                }
                
                if (rayDirY < 0) {
                    stepY = -1
                    sideDistY = (gameEngine.player.y - mapY) * deltaDistY
                } else {
                    stepY = 1
                    sideDistY = (mapY + 1.0 - gameEngine.player.y) * deltaDistY
                }
                
                // Perform DDA
                var hit = false
                var side = 0 // 0 for NS wall, 1 for EW wall
                var hitTileType: StaticTileType? = null
                var hitDynamicType: DynamicTileType? = null
                
                while (!hit) {
                    // Jump to next map square, either in x-direction, or in y-direction
                    if (sideDistX < sideDistY) {
                        sideDistX += deltaDistX
                        mapX += stepX
                        side = 0
                    } else {
                        sideDistY += deltaDistY
                        mapY += stepY
                        side = 1
                    }
                    
                    // Check if ray has hit a wall
                    if (mapX < 0 || mapX >= gameEngine.mapData.width || mapY < 0 || mapY >= gameEngine.mapData.height) {
                        hit = true
                    } else {
                        val tile = gameEngine.mapData.getTile(mapX, mapY)
                        
                        // Check for walls
                        if (tile.staticType == StaticTileType.WALL) {
                            hit = true
                            hitTileType = StaticTileType.WALL
                        }
                        
                        // Check for doors
                        if (tile.dynamicType == DynamicTileType.DOOR) {
                            val doorKey = Pair(mapX, mapY)
                            val doorOpen = gameEngine.isTileAccessible(mapX, mapY)
                            
                            if (!doorOpen) {
                                hit = true
                                hitDynamicType = DynamicTileType.DOOR
                            }
                        }
                        
                        // Check for secret walls
                        if (tile.dynamicType == DynamicTileType.SECRET_WALL) {
                            val wallKey = Pair(mapX, mapY)
                            val wallRevealed = gameEngine.isTileAccessible(mapX, mapY)
                            
                            if (!wallRevealed) {
                                hit = true
                                hitDynamicType = DynamicTileType.SECRET_WALL
                            }
                        }
                        
                        // Check for enemies
                        if (tile.dynamicType == DynamicTileType.ENEMY) {
                            hit = true
                            hitDynamicType = DynamicTileType.ENEMY
                        }
                        
                        // Check for pickups
                        if (tile.dynamicType == DynamicTileType.PICKUP && !gameEngine.isPickupCollected(mapX, mapY)) {
                            hit = true
                            hitDynamicType = DynamicTileType.PICKUP
                        }
                    }
                }
                
                // Calculate distance projected on camera direction
                val perpWallDist = if (side == 0) {
                    (mapX - gameEngine.player.x + (1 - stepX) / 2) / rayDirX
                } else {
                    (mapY - gameEngine.player.y + (1 - stepY) / 2) / rayDirY
                }
                
                // Calculate height of line to draw on screen
                val lineHeight = if (perpWallDist > 0) (height / perpWallDist).toInt() else height
                
                // Calculate lowest and highest pixel to fill in current stripe
                var drawStart = -lineHeight / 2 + height / 2
                if (drawStart < 0) drawStart = 0
                var drawEnd = lineHeight / 2 + height / 2
                if (drawEnd >= height) drawEnd = height - 1
                
                // Choose wall color based on type
                val color = when {
                    hitDynamicType == DynamicTileType.DOOR -> doorColor
                    hitDynamicType == DynamicTileType.SECRET_WALL -> secretWallColor
                    hitDynamicType == DynamicTileType.ENEMY -> enemyColor
                    hitDynamicType == DynamicTileType.PICKUP -> pickupColor
                    hitTileType == StaticTileType.WALL -> if (side == 1) shadedWallColor else wallColor
                    else -> Color.Gray // Should not happen
                }
                
                // Draw the vertical line
                drawLine(
                    color = color,
                    start = Offset(x.toFloat(), drawStart.toFloat()),
                    end = Offset(x.toFloat(), drawEnd.toFloat()),
                    strokeWidth = 1.5f
                )
            }
        }
        
        // Score and controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Score: ${gameEngine.score}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "WASD / Arrow Keys: Move",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Text(
                text = "Spacebar: Interact with doors/walls",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Performance information
            Text(
                text = "FPS: ${frameRate.value}",
                color = Color.Green,
                fontSize = 14.sp
            )
            
            // Debug information - show currently pressed keys
            Text(
                text = "Active keys: ${pressedKeysState.value.joinToString(", ") { it.toString() }}",
                color = Color.Yellow,
                fontSize = 12.sp
            )
        }
    }
}
