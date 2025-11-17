package com.viewmodel

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ViewModelConfigManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val configsDir = File("config/viewmodel/configs")
    private val activeFile = File("config/viewmodel/active.txt")
    private const val DEFAULT_NAME = "Default"

    private var configs: MutableMap<String, ViewModelConfig> = mutableMapOf()

    var currentName: String = DEFAULT_NAME
        private set

    fun load() {
        configsDir.mkdirs()
        configs.clear()

        val files = configsDir.listFiles { file -> file.extension.equals("json", ignoreCase = true) }
            ?.toList()
            ?: emptyList()

        for (file in files) {
            runCatching { json.decodeFromString<ViewModelConfig>(file.readText()) }
                .onSuccess { configs[file.nameWithoutExtension] = it }
                .onFailure { println("[ViewModel] Failed to load config ${file.nameWithoutExtension}: ${it.message}") }
        }

        if (!configs.containsKey(DEFAULT_NAME)) {
            configs[DEFAULT_NAME] = ViewModelConfig()
            println("[ViewModel] Default config created")
        }

        val savedName = activeFile.takeIf { it.exists() }?.readText()?.trim().orEmpty()
        currentName = if (configs.containsKey(savedName)) savedName else DEFAULT_NAME

        ViewModelConfig.current = configs[currentName] ?: ViewModelConfig()

        saveCurrent()
    }

    fun getConfigNames(): List<String> = configs.keys.sorted()

    fun saveCurrent() {
        configsDir.mkdirs()
        val activeConfig = ViewModelConfig.current
        configs[currentName] = activeConfig

        val file = File(configsDir, "$currentName.json")
        runCatching { file.writeText(json.encodeToString(activeConfig)) }
            .onFailure { println("[ViewModel] Failed to save config $currentName: ${it.message}") }

        runCatching {
            activeFile.parentFile?.mkdirs()
            activeFile.writeText(currentName)
        }.onFailure { println("[ViewModel] Failed to save active config: ${it.message}") }
    }

    fun setActive(name: String): Boolean {
        val target = configs[name] ?: return false
        saveCurrent()
        currentName = name
        ViewModelConfig.current = target
        saveCurrent()
        return true
    }

    fun createConfig(rawName: String): Boolean {
        val name = sanitize(rawName)
        if (configs.containsKey(name)) return false

        saveCurrent()
        val config = ViewModelConfig()
        configs[name] = config
        currentName = name
        ViewModelConfig.current = config
        saveCurrent()
        return true
    }

    fun deleteConfig(name: String): Boolean {
        if (!configs.containsKey(name) || configs.size <= 1) return false

        configs.remove(name)
        File(configsDir, "$name.json").takeIf { it.exists() }?.delete()

        if (currentName == name) {
            currentName = getConfigNames().first()
            ViewModelConfig.current = configs[currentName] ?: ViewModelConfig()
        }

        saveCurrent()
        return true
    }

    fun renameConfig(oldName: String, rawNewName: String): Boolean {
        val newName = sanitize(rawNewName)
        val config = configs[oldName] ?: return false
        if (configs.containsKey(newName)) return false

        configs.remove(oldName)
        File(configsDir, "$oldName.json").takeIf { it.exists() }?.renameTo(File(configsDir, "$newName.json"))
        configs[newName] = config

        if (currentName == oldName) {
            currentName = newName
        }

        saveCurrent()
        return true
    }

    private fun sanitize(name: String): String {
        val cleaned = name.replace("[\\\\/:*?\"<>|]".toRegex(), "").trim()
        return cleaned.ifEmpty { DEFAULT_NAME }
    }
}
