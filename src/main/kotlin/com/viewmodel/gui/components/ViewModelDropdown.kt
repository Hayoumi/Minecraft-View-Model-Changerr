package com.viewmodel.gui.components

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import kotlin.math.floor
import kotlin.math.max

class ViewModelDropdown(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val placeholder: Text,
    private val selectedSupplier: () -> String,
    private val onSelect: (String) -> Unit
) : ClickableWidget(x, y, width, height, placeholder) {

    private var options: List<String> = emptyList()
    private var expanded = false
    private var scrollIndex = 0

    private val rowHeight = 28
    private val maxVisibleRows = 5
    private val listSpacing = 6

    fun setOptions(values: List<String>) {
        options = values
        scrollIndex = scrollIndex.coerceIn(0, max(0, options.size - maxVisibleRows))
    }

    fun collapse() {
        expanded = false
    }

    fun isExpanded(): Boolean = expanded

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        GuiPrimitives.fillRoundedRect(
            context,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            height / 2f,
            ViewModelPalette.PANEL_ACCENT
        )

        val current = selectedSupplier()
        val display = current.ifEmpty { placeholder.string }
        val textColor = if (current.isEmpty()) ViewModelPalette.TEXT_MUTED else ViewModelPalette.TEXT_PRIMARY
        val shown = clippedText(display, width - 38)
        context.drawText(
            textRenderer,
            Text.literal(shown),
            x + 16,
            y + (height - textRenderer.fontHeight) / 2,
            ViewModelPalette.rgb(textColor),
            false
        )

        drawChevron(context)

        if (expanded) {
            renderDropdown(context, mouseX, mouseY)
        }
    }

    private fun drawChevron(context: DrawContext) {
        val centerX = x + width - 22
        val centerY = y + height / 2 - 1
        val color = ViewModelPalette.NEUTRAL
        context.fill(centerX - 4, centerY, centerX - 1, centerY + 2, color)
        context.fill(centerX, centerY + 2, centerX + 3, centerY + 4, color)
    }

    private fun renderDropdown(context: DrawContext, mouseX: Int, mouseY: Int) {
        val rows = options.drop(scrollIndex).take(maxVisibleRows)
        if (rows.isEmpty()) return

        val listTop = y + height + listSpacing
        val listHeight = rows.size * rowHeight + 16

        GuiPrimitives.fillRoundedRect(
            context,
            x.toFloat(),
            listTop.toFloat(),
            width.toFloat(),
            listHeight.toFloat(),
            24f,
            ViewModelPalette.PANEL
        )

        var currentY = listTop + 8
        rows.forEachIndexed { index, option ->
            val hovered = mouseX in x..(x + width) && mouseY in currentY..(currentY + rowHeight)
            val highlightColor = if (hovered) ViewModelPalette.SURFACE_HIGHLIGHT else ViewModelPalette.PANEL_ACCENT

            GuiPrimitives.fillRoundedRect(
                context,
                x + 6f,
                currentY.toFloat(),
                (width - 12).toFloat(),
                (rowHeight - 4).toFloat(),
                (rowHeight - 4) / 2f,
                highlightColor
            )

            val color = if (option == selectedSupplier()) ViewModelPalette.ACCENT else ViewModelPalette.TEXT_PRIMARY
            context.drawText(
                textRenderer,
                Text.literal(option),
                x + 18,
                currentY + (rowHeight - textRenderer.fontHeight) / 2,
                ViewModelPalette.rgb(color),
                false
            )

            currentY += rowHeight
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)
        expanded = !expanded
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (expanded) {
            val option = optionAt(mouseX, mouseY)
            if (option != null) {
                expanded = false
                onSelect(option)
                return true
            }
            if (!isInsideBase(mouseX, mouseY) && !isInsideDropdown(mouseX, mouseY)) {
                expanded = false
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!expanded) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        val listTop = y + height + listSpacing
        val listBottom = listTop + maxVisibleRows * rowHeight
        if (mouseX < x || mouseX > x + width || mouseY < listTop || mouseY > listBottom) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }

        val next = scrollIndex - verticalAmount.toInt()
        scrollIndex = next.coerceIn(0, max(0, options.size - maxVisibleRows))
        return true
    }

    fun isInsideDropdown(mouseX: Double, mouseY: Double): Boolean {
        if (!expanded) return false
        val listTop = y + height + listSpacing
        val listBottom = listTop + maxVisibleRows * rowHeight + 16
        return mouseX >= x && mouseX <= x + width && mouseY >= listTop && mouseY <= listBottom
    }

    private fun isInsideBase(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    private fun optionAt(mouseX: Double, mouseY: Double): String? {
        if (!expanded) return null
        if (mouseX < x || mouseX > x + width) return null

        val listTop = y + height + listSpacing + 8
        val relativeY = mouseY - listTop
        if (relativeY < 0) return null

        val index = floor(relativeY / rowHeight).toInt()
        val actual = scrollIndex + index
        if (index < 0 || actual !in options.indices) return null

        return options[actual]
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        val current = selectedSupplier()
        val spoken = if (current.isEmpty()) placeholder else Text.literal(current)
        builder.put(NarrationPart.TITLE, spoken)
    }

    private fun clippedText(text: String, maxWidth: Int): String {
        if (maxWidth <= 0 || textRenderer.getWidth(text) <= maxWidth) return text
        val ellipsis = "..."
        val ellipsisWidth = textRenderer.getWidth(ellipsis)
        var result = text
        while (result.isNotEmpty() && textRenderer.getWidth(result) + ellipsisWidth > maxWidth) {
            result = result.dropLast(1)
        }
        return if (result.isEmpty()) text else result + ellipsis
    }
}
