package com.viewmodel.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text
import kotlin.math.round

class CompactSlider(
    x: Int, y: Int, width: Int, height: Int,
    private val label: Text,
    value: Float,
    private val min: Float,
    private val max: Float,
    private val defaultValue: Float,
    private val onChange: (Float) -> Unit,
    private val onReset: () -> Unit
) : SliderWidget(
    x, y, width, height,
    Text.empty(),
    ((value - min) / (max - min)).toDouble()
) {

    companion object {
        const val RESET_GUTTER = 56
    }

    private val trackBg = 0xFF151922.toInt()
    private val trackFill = 0xFF34C759.toInt()
    private val handleColor = 0xFFF6F7F9.toInt()
    private val handleBorder = 0x18000000
    private val textPrimary = 0xFFF6F7F9.toInt()
    private val textMuted = 0xFF98A2B3.toInt()
    private val resetIdle = 0xFF1C222C.toInt()
    private val resetHover = 0xFF34C759.toInt()
    private val resetTextIdle = 0xFF34C759.toInt()
    private val resetTextHover = 0xFF071109.toInt()

    private val resetBtnWidth = 52
    private val resetBtnHeight = 22
    private val resetGap = 8
    private val trackHeight = 6
    private val handleSize = 16

    private var currentValue = value
    private val step = 0.05f

    init {
        updateMessage()
    }

    override fun updateMessage() {
        val v = value.toFloat()
        currentValue = round((min + v * (max - min)) / step) * step
        message = Text.empty()
    }

    override fun applyValue() {
        val v = value.toFloat()
        currentValue = round((min + v * (max - min)) / step) * step
        onChange(currentValue)
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val textRenderer = MinecraftClient.getInstance().textRenderer

        val labelY = y + 2
        context.drawText(textRenderer, label, x, labelY, textPrimary, false)

        val displayValue = "%.2f".format(currentValue)
        val labelWidth = textRenderer.getWidth(label)
        context.drawText(
            textRenderer,
            Text.literal(displayValue),
            x + labelWidth + 8,
            labelY,
            textMuted,
            false
        )

        val trackCenterY = y + height - 10
        val pillRadius = trackHeight / 2
        val trackTop = trackCenterY - pillRadius
        UiPrimitives.fillRoundedRect(context, x, trackTop, width, trackHeight, pillRadius, trackBg)

        val filledWidth = (value * width).toInt().coerceIn(0, width)
        if (filledWidth > 0) {
            UiPrimitives.fillRoundedRect(context, x, trackTop, filledWidth, trackHeight, pillRadius, trackFill)
        }

        val handleCenterX = (x + filledWidth).coerceIn(x, x + width)
        val handleX = handleCenterX - handleSize / 2
        val handleY = trackCenterY - handleSize / 2
        UiPrimitives.fillRoundedRect(context, handleX, handleY, handleSize, handleSize, handleSize / 2, handleColor)
        UiPrimitives.drawRoundedBorder(context, handleX, handleY, handleSize, handleSize, handleSize / 2, handleBorder)

        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnHeight) / 2
        val resetHovered = mouseX >= resetX && mouseX <= resetX + resetBtnWidth &&
            mouseY >= resetY && mouseY <= resetY + resetBtnHeight

        val resetFill = if (resetHovered) resetHover else resetIdle
        val resetTextColor = if (resetHovered) resetTextHover else resetTextIdle
        UiPrimitives.fillRoundedRect(context, resetX, resetY, resetBtnWidth, resetBtnHeight, resetBtnHeight / 2, resetFill)
        UiPrimitives.drawRoundedBorder(context, resetX, resetY, resetBtnWidth, resetBtnHeight, resetBtnHeight / 2, handleBorder)

        val resetLabel = "RESET"
        val resetTextX = resetX + (resetBtnWidth - textRenderer.getWidth(resetLabel)) / 2
        val resetTextY = resetY + (resetBtnHeight - 8) / 2
        context.drawText(textRenderer, Text.literal(resetLabel), resetTextX, resetTextY, resetTextColor, false)
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val inSlider =
            mouseX >= x && mouseX < (x + width) &&
            mouseY >= y && mouseY < (y + height)

        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnHeight) / 2
        val inReset =
            mouseX >= resetX && mouseX < (resetX + resetBtnWidth) &&
            mouseY >= resetY && mouseY < (resetY + resetBtnHeight)

        return inSlider || inReset
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnHeight) / 2

        if (mouseX >= resetX && mouseX <= resetX + resetBtnWidth &&
            mouseY >= resetY && mouseY <= resetY + resetBtnHeight
        ) {
            reset()
        } else {
            super.onClick(mouseX, mouseY)
        }
    }

    fun reset() {
        currentValue = defaultValue
        onReset()
        value = ((currentValue - min) / (max - min)).coerceIn(0f, 1f).toDouble()
        updateMessage()
        applyValue()
    }
}
