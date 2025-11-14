package com.viewmodel

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ViewModelConfig(
    var size: Float = 1.0f,
    var positionX: Float = 0.0f,
    var positionY: Float = 0.0f,
    var positionZ: Float = 0.0f,
    var rotationYaw: Float = 0.0f,
    var rotationPitch: Float = 0.0f,
    var rotationRoll: Float = 0.0f,
    var noSwing: Boolean = false,
    var scaleSwing: Boolean = false
    // УБРАЛИ noEquipReset - теперь всегда включен
) {
    companion object {
        private val json = Json { 
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        
        private val configFile = File("config/viewmodel.json")
        
        @JvmField
        var current = ViewModelConfig()
        
        @JvmStatic
        fun load() {
            try {
                if (configFile.exists()) {
                    current = json.decodeFromString(configFile.readText())
                    println("[ViewModel] Config loaded")
                }
            } catch (e: Exception) {
                println("[ViewModel] Failed to load config: ${e.message}")
                current = ViewModelConfig()
            }
        }
        
        @JvmStatic
        fun save() {
            try {
                configFile.parentFile.mkdirs()
                configFile.writeText(json.encodeToString(current))
                println("[ViewModel] Config saved")
            } catch (e: Exception) {
                println("[ViewModel] Failed to save config: ${e.message}")
            }
        }
    }
}
