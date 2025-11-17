package com.viewmodel

import com.viewmodel.gui.ViewModelScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object ViewModelMod : ClientModInitializer {
    private val LOGGER = LoggerFactory.getLogger("viewmodel")
    
    private lateinit var openGuiKey: KeyBinding

    override fun onInitializeClient() {
        LOGGER.info("ViewModel Mod initialized!")

        ViewModelConfigManager.load()
        
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.viewmodel.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.viewmodel"
            )
        )
        
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openGuiKey.wasPressed()) {
                client.setScreen(ViewModelScreen())
            }
        }
    }
}
