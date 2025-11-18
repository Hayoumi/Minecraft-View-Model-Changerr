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

    private val base = 0xFF252525.toInt()
    private val hover = 0xFFFFFFFF.toInt()
    private val border = 0xFF3A3A3A.toInt()
    private val text = 0xFFE0E0E0.toInt()
    private val selectedFill = 0xFF2F2F2F.toInt()

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        val fillColor = when {
            hovered -> hover
            selected -> selectedFill
            else -> base
        }
        val textColor = if (hovered) 0xFF000000.toInt() else text

        context.fill(x, y, x + width, y + height, fillColor)
        drawBorder(context)

        val renderer = MinecraftClient.getInstance().textRenderer
        val tx = x + (width - renderer.getWidth(message)) / 2
        val ty = y + (height - 8) / 2
        context.drawText(renderer, message, tx, ty, textColor, false)
    }

    private fun drawBorder(context: DrawContext) {
        context.fill(x, y, x + width, y + 1, border)
        context.fill(x, y + height - 1, x + width, y + height, border)
        context.fill(x, y, x + 1, y + height, border)
        context.fill(x + width - 1, y, x + width, y + height, border)
    }
}
