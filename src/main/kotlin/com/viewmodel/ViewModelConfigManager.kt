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

        ensureDefaultExists()

        val savedName = activeFile.takeIf { it.exists() }?.readText()?.trim().orEmpty()
        currentName = if (configs.containsKey(savedName)) savedName else DEFAULT_NAME

        ViewModelConfig.current = configs[currentName] ?: ViewModelConfig()

        saveCurrent()
    }

    fun getConfigNames(): List<String> = configs.keys.sorted()

    fun isDefault(name: String): Boolean = name == DEFAULT_NAME

    fun saveCurrent() {
        configsDir.mkdirs()
        val activeConfig = ViewModelConfig.current
        configs[currentName] = activeConfig

        persist(currentName, activeConfig)
        persistActiveName()
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
        val name = chooseName(rawName)
        if (configs.containsKey(name)) return false

        val config = ViewModelConfig()
        configs[name] = config
        persist(name, config)
        persistActiveName()
        return true
    }

    fun deleteConfig(name: String): Boolean {
        if (!configs.containsKey(name) || configs.size <= 1 || name == DEFAULT_NAME) return false

        configs.remove(name)
        File(configsDir, "$name.json").takeIf { it.exists() }?.delete()

        if (currentName == name) {
            ensureDefaultExists()
            currentName = getConfigNames().first()
            ViewModelConfig.current = configs[currentName] ?: ViewModelConfig()
        }

        saveCurrent()
        return true
    }

    fun renameConfig(oldName: String, rawNewName: String): Boolean {
        if (oldName == DEFAULT_NAME) return false

        val newName = chooseName(rawNewName)
        val config = configs[oldName] ?: return false
        if (configs.containsKey(newName) && newName != oldName) return false

        if (newName == oldName) {
            nameUnchangedPersist(oldName, config)
            return true
        }

        configs.remove(oldName)
        File(configsDir, "$oldName.json").takeIf { it.exists() }?.renameTo(File(configsDir, "$newName.json"))
        configs[newName] = config

        if (currentName == oldName) {
            currentName = newName
        }

        persist(newName, config)
        persistActiveName()
        return true
    }

    private fun ensureDefaultExists() {
        if (!configs.containsKey(DEFAULT_NAME)) {
            configs[DEFAULT_NAME] = ViewModelConfig()
            println("[ViewModel] Default config created")
        }
    }

    private fun nameUnchangedPersist(name: String, config: ViewModelConfig) {
        persist(name, config)
        if (currentName == name) {
            persistActiveName()
        }
    }

    private fun persist(name: String, config: ViewModelConfig) {
        val file = File(configsDir, "$name.json")
        runCatching { file.writeText(json.encodeToString(config)) }
            .onFailure { println("[ViewModel] Failed to save config $name: ${it.message}") }
    }

    private fun persistActiveName() {
        runCatching {
            activeFile.parentFile?.mkdirs()
            activeFile.writeText(currentName)
        }.onFailure { println("[ViewModel] Failed to save active config: ${it.message}") }
    }

    private fun chooseName(rawName: String): String {
        val cleaned = sanitize(rawName)
        if (cleaned.isNotEmpty()) return cleaned

        var index = 1
        var candidate: String
        do {
            candidate = "Config $index"
            index++
        } while (configs.containsKey(candidate))

        return candidate
    }

    private fun sanitize(name: String): String {
        val cleaned = name.replace("[\\\\/:*?\"<>|]".toRegex(), "").trim()
        return cleaned
    }
}
