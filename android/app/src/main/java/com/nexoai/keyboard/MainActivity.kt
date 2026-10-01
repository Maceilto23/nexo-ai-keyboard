package com.nexoai.keyboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var config: ConfigStore
    private val executor = Executors.newSingleThreadExecutor()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = ConfigStore(this)
        window.statusBarColor = Color.rgb(7, 18, 31)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(36))
            setBackgroundColor(Color.rgb(7, 18, 31))
        }
        scroll.addView(root)

        fun title(text: String, size: Float = 18f) = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, dp(8))
        }
        fun field(hint: String, value: String, password: Boolean = false) = EditText(this).apply {
            this.hint = hint
            setHintTextColor(Color.rgb(130, 155, 180))
            setTextColor(Color.WHITE)
            setText(value)
            setBackgroundColor(Color.rgb(13, 34, 55))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            if (password) inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        fun button(text: String, action: () -> Unit) = Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { action() }
        }

        root.addView(title("NEXO AI", 30f))
        root.addView(TextView(this).apply {
            text = "Seu teclado inteligente para WhatsApp e qualquer campo de texto do Android. v3.1"
            textSize = 15f
            setTextColor(Color.rgb(176, 205, 225))
            setPadding(0, 0, 0, dp(16))
        })

        root.addView(button("1. Ativar teclado no Android") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(button("2. Escolher teclado agora") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        root.addView(button("Abrir site oficial do Nexo AI") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ConfigStore.OFFICIAL_SITE)))
        })

        root.addView(title("Conexão com a IA"))
        val endpoint = field("Endpoint HTTPS", config.endpoint)
        val token = field("APP_TOKEN", config.appToken, true)
        root.addView(endpoint, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        root.addView(space())
        root.addView(token, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        root.addView(button("Usar endpoint oficial") {
            endpoint.setText(ConfigStore.DEFAULT_ENDPOINT)
            Toast.makeText(this, "Endpoint oficial preenchido.", Toast.LENGTH_SHORT).show()
        })

        root.addView(title("Minha Cara"))
        val style = field("Como a IA deve escrever por você", config.personalStyle).apply {
            minLines = 3
            gravity = Gravity.TOP
        }
        root.addView(style, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        root.addView(title("Tamanho padrão"))
        val spinner = Spinner(this)
        val lengths = listOf("Automático", "Curto", "Médio", "Longo")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lengths)
        spinner.setSelection(when (config.outputLength) { "short" -> 1; "medium" -> 2; "long" -> 3; else -> 0 })
        root.addView(spinner)

        val outputLimit = field("Limite de caracteres da resposta (0 = automático)", config.outputMaxChars.toString()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(outputLimit, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        root.addView(title("Atalhos personalizados"))
        val customLabel1 = field("Nome do atalho 1", config.customLabel1)
        val customPrompt1 = field("Instrução do atalho 1", config.customPrompt1)
        val customLabel2 = field("Nome do atalho 2", config.customLabel2)
        val customPrompt2 = field("Instrução do atalho 2", config.customPrompt2)
        val customLabel3 = field("Nome do atalho 3", config.customLabel3)
        val customPrompt3 = field("Instrução do atalho 3", config.customPrompt3)
        listOf(customLabel1, customPrompt1, customLabel2, customPrompt2, customLabel3, customPrompt3).forEach {
            root.addView(it, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            root.addView(space())
        }

        val historySwitch = Switch(this).apply {
            text = "Salvar histórico local das transformações (opcional)"
            setTextColor(Color.WHITE)
            isChecked = config.historyEnabled
        }
        root.addView(historySwitch)
        root.addView(button("Ver histórico local") { showHistory() })

        val status = TextView(this).apply {
            setTextColor(Color.rgb(34, 211, 238))
            setPadding(0, dp(10), 0, dp(10))
        }
        root.addView(status)

        root.addView(button("Salvar configurações") {
            config.endpoint = endpoint.text.toString()
            config.appToken = token.text.toString()
            config.personalStyle = style.text.toString()
            config.outputLength = when (spinner.selectedItemPosition) { 1 -> "short"; 2 -> "medium"; 3 -> "long"; else -> "auto" }
            config.outputMaxChars = outputLimit.text.toString().toIntOrNull() ?: 0
            config.historyEnabled = historySwitch.isChecked
            config.customLabel1 = customLabel1.text.toString(); config.customPrompt1 = customPrompt1.text.toString()
            config.customLabel2 = customLabel2.text.toString(); config.customPrompt2 = customPrompt2.text.toString()
            config.customLabel3 = customLabel3.text.toString(); config.customPrompt3 = customPrompt3.text.toString()
            status.text = "Configurações salvas."
        })

        root.addView(button("Testar IA") {
            config.endpoint = endpoint.text.toString()
            config.appToken = token.text.toString()
            config.personalStyle = style.text.toString()
            status.text = "Testando conexão…"
            executor.execute {
                val result = AiApiClient(this).transform("Olá! Este é um teste do Nexo AI.", AiMode.OPTIMIZE)
                runOnUiThread { status.text = result.fold({ "IA online: $it" }, { "Erro: ${it.message}" }) }
            }
        })

        root.addView(title("Balão flutuante"))
        val overlaySwitch = Switch(this).apply {
            text = "Ativar assistente flutuante"
            setTextColor(Color.WHITE)
            isChecked = config.overlayEnabled && Settings.canDrawOverlays(this@MainActivity)
        }
        root.addView(overlaySwitch)
        overlaySwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (!Settings.canDrawOverlays(this)) {
                    overlaySwitch.isChecked = false
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    requestNotificationIfNeeded()
                    config.overlayEnabled = true
                    startForegroundService(Intent(this, OverlayService::class.java))
                    status.text = "Balão ativado."
                }
            } else {
                config.overlayEnabled = false
                stopService(Intent(this, OverlayService::class.java))
                status.text = "Balão desativado."
            }
        }

        root.addView(title("Privacidade"))
        root.addView(TextView(this).apply {
            text = "A IA só recebe texto quando você toca em uma ação. Campos de senha bloqueiam o envio. A chave OpenAI fica apenas no servidor."
            setTextColor(Color.rgb(176, 205, 225))
            textSize = 14f
        })

        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        if (config.overlayEnabled && Settings.canDrawOverlays(this)) {
            try { startForegroundService(Intent(this, OverlayService::class.java)) } catch (_: Exception) {}
        }
    }

    private fun showHistory() {
        val items = HistoryStore(this).recent()
        val message = if (items.isEmpty()) "O histórico local está vazio." else items.joinToString("\n\n") {
            "${it.mode}\nAntes: ${it.original.take(180)}\nDepois: ${it.result.take(220)}"
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Histórico local")
            .setMessage(message)
            .setPositiveButton("Fechar", null)
            .setNegativeButton("Limpar") { _, _ -> HistoryStore(this).clear() }
            .show()
    }

    private fun requestNotificationIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun space() = Space(this).apply { minimumHeight = dp(8) }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
