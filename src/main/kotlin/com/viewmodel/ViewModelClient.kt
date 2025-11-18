package com.viewmodel

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object ViewModelClient : ClientModInitializer {
    private lateinit var openMenuKey: KeyBinding

    override fun onInitializeClient() {
        // Register keybinding
        openMenuKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.viewmodel.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.category.viewmodel"
            )
        )

        // Handle keybinding
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openMenuKey.wasPressed()) {
                client.setScreen(com.viewmodel.gui.ViewModelScreen())
            }
        }
    }
}