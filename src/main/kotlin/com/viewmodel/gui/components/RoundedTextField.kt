package com.viewmodel.gui.components

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class RoundedTextField(
    private val font: TextRenderer,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val placeholder: Text
) : TextFieldWidget(font, x, y, width, height, placeholder) {

    init {
        setDrawsBackground(false)
        setEditableColor(ViewModelPalette.TEXT_PRIMARY)
        setUneditableColor(ViewModelPalette.TEXT_MUTED)
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val fillColor = if (isFocused) ViewModelPalette.SURFACE_HIGHLIGHT else ViewModelPalette.SURFACE_MUTED
        GuiPrimitives.fillRoundedRect(
            context,
            this.x.toFloat(),
            this.y.toFloat(),
            this.width.toFloat(),
            this.height.toFloat(),
            this.height / 2f,
            fillColor
        )

        super.renderWidget(context, mouseX, mouseY, delta)

        if (text.isEmpty() && !isFocused) {
            context.drawText(
                font,
                placeholder,
                this.x + 12,
                this.y + (this.height - font.fontHeight) / 2,
                ViewModelPalette.rgb(ViewModelPalette.TEXT_MUTED),
                false
            )
        }
    }
}
