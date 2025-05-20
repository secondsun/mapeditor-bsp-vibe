package game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import model.DynamicTileType
import model.MapData
import model.StaticTileType
import kotlin.math.floor

/**
 * Manages the game state and logic
 */
class GameEngine(val mapData: MapData) {
    // Player instance
    val player = Player()
    
    // Game state
    var isRunning by mutableStateOf(true)
    var score by mutableStateOf(0)
    
    // Keyboard state
    private var keyUp = false
    private var keyDown = false
    private var keyLeft = false
    private var keyRight = false
    private var keySpace = false
    
    // Track which dynamic objects are active (doors, secret walls, pickups)
    private val activeDoors = mutableMapOf<Pair<Int, Int>, Boolean>() // true = open, false = closed
    private val activeSecretWalls = mutableMapOf<Pair<Int, Int>, Boolean>() // true = revealed, false = hidden
    private val collectedPickups = mutableSetOf<Pair<Int, Int>>()
    
    init {
        // Initialize player position at the player start marker
        var startFound = false
        for (y in 0 until mapData.height) {
            for (x in 0 until mapData.width) {
                val tile = mapData.getTile(x, y)
                if (tile.dynamicType == DynamicTileType.PLAYER_START) {
                    player.x = x + 0.5 // Center of the tile
                    player.y = y + 0.5
                    // Face east by default
                    player.angle = 0.0
                    startFound = true
                    break
                }
            }
            if (startFound) break
        }
        
        // If no player start found, place in the center of the map
        if (!startFound) {
            player.x = mapData.width / 2.0
            player.y = mapData.height / 2.0
        }
        
        // Initialize doors as closed
        for (y in 0 until mapData.height) {
            for (x in 0 until mapData.width) {
                val tile = mapData.getTile(x, y)
                if (tile.dynamicType == DynamicTileType.DOOR) {
                    activeDoors[Pair(x, y)] = false
                } else if (tile.dynamicType == DynamicTileType.SECRET_WALL) {
                    activeSecretWalls[Pair(x, y)] = false
                }
            }
        }
    }
    
    /**
     * Process keyboard input to move player
     */
    fun processInput(pressedKeys: Set<Key>) {
        // Update key states
        val keyW = Key.W in pressedKeys
        val keyS = Key.S in pressedKeys
        val keyA = Key.A in pressedKeys
        val keyD = Key.D in pressedKeys
        
        val arrowUp = Key.DirectionUp in pressedKeys
        val arrowDown = Key.DirectionDown in pressedKeys
        val arrowLeft = Key.DirectionLeft in pressedKeys
        val arrowRight = Key.DirectionRight in pressedKeys
        
        // Space is an event rather than a continuous state
        val spacePressed = Key.Spacebar in pressedKeys
        
        // WASD controls - FPS style (W/S = forward/back, A/D = strafe left/right)
        if (keyW) player.moveForward()
        if (keyS) player.moveBackward()
        if (keyA) player.strafeLeft()
        if (keyD) player.strafeRight()
        
        // Arrow controls - rotate and move
        if (arrowUp) player.moveForward()
        if (arrowDown) player.moveBackward()
        if (arrowLeft) player.rotateLeft()
        if (arrowRight) player.rotateRight()
        
        // Update key state for collision handling
        keyUp = keyW || arrowUp
        keyDown = keyS || arrowDown
        keyLeft = keyA || arrowLeft
        keyRight = keyD || arrowRight
        
        // Handle wall collision
        handleCollision()
        
        // Process space key (interaction)
        if (spacePressed && !keySpace) {
            interact()
        }
        keySpace = spacePressed
    }
    
    /**
     * Prevent walking through walls
     */
    private fun handleCollision() {
        val mapX = floor(player.x).toInt()
        val mapY = floor(player.y).toInt()
        
        // Store player position before collision
        val oldX = player.x
        val oldY = player.y
        
        // Calculate the collision boundary
        val collisionMargin = 0.2  // Keep some distance from walls
        
        // Only check immediate surrounding tiles for better performance
        for (dy in -1..1) {
            for (dx in -1..1) {
                val checkX = mapX + dx
                val checkY = mapY + dy
                
                if (checkX in 0 until mapData.width && checkY in 0 until mapData.height) {
                    val tile = mapData.getTile(checkX, checkY)
                    val isSolid = tile.staticType == StaticTileType.WALL ||
                            (tile.dynamicType == DynamicTileType.DOOR && !(activeDoors[Pair(checkX, checkY)] ?: false)) ||
                            (tile.dynamicType == DynamicTileType.SECRET_WALL && !(activeSecretWalls[Pair(checkX, checkY)] ?: false))
                    
                    if (isSolid) {
                        // Simple collision resolution - more efficient algorithm
                        val minX = checkX.toDouble()
                        val minY = checkY.toDouble() 
                        val maxX = minX + 1.0
                        val maxY = minY + 1.0
                        
                        // Check if player is colliding with this wall
                        if (player.x + collisionMargin > minX && player.x - collisionMargin < maxX &&
                            player.y + collisionMargin > minY && player.y - collisionMargin < maxY) {
                            
                            // Find the side with minimum penetration
                            val left = player.x + collisionMargin - minX
                            val right = maxX - (player.x - collisionMargin)
                            val top = player.y + collisionMargin - minY
                            val bottom = maxY - (player.y - collisionMargin)
                            
                            // Resolve collision along axis with smallest penetration
                            val minPenetration = minOf(left, right, top, bottom)
                            
                            when (minPenetration) {
                                left -> player.x = minX - collisionMargin
                                right -> player.x = maxX + collisionMargin
                                top -> player.y = minY - collisionMargin
                                bottom -> player.y = maxY + collisionMargin
                            }
                        }
                    }
                    
                    // Check for pickups - simplified check
                    if (tile.dynamicType == DynamicTileType.PICKUP) {
                        val pickupKey = Pair(checkX, checkY)
                        
                        if (!collectedPickups.contains(pickupKey)) {
                            // Use a simpler distance check
                            val dx = player.x - (checkX + 0.5)
                            val dy = player.y - (checkY + 0.5)
                            val distSquared = dx * dx + dy * dy
                            
                            if (distSquared < 0.3 * 0.3) {  // Slightly smaller pickup radius
                                collectedPickups.add(pickupKey)
                                score += 100
                            }
                        }
                    }
                }
            }
        }
        
        // If player is stuck, revert to previous position
        if (player.x == oldX && player.y == oldY) {
            return
        }
        
        // Ensure player is within map bounds
        player.x = player.x.coerceIn(0.2, mapData.width.toDouble() - 0.2)
        player.y = player.y.coerceIn(0.2, mapData.height.toDouble() - 0.2)
    }
    
    /**
     * Handle spacebar interactions (doors, secret walls)
     */
    private fun interact() {
        // Check for interaction with objects in front of player
        val interactDistance = 1.5
        val interactX = player.x + player.dirX * interactDistance
        val interactY = player.y + player.dirY * interactDistance
        
        val mapX = floor(interactX).toInt()
        val mapY = floor(interactY).toInt()
        
        if (mapX in 0 until mapData.width && mapY in 0 until mapData.height) {
            val tile = mapData.getTile(mapX, mapY)
            
            // Handle door interaction
            if (tile.dynamicType == DynamicTileType.DOOR) {
                val doorKey = Pair(mapX, mapY)
                val currentState = activeDoors[doorKey] ?: false
                activeDoors[doorKey] = !currentState
            }
            
            // Handle secret wall interaction
            if (tile.dynamicType == DynamicTileType.SECRET_WALL) {
                val wallKey = Pair(mapX, mapY)
                val currentState = activeSecretWalls[wallKey] ?: false
                activeSecretWalls[wallKey] = !currentState
            }
        }
    }
    
    /**
     * Check if a tile is accessible (not a wall, closed door, etc.)
     */
    fun isTileAccessible(mapX: Int, mapY: Int): Boolean {
        if (mapX < 0 || mapX >= mapData.width || mapY < 0 || mapY >= mapData.height) {
            return false
        }
        
        val tile = mapData.getTile(mapX, mapY)
        
        // Check static walls
        if (tile.staticType == StaticTileType.WALL) {
            return false
        }
        
        // Check doors
        if (tile.dynamicType == DynamicTileType.DOOR) {
            val doorKey = Pair(mapX, mapY)
            val doorOpen = activeDoors[doorKey] ?: false
            return doorOpen
        }
        
        // Check secret walls
        if (tile.dynamicType == DynamicTileType.SECRET_WALL) {
            val wallKey = Pair(mapX, mapY)
            val wallRevealed = activeSecretWalls[wallKey] ?: false
            return wallRevealed
        }
        
        return true
    }
    
    /**
     * Check if a pickup has been collected
     */
    fun isPickupCollected(mapX: Int, mapY: Int): Boolean {
        return collectedPickups.contains(Pair(mapX, mapY))
    }
    
    /**
     * Helper function for abs
     */
    private fun abs(value: Double): Double = kotlin.math.abs(value)
}
