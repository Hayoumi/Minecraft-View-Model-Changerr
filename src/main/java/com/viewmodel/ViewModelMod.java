package com.viewmodel;

import com.viewmodel.client.gui.ViewmodelConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the keybinding that opens the compact config screen.
 */
public final class ViewModelMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("viewmodel");
    private static KeyBinding openScreenKey;

    @Override
    public void onInitializeClient() {
        ViewModelConfig.load();

        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.viewmodel.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.category.viewmodel"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.wasPressed()) {
                openConfigScreen(client);
            }
        });
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        client.setScreen(new ViewmodelConfigScreen(client.currentScreen));
    }
}
