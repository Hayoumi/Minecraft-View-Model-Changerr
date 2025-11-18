package com.viewmodel.gui.components

object ViewModelPalette {
    const val PANEL = 0xE6121317.toInt()
    const val PANEL_ACCENT = 0xD0181A21.toInt()
    const val SURFACE_HIGHLIGHT = 0xF01F212A.toInt()
    const val SURFACE_MUTED = 0xC014151B.toInt()

    const val ACCENT = 0xFF7FE8D0.toInt()
    const val ACCENT_ALT = 0xFF7AB8FF.toInt()
    const val WARNING = 0xFFFF7B7B.toInt()
    const val SUCCESS = 0xFF55E0B7.toInt()
    const val NEUTRAL = 0xFF7E8697.toInt()

    const val TEXT_PRIMARY = 0xFFF5F5FA.toInt()
    const val TEXT_SECONDARY = 0xFFB8BBC8.toInt()
    const val TEXT_MUTED = 0xFF6E7385.toInt()

    fun rgb(color: Int): Int = color and 0x00FFFFFF
}
