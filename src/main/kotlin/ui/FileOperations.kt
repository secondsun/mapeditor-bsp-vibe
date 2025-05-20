package ui

import androidx.compose.ui.awt.ComposeWindow
import model.MapData
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Utility for file operations in the map editor
 */
object FileOperations {
    private const val FILE_EXTENSION = "wmap"
    
    /**
     * Save the current map to a file
     */
    fun saveMap(window: ComposeWindow, editorState: EditorState): Boolean {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Save Map"
            fileFilter = FileNameExtensionFilter("Wolf Map Files (.$FILE_EXTENSION)", FILE_EXTENSION)
            
            // If there's already a file path, use it
            editorState.currentFilePath?.let {
                selectedFile = File(it)
            }
        }
        
        val result = fileChooser.showSaveDialog(window)
        if (result == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            
            // Add extension if needed
            if (!file.name.endsWith(".$FILE_EXTENSION")) {
                file = File(file.absolutePath + ".$FILE_EXTENSION")
            }
            
            try {
                // Serialize and save the map
                val serializedMap = editorState.mapData.serialize()
                file.writeText(serializedMap)
                
                // Update the current file path and unsaved changes flag
                editorState.currentFilePath = file.absolutePath
                editorState.hasUnsavedChanges = false
                
                JOptionPane.showMessageDialog(
                    window,
                    "Map saved successfully to ${file.name}",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE
                )
                
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(
                    window,
                    "Error saving map: ${e.message}",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        
        return false
    }
    
    /**
     * Load a map from a file
     */
    fun loadMap(window: ComposeWindow, editorState: EditorState): Boolean {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Load Map"
            fileFilter = FileNameExtensionFilter("Wolf Map Files (.$FILE_EXTENSION)", FILE_EXTENSION)
        }
        
        val result = fileChooser.showOpenDialog(window)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            
            try {
                // Read and deserialize the map
                val serializedMap = file.readText()
                val loadedMap = MapData.deserialize(serializedMap)
                
                if (loadedMap != null) {
                    // Update the editor state with the loaded map
                    editorState.mapData = loadedMap
                    editorState.currentFilePath = file.absolutePath
                    editorState.hasUnsavedChanges = false
                    
                    // Force UI update by incrementing the version
                    loadedMap.forceUpdate()
                    
                    JOptionPane.showMessageDialog(
                        window,
                        "Map loaded successfully from ${file.name}",
                        "Load Successful",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                    
                    return true
                } else {
                    JOptionPane.showMessageDialog(
                        window,
                        "Failed to parse map file. The file may be corrupted or in an invalid format.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(
                    window,
                    "Error loading map: ${e.message}",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        
        return false
    }
    
    /**
     * Check if there are unsaved changes and prompt to save
     */
    fun checkUnsavedChanges(window: ComposeWindow, editorState: EditorState): Boolean {
        if (!editorState.hasUnsavedChanges) {
            return true
        }
        
        val options = arrayOf("Save", "Don't Save", "Cancel")
        val result = JOptionPane.showOptionDialog(
            window,
            "There are unsaved changes. Do you want to save them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        )
        
        return when (result) {
            0 -> saveMap(window, editorState) // Save
            1 -> true // Don't Save
            else -> false // Cancel or close dialog
        }
    }
}
