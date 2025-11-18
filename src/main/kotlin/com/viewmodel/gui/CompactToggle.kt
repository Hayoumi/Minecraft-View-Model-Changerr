package com.viewmodel.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class CompactToggle(
    x: Int, y: Int, width: Int, height: Int,
    private val label: Text,
    private var enabled: Boolean,
    private val onChange: (Boolean) -> Unit
) : ButtonWidget(x, y, width, height, label, { button ->
    (button as? CompactToggle)?.toggle()
}, DEFAULT_NARRATION_SUPPLIER) {

    // Мягкая палитра
    private val TOGGLE_OFF = 0xFF1F2227.toInt()
    private val TOGGLE_ON = 0xFF0A84FF.toInt()
    private val HANDLE = 0xFFFFFFFF.toInt()
    private val TEXT = 0xFFF5F7FA.toInt()
    private val TRACK_BORDER = 0x40333B45
    private val radius = 9

    private var progress = if (enabled) 1f else 0f
    private val defaultValue = enabled

    private fun toggle() {
        enabled = !enabled
        onChange(enabled)
    }

    fun reset() {
        if (enabled != defaultValue) {
            enabled = defaultValue
            onChange(enabled)
        }
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val textRenderer = net.minecraft.client.MinecraftClient.getInstance().textRenderer
        
        // Плавная анимация
        val target = if (enabled) 1f else 0f
        progress += (target - progress) * 0.2f
        
        // Текст
        context.drawText(textRenderer, label, x, y + (height - 8) / 2, TEXT, false)
        
        // Toggle switch
        val toggleW = 36
        val toggleH = 18
        val toggleX = x + width - toggleW
        val toggleY = y + (height - toggleH) / 2
        
        // Фон
        val bgColor = interpolate(TOGGLE_OFF, TOGGLE_ON, progress)
        UiPrimitives.fillRoundedRect(context, toggleX, toggleY, toggleW, toggleH, radius, bgColor)
        UiPrimitives.drawRoundedBorder(context, toggleX, toggleY, toggleW, toggleH, radius, TRACK_BORDER)
        
        // Ручка
        val handleSize = 14
        val handleX = (toggleX + 2 + (toggleW - handleSize - 4) * progress).toInt()
        val handleY = toggleY + (toggleH - handleSize) / 2
        
        // Ручка (темная на светлом фоне, светлая на темном)
        val handleColor = if (enabled) HANDLE else 0xFFFFFFFF.toInt()
        context.fill(handleX, handleY, handleX + handleSize, handleY + handleSize, handleColor)
    }

    private fun interpolate(c1: Int, c2: Int, p: Float): Int {
        val r1 = (c1 shr 16 and 0xFF)
        val g1 = (c1 shr 8 and 0xFF)
        val b1 = (c1 and 0xFF)
        
        val r2 = (c2 shr 16 and 0xFF)
        val g2 = (c2 shr 8 and 0xFF)
        val b2 = (c2 and 0xFF)
        
        val r = (r1 + (r2 - r1) * p).toInt()
        val g = (g1 + (g2 - g1) * p).toInt()
        val b = (b1 + (b2 - b1) * p).toInt()
        
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }
}
