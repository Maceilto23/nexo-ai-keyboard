package com.nexoai.keyboard

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.concurrent.Executors

class ProcessTextActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var input: EditText
    private lateinit var output: EditText
    private lateinit var status: TextView
    private var processTextMode = false

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processTextMode = intent.action == Intent.ACTION_PROCESS_TEXT
        val incoming = when {
            processTextMode -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
            intent.hasExtra("text") -> intent.getStringExtra("text").orEmpty()
            else -> clipboardText()
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.rgb(11, 23, 38))
        }
        root.addView(TextView(this).apply {
            text = "Nexo AI"
            textSize = 24f
            setTextColor(Color.WHITE)
        })
        input = EditText(this).apply {
            setText(incoming)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            hint = "Texto para trabalhar"
            minLines = 3
            gravity = Gravity.TOP
        }
        root.addView(input, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val custom = EditText(this).apply {
            hint = "Instrução personalizada (opcional)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }
        root.addView(custom, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val scroll = HorizontalScrollView(this)
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        scroll.addView(actions)
        listOf(AiMode.AUTO, AiMode.REPLY, AiMode.OPTIMIZE, AiMode.PROFESSIONAL, AiMode.CORRECT, AiMode.ORGANIZE, AiMode.SUMMARIZE, AiMode.DIRECT, AiMode.POLITE, AiMode.IMPACTFUL, AiMode.MY_STYLE).forEach { mode ->
            actions.addView(Button(this).apply {
                text = mode.label
                isAllCaps = false
                setOnClickListener { runAi(mode, custom.text.toString()) }
            })
        }
        root.addView(scroll)

        status = TextView(this).apply {
            setTextColor(Color.rgb(34, 211, 238))
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(status)

        output = EditText(this).apply {
            hint = "Resultado"
            minLines = 4
            gravity = Gravity.TOP
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }
        root.addView(output, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        bottom.addView(Button(this).apply {
            text = if (processTextMode) "Usar texto" else "Copiar"
            isAllCaps = false
            setOnClickListener {
                val value = output.text.toString().ifBlank { input.text.toString() }
                if (processTextMode) {
                    setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, value))
                    finish()
                } else {
                    copy(value)
                    status.text = "Resultado copiado."
                }
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bottom.addView(Button(this).apply {
            text = "Fechar"
            isAllCaps = false
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(bottom)

        setContentView(root)
    }

    private fun runAi(mode: AiMode, customInstruction: String) {
        val text = input.text.toString().trim()
        if (text.isBlank()) { status.text = "Digite ou selecione algum texto."; return }
        status.text = "Nexo AI pensando…"
        executor.execute {
            val result = AiApiClient(this).transform(text, if (customInstruction.isBlank()) mode else AiMode.CUSTOM, customInstruction = customInstruction)
            runOnUiThread {
                result.onSuccess { output.setText(it); status.text = "Pronto." }
                    .onFailure { status.text = "Erro: ${it.message}" }
            }
        }
    }

    private fun clipboardText(): String {
        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return cb.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
    }
    private fun copy(text: String) {
        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("Nexo AI", text))
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
