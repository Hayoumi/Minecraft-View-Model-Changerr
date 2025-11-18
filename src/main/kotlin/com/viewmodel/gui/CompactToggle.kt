package com.viewmodel.gui

import net.minecraft.client.MinecraftClient
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

    private val toggleOff = 0xFF171B23.toInt()
    private val toggleOn = 0xFF34C759.toInt()
    private val handleColor = 0xFFF6F7F9.toInt()
    private val textColor = 0xFFF6F7F9.toInt()
    private val borderColor = 0x19333B45
    private val radius = 14

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
        val textRenderer = MinecraftClient.getInstance().textRenderer

        val target = if (enabled) 1f else 0f
        progress += (target - progress) * 0.2f

        context.drawText(textRenderer, label, x, y + (height - 8) / 2, textColor, false)

        val toggleW = 46
        val toggleH = 24
        val toggleX = x + width - toggleW
        val toggleY = y + (height - toggleH) / 2

        val bgColor = interpolate(toggleOff, toggleOn, progress)
        UiPrimitives.fillRoundedRect(context, toggleX, toggleY, toggleW, toggleH, radius, bgColor)
        UiPrimitives.drawRoundedBorder(context, toggleX, toggleY, toggleW, toggleH, radius, borderColor)

        val handleSize = 20
        val handleX = (toggleX + 2 + (toggleW - handleSize - 4) * progress).toInt()
        val handleY = toggleY + (toggleH - handleSize) / 2

        UiPrimitives.fillRoundedRect(context, handleX, handleY, handleSize, handleSize, handleSize / 2, handleColor)
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
