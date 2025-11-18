package com.viewmodel.gui.components

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ViewModelToggle(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val label: Text,
    private val stateSupplier: () -> Boolean,
    private val onToggle: (Boolean) -> Unit
) : ClickableWidget(x, y, width, height, label) {

    private var animation = if (stateSupplier()) 1f else 0f

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        GuiPrimitives.fillRoundedRect(
            context,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            height / 2f,
            ViewModelPalette.PANEL_ACCENT
        )

        val target = if (stateSupplier()) 1f else 0f
        animation = MathHelper.lerp(0.25f * delta.coerceIn(0f, 1f), animation, target)

        val labelColor = if (hovered) ViewModelPalette.TEXT_PRIMARY else ViewModelPalette.TEXT_SECONDARY
        context.drawText(
            textRenderer,
            label,
            x + 12,
            y + (height - textRenderer.fontHeight) / 2,
            ViewModelPalette.rgb(labelColor),
            false
        )

        val switchWidth = 48f
        val switchHeight = height - 12f
        val switchX = x + width - switchWidth - 12f
        val switchY = y + (height - switchHeight) / 2f

        val trackColor = colorMix(ViewModelPalette.SURFACE_MUTED, ViewModelPalette.ACCENT, animation)
        GuiPrimitives.fillCapsule(context, switchX, switchY, switchWidth, switchHeight, trackColor)

        val knobSize = switchHeight - 6f
        val knobMin = switchX + 3f
        val knobMax = switchX + switchWidth - knobSize - 3f
        val knobX = MathHelper.lerp(animation, knobMin, knobMax)
        val knobColor = colorMix(ViewModelPalette.SURFACE_HIGHLIGHT, ViewModelPalette.ACCENT, animation * 0.5f)
        GuiPrimitives.fillCapsule(context, knobX, switchY + 3f, knobSize, knobSize, knobColor)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)
        onToggle(!stateSupplier())
    }

    private fun colorMix(from: Int, to: Int, progress: Float): Int {
        val clamped = MathHelper.clamp(progress, 0f, 1f)
        val inv = 1f - clamped

        val a = ((from ushr 24 and 0xFF) * inv + (to ushr 24 and 0xFF) * clamped).toInt()
        val r = ((from ushr 16 and 0xFF) * inv + (to ushr 16 and 0xFF) * clamped).toInt()
        val g = ((from ushr 8 and 0xFF) * inv + (to ushr 8 and 0xFF) * clamped).toInt()
        val b = ((from and 0xFF) * inv + (to and 0xFF) * clamped).toInt()

        return ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        val state = if (stateSupplier()) Text.translatable("gui.yes") else Text.translatable("gui.no")
        builder.put(NarrationPart.TITLE, Text.literal("${label.string}: ${state.string}"))
    }
}
