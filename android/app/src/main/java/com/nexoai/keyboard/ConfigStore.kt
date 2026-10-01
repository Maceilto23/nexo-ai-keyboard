package com.nexoai.keyboard

import android.content.Context

class ConfigStore(context: Context) {
    companion object {
        const val DEFAULT_ENDPOINT = "https://nexo-keyboard-api.onrender.com/api.php"
        const val OFFICIAL_SITE = "https://nexo-keyboard.page.gd"
    }
    private val prefs = context.getSharedPreferences("nexo_ai_config", Context.MODE_PRIVATE)

    var endpoint: String
        get() = prefs.getString("endpoint", DEFAULT_ENDPOINT) ?: ""
        set(value) = prefs.edit().putString("endpoint", value.trim()).apply()

    var appToken: String
        get() = prefs.getString("app_token", "") ?: ""
        set(value) = prefs.edit().putString("app_token", value.trim()).apply()

    var personalStyle: String
        get() = prefs.getString("personal_style", "Natural, direto, inteligente, cordial e com português brasileiro atual.") ?: ""
        set(value) = prefs.edit().putString("personal_style", value.trim()).apply()

    var outputLength: String
        get() = prefs.getString("output_length", "auto") ?: "auto"
        set(value) = prefs.edit().putString("output_length", value).apply()

    var maxChars: Int
        get() = prefs.getInt("max_chars", 12000).coerceIn(500, 12000)
        set(value) = prefs.edit().putInt("max_chars", value.coerceIn(500, 12000)).apply()

    var outputMaxChars: Int
        get() = prefs.getInt("output_max_chars", 0).coerceIn(0, 4000)
        set(value) = prefs.edit().putInt("output_max_chars", value.coerceIn(0, 4000)).apply()

    var historyEnabled: Boolean
        get() = prefs.getBoolean("history_enabled", false)
        set(value) = prefs.edit().putBoolean("history_enabled", value).apply()

    var customLabel1: String
        get() = prefs.getString("custom_label_1", "Meu Atalho 1") ?: "Meu Atalho 1"
        set(value) = prefs.edit().putString("custom_label_1", value.trim()).apply()
    var customPrompt1: String
        get() = prefs.getString("custom_prompt_1", "") ?: ""
        set(value) = prefs.edit().putString("custom_prompt_1", value.trim()).apply()
    var customLabel2: String
        get() = prefs.getString("custom_label_2", "Meu Atalho 2") ?: "Meu Atalho 2"
        set(value) = prefs.edit().putString("custom_label_2", value.trim()).apply()
    var customPrompt2: String
        get() = prefs.getString("custom_prompt_2", "") ?: ""
        set(value) = prefs.edit().putString("custom_prompt_2", value.trim()).apply()
    var customLabel3: String
        get() = prefs.getString("custom_label_3", "Meu Atalho 3") ?: "Meu Atalho 3"
        set(value) = prefs.edit().putString("custom_label_3", value.trim()).apply()
    var customPrompt3: String
        get() = prefs.getString("custom_prompt_3", "") ?: ""
        set(value) = prefs.edit().putString("custom_prompt_3", value.trim()).apply()

    var overlayEnabled: Boolean
        get() = prefs.getBoolean("overlay_enabled", false)
        set(value) = prefs.edit().putBoolean("overlay_enabled", value).apply()
}
