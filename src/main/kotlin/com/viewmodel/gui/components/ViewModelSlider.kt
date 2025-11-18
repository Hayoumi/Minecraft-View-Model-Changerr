package com.viewmodel.gui.components

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import kotlin.math.max
import kotlin.math.round

class ViewModelSlider(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val label: Text,
    private val min: Float,
    private val max: Float,
    private val step: Float,
    private val valueSupplier: () -> Float,
    private val formatter: (Float) -> String,
    private val onValueChanged: (Float) -> Unit
) : ClickableWidget(x, y, width, height, label) {

    private var dragging = false
    private val labelPadding = 10
    private val trackPadding = 12

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        val headerColor = if (hovered) ViewModelPalette.TEXT_PRIMARY else ViewModelPalette.TEXT_SECONDARY

        GuiPrimitives.fillRoundedRect(
            context,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            height / 2f,
            ViewModelPalette.PANEL_ACCENT
        )

        val value = valueSupplier().coerceIn(min, max)
        val normalized = if (max == min) 0f else MathHelper.clamp((value - min) / (max - min), 0f, 1f)

        context.drawText(textRenderer, label, x + labelPadding, y + 6, ViewModelPalette.rgb(headerColor), false)

        val valueText = formatter(value)
        context.drawText(
            textRenderer,
            valueText,
            x + width - textRenderer.getWidth(valueText) - labelPadding,
            y + 6,
            ViewModelPalette.rgb(ViewModelPalette.TEXT_SECONDARY),
            false
        )

        val trackX = x + trackPadding
        val trackWidth = width - (trackPadding * 2)
        val trackHeight = 6f
        val trackY = y + height - trackHeight - 8

        GuiPrimitives.fillCapsule(
            context,
            trackX.toFloat(),
            trackY.toFloat(),
            trackWidth.toFloat(),
            trackHeight,
            ViewModelPalette.SURFACE_MUTED
        )

        val filledWidth = trackWidth * normalized
        if (filledWidth > 0.5f) {
            GuiPrimitives.fillCapsule(
                context,
                trackX.toFloat(),
                trackY.toFloat(),
                filledWidth,
                trackHeight,
                ViewModelPalette.ACCENT
            )
        }

        val knobSize = max(12f, trackHeight + 8f)
        val knobCenterX = trackX + filledWidth
        val knobX = knobCenterX - knobSize / 2f
        val knobY = trackY + trackHeight / 2f - knobSize / 2f
        GuiPrimitives.fillCapsule(context, knobX, knobY, knobSize, knobSize, ViewModelPalette.SURFACE_HIGHLIGHT)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)
        dragging = true
        updateValue(mouseX)
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        super.onRelease(mouseX, mouseY)
        dragging = false
    }

    override fun onDrag(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double) {
        if (!dragging) return
        updateValue(mouseX)
    }

    private fun updateValue(mouseX: Double) {
        val trackX = x + trackPadding
        val trackWidth = width - (trackPadding * 2)
        val ratio = ((mouseX - trackX) / trackWidth).coerceIn(0.0, 1.0)
        val calculated = min + (max - min) * ratio.toFloat()
        val snapped = snap(calculated)
        onValueChanged(snapped)
    }

    private fun snap(value: Float): Float {
        if (step <= 0f) return value.coerceIn(min, max)
        val snapped = round(value / step) * step
        return snapped.coerceIn(min, max)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(
            NarrationPart.TITLE,
            Text.literal("${label.string}: ${formatter(valueSupplier())}")
        )
    }
}
