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

    private val base = 0xFF141821.toInt()
    private val hover = 0xFF34C759.toInt()
    private val selectedFill = 0xFF10131B.toInt()
    private val text = 0xFFF6F7F9.toInt()
    private val textInverse = 0xFF041007.toInt()
    private val border = 0x19333B45
    private val radius = 16

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        val fillColor = when {
            selected -> selectedFill
            hovered -> hover
            else -> base
        }
        val textColor = if (hovered) textInverse else text

        UiPrimitives.fillRoundedRect(context, x, y, width, height, radius, fillColor)
        UiPrimitives.drawRoundedBorder(context, x, y, width, height, radius, border)

        val renderer = MinecraftClient.getInstance().textRenderer
        val tx = x + (width - renderer.getWidth(message)) / 2
        val ty = y + (height - 8) / 2
        context.drawText(renderer, message, tx, ty, textColor, false)
    }
}
