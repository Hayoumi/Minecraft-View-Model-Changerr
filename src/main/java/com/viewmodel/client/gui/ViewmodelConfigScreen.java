package com.viewmodel.client.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import com.viewmodel.ViewModelConfig;
import com.viewmodel.ViewModelProfile;
import com.viewmodel.ViewModelProfileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Minimal two-panel screen used to tweak viewmodel transforms.
 */
public class ViewmodelConfigScreen extends Screen {
    private static final int LEFT_PANEL_WIDTH = 160;
    private static final int RIGHT_PANEL_WIDTH = 350;
    private static final int PANEL_SPACING = 12;
    private static final int PANEL_PADDING = 14;
    private static final int LABEL_COLUMN_WIDTH = 34;
    private static final int VALUE_COLUMN_WIDTH = 60;
    private static final int ROW_SPACING = 28;
    private static final int RESET_BUTTON_SIZE = 20;
    private static final int COLUMN_GAP = 6;
    private static final int SECTION_HEADER_GAP = 16;
    private static final int SECTION_LABEL_OFFSET = 12;
    private static final int SECTION_LABEL_TO_SLIDER_GAP = 10;
    private static final int RIGHT_PANEL_CONTENT_OFFSET = 28;
    private static final int PROFILES_HEADER_GAP = 22;
    private static final int STATUS_FOOTER_SPACE = 20;
    private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("+0.00;-0.00");
    private static final int COLOR_BACKDROP = 0xD0101010;
    private static final int COLOR_PANEL_BG = 0xFF121212;
    private static final int COLOR_PANEL_BORDER = 0xFF2A2A2A;
    private static final int COLOR_TEXT_PRIMARY = 0xFFF0F0F0;
    private static final int COLOR_TEXT_MUTED = 0xFF9FA1A5;
    private static final int COLOR_TEXT_DARK = 0xFF050505;
    private static final int COLOR_ACCENT = 0xFF5CC8C1;
    private static final int COLOR_ACCENT_HOVER = 0xFF6FD6CF;
    private static final int COLOR_TRACK_BG = 0xFF1E1E1E;
    private static final int COLOR_TRACK_FILL = 0xFF5CC8C1;
    private static final int COLOR_TOGGLE_OFF = 0xFF1F1F1F;

    private final Screen parent;
    private final ViewModelProfileManager profileManager = ViewModelConfig.profiles();
    private final List<SliderLine> sliderLines = new ArrayList<>();
    private final List<SectionLabel> sectionLabels = new ArrayList<>();

    private ProfileDropdownWidget profileDropdown;
    private ToggleSwitchWidget noSwingToggle;
    private ToggleSwitchWidget scaleSwingToggle;
    private Text statusMessage = Text.empty();
    private int statusTicks;

    private int leftPanelX;
    private int leftPanelY;
    private int rightPanelX;
    private int rightPanelY;
    private int leftPanelHeight = 210;
    private int rightPanelHeight = 440;

    public ViewmodelConfigScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.sliderLines.clear();
        this.sectionLabels.clear();
        this.clearChildren();

        int totalWidth = LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH + PANEL_SPACING;
        this.leftPanelX = (this.width - totalWidth) / 2;
        this.rightPanelX = this.leftPanelX + LEFT_PANEL_WIDTH + PANEL_SPACING;
        this.leftPanelY = (this.height - this.rightPanelHeight) / 2;
        this.rightPanelY = this.leftPanelY;

        buildSidebar();
        buildSliders();
    }

    private void buildSidebar() {
        int dropdownWidth = LEFT_PANEL_WIDTH - PANEL_PADDING * 2;
        List<String> names = profileManager.profileNames();
        ProfileDropdownWidget dropdown = new ProfileDropdownWidget(
            leftPanelX + PANEL_PADDING,
            leftPanelY + PANEL_PADDING + PROFILES_HEADER_GAP,
            dropdownWidth,
            18,
            names,
            profileManager.getActiveIndex(),
            this::handleProfileSelection
        );

        int buttonsY = dropdown.getY() + dropdown.getHeight() + 10;
        int buttonWidth = (dropdownWidth - 8) / 3;

        this.addDrawableChild(new AccentButton(
            leftPanelX + PANEL_PADDING,
            buttonsY,
            buttonWidth,
            18,
            Text.literal("Create"),
            this::handleCreateProfile
        ));

        this.addDrawableChild(new MinimalButton(
            leftPanelX + PANEL_PADDING + buttonWidth + 4,
            buttonsY,
            buttonWidth,
            18,
            Text.literal("Rename"),
            this::handleRenameProfile,
            0xFF191919,
            0xFF242424
        ));

        this.addDrawableChild(new DangerButton(
            leftPanelX + PANEL_PADDING + (buttonWidth + 4) * 2,
            buttonsY,
            buttonWidth,
            18,
            Text.literal("Delete"),
            this::handleDeleteProfile
        ));

        int buttonsBottom = buttonsY + 18;
        this.leftPanelHeight = Math.max(
            140,
            buttonsBottom - leftPanelY + PANEL_PADDING + STATUS_FOOTER_SPACE
        );

        this.profileDropdown = this.addDrawableChild(dropdown);
    }

    private void buildSliders() {
        int cursorY = rightPanelY + PANEL_PADDING + RIGHT_PANEL_CONTENT_OFFSET;

        cursorY = addSliderRow(
            "SIZE",
            0.15,
            2.0,
            0.01,
            () -> ViewModelConfig.current.getSize(),
            ViewModelProfile.baseline().size(),
            value -> setAndSync(ViewModelConfig.current::setSize, value),
            cursorY
        );

        cursorY += SECTION_HEADER_GAP;
        sectionLabels.add(new SectionLabel("POSITION", rightPanelX + PANEL_PADDING, cursorY - SECTION_LABEL_OFFSET));
        cursorY += SECTION_LABEL_TO_SLIDER_GAP;

        cursorY = addSliderRow(
            "X",
            -50.0,
            50.0,
            0.50,
            () -> ViewModelConfig.current.getPositionX(),
            ViewModelProfile.baseline().positionX(),
            value -> setAndSync(ViewModelConfig.current::setPositionX, value),
            cursorY
        );

        cursorY = addSliderRow(
            "Y",
            -50.0,
            50.0,
            0.50,
            () -> ViewModelConfig.current.getPositionY(),
            ViewModelProfile.baseline().positionY(),
            value -> setAndSync(ViewModelConfig.current::setPositionY, value),
            cursorY
        );

        cursorY = addSliderRow(
            "Z",
            -50.0,
            50.0,
            0.50,
            () -> ViewModelConfig.current.getPositionZ(),
            ViewModelProfile.baseline().positionZ(),
            value -> setAndSync(ViewModelConfig.current::setPositionZ, value),
            cursorY
        );

        cursorY += SECTION_HEADER_GAP;
        sectionLabels.add(new SectionLabel("ROTATION", rightPanelX + PANEL_PADDING, cursorY - SECTION_LABEL_OFFSET));
        cursorY += SECTION_LABEL_TO_SLIDER_GAP;

        cursorY = addSliderRow(
            "Yaw",
            -180.0,
            180.0,
            1.0,
            () -> ViewModelConfig.current.getRotationYaw(),
            ViewModelProfile.baseline().rotationYaw(),
            value -> setAndSync(ViewModelConfig.current::setRotationYaw, value),
            cursorY
        );

        cursorY = addSliderRow(
            "Pitch",
            -180.0,
            180.0,
            1.0,
            () -> ViewModelConfig.current.getRotationPitch(),
            ViewModelProfile.baseline().rotationPitch(),
            value -> setAndSync(ViewModelConfig.current::setRotationPitch, value),
            cursorY
        );

        cursorY = addSliderRow(
            "Roll",
            -180.0,
            180.0,
            1.0,
            () -> ViewModelConfig.current.getRotationRoll(),
            ViewModelProfile.baseline().rotationRoll(),
            value -> setAndSync(ViewModelConfig.current::setRotationRoll, value),
            cursorY
        );

        cursorY += 12;
        this.noSwingToggle = this.addDrawableChild(new ToggleSwitchWidget(
            rightPanelX + PANEL_PADDING,
            cursorY,
            RIGHT_PANEL_WIDTH - PANEL_PADDING * 2,
            20,
            Text.literal("NO SWING"),
            () -> ViewModelConfig.current.getNoSwing(),
            value -> setAndSync(ViewModelConfig.current::setNoSwing, value)
        ));

        cursorY += 28;
        this.scaleSwingToggle = this.addDrawableChild(new ToggleSwitchWidget(
            rightPanelX + PANEL_PADDING,
            cursorY,
            RIGHT_PANEL_WIDTH - PANEL_PADDING * 2,
            20,
            Text.literal("SCALE SWING"),
            () -> ViewModelConfig.current.getScaleSwing(),
            value -> setAndSync(ViewModelConfig.current::setScaleSwing, value)
        ));

        cursorY += 20;
        int resetButtonY = Math.max(cursorY, rightPanelY + rightPanelHeight - PANEL_PADDING - 24);
        this.addDrawableChild(new MinimalButton(
            rightPanelX + PANEL_PADDING,
            resetButtonY,
            RIGHT_PANEL_WIDTH - PANEL_PADDING * 2,
            20,
            Text.literal("Reset All"),
            this::handleResetAll,
            0xFF181818,
            0xFF212121,
            COLOR_ACCENT
        ));
    }

    private int addSliderRow(
        String label,
        double min,
        double max,
        double step,
        DoubleSupplier supplier,
        double resetValue,
        DoubleConsumer setter,
        int y
    ) {
        int labelX = rightPanelX + PANEL_PADDING;
        int sliderX = labelX + LABEL_COLUMN_WIDTH + COLUMN_GAP;
        int sliderWidth = RIGHT_PANEL_WIDTH
            - PANEL_PADDING * 2
            - LABEL_COLUMN_WIDTH
            - VALUE_COLUMN_WIDTH
            - RESET_BUTTON_SIZE
            - COLUMN_GAP * 3;

        NeonSlider slider = this.addDrawableChild(new NeonSlider(
            sliderX,
            y,
            sliderWidth,
            16,
            min,
            max,
            step,
            supplier.getAsDouble(),
            setter
        ));

        int valueX = sliderX + sliderWidth + COLUMN_GAP;
        sliderLines.add(new SliderLine(label, slider, supplier, labelX, y + 4, valueX));

        this.addDrawableChild(new ResetButton(
            valueX + VALUE_COLUMN_WIDTH + COLUMN_GAP,
            y,
            () -> slider.resetTo(resetValue)
        ));

        return y + ROW_SPACING;
    }

    private void handleCreateProfile() {
        if (this.client == null) {
            return;
        }

        this.client.setScreen(new NamePromptScreen(
            this,
            Text.literal("Create profile"),
            "",
            input -> {
                if (input.isBlank()) {
                    return Text.literal("Name cannot be empty");
                }
                ViewModelProfile created = profileManager.create(input);
                if (created == null) {
                    return Text.literal("Unable to create profile");
                }
                setStatus(Text.literal("Created " + created.name()));
                this.clearAndInit();
                return null;
            }
        ));
    }

    private void handleRenameProfile() {
        if (this.client == null) {
            return;
        }
        String currentName = profileManager.getActiveProfile().name();
        this.client.setScreen(new NamePromptScreen(
            this,
            Text.literal("Rename profile"),
            currentName,
            input -> {
                if (input.isBlank()) {
                    return Text.literal("Name cannot be empty");
                }
                if (!profileManager.renameActive(input)) {
                    return Text.literal("Name already exists");
                }
                setStatus(Text.literal("Renamed to " + profileManager.getActiveProfile().name()));
                this.clearAndInit();
                return null;
            }
        ));
    }

    private void handleDeleteProfile() {
        if (!profileManager.deleteActive()) {
            setStatus(Text.literal("Cannot delete last profile").formatted(Formatting.RED));
            return;
        }
        setStatus(Text.literal("Deleted profile"));
        this.clearAndInit();
    }

    private void handleProfileSelection(int index) {
        if (index == profileManager.getActiveIndex()) {
            return;
        }
        profileManager.select(index);
        setStatus(Text.literal("Switched to " + profileManager.getActiveProfile().name()));
        this.clearAndInit();
    }

    private void handleResetAll() {
        ViewModelProfile baseline = ViewModelProfile.baseline();
        baseline.apply(ViewModelConfig.current);
        profileManager.updateActiveFromConfig();

        for (SliderLine line : sliderLines) {
            line.slider().syncFrom(line.supplier().getAsDouble());
        }
        if (noSwingToggle != null) {
            noSwingToggle.refreshFromConfig();
        }
        if (scaleSwingToggle != null) {
            scaleSwingToggle.refreshFromConfig();
        }
        setStatus(Text.literal("Reset to defaults"));
    }

    private void setAndSync(FloatSetter setter, double value) {
        setter.accept((float) value);
        profileManager.updateActiveFromConfig();
    }

    private void setAndSync(BooleanSetter setter, boolean value) {
        setter.accept(value);
        profileManager.updateActiveFromConfig();
    }

    private void setStatus(Text message) {
        this.statusMessage = message;
        this.statusTicks = 80;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (profileDropdown != null && profileDropdown.isOpen()) {
            if (profileDropdown.isMouseOver(mouseX, mouseY)) {
                if (profileDropdown.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            } else {
                profileDropdown.close();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        super.tick();
        if (statusTicks > 0) {
            statusTicks--;
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderDimBackground(context);
        drawPanel(context, leftPanelX, leftPanelY, LEFT_PANEL_WIDTH, leftPanelHeight);
        drawPanel(context, rightPanelX, rightPanelY, RIGHT_PANEL_WIDTH, rightPanelHeight);

        TextRenderer font = this.textRenderer;
        context.drawText(font, Text.literal("PROFILES"), leftPanelX + PANEL_PADDING, leftPanelY + PANEL_PADDING, COLOR_TEXT_PRIMARY, false);
        context.drawText(font, Text.literal("VIEWMODEL SETTINGS"), rightPanelX + PANEL_PADDING, rightPanelY + PANEL_PADDING, COLOR_TEXT_PRIMARY, false);

        for (SectionLabel section : sectionLabels) {
            context.drawText(font, Text.literal(section.text()), section.x(), section.y(), COLOR_TEXT_MUTED, false);
        }

        super.render(context, mouseX, mouseY, delta);

        for (SliderLine line : sliderLines) {
            context.drawText(font, Text.literal(line.label()), line.labelX(), line.labelY(), COLOR_TEXT_PRIMARY, false);
            context.drawText(font, Text.literal(line.slider().formattedValue(VALUE_FORMAT)), line.valueX(), line.labelY(), COLOR_ACCENT, false);
        }

        if (statusMessage != null && statusTicks > 0 && !statusMessage.getString().isEmpty()) {
            context.drawText(
                font,
                statusMessage,
                leftPanelX + PANEL_PADDING,
                leftPanelY + leftPanelHeight - PANEL_PADDING,
                COLOR_ACCENT,
                false
            );
        }
    }

    private void renderDimBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, COLOR_BACKDROP);
    }

    private void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, COLOR_PANEL_BG);
        context.drawBorder(x, y, width, height, COLOR_PANEL_BORDER);
    }

    private interface FloatSetter {
        void accept(float value);
    }

    private interface BooleanSetter {
        void accept(boolean value);
    }

    private record SliderLine(String label, NeonSlider slider, DoubleSupplier supplier, int labelX, int labelY, int valueX) {}

    private record SectionLabel(String text, int x, int y) {}

    private static class NeonSlider extends SliderWidget {
        private final double min;
        private final double max;
        private final double step;
        private final DoubleConsumer onChange;

        NeonSlider(int x, int y, int width, int height, double min, double max, double step, double initialValue, DoubleConsumer onChange) {
            super(x, y, width, height, Text.empty(), normalize(initialValue, min, max));
            this.min = min;
            this.max = max;
            this.step = step;
            this.onChange = onChange;
            setValueFromActual(initialValue);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.empty());
        }

        @Override
        protected void applyValue() {
            double snapped = snap(getActualValue());
            setValueFromActual(snapped);
            onChange.accept(snapped);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int trackY = getY() + this.height / 2 - 2;
            int knobWidth = 6;
            int knobX = getX() + (int) (this.value * (this.width - knobWidth));

            context.fill(getX(), trackY, getX() + this.width, trackY + 4, COLOR_TRACK_BG);
            context.fill(getX(), trackY, knobX + knobWidth, trackY + 4, COLOR_TRACK_FILL);
            context.fill(knobX, getY(), knobX + knobWidth, getY() + this.height, 0xFFE7E7E7);
        }

        public void resetTo(double target) {
            setValueFromActual(target);
            onChange.accept(target);
        }

        public String formattedValue(DecimalFormat format) {
            return format.format(getActualValue());
        }

        private double getActualValue() {
            return MathHelper.lerp(this.value, this.min, this.max);
        }

        private void setValueFromActual(double actual) {
            double normalized = normalize(actual, min, max);
            this.value = MathHelper.clamp(normalized, 0.0, 1.0);
        }

        private double snap(double value) {
            return step <= 0 ? value : Math.round(value / step) * step;
        }

        public void syncFrom(double actualValue) {
            setValueFromActual(actualValue);
        }

        private static double normalize(double value, double min, double max) {
            return (value - min) / (max - min);
        }
    }

    private static class ResetButton extends ClickableWidget {
        private final Runnable action;

        ResetButton(int x, int y, Runnable action) {
            super(x, y, RESET_BUTTON_SIZE, RESET_BUTTON_SIZE, Text.literal("\u21ba"));
            this.action = action;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int color = this.isHovered() ? 0xFF2A2A2A : 0xFF1C1C1C;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), COLOR_ACCENT);
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            float scale = 1.2f;
            int textWidth = font.getWidth(getMessage());
            int textHeight = font.fontHeight;
            context.getMatrices().push();
            context.getMatrices().translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawText(
                font,
                getMessage(),
                (int) (-textWidth / 2f),
                (int) (-textHeight / 2f),
                COLOR_ACCENT,
                false
            );
            context.getMatrices().pop();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            action.run();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private static class MinimalButton extends PressableWidget {
        private final Runnable action;
        private final int baseColor;
        private final int hoverColor;
        private final int textColor;

        MinimalButton(int x, int y, int width, int height, Text text, Runnable action, int baseColor, int hoverColor) {
            this(x, y, width, height, text, action, baseColor, hoverColor, COLOR_TEXT_PRIMARY);
        }

        MinimalButton(int x, int y, int width, int height, Text text, Runnable action, int baseColor, int hoverColor, int textColor) {
            super(x, y, width, height, text);
            this.action = action;
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = this.isHovered() ? hoverColor : baseColor;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), COLOR_PANEL_BORDER);
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            int textX = getX() + (getWidth() - font.getWidth(getMessage())) / 2;
            context.drawText(font, getMessage(), textX, getY() + 5, textColor, false);
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private static class AccentButton extends MinimalButton {
        AccentButton(int x, int y, int width, int height, Text text, Runnable action) {
            super(x, y, width, height, text, action, COLOR_ACCENT, COLOR_ACCENT_HOVER);
        }
    }

    private static class DangerButton extends MinimalButton {
        DangerButton(int x, int y, int width, int height, Text text, Runnable action) {
            super(x, y, width, height, text.copy().formatted(Formatting.WHITE), action, 0xFF2A1717, 0xFF391F1F);
        }
    }

    private static class ToggleSwitchWidget extends ClickableWidget {
        private final Text label;
        private final BooleanSupplier supplier;
        private final BooleanSetter consumer;
        private boolean value;

        ToggleSwitchWidget(int x, int y, int width, int height, Text label, BooleanSupplier supplier, BooleanSetter consumer) {
            super(x, y, width, height, label);
            this.label = label;
            this.supplier = supplier;
            this.consumer = consumer;
            this.value = supplier.getAsBoolean();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            context.drawText(font, label, getX(), getY() + 6, COLOR_TEXT_PRIMARY, false);

            int switchWidth = font.getWidth("OFF") + 16;
            int switchHeight = 16;
            int switchX = getX() + getWidth() - switchWidth;
            int switchY = getY() + (getHeight() - switchHeight) / 2;
            int trackColor = value ? COLOR_ACCENT : COLOR_TOGGLE_OFF;

            context.fill(switchX, switchY, switchX + switchWidth, switchY + switchHeight, trackColor);
            context.drawBorder(switchX, switchY, switchWidth, switchHeight, COLOR_PANEL_BORDER);
            int knobX = value ? switchX + switchWidth - switchHeight : switchX;
            context.fill(knobX, switchY, knobX + switchHeight, switchY + switchHeight, 0xFFEFEFEF);

            String state = value ? "ON" : "OFF";
            int stateWidth = font.getWidth(state);
            int textX = switchX + (switchWidth - stateWidth) / 2;
            context.drawText(font, Text.literal(state), textX, switchY + 4, COLOR_TEXT_DARK, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.value = !this.value;
            this.consumer.accept(this.value);
        }

        public void refreshFromConfig() {
            this.value = supplier.getAsBoolean();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private static class ProfileDropdownWidget extends ClickableWidget {
        private static final int MAX_VISIBLE = 5;
        private List<String> entries;
        private final java.util.function.IntConsumer onSelect;
        private boolean open;
        private int selectedIndex;
        private int scrollOffset;

        ProfileDropdownWidget(
            int x,
            int y,
            int width,
            int height,
            List<String> entries,
            int selectedIndex,
            java.util.function.IntConsumer onSelect
        ) {
            super(x, y, width, height, Text.empty());
            this.entries = new ArrayList<>(entries);
            this.onSelect = onSelect;
            setSelectedIndex(selectedIndex);
        }

        public void setEntries(List<String> entries, int selectedIndex) {
            this.entries = new ArrayList<>(entries);
            setSelectedIndex(selectedIndex);
        }

        private void setSelectedIndex(int selectedIndex) {
            if (entries.isEmpty()) {
                this.selectedIndex = -1;
                return;
            }
            this.selectedIndex = MathHelper.clamp(selectedIndex, 0, entries.size() - 1);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!open) {
                open = true;
                return;
            }

            if (mouseY >= getY() + getHeight()) {
                int row = (int) ((mouseY - (getY() + getHeight())) / getHeight());
                int index = scrollOffset + row;
                if (index >= 0 && index < entries.size()) {
                    onSelect.accept(index);
                }
            }

            open = false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!open || !isMouseOver(mouseX, mouseY)) {
                return false;
            }
            int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
            double delta = verticalAmount != 0 ? verticalAmount : horizontalAmount;
            scrollOffset = MathHelper.clamp(scrollOffset - (int) Math.signum(delta), 0, maxOffset);
            return true;
        }

        public boolean isOpen() {
            return open;
        }

        public void close() {
            this.open = false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (open) {
                int visible = Math.min(MAX_VISIBLE, entries.size());
                int height = getHeight() * (visible + 1);
                return mouseX >= getX()
                    && mouseX <= getX() + getWidth()
                    && mouseY >= getY()
                    && mouseY <= getY() + height;
            }
            return super.isMouseOver(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            drawBox(context, getY(), selectedIndex);
            if (open) {
                int visible = Math.min(MAX_VISIBLE, entries.size());
                for (int i = 0; i < visible; i++) {
                    int idx = scrollOffset + i;
                    int rowY = getY() + getHeight() * (i + 1);
                    drawBox(context, rowY, idx);
                }
            }
        }

        private void drawBox(DrawContext context, int y, int index) {
            String label = (index >= 0 && index < entries.size()) ? entries.get(index) : "None";
            boolean isSelected = index == selectedIndex;
            int background = isSelected ? 0xFF1C1C1C : 0xFF111111;
            context.fill(getX(), y, getX() + getWidth(), y + getHeight(), background);
            context.drawBorder(getX(), y, getWidth(), getHeight(), COLOR_PANEL_BORDER);
            context.drawText(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(label),
                getX() + 6,
                y + 5,
                COLOR_TEXT_PRIMARY,
                false
            );
            if (y == getY()) {
                context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.literal("\u25bc"),
                    getX() + getWidth() - 10,
                    y + 4,
                    COLOR_ACCENT,
                    false
                );
            }
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private static class NamePromptScreen extends Screen {
        private final Screen parent;
        private final Function<String, Text> submitter;
        private final String initialValue;
        private TextFieldWidget textField;
        private Text errorMessage = Text.empty();

        protected NamePromptScreen(Screen parent, Text title, String initialValue, Function<String, Text> submitter) {
            super(title);
            this.parent = parent;
            this.initialValue = initialValue;
            this.submitter = submitter;
        }

        @Override
        protected void init() {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            this.textField = new TextFieldWidget(font, this.width / 2 - 90, this.height / 2 - 10, 180, 20, Text.empty());
            this.textField.setText(initialValue);
            this.textField.setChangedListener(value -> this.errorMessage = Text.empty());
            this.setInitialFocus(this.textField);
            this.addDrawableChild(textField);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), button -> submit())
                .dimensions(this.width / 2 - 90, this.height / 2 + 20, 85, 20)
                .build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> close())
                .dimensions(this.width / 2 + 5, this.height / 2 + 20, 85, 20)
                .build());
        }

        private void submit() {
            Text result = submitter.apply(textField.getText().trim());
            if (result == null) {
                this.client.setScreen(parent);
            } else {
                this.errorMessage = result.copy().formatted(Formatting.RED);
            }
        }

        @Override
        public void close() {
            this.client.setScreen(parent);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                close();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            context.fill(0, 0, this.width, this.height, 0xAA05080C);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFFFF);
            super.render(context, mouseX, mouseY, delta);
            if (errorMessage != null && !errorMessage.getString().isEmpty()) {
                context.drawCenteredTextWithShadow(this.textRenderer, errorMessage, this.width / 2, this.height / 2 + 46, 0xFFFF6B6B);
            }
        }
    }
}
