package com.viewmodel

import kotlinx.serialization.Serializable

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
        @JvmField
        var current = ViewModelConfig()
    }
}
