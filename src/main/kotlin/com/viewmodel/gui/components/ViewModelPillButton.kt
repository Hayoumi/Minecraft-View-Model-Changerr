package com.viewmodel.gui.components

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ViewModelPillButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val label: Text,
    private val backgroundColor: Int,
    private val textColor: Int = ViewModelPalette.TEXT_PRIMARY,
    private val onPress: () -> Unit
) : ClickableWidget(x, y, width, height, label) {

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val baseColor = when {
            !active -> blend(backgroundColor, ViewModelPalette.NEUTRAL, 0.5f)
            isHovered -> blend(backgroundColor, 0xFFFFFFFF.toInt(), 0.1f)
            else -> backgroundColor
        }

        GuiPrimitives.fillCapsule(
            context,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            baseColor
        )

        val textY = y + (height - textRenderer.fontHeight) / 2
        val textX = x + (width - textRenderer.getWidth(label)) / 2
        context.drawText(textRenderer, label, textX, textY, ViewModelPalette.rgb(textColor), false)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)
        if (active) {
            onPress()
        }
    }

    private fun blend(from: Int, to: Int, progress: Float): Int {
        val clamped = MathHelper.clamp(progress, 0f, 1f)
        val inv = 1f - clamped

        val a = ((from ushr 24 and 0xFF) * inv + (to ushr 24 and 0xFF) * clamped).toInt()
        val r = ((from ushr 16 and 0xFF) * inv + (to ushr 16 and 0xFF) * clamped).toInt()
        val g = ((from ushr 8 and 0xFF) * inv + (to ushr 8 and 0xFF) * clamped).toInt()
        val b = ((from and 0xFF) * inv + (to and 0xFF) * clamped).toInt()

        return ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, label)
    }
}
