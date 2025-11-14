package com.viewmodel.gui

import com.viewmodel.ViewModelConfig
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ViewModelScreen : Screen(Text.empty()) {

    private val sliders = mutableListOf<CompactSlider>()
    private val toggles = mutableListOf<CompactToggle>()
    
    // Палитра
    private val PANEL = 0xFF1A1A1A.toInt()
    private val CARD = 0xFF252525.toInt()
    private val ACCENT = 0xFFFFFFFF.toInt()
    private val TEXT = 0xFFE0E0E0.toInt()
    private val TEXT_DIM = 0xFF808080.toInt()
    private val BORDER = 0xFF3A3A3A.toInt()

    companion object {
        const val WIDTH = 300
        const val ITEM_H = 28
        const val SPACING = 6
        const val PADDING = 14
    }

    override fun init() {
        super.init()
        clearChildren()
        sliders.clear()
        toggles.clear()

        val x = (width - WIDTH) / 2
        var y = 56

        // Size
        y += addSlider(
            x, y, "Size",
            ViewModelConfig.current.size, 0.1f, 3.0f, 1.0f,
            { ViewModelConfig.current.size = it },
            { ViewModelConfig.current.size = 1.0f }
        )
        
        // Position
        y += addSeparator(y, "Position")
        y += addSlider(
            x, y, "X",
            ViewModelConfig.current.positionX, -100f, 100f, 0f,
            { ViewModelConfig.current.positionX = it },
            { ViewModelConfig.current.positionX = 0f }
        )
        y += addSlider(
            x, y, "Y",
            ViewModelConfig.current.positionY, -100f, 100f, 0f,
            { ViewModelConfig.current.positionY = it },
            { ViewModelConfig.current.positionY = 0f }
        )
        y += addSlider(
            x, y, "Z",
            ViewModelConfig.current.positionZ, -100f, 100f, 0f,
            { ViewModelConfig.current.positionZ = it },
            { ViewModelConfig.current.positionZ = 0f }
        )
        
        // Rotation
        y += addSeparator(y, "Rotation")
        y += addSlider(
            x, y, "Yaw",
            ViewModelConfig.current.rotationYaw, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationYaw = it },
            { ViewModelConfig.current.rotationYaw = 0f }
        )
        y += addSlider(
            x, y, "Pitch",
            ViewModelConfig.current.rotationPitch, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationPitch = it },
            { ViewModelConfig.current.rotationPitch = 0f }
        )
        y += addSlider(
            x, y, "Roll",
            ViewModelConfig.current.rotationRoll, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationRoll = it },
            { ViewModelConfig.current.rotationRoll = 0f }
        )
        
        // Animation
        y += addSeparator(y, "Animation")
        y += addToggle(
            x, y, "Scale Swing",
            ViewModelConfig.current.scaleSwing
        ) { ViewModelConfig.current.scaleSwing = it }
        y += addToggle(
            x, y, "No Swing",
            ViewModelConfig.current.noSwing
        ) { ViewModelConfig.current.noSwing = it }

        sliders.forEach { addDrawableChild(it) }
        toggles.forEach { addDrawableChild(it) }
    }

    private fun addSlider(
        x: Int, y: Int, label: String,
        value: Float, min: Float, max: Float, default: Float,
        onChange: (Float) -> Unit, onReset: () -> Unit
    ): Int {
        // ширина слайдера — минус место под кнопку сброса
        sliders.add(
            CompactSlider(
                x + PADDING,
                y,
                WIDTH - PADDING * 2 - 24,
                ITEM_H,
                Text.literal(label),
                value,
                min,
                max,
                default,
                onChange,
                onReset
            )
        )
        return ITEM_H + SPACING
    }

    private fun addToggle(
        x: Int, y: Int, label: String,
        value: Boolean,
        onChange: (Boolean) -> Unit
    ): Int {
        toggles.add(
            CompactToggle(
                x + PADDING,
                y,
                WIDTH - PADDING * 2,
                ITEM_H,
                Text.literal(label),
                value,
                onChange
            )
        )
        return ITEM_H + SPACING
    }

    private fun addSeparator(y: Int, title: String): Int {
        // просто сдвигаем Y, сами секции рисуем в render()
        return 20
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val x = (width - WIDTH) / 2
        val panelHeight = height - 56
        
        // фон панели
        context.fill(x, 18, x + WIDTH, 18 + panelHeight, PANEL)
        drawBorder(context, x, 18, WIDTH, panelHeight)
        
        // заголовок без тени
        context.drawText(
            textRenderer,
            Text.literal("ViewModel").styled { it.withBold(true) },
            x + WIDTH / 2 - textRenderer.getWidth("ViewModel") / 2,
            28,
            ACCENT,
            false
        )
        
        // линия под заголовком
        context.fill(x + 20, 42, x + WIDTH - 20, 43, BORDER)
        
        // секции: Y синхронизирован со слайдерами (первый слайдер тоже на 56)
        var sectionY = 56
        sectionY = renderSectionTitle(context, x, sectionY, "Transform")
        sectionY += ITEM_H + SPACING
        
        sectionY = renderSectionTitle(context, x, sectionY, "Position")
        sectionY += (ITEM_H + SPACING) * 3
        
        sectionY = renderSectionTitle(context, x, sectionY, "Rotation")
        sectionY += (ITEM_H + SPACING) * 3
        
        renderSectionTitle(context, x, sectionY, "Animation")
        
        // сами виджеты
        super.render(context, mouseX, mouseY, delta)
        
        // нижняя кнопка Reset All
        renderResetButton(context, mouseX, mouseY)
    }

    private fun renderSectionTitle(context: DrawContext, x: Int, y: Int, title: String): Int {
        // y — это Y первого элемента секции
        // заголовок рисуем чуть выше него, с нормальным отступом
        context.drawText(
            textRenderer,
            Text.literal(title).styled { it.withBold(true) },
            x + PADDING,
            y - 24,          // ↑ расстояние между заголовком и первым слайдером
            TEXT_DIM,
            false
        )
        return y + 20       // сдвиг "базы" для следующей секции
    }

    private fun renderResetButton(context: DrawContext, mouseX: Int, mouseY: Int) {
        val btnW = 130
        val btnH = 26
        val btnX = (width - btnW) / 2
        val btnY = height - 36
        
        val hovered = mouseX >= btnX && mouseX <= btnX + btnW &&
                      mouseY >= btnY && mouseY <= btnY + btnH
        
        val btnColor = if (hovered) ACCENT else CARD
        val textColor = if (hovered) 0xFF000000.toInt() else TEXT
        
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnColor)
        drawBorder(context, btnX, btnY, btnW, btnH)
        
        context.drawText(
            textRenderer,
            Text.literal("Reset All"),
            btnX + (btnW - textRenderer.getWidth("Reset All")) / 2,
            btnY + (btnH - 8) / 2,
            textColor,
            false
        )
    }

    private fun drawBorder(context: DrawContext, x: Int, y: Int, w: Int, h: Int) {
        context.fill(x, y, x + w, y + 1, BORDER)
        context.fill(x, y + h - 1, x + w, y + h, BORDER)
        context.fill(x, y, x + 1, y + h, BORDER)
        context.fill(x + w - 1, y, x + w, y + h, BORDER)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val btnW = 130
        val btnH = 26
        val btnX = (width - btnW) / 2
        val btnY = height - 36
        
        if (mouseX >= btnX && mouseX <= btnX + btnW &&
            mouseY >= btnY && mouseY <= btnY + btnH
        ) {
            sliders.forEach { it.reset() }
            toggles.forEach { it.reset() }
            client?.setScreen(ViewModelScreen())
            return true
        }
        
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun shouldPause() = false

    override fun close() {
        ViewModelConfig.save()
        super.close()
    }
}
