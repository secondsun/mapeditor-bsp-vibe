package model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Represents the types of tiles that can be placed in the first data plane (static structures)
 */
enum class StaticTileType {
    EMPTY,
    WALL,
    FLOOR;
    
    companion object {
        fun getDisplayName(type: StaticTileType): String {
            return when (type) {
                EMPTY -> "Empty"
                WALL -> "Wall"
                FLOOR -> "Floor"
            }
        }
    }
}

/**
 * Represents the types of tiles that can be placed in the second data plane (dynamic elements)
 */
enum class DynamicTileType {
    NONE,
    PLAYER_START,
    ENEMY,
    PICKUP,
    DOOR,
    SECRET_WALL;
    
    companion object {
        fun getDisplayName(type: DynamicTileType): String {
            return when (type) {
                NONE -> "None"
                PLAYER_START -> "Player Start"
                ENEMY -> "Enemy"
                PICKUP -> "Pickup"
                DOOR -> "Door"
                SECRET_WALL -> "Secret Wall"
            }
        }
    }
}

/**
 * Represents a single map tile with both static and dynamic data
 */
class MapTile {
    var staticType by mutableStateOf(StaticTileType.EMPTY)
    var dynamicType by mutableStateOf(DynamicTileType.NONE)
}

/**
 * Represents the entire map data
 */
class MapData(val width: Int = 64, val height: Int = 64) {
    // Use version counter to force recomposition
    var version by mutableStateOf(0)
        private set
    
    private val tiles: Array<Array<MapTile>> = Array(height) {
        Array(width) { MapTile() }
    }
    
    /**
     * Forces a UI update by incrementing the version
     * Useful when loading maps or after batch operations
     */
    fun forceUpdate() {
        version++
    }
    
    fun getTile(x: Int, y: Int): MapTile {
        if (x in 0 until width && y in 0 until height) {
            return tiles[y][x]
        }
        throw IndexOutOfBoundsException("Coordinates out of bounds: $x, $y")
    }
    
    fun setStaticTile(x: Int, y: Int, type: StaticTileType) {
        if (x in 0 until width && y in 0 until height) {
            tiles[y][x].staticType = type
            // Increment version to trigger recomposition
            version++
        }
    }
    
    fun setDynamicTile(x: Int, y: Int, type: DynamicTileType) {
        if (x in 0 until width && y in 0 until height) {
            tiles[y][x].dynamicType = type
            // Increment version to trigger recomposition
            version++
        }
    }
    
    fun clearPlayerStart() {
        // Ensure only one player start position exists
        var changed = false
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (tiles[y][x].dynamicType == DynamicTileType.PLAYER_START) {
                    tiles[y][x].dynamicType = DynamicTileType.NONE
                    changed = true
                }
            }
        }
        
        // Only increment version if something changed
        if (changed) {
            version++
        }
    }
    
    fun serialize(): String {
        // Simple serialization for saving maps
        val sb = StringBuilder()
        sb.append("$width,$height\n")
        
        // First data plane (static)
        for (y in 0 until height) {
            for (x in 0 until width) {
                sb.append(tiles[y][x].staticType.ordinal)
                if (x < width - 1) sb.append(",")
            }
            sb.append("\n")
        }
        
        // Second data plane (dynamic)
        for (y in 0 until height) {
            for (x in 0 until width) {
                sb.append(tiles[y][x].dynamicType.ordinal)
                if (x < width - 1) sb.append(",")
            }
            sb.append("\n")
        }
        
        return sb.toString()
    }
    
    companion object {
        fun deserialize(data: String): MapData? {
            try {
                val lines = data.lines()
                val dimensions = lines[0].split(",")
                val width = dimensions[0].toInt()
                val height = dimensions[1].toInt()
                
                val map = MapData(width, height)
                
                // Parse static tiles
                for (y in 0 until height) {
                    val values = lines[y + 1].split(",")
                    for (x in 0 until width) {
                        val ordinal = values[x].toInt()
                        map.setStaticTile(x, y, StaticTileType.values()[ordinal])
                    }
                }
                
                // Parse dynamic tiles
                for (y in 0 until height) {
                    val values = lines[y + height + 1].split(",")
                    for (x in 0 until width) {
                        val ordinal = values[x].toInt()
                        map.setDynamicTile(x, y, DynamicTileType.values()[ordinal])
                    }
                }
                
                return map
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}
