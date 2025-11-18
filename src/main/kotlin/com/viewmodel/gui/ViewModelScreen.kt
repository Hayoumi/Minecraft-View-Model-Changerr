package com.viewmodel.gui

import com.viewmodel.ViewModelConfig
import com.viewmodel.ViewModelConfigManager
import com.viewmodel.gui.components.GuiPrimitives
import com.viewmodel.gui.components.RoundedTextField
import com.viewmodel.gui.components.ViewModelDropdown
import com.viewmodel.gui.components.ViewModelPalette
import com.viewmodel.gui.components.ViewModelPillButton
import com.viewmodel.gui.components.ViewModelSlider
import com.viewmodel.gui.components.ViewModelToggle
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.util.Locale
import kotlin.math.max

class ViewModelScreen : Screen(Text.translatable("viewmodel.gui.title")) {

    private data class SliderDefinition(
        val key: String,
        val min: Float,
        val max: Float,
        val step: Float,
        val getter: () -> Float,
        val setter: (Float) -> Unit,
        val formatter: (Float) -> String
    )

    private data class ToggleDefinition(
        val key: String,
        val getter: () -> Boolean,
        val setter: (Boolean) -> Unit
    )

    private data class PanelBounds(
        var x: Int = 0,
        var y: Int = 0,
        var width: Int = 0,
        var height: Int = 0
    )

    private val mainPanel = PanelBounds()
    private val configPanel = PanelBounds()

    private val sliderWidgets = mutableListOf<ViewModelSlider>()
    private val toggleWidgets = mutableListOf<ViewModelToggle>()

    private lateinit var dropdown: ViewModelDropdown
    private lateinit var nameField: RoundedTextField
    private lateinit var createButton: ViewModelPillButton
    private lateinit var renameButton: ViewModelPillButton
    private lateinit var deleteButton: ViewModelPillButton
    private lateinit var resetButton: ViewModelPillButton

    private var pendingSave = false
    private var saveAt = 0L

    private var statusText: Text? = null
    private var statusColor: Int = ViewModelPalette.TEXT_SECONDARY
    private var statusUntil: Long = 0L

    private val panelPadding = 14
    private val headerHeight = 40
    private val sliderHeight = 44
    private val sliderSpacing = 6
    private val sliderColumns = 2
    private val sliderColumnGap = 10
    private val toggleHeight = 30
    private val toggleSpacing = 6
    private val toggleColumns = 2
    private val toggleColumnGap = 8
    private val actionButtonHeight = 30
    private val dropdownHeight = 30
    private val inputHeight = 26
    private val panelCornerRadius = 22f

    override fun init() {
        super.init()
        clearChildren()
        sliderWidgets.clear()
        toggleWidgets.clear()

        computePanels()
        addConfigControls()
        addMainControls()
        reloadConfigs()
        refreshButtonStates()
    }

    override fun tick() {
        if (pendingSave && Util.getMeasuringTimeMs() >= saveAt) {
            ViewModelConfigManager.saveCurrent()
            pendingSave = false
        }
    }

    override fun close() {
        if (pendingSave) {
            pendingSave = false
        }
        ViewModelConfigManager.saveCurrent()
        super.close()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawPanel(context, configPanel, Text.translatable("viewmodel.gui.configs"))
        drawPanel(context, mainPanel, title)

        drawSubtitle(context)
        drawConfigHeader(context)

        super.render(context, mouseX, mouseY, delta)
        drawStatus(context)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val handled = super.mouseClicked(mouseX, mouseY, button)
        if (::dropdown.isInitialized && dropdown.isExpanded()) {
            val baseArea = mouseX >= dropdown.x && mouseX <= dropdown.x + dropdown.width &&
                mouseY >= dropdown.y && mouseY <= dropdown.y + dropdown.height
            if (!baseArea && !dropdown.isInsideDropdown(mouseX, mouseY)) {
                dropdown.collapse()
            }
        }
        return handled
    }

    // Determine panel locations so the main canvas stays centered with a left config rail.
    private fun computePanels() {
        val sliderCount = sliderDefinitions().size
        val toggleCount = toggleDefinitions().size
        val sliderRows = rowsFor(sliderCount, sliderColumns)
        val toggleRows = rowsFor(toggleCount, toggleColumns)

        val sliderSectionHeight = if (sliderRows == 0) 0 else sliderRows * sliderHeight + (sliderRows - 1) * sliderSpacing
        val toggleSectionHeight = if (toggleRows == 0) 0 else toggleRows * toggleHeight + (toggleRows - 1) * toggleSpacing
        val sectionsGap = if (sliderRows > 0 && toggleRows > 0) 12 else 0

        val minMargin = 18
        val mainWidthTarget = 360
        val minMainWidth = 300
        val availableMainWidth = width - minMargin * 2
        mainPanel.width = max(minMainWidth, minOf(mainWidthTarget, availableMainWidth))
        val baseHeight = panelPadding * 2 + headerHeight + sliderSectionHeight + toggleSectionHeight + sectionsGap + actionButtonHeight + 12
        mainPanel.height = max(baseHeight, headerHeight + actionButtonHeight + panelPadding * 2)

        mainPanel.x = width / 2 - mainPanel.width / 2
        mainPanel.y = height / 2 - mainPanel.height / 2

        val configGap = 16
        val availableLeft = mainPanel.x - configGap - minMargin

        configPanel.width = when {
            availableLeft >= 200 -> 200
            availableLeft >= 160 -> availableLeft
            else -> max(150, availableLeft)
        }

        val configTopSpace = panelPadding + 32
        val configContentHeight =
            configTopSpace + dropdownHeight + 10 + inputHeight + 12 + actionButtonHeight * 3 + 18 + textRenderer.fontHeight + panelPadding
        configPanel.height = max(configContentHeight, actionButtonHeight * 3 + panelPadding * 2)
        configPanel.x = (mainPanel.x - configGap - configPanel.width).coerceAtLeast(minMargin)
        configPanel.y = mainPanel.y
    }

    private fun addMainControls() {
        val sliderDefs = sliderDefinitions()
        val toggleDefs = toggleDefinitions()
        val sliderRows = rowsFor(sliderDefs.size, sliderColumns)
        val sliderAreaWidth = mainPanel.width - panelPadding * 2
        val sliderColumnWidth = if (sliderColumns <= 1) sliderAreaWidth else {
            val totalGap = sliderColumnGap * (sliderColumns - 1)
            ((sliderAreaWidth - totalGap) / sliderColumns).coerceAtLeast(120)
        }

        val sliderStartX = mainPanel.x + panelPadding
        val sliderStartY = mainPanel.y + panelPadding + headerHeight

        sliderDefs.forEachIndexed { index, definition ->
            val column = if (sliderColumns <= 1) 0 else index % sliderColumns
            val row = if (sliderColumns <= 1) index else index / sliderColumns
            val sliderX = sliderStartX + column * (sliderColumnWidth + sliderColumnGap)
            val sliderY = sliderStartY + row * (sliderHeight + sliderSpacing)

            val slider = ViewModelSlider(
                sliderX,
                sliderY,
                sliderColumnWidth,
                sliderHeight,
                textRenderer,
                Text.translatable(definition.key),
                definition.min,
                definition.max,
                definition.step,
                definition.getter,
                definition.formatter
            ) {
                definition.setter(it)
                scheduleSave()
            }
            sliderWidgets += slider
            addDrawableChild(slider)
        }

        val toggleColumnWidth = if (toggleColumns <= 1) sliderAreaWidth else {
            val totalGap = toggleColumnGap * (toggleColumns - 1)
            ((sliderAreaWidth - totalGap) / toggleColumns).coerceAtLeast(110)
        }
        var toggleStartY = sliderStartY
        if (sliderRows > 0) {
            toggleStartY += sliderRows * sliderHeight + (sliderRows - 1) * sliderSpacing
        }
        if (sliderRows > 0 && toggleDefs.isNotEmpty()) {
            toggleStartY += 12
        }

        toggleDefs.forEachIndexed { index, definition ->
            val column = if (toggleColumns <= 1) 0 else index % toggleColumns
            val row = if (toggleColumns <= 1) index else index / toggleColumns
            val toggleX = sliderStartX + column * (toggleColumnWidth + toggleColumnGap)
            val toggleY = toggleStartY + row * (toggleHeight + toggleSpacing)

            val toggle = ViewModelToggle(
                toggleX,
                toggleY,
                toggleColumnWidth,
                toggleHeight,
                textRenderer,
                Text.translatable(definition.key),
                definition.getter
            ) {
                definition.setter(it)
                scheduleSave()
            }
            toggleWidgets += toggle
            addDrawableChild(toggle)
        }

        resetButton = ViewModelPillButton(
            sliderStartX,
            mainPanel.y + mainPanel.height - panelPadding - actionButtonHeight,
            sliderAreaWidth,
            actionButtonHeight,
            textRenderer,
            Text.translatable("viewmodel.gui.reset"),
            ViewModelPalette.SURFACE_HIGHLIGHT
        ) {
            resetCurrentProfile()
        }
        addDrawableChild(resetButton)
    }

    private fun addConfigControls() {
        val leftPadding = 16
        val innerWidth = configPanel.width - leftPadding * 2
        val titleY = configPanel.y + panelPadding
        dropdown = ViewModelDropdown(
            configPanel.x + leftPadding,
            titleY + 32,
            innerWidth,
            dropdownHeight,
            textRenderer,
            Text.translatable("viewmodel.gui.config_placeholder"),
            { ViewModelConfigManager.currentName }
        ) { name ->
            if (ViewModelConfigManager.setActive(name)) {
                scheduleSave()
                refreshButtonStates()
                showStatus(Text.translatable("viewmodel.gui.status.profile", name), ViewModelPalette.SUCCESS)
                dropdown.collapse()
            }
        }

        addDrawableChild(dropdown)

        nameField = RoundedTextField(
            textRenderer,
            configPanel.x + leftPadding,
            dropdown.y + dropdown.height + 10,
            innerWidth,
            inputHeight,
            Text.translatable("viewmodel.gui.config_input")
        )
        nameField.setMaxLength(32)
        addDrawableChild(nameField)

        val halfWidth = (innerWidth - 6) / 2
        val buttonY = nameField.y + nameField.height + 12
        createButton = ViewModelPillButton(
            configPanel.x + leftPadding,
            buttonY,
            halfWidth,
            actionButtonHeight,
            textRenderer,
            Text.translatable("viewmodel.gui.create"),
            ViewModelPalette.ACCENT
        ) { handleCreate() }
        renameButton = ViewModelPillButton(
            configPanel.x + leftPadding + halfWidth + 6,
            buttonY,
            halfWidth,
            actionButtonHeight,
            textRenderer,
            Text.translatable("viewmodel.gui.rename"),
            ViewModelPalette.SURFACE_HIGHLIGHT
        ) { handleRename() }
        deleteButton = ViewModelPillButton(
            configPanel.x + leftPadding,
            buttonY + actionButtonHeight + 10,
            innerWidth,
            actionButtonHeight,
            textRenderer,
            Text.translatable("viewmodel.gui.delete"),
            ViewModelPalette.WARNING
        ) { handleDelete() }

        addDrawableChild(createButton)
        addDrawableChild(renameButton)
        addDrawableChild(deleteButton)
    }

    private fun handleCreate() {
        val created = ViewModelConfigManager.createConfig(nameField.text)
        if (created != null) {
            ViewModelConfigManager.setActive(created)
            nameField.text = ""
            reloadConfigs()
            refreshButtonStates()
            showStatus(Text.translatable("viewmodel.gui.status.created", created), ViewModelPalette.SUCCESS)
        } else {
            showStatus(Text.translatable("viewmodel.gui.status.create_failed"), ViewModelPalette.WARNING)
        }
    }

    private fun handleRename() {
        val newName = nameField.text
        if (newName.isBlank()) {
            showStatus(Text.translatable("viewmodel.gui.status.rename_failed"), ViewModelPalette.WARNING)
            return
        }

        val current = ViewModelConfigManager.currentName
        val renamed = ViewModelConfigManager.renameConfig(current, newName)
        if (renamed != null) {
            nameField.text = ""
            reloadConfigs()
            refreshButtonStates()
            showStatus(Text.translatable("viewmodel.gui.status.renamed", renamed), ViewModelPalette.SUCCESS)
        } else {
            showStatus(Text.translatable("viewmodel.gui.status.rename_failed"), ViewModelPalette.WARNING)
        }
    }

    private fun handleDelete() {
        val current = ViewModelConfigManager.currentName
        if (ViewModelConfigManager.deleteConfig(current)) {
            reloadConfigs()
            refreshButtonStates()
            showStatus(Text.translatable("viewmodel.gui.status.deleted", current), ViewModelPalette.SUCCESS)
        } else {
            showStatus(Text.translatable("viewmodel.gui.status.delete_failed"), ViewModelPalette.WARNING)
        }
    }

    private fun resetCurrentProfile() {
        val config = ViewModelConfig.current
        val defaults = ViewModelConfig()
        config.size = defaults.size
        config.positionX = defaults.positionX
        config.positionY = defaults.positionY
        config.positionZ = defaults.positionZ
        config.rotationYaw = defaults.rotationYaw
        config.rotationPitch = defaults.rotationPitch
        config.rotationRoll = defaults.rotationRoll
        config.noSwing = defaults.noSwing
        config.scaleSwing = defaults.scaleSwing
        scheduleSave()
        showStatus(Text.translatable("viewmodel.gui.status.reset"), ViewModelPalette.SUCCESS)
    }

    private fun reloadConfigs() {
        if (::dropdown.isInitialized) {
            dropdown.setOptions(ViewModelConfigManager.getConfigNames())
        }
    }

    private fun refreshButtonStates() {
        val isDefault = ViewModelConfigManager.isDefault(ViewModelConfigManager.currentName)
        if (::renameButton.isInitialized) {
            renameButton.active = !isDefault
        }
        if (::deleteButton.isInitialized) {
            deleteButton.active = !isDefault
        }
    }

    private fun sliderDefinitions(): List<SliderDefinition> = listOf(
        SliderDefinition(
            "viewmodel.gui.size",
            0.2f,
            2.5f,
            0.01f,
            { ViewModelConfig.current.size },
            { ViewModelConfig.current.size = it },
            { value -> String.format(Locale.ROOT, "%.2fx", value) }
        ),
        SliderDefinition(
            "viewmodel.gui.position_x",
            -80f,
            80f,
            1f,
            { ViewModelConfig.current.positionX },
            { ViewModelConfig.current.positionX = it },
            { value -> formatOffset(value) }
        ),
        SliderDefinition(
            "viewmodel.gui.position_y",
            -80f,
            80f,
            1f,
            { ViewModelConfig.current.positionY },
            { ViewModelConfig.current.positionY = it },
            { value -> formatOffset(value) }
        ),
        SliderDefinition(
            "viewmodel.gui.position_z",
            -80f,
            80f,
            1f,
            { ViewModelConfig.current.positionZ },
            { ViewModelConfig.current.positionZ = it },
            { value -> formatOffset(value) }
        ),
        SliderDefinition(
            "viewmodel.gui.rotation_yaw",
            -180f,
            180f,
            1f,
            { ViewModelConfig.current.rotationYaw },
            { ViewModelConfig.current.rotationYaw = it },
            { value -> formatDegrees(value) }
        ),
        SliderDefinition(
            "viewmodel.gui.rotation_pitch",
            -180f,
            180f,
            1f,
            { ViewModelConfig.current.rotationPitch },
            { ViewModelConfig.current.rotationPitch = it },
            { value -> formatDegrees(value) }
        ),
        SliderDefinition(
            "viewmodel.gui.rotation_roll",
            -180f,
            180f,
            1f,
            { ViewModelConfig.current.rotationRoll },
            { ViewModelConfig.current.rotationRoll = it },
            { value -> formatDegrees(value) }
        )
    )

    private fun toggleDefinitions(): List<ToggleDefinition> = listOf(
        ToggleDefinition(
            "viewmodel.gui.no_swing",
            { ViewModelConfig.current.noSwing },
            { ViewModelConfig.current.noSwing = it }
        ),
        ToggleDefinition(
            "viewmodel.gui.scale_swing",
            { ViewModelConfig.current.scaleSwing },
            { ViewModelConfig.current.scaleSwing = it }
        )
    )

    private fun rowsFor(count: Int, columns: Int): Int =
        if (count <= 0 || columns <= 0) 0 else (count + columns - 1) / columns

    private fun drawPanel(context: DrawContext, panel: PanelBounds, header: Text) {
        GuiPrimitives.fillRoundedRect(
            context,
            panel.x.toFloat(),
            panel.y.toFloat(),
            panel.width.toFloat(),
            panel.height.toFloat(),
            panelCornerRadius,
            ViewModelPalette.PANEL
        )
        context.drawText(
            textRenderer,
            header,
            panel.x + panelPadding,
            panel.y + panelPadding - 2,
            ViewModelPalette.rgb(ViewModelPalette.TEXT_PRIMARY),
            false
        )
    }

    private fun drawSubtitle(context: DrawContext) {
        val subtitle = Text.translatable("viewmodel.gui.subtitle")
        context.drawText(
            textRenderer,
            subtitle,
            mainPanel.x + panelPadding,
            mainPanel.y + panelPadding + 12,
            ViewModelPalette.rgb(ViewModelPalette.TEXT_MUTED),
            false
        )
    }

    private fun drawConfigHeader(context: DrawContext) {
        if (!::dropdown.isInitialized) return
        val label = Text.translatable("viewmodel.gui.active_profile")
        context.drawText(
            textRenderer,
            label,
            dropdown.x,
            dropdown.y - 14,
            ViewModelPalette.rgb(ViewModelPalette.TEXT_MUTED),
            false
        )
    }

    private fun drawStatus(context: DrawContext) {
        val now = Util.getMeasuringTimeMs()
        val message = statusText ?: return
        if (now > statusUntil) {
            statusText = null
            return
        }

        context.drawText(
            textRenderer,
            message,
            configPanel.x + panelPadding,
            configPanel.y + configPanel.height - panelPadding - textRenderer.fontHeight,
            ViewModelPalette.rgb(statusColor),
            false
        )
    }

    private fun showStatus(text: Text, color: Int, duration: Long = 2500L) {
        statusText = text
        statusColor = color
        statusUntil = Util.getMeasuringTimeMs() + duration
    }

    private fun formatOffset(value: Float): String =
        String.format(Locale.ROOT, "%+.0f", value)

    private fun formatDegrees(value: Float): String =
        String.format(Locale.ROOT, "%+.0f deg", value)

    // Debounce disk writes while the player drags sliders.
    private fun scheduleSave() {
        pendingSave = true
        saveAt = Util.getMeasuringTimeMs() + 300
    }
}
