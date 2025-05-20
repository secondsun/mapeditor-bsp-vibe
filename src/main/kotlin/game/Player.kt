package game

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Represents the player in the game
 */
class Player(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var angle: Double = 0.0, // Radians, 0 = East, PI/2 = North, PI = West, 3PI/2 = South
    var moveSpeed: Double = 0.03,    // Further reduced for smoother movement with continuous updates
    var rotateSpeed: Double = 0.03   // Further reduced for smoother rotation with continuous updates
) {
    val dirX: Double
        get() = cos(angle)
    
    val dirY: Double
        get() = sin(angle)
    
    // Camera plane is perpendicular to the direction vector
    val planeX: Double
        get() = sin(-angle) * 0.66
    
    val planeY: Double
        get() = cos(-angle) * 0.66
    
    fun moveForward() {
        x += dirX * moveSpeed
        y += dirY * moveSpeed
    }
    
    fun moveBackward() {
        x -= dirX * moveSpeed
        y -= dirY * moveSpeed
    }
    
    fun strafeLeft() {
        x += dirY * moveSpeed
        y -= dirX * moveSpeed
    }
    
    fun strafeRight() {
        x -= dirY * moveSpeed
        y += dirX * moveSpeed
    }
    
    fun rotateLeft() {
        angle -= rotateSpeed
        // Keep angle within [0, 2*PI)
        if (angle < 0) angle += 2 * PI
    }
    
    fun rotateRight() {
        angle += rotateSpeed
        // Keep angle within [0, 2*PI)
        if (angle >= 2 * PI) angle -= 2 * PI
    }
}
