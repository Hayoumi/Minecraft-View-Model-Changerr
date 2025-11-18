package com.viewmodel.gui

import com.viewmodel.ViewModelConfig
import com.viewmodel.ViewModelConfigManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget as ConfigTextFieldWidget
import net.minecraft.text.Text
import kotlin.math.max

class ViewModelScreen : Screen(Text.empty()) {

    private data class Bounds(val x: Int, val y: Int, val w: Int, val h: Int) {
        fun contains(mx: Double, my: Double): Boolean = mx >= x && mx <= x + w && my >= y && my <= y + h
        fun contains(mx: Int, my: Int): Boolean = contains(mx.toDouble(), my.toDouble())
    }

    private data class SectionHeader(val title: String, val y: Int)

    private val sliders = mutableListOf<CompactSlider>()
    private val toggles = mutableListOf<CompactToggle>()
    private val buttons = mutableListOf<CompactButton>()

    // Палитра
    private val PANEL = 0xF0171A1F.toInt()
    private val CARD = 0xF01D2026.toInt()
    private val ACCENT = 0xFF0A84FF.toInt()
    private val TEXT = 0xFFF5F7FA.toInt()
    private val TEXT_DIM = 0xFF9EA3AA.toInt()
    private val BORDER = 0xFF2E3239.toInt()
    private val BORDER_SOFT = 0x40333B45

    companion object {
        const val WIDTH = 352
        const val ITEM_H = 26
        const val SPACING = 6
        const val PADDING = 14
        const val CONFIG_WIDTH = 204
        const val PANEL_GAP = 18
        const val LIST_ITEM_H = 18
        const val HEADER_SPACING = 36
        const val RADIUS = 12
        const val SECTION_LABEL_OFFSET = 12
        const val SECTION_GAP = 18
    }

    private lateinit var nameField: ConfigTextFieldWidget
    private var currentName = ""
    private var allConfigs: List<String> = emptyList()

    private val sectionHeaders = mutableListOf<SectionHeader>()

    private var contentStartY = 0
    private var panelX = 0
    private var panelTop = 32
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
        sectionHeaders.clear()
        configMenuOpen = false
        dropdownItems = emptyList()

        currentName = ViewModelConfigManager.currentName
        allConfigs = ViewModelConfigManager.getConfigNames()

        val totalWidth = CONFIG_WIDTH + PANEL_GAP + WIDTH
        val startX = (width - totalWidth) / 2
        panelX = startX + CONFIG_WIDTH + PANEL_GAP

        val titleY = panelTop + 16
        contentStartY = titleY + HEADER_SPACING

        var y = contentStartY

        addSectionHeader("Transform", y - SECTION_LABEL_OFFSET)
        y += addSlider(
            panelX, y, "Size",
            ViewModelConfig.current.size, 0.1f, 3.0f, 1.0f,
            { ViewModelConfig.current.size = it; ViewModelConfigManager.saveCurrent() },
            { ViewModelConfig.current.size = 1.0f; ViewModelConfigManager.saveCurrent() }
        )
        y += SECTION_GAP

        addSectionHeader("Position", y - SECTION_LABEL_OFFSET)
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
        y += SECTION_GAP

        addSectionHeader("Rotation", y - SECTION_LABEL_OFFSET)
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
        y += SECTION_GAP

        addSectionHeader("Animation", y - SECTION_LABEL_OFFSET)
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
        panelHeight = (contentBottom - panelTop) + PADDING + 16

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

    private fun addSectionHeader(title: String, y: Int) {
        sectionHeaders.add(SectionHeader(title, y))
    }

    private fun setupConfigControls(configX: Int) {
        val padding = 12
        val dropdownHeight = 20
        val fieldHeight = 18
        val buttonHeight = 20
        val verticalGap = 10

        val configHeight = padding + 16 + dropdownHeight + verticalGap + 12 + fieldHeight + verticalGap + buttonHeight * 2 + 6 + padding
        val top = panelTop + max(0, (panelHeight - configHeight) / 2)
        configBox = Bounds(configX, top, CONFIG_WIDTH, configHeight)

        val dropdownY = top + padding + 14
        dropdownBounds = Bounds(configX + padding, dropdownY, CONFIG_WIDTH - padding * 2, dropdownHeight)

        val nameLabelY = dropdownY + dropdownHeight + verticalGap
        nameField = ConfigTextFieldWidget(textRenderer, configX + padding, nameLabelY + 12, CONFIG_WIDTH - padding * 2, fieldHeight, Text.empty())
        nameField.text = currentName
        nameField.setEditableColor(TEXT)

        val buttonsY = nameField.y + fieldHeight + verticalGap
        val buttonWidth = (CONFIG_WIDTH - padding * 2 - SPACING) / 2
        buttons.add(
            CompactButton(configX + padding, buttonsY, buttonWidth, buttonHeight, Text.literal("New")) { createConfigProfile() }
        )
        val rename = CompactButton(
            configX + padding + buttonWidth + SPACING,
            buttonsY,
            buttonWidth,
            buttonHeight,
            Text.literal("Rename")
        ) { renameConfigProfile() }
        val delete = CompactButton(
            configX + padding,
            buttonsY + buttonHeight + 4,
            CONFIG_WIDTH - padding * 2,
            buttonHeight,
            Text.literal("Delete")
        ) { deleteConfigProfile() }

        val canModify = !ViewModelConfigManager.isDefault(currentName) && allConfigs.size > 1
        rename.active = canModify
        delete.active = canModify

        buttons.add(rename)
        buttons.add(delete)
    }

    private fun selectConfigProfile(name: String) {
        if (ViewModelConfigManager.setActive(name)) {
            currentName = ViewModelConfigManager.currentName
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun createConfigProfile() {
        val requested = nameField.text.ifBlank { "New" }
        if (ViewModelConfigManager.createConfig(requested)) {
            allConfigs = ViewModelConfigManager.getConfigNames()
            nameField.text = requested
            configMenuOpen = false
        }
    }

    private fun renameConfigProfile() {
        if (ViewModelConfigManager.renameConfig(currentName, nameField.text)) {
            currentName = ViewModelConfigManager.currentName
            allConfigs = ViewModelConfigManager.getConfigNames()
            client?.setScreen(ViewModelScreen())
        }
    }

    private fun deleteConfigProfile() {
        if (ViewModelConfigManager.deleteConfig(currentName)) {
            currentName = ViewModelConfigManager.currentName
            allConfigs = ViewModelConfigManager.getConfigNames()
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
        UiPrimitives.fillRoundedRect(context, panelX, panelTop, WIDTH, panelHeight, RADIUS, PANEL)
        UiPrimitives.drawRoundedBorder(context, panelX, panelTop, WIDTH, panelHeight, RADIUS, BORDER)

        val title = Text.literal("ViewModel").styled { it.withBold(true) }
        val titleY = panelTop + 16
        context.drawText(
            textRenderer,
            title,
            panelX + WIDTH / 2 - textRenderer.getWidth(title) / 2,
            titleY,
            ACCENT,
            false
        )

        context.fill(panelX + PADDING, titleY + 14, panelX + WIDTH - PADDING, titleY + 15, BORDER_SOFT)

        sectionHeaders.forEach { renderSectionTitle(context, panelX, it.y, it.title) }
    }

    private fun renderConfigCard(context: DrawContext, mouseX: Int, mouseY: Int) {
        UiPrimitives.fillRoundedRect(context, configBox.x, configBox.y, configBox.w, configBox.h, RADIUS, CARD)
        UiPrimitives.drawRoundedBorder(context, configBox.x, configBox.y, configBox.w, configBox.h, RADIUS, BORDER)

        context.drawText(
            textRenderer,
            Text.literal("Config").styled { it.withBold(true) },
            configBox.x + 14,
            configBox.y + 6,
            ACCENT,
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
        val width = dropdownBounds.w
        val preferredX = configBox.x + configBox.w + 12
        val listX = preferredX.takeIf { it + width < this.width - 12 }
            ?: (configBox.x - width - 12).coerceAtLeast(12)

        val totalHeight = if (allConfigs.isNotEmpty()) allConfigs.size * (LIST_ITEM_H + 2) - 2 else 0
        var bgTop = dropdownBounds.y - 6
        val maxTop = (height - totalHeight - 24).coerceAtLeast(12)
        bgTop = bgTop.coerceIn(12, maxTop)

        if (totalHeight > 0) {
            val bgHeight = totalHeight + 8
            UiPrimitives.fillRoundedRect(context, listX, bgTop, width, bgHeight, RADIUS, CARD)
            UiPrimitives.drawRoundedBorder(context, listX, bgTop, width, bgHeight, RADIUS, BORDER)
            dropdownMenuBounds = Bounds(listX, bgTop, width, bgHeight)
        }

        var listY = bgTop + 4
        val items = mutableListOf<Pair<String, Bounds>>()

        allConfigs.forEach { config ->
            val bounds = Bounds(listX, listY, width, LIST_ITEM_H)
            val hovered = bounds.contains(mouseX, mouseY)
            drawButtonLike(context, bounds, config, hovered, config == currentName)
            items.add(config to bounds)
            listY += LIST_ITEM_H + 2
        }

        dropdownItems = items
    }

    private fun drawButtonLike(context: DrawContext, bounds: Bounds, label: String, hovered: Boolean, selected: Boolean) {
        val fillColor = when {
            hovered -> ACCENT
            selected -> 0xFF222732.toInt()
            else -> CARD
        }
        val textColor = if (hovered) 0xFF0B1726.toInt() else TEXT

        UiPrimitives.fillRoundedRect(context, bounds.x, bounds.y, bounds.w, bounds.h, RADIUS - 2, fillColor)
        UiPrimitives.drawRoundedBorder(context, bounds.x, bounds.y, bounds.w, bounds.h, RADIUS - 2, BORDER_SOFT)

        val tx = bounds.x + (bounds.w - textRenderer.getWidth(label)) / 2
        val ty = bounds.y + (bounds.h - 8) / 2
        context.drawText(textRenderer, Text.literal(label), tx, ty, textColor, false)
    }

    private fun renderSectionTitle(context: DrawContext, x: Int, y: Int, title: String) {
        context.drawText(
            textRenderer,
            Text.literal(title).styled { it.withBold(true) },
            x + PADDING,
            y,
            TEXT_DIM,
            false
        )
    }

    private fun renderResetButton(context: DrawContext, mouseX: Int, mouseY: Int) {
        val btnW = 124
        val btnH = 26
        val btnX = panelX + WIDTH - btnW - PADDING
        val btnY = panelTop + panelHeight + 14

        resetBounds = Bounds(btnX, btnY, btnW, btnH)

        val hovered = resetBounds.contains(mouseX.toDouble(), mouseY.toDouble())
        val btnColor = if (hovered) ACCENT else CARD
        val textColor = if (hovered) 0xFF000000.toInt() else TEXT

        UiPrimitives.fillRoundedRect(context, btnX, btnY, btnW, btnH, RADIUS, btnColor)
        UiPrimitives.drawRoundedBorder(context, btnX, btnY, btnW, btnH, RADIUS, BORDER)

        context.drawText(
            textRenderer,
            Text.literal("Reset All"),
            btnX + (btnW - textRenderer.getWidth("Reset All")) / 2,
            btnY + (btnH - 8) / 2,
            textColor,
            false
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dropdownBounds.contains(mouseX, mouseY)) {
            configMenuOpen = !configMenuOpen
            return true
        }

        if (configMenuOpen) {
            dropdownItems.firstOrNull { it.second.contains(mouseX, mouseY) }?.let {
                configMenuOpen = false
                selectConfigProfile(it.first)
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
