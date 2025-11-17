package com.viewmodel.gui

import com.viewmodel.ViewModelConfig
import com.viewmodel.ViewModelConfigManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ViewModelScreen : Screen(Text.empty()) {

    private val sliders = mutableListOf<CompactSlider>()
    private val toggles = mutableListOf<CompactToggle>()
    private val buttons = mutableListOf<CompactButton>()

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
        const val DRAWER_WIDTH = 200
        const val DRAWER_HEIGHT_OFFSET = 36
        const val LIST_ITEM_H = 20
    }

    private lateinit var nameField: TextFieldWidget
    private var contentStartY = 80
    private var currentName = ""
    private var allConfigs: List<String> = emptyList()

    override fun init() {
        super.init()
        clearChildren()
        sliders.clear()
        toggles.clear()
        buttons.clear()

        currentName = ViewModelConfigManager.currentName
        allConfigs = ViewModelConfigManager.getConfigNames()

        val drawerX = 28
        setupConfigControls(drawerX)

        val x = (width - WIDTH) / 2
        var y = contentStartY

        // Size
        y += addSlider(
            x, y, "Size",
            ViewModelConfig.current.size, 0.1f, 3.0f, 1.0f,
            { ViewModelConfig.current.size = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.size = 1.0f; ViewModelConfigManager.saveCurrent() }
        )
        
        // Position
        y += addSeparator(y, "Position")
        y += addSlider(
            x, y, "X",
            ViewModelConfig.current.positionX, -100f, 100f, 0f,
            { ViewModelConfig.current.positionX = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionX = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            x, y, "Y",
            ViewModelConfig.current.positionY, -100f, 100f, 0f,
            { ViewModelConfig.current.positionY = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionY = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            x, y, "Z",
            ViewModelConfig.current.positionZ, -100f, 100f, 0f,
            { ViewModelConfig.current.positionZ = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionZ = 0f; ViewModelConfigManager.saveCurrent() }
        )
        
        // Rotation
        y += addSeparator(y, "Rotation")
        y += addSlider(
            x, y, "Yaw",
            ViewModelConfig.current.rotationYaw, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationYaw = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationYaw = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            x, y, "Pitch",
            ViewModelConfig.current.rotationPitch, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationPitch = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationPitch = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            x, y, "Roll",
            ViewModelConfig.current.rotationRoll, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationRoll = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationRoll = 0f; ViewModelConfigManager.saveCurrent() }
        )
        
        // Animation
        y += addSeparator(y, "Animation")
        y += addToggle(
            x, y, "Scale Swing",
            ViewModelConfig.current.scaleSwing
        ) { ViewModelConfig.current.scaleSwing = it; ViewModelConfigManager.saveCurrent() }
        y += addToggle(
            x, y, "No Swing",
            ViewModelConfig.current.noSwing
        ) { ViewModelConfig.current.noSwing = it; ViewModelConfigManager.saveCurrent() }

        sliders.forEach { addDrawableChild(it) }
        toggles.forEach { addDrawableChild(it) }
        buttons.forEach { addDrawableChild(it) }
        addSelectableChild(nameField)
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

    private fun setupConfigControls(drawerX: Int) {
        val drawerY = 28
        val fieldWidth = DRAWER_WIDTH - PADDING * 2
        contentStartY = 84

        val nameY = drawerY + 36
        nameField = TextFieldWidget(textRenderer, drawerX + PADDING, nameY, fieldWidth, 16, Text.empty())
        nameField.text = currentName
        nameField.setEditableColor(TEXT)

        val buttonWidth = (DRAWER_WIDTH - PADDING * 2 - SPACING) / 2
        val controlsY = nameY + 22
        buttons.add(
            CompactButton(drawerX + PADDING, controlsY, buttonWidth, 18, Text.literal("New")) { createConfig() }
        )
        val rename = CompactButton(
            drawerX + PADDING + buttonWidth + SPACING,
            controlsY,
            buttonWidth,
            18,
            Text.literal("Rename")
        ) { renameConfig() }
        val delete = CompactButton(
            drawerX + PADDING,
            controlsY + 22,
            DRAWER_WIDTH - PADDING * 2,
            18,
            Text.literal("Delete")
        ) { deleteConfig() }

        val canModify = !ViewModelConfigManager.isDefault(currentName) && allConfigs.size > 1
        rename.active = canModify
        delete.active = canModify

        buttons.add(rename)
        buttons.add(delete)

        var listY = controlsY + 22 + 26
        allConfigs.forEach { config ->
            val entry = CompactButton(
                drawerX + PADDING,
                listY,
                DRAWER_WIDTH - PADDING * 2,
                LIST_ITEM_H,
                Text.literal(config),
                selected = config == currentName
            ) { selectConfig(config) }
            listY += LIST_ITEM_H + 4
            buttons.add(entry)
        }
    }

    private fun renderConfigDrawer(context: DrawContext, drawerX: Int, mouseX: Int, mouseY: Int) {
        val drawerY = 28
        val drawerHeight = height - DRAWER_HEIGHT_OFFSET
        context.fill(drawerX, drawerY, drawerX + DRAWER_WIDTH, drawerY + drawerHeight, CARD)
        drawBorder(context, drawerX, drawerY, DRAWER_WIDTH, drawerHeight)

        context.drawText(
            textRenderer,
            Text.literal("Configs").styled { it.withBold(true) },
            drawerX + PADDING,
            drawerY - 10,
            TEXT_DIM,
            false
        )

        context.drawText(
            textRenderer,
            Text.literal("Name"),
            drawerX + PADDING,
            48,
            TEXT_DIM,
            false
        )

        nameField.render(context, mouseX, mouseY, 0f)
    }

    private fun selectConfig(name: String) {
        if (ViewModelConfigManager.setActive(name)) {
            currentName = ViewModelConfigManager.currentName
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun createConfig() {
        val requested = nameField.text.ifBlank { "New" }
        if (ViewModelConfigManager.createConfig(requested)) {
            currentName = ViewModelConfigManager.currentName
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun renameConfig() {
        if (ViewModelConfigManager.renameConfig(currentName, nameField.text)) {
            currentName = ViewModelConfigManager.currentName
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun deleteConfig() {
        if (ViewModelConfigManager.deleteConfig(currentName)) {
            currentName = ViewModelConfigManager.currentName
            client?.setScreen(ViewModelScreen())
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val drawerX = 28
        val x = (width - WIDTH) / 2
        val panelHeight = height - 56

        renderConfigDrawer(context, drawerX, mouseX, mouseY)

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

        // секции: Y синхронизирован со слайдерами (первый слайдер стартует после конфигов)
        var sectionY = contentStartY
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
        ViewModelConfigManager.saveCurrent()
        super.close()
    }
}
