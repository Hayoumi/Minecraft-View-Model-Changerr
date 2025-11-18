package com.viewmodel.gui

import com.viewmodel.ViewModelConfig
import com.viewmodel.ViewModelConfigManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import kotlin.math.max

class ViewModelScreen : Screen(Text.empty()) {

    private data class Bounds(val x: Int, val y: Int, val w: Int, val h: Int) {
        fun contains(mx: Double, my: Double): Boolean = mx >= x && mx <= x + w && my >= y && my <= y + h
        fun contains(mx: Int, my: Int): Boolean = contains(mx.toDouble(), my.toDouble())
    }

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
        const val ITEM_H = 24
        const val SPACING = 4
        const val PADDING = 12
        const val CONFIG_WIDTH = 170
        const val PANEL_GAP = 16
        const val LIST_ITEM_H = 18
        const val HEADER_SPACING = 34
    }

    private lateinit var nameField: TextFieldWidget
    private var currentName = ""
    private var allConfigs: List<String> = emptyList()

    private var contentStartY = 0
    private var panelX = 0
    private var panelTop = 30
    private var panelHeight = 0
    private var resetBounds = Bounds(0, 0, 0, 0)

    private var configBox = Bounds(0, 0, CONFIG_WIDTH, 0)
    private var dropdownBounds = Bounds(0, 0, 0, 0)
    private var dropdownMenuBounds: Bounds? = null
    private var dropdownItems: List<Pair<String, Bounds>> = emptyList()
    private var configMenuOpen = false

    override fun init() {
        super.init()
        clearChildren()
        sliders.clear()
        toggles.clear()
        buttons.clear()
        configMenuOpen = false
        dropdownItems = emptyList()

        currentName = ViewModelConfigManager.currentName
        allConfigs = ViewModelConfigManager.getConfigNames()

        val totalWidth = CONFIG_WIDTH + PANEL_GAP + WIDTH
        val startX = (width - totalWidth) / 2
        panelX = startX + CONFIG_WIDTH + PANEL_GAP
        contentStartY = panelTop + HEADER_SPACING

        var y = contentStartY

        // Size
        y += addSlider(
            panelX, y, "Size",
            ViewModelConfig.current.size, 0.1f, 3.0f, 1.0f,
            { ViewModelConfig.current.size = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.size = 1.0f; ViewModelConfigManager.saveCurrent() }
        )

        // Position
        y += addSeparator(y, "Position")
        y += addSlider(
            panelX, y, "X",
            ViewModelConfig.current.positionX, -100f, 100f, 0f,
            { ViewModelConfig.current.positionX = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionX = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            panelX, y, "Y",
            ViewModelConfig.current.positionY, -100f, 100f, 0f,
            { ViewModelConfig.current.positionY = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionY = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            panelX, y, "Z",
            ViewModelConfig.current.positionZ, -100f, 100f, 0f,
            { ViewModelConfig.current.positionZ = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.positionZ = 0f; ViewModelConfigManager.saveCurrent() }
        )

        // Rotation
        y += addSeparator(y, "Rotation")
        y += addSlider(
            panelX, y, "Yaw",
            ViewModelConfig.current.rotationYaw, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationYaw = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationYaw = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            panelX, y, "Pitch",
            ViewModelConfig.current.rotationPitch, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationPitch = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationPitch = 0f; ViewModelConfigManager.saveCurrent() }
        )
        y += addSlider(
            panelX, y, "Roll",
            ViewModelConfig.current.rotationRoll, -180f, 180f, 0f,
            { ViewModelConfig.current.rotationRoll = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.rotationRoll = 0f; ViewModelConfigManager.saveCurrent() }
        )

        // Animation
        y += addSeparator(y, "Animation")
        y += addToggle(
            panelX, y, "Scale Swing",
            ViewModelConfig.current.scaleSwing
        ) { ViewModelConfig.current.scaleSwing = it; ViewModelConfigManager.saveCurrent() }
        y += addToggle(
            panelX, y, "No Swing",
            ViewModelConfig.current.noSwing
        ) { ViewModelConfig.current.noSwing = it; ViewModelConfigManager.saveCurrent() }

        val contentBottom = (sliders.map { it.y + it.height } + toggles.map { it.y + it.height })
            .maxOrNull() ?: contentStartY
        panelHeight = (contentBottom - panelTop) + PADDING + 8

        setupConfigControls(startX)

        sliders.forEach { addDrawableChild(it) }
        toggles.forEach { addDrawableChild(it) }
        buttons.forEach { addDrawableChild(it) }
        addDrawableChild(nameField)
    }

    private fun addSlider(
        x: Int, y: Int, label: String,
        value: Float, min: Float, max: Float, default: Float,
        onChange: (Float) -> Unit, onReset: () -> Unit
    ): Int {
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

    @Suppress("UNUSED_PARAMETER")
    private fun addSeparator(y: Int, title: String): Int {
        // просто сдвигаем Y, сами секции рисуем в render()
        return 14
    }

    private fun setupConfigControls(configX: Int) {
        val padding = 10
        val dropdownHeight = 18
        val fieldHeight = 16
        val buttonHeight = 18
        val verticalGap = 8

        val configHeight = padding + 12 + dropdownHeight + verticalGap + 12 + fieldHeight + verticalGap + buttonHeight * 2 + 4 + padding
        val top = panelTop + max(0, (panelHeight - configHeight) / 2)
        configBox = Bounds(configX, top, CONFIG_WIDTH, configHeight)

        val dropdownY = top + padding + 14
        dropdownBounds = Bounds(configX + padding, dropdownY, CONFIG_WIDTH - padding * 2, dropdownHeight)

        val nameLabelY = dropdownY + dropdownHeight + verticalGap
        nameField = TextFieldWidget(textRenderer, configX + padding, nameLabelY + 12, CONFIG_WIDTH - padding * 2, fieldHeight, Text.empty())
        nameField.text = currentName
        nameField.setEditableColor(TEXT)

        val buttonsY = nameField.y + fieldHeight + verticalGap
        val buttonWidth = (CONFIG_WIDTH - padding * 2 - SPACING) / 2
        buttons.add(
            CompactButton(configX + padding, buttonsY, buttonWidth, buttonHeight, Text.literal("New")) { createConfig() }
        )
        val rename = CompactButton(
            configX + padding + buttonWidth + SPACING,
            buttonsY,
            buttonWidth,
            buttonHeight,
            Text.literal("Rename")
        ) { renameConfig() }
        val delete = CompactButton(
            configX + padding,
            buttonsY + buttonHeight + 4,
            CONFIG_WIDTH - padding * 2,
            buttonHeight,
            Text.literal("Delete")
        ) { deleteConfig() }

        val canModify = !ViewModelConfigManager.isDefault(currentName) && allConfigs.size > 1
        rename.active = canModify
        delete.active = canModify

        buttons.add(rename)
        buttons.add(delete)
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
            allConfigs = ViewModelConfigManager.getConfigNames()
            nameField.text = requested
            configMenuOpen = false
        }
    }

    private fun renameConfig() {
        if (ViewModelConfigManager.renameConfig(currentName, nameField.text)) {
            currentName = ViewModelConfigManager.currentName
            allConfigs = ViewModelConfigManager.getConfigNames()
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun deleteConfig() {
        if (ViewModelConfigManager.deleteConfig(currentName)) {
            currentName = ViewModelConfigManager.currentName
            allConfigs = ViewModelConfigManager.getConfigNames()
            client?.setScreen(ViewModelScreen())
        }
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
        renderConfigCard(context, mouseX, mouseY)
        renderPanel(context)

        super.render(context, mouseX, mouseY, delta)

        if (configMenuOpen) {
            renderDropdown(context, mouseX, mouseY)
        }

        renderResetButton(context, mouseX, mouseY)
    }

    private fun renderPanel(context: DrawContext) {
        val panelBottom = panelTop + panelHeight
        context.fill(panelX, panelTop, panelX + WIDTH, panelBottom, PANEL)
        drawBorder(context, panelX, panelTop, WIDTH, panelHeight)

        val title = Text.literal("ViewModel").styled { it.withBold(true) }
        val titleY = panelTop + 10
        context.drawText(
            textRenderer,
            title,
            panelX + WIDTH / 2 - textRenderer.getWidth(title) / 2,
            titleY,
            ACCENT,
            false
        )

        context.fill(panelX + PADDING, titleY + 12, panelX + WIDTH - PADDING, titleY + 13, BORDER)

        var sectionY = contentStartY
        sectionY = renderSectionTitle(context, panelX, sectionY, "Transform")
        sectionY += ITEM_H + SPACING

        sectionY = renderSectionTitle(context, panelX, sectionY, "Position")
        sectionY += (ITEM_H + SPACING) * 3

        sectionY = renderSectionTitle(context, panelX, sectionY, "Rotation")
        sectionY += (ITEM_H + SPACING) * 3

        renderSectionTitle(context, panelX, sectionY, "Animation")
    }

    private fun renderConfigCard(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.fill(configBox.x, configBox.y, configBox.x + configBox.w, configBox.y + configBox.h, CARD)
        drawBorder(context, configBox.x, configBox.y, configBox.w, configBox.h)

        context.drawText(
            textRenderer,
            Text.literal("Config").styled { it.withBold(true) },
            configBox.x + 10,
            configBox.y + 6,
            TEXT_DIM,
            false
        )

        context.drawText(
            textRenderer,
            Text.literal("Profile"),
            dropdownBounds.x,
            dropdownBounds.y - 10,
            TEXT_DIM,
            false
        )

        drawButtonLike(context, dropdownBounds, currentName, dropdownBounds.contains(mouseX, mouseY), configMenuOpen)

        context.drawText(
            textRenderer,
            Text.literal("Name"),
            nameField.x,
            nameField.y - 10,
            TEXT_DIM,
            false
        )
    }

    private fun renderDropdown(context: DrawContext, mouseX: Int, mouseY: Int) {
        val listX = dropdownBounds.x
        val width = dropdownBounds.w
        val totalHeight = if (allConfigs.isNotEmpty()) allConfigs.size * (LIST_ITEM_H + 2) - 2 else 0
        if (totalHeight > 0) {
            val bgTop = dropdownBounds.y + dropdownBounds.h + 2
            val bgHeight = totalHeight + 4
            context.fill(listX, bgTop, listX + width, bgTop + bgHeight, CARD)
            drawBorder(context, listX, bgTop, width, bgHeight)
        }

        var listY = dropdownBounds.y + dropdownBounds.h + 4
        val items = mutableListOf<Pair<String, Bounds>>()

        allConfigs.forEach { config ->
            val bounds = Bounds(listX, listY, width, LIST_ITEM_H)
            val hovered = bounds.contains(mouseX, mouseY)
            drawButtonLike(context, bounds, config, hovered, config == currentName)
            items.add(config to bounds)
            listY += LIST_ITEM_H + 2
        }

        dropdownMenuBounds = Bounds(listX, dropdownBounds.y + dropdownBounds.h + 4, width, max(0, totalHeight))
        dropdownItems = items
    }

    private fun drawButtonLike(context: DrawContext, bounds: Bounds, label: String, hovered: Boolean, selected: Boolean) {
        val fillColor = when {
            hovered -> ACCENT
            selected -> 0xFF2F2F2F.toInt()
            else -> CARD
        }
        val textColor = if (hovered) 0xFF000000.toInt() else TEXT

        context.fill(bounds.x, bounds.y, bounds.x + bounds.w, bounds.y + bounds.h, fillColor)
        drawBorder(context, bounds.x, bounds.y, bounds.w, bounds.h)

        val tx = bounds.x + (bounds.w - textRenderer.getWidth(label)) / 2
        val ty = bounds.y + (bounds.h - 8) / 2
        context.drawText(textRenderer, Text.literal(label), tx, ty, textColor, false)
    }

    private fun renderSectionTitle(context: DrawContext, x: Int, y: Int, title: String): Int {
        context.drawText(
            textRenderer,
            Text.literal(title).styled { it.withBold(true) },
            x + PADDING,
            y - 18,
            TEXT_DIM,
            false
        )
        return y + 12
    }

    private fun renderResetButton(context: DrawContext, mouseX: Int, mouseY: Int) {
        val btnW = 120
        val btnH = 24
        val btnX = panelX + WIDTH - btnW - PADDING
        val btnY = panelTop + panelHeight + 10

        resetBounds = Bounds(btnX, btnY, btnW, btnH)

        val hovered = resetBounds.contains(mouseX.toDouble(), mouseY.toDouble())
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
        if (dropdownBounds.contains(mouseX, mouseY)) {
            configMenuOpen = !configMenuOpen
            return true
        }

        if (configMenuOpen) {
            dropdownItems.firstOrNull { it.second.contains(mouseX, mouseY) }?.let {
                configMenuOpen = false
                selectConfig(it.first)
                return true
            }

            val menuBounds = dropdownMenuBounds
            if (menuBounds != null && !menuBounds.contains(mouseX, mouseY)) {
                configMenuOpen = false
            }
        }

        if (resetBounds.contains(mouseX, mouseY)) {
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
