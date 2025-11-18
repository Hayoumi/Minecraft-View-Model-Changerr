package com.viewmodel.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class CompactButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    var selected: Boolean = false,
    onPress: PressAction
) : ButtonWidget(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER) {

    private val base = 0xFF1F2227.toInt()
    private val hover = 0xFF0A84FF.toInt()
    private val border = 0x40333B45
    private val text = 0xFFF5F7FA.toInt()
    private val selectedFill = 0xFF28303A.toInt()
    private val radius = 10

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        val fillColor = when {
            hovered -> hover
            selected -> selectedFill
            else -> base
        }
        val textColor = if (hovered) 0xFF0B1726.toInt() else text

        UiPrimitives.fillRoundedRect(context, x, y, width, height, radius, fillColor)
        UiPrimitives.drawRoundedBorder(context, x, y, width, height, radius, border)

        val renderer = MinecraftClient.getInstance().textRenderer
        val tx = x + (width - renderer.getWidth(message)) / 2
        val ty = y + (height - 8) / 2
        context.drawText(renderer, message, tx, ty, textColor, false)
    }
}
