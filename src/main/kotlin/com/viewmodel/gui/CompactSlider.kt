package com.viewmodel.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text
import net.minecraft.client.MinecraftClient
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

    private val TRACK_BG = 0xFF3A3A3A.toInt()
    private val TRACK_FILL = 0xFFFFFFFF.toInt()
    private val HANDLE = 0xFFFFFFFF.toInt()
    private val TEXT = 0xFFE0E0E0.toInt()
    private val TEXT_DIM = 0xFFA0A0A0.toInt()
    private val RESET_BTN = 0xFF3A3A3A.toInt()
    private val RESET_HOVER = 0xFF505050.toInt()

    private var currentValue = value
    private val resetBtnSize = 18
    private val resetGap = 6
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
        val mc = MinecraftClient.getInstance()
        val textRenderer = mc.textRenderer

        // Лейбл
        context.drawText(textRenderer, label, x, y, TEXT, false)

        // Значение справа от лейбла
        val displayValue = "%.2f".format(currentValue)
        val labelWidth = textRenderer.getWidth(label)
        context.drawText(
            textRenderer,
            Text.literal(displayValue),
            x + labelWidth + 8,
            y,
            TEXT_DIM,
            false
        )

        // Трек на ширину виджета
        val trackY = y + 14
        val trackH = 3
        val trackLeft = x
        val trackRight = x + width
        context.fill(trackLeft, trackY, trackRight, trackY + trackH, TRACK_BG)

        // Ручка как в ванильном SliderWidget
        val innerWidth = width - 8
        val handleLeft = (x + value * innerWidth).toInt()
        val handleCenterX = handleLeft + 4

        // Заполнение до центра ручки
        if (handleCenterX > trackLeft) {
            context.fill(trackLeft, trackY, handleCenterX, trackY + trackH, TRACK_FILL)
        }

        // Круглая ручка
        val handleRadius = 4
        drawCircle(context, handleCenterX, trackY + trackH / 2, handleRadius, HANDLE)

        // Кнопка сброса — справа от слайдера, но в зарезервированном пространстве панели
        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnSize) / 2
        val resetHovered = mouseX >= resetX && mouseX <= resetX + resetBtnSize &&
                mouseY >= resetY && mouseY <= resetY + resetBtnSize

        val resetColor = if (resetHovered) RESET_HOVER else RESET_BTN
        context.fill(resetX, resetY, resetX + resetBtnSize, resetY + resetBtnSize, resetColor)

        // Центруем "⟲"
        val emoji = "⟲"
        val emojiWidth = textRenderer.getWidth(emoji)
        val emojiX = resetX + (resetBtnSize - emojiWidth) / 2
        val emojiY = resetY + (resetBtnSize - textRenderer.fontHeight) / 2
        context.drawText(textRenderer, Text.literal(emoji), emojiX, emojiY, TEXT, false)
    }

    // расширяем область клика: и слайдер, и кнопка
    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val inSlider =
            mouseX >= x && mouseX < (x + width) &&
            mouseY >= y && mouseY < (y + height)

        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnSize) / 2
        val inReset =
            mouseX >= resetX && mouseX < (resetX + resetBtnSize) &&
            mouseY >= resetY && mouseY < (resetY + resetBtnSize)

        return inSlider || inReset
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        val resetX = x + width + resetGap
        val resetY = y + (height - resetBtnSize) / 2

        if (mouseX >= resetX && mouseX <= resetX + resetBtnSize &&
            mouseY >= resetY && mouseY <= resetY + resetBtnSize
        ) {
            reset()
        } else {
            super.onClick(mouseX, mouseY)
        }
    }

    fun reset() {
        // ставим дефолт, дергаем onReset и обновляем слайдер
        currentValue = defaultValue
        onReset()
        value = ((currentValue - min) / (max - min)).coerceIn(0f, 1f).toDouble()
        updateMessage()
        applyValue()
    }

    private fun drawCircle(context: DrawContext, cx: Int, cy: Int, r: Int, color: Int) {
        context.fill(cx - r, cy - r, cx + r, cy + r, color)
    }
}
