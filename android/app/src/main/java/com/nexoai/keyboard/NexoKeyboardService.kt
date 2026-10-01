package com.nexoai.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.widget.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class NexoKeyboardService : InputMethodService() {
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val requestSeq = AtomicLong(0)
    private lateinit var config: ConfigStore
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private var caps = false
    private var symbols = false
    private var secureField = false
    private var lastUndo: UndoState? = null

    data class TargetSnapshot(
        val text: String,
        val fullExtractedText: String,
        val startOffset: Int,
        val selectionStart: Int,
        val selectionEnd: Int,
        val selectedOnly: Boolean,
        val packageName: String?,
        val fromClipboard: Boolean = false
    )

    data class UndoState(val inserted: String, val original: String, val packageName: String?)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        config = ConfigStore(this)
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(6))
            setBackgroundColor(Color.rgb(5, 17, 29))
        }
        rebuildKeyboard()
        return root
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        secureField = isPasswordInput(attribute?.inputType ?: 0)
        lastUndo = null
        if (::status.isInitialized) status.text = if (secureField) "🔒 Modo seguro — IA desativada" else "Nexo AI pronto"
    }

    private fun isPasswordInput(inputType: Int): Boolean {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val cls = inputType and InputType.TYPE_MASK_CLASS
        return (cls == InputType.TYPE_CLASS_TEXT && variation in setOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        )) || (cls == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    private fun rebuildKeyboard() {
        root.removeAllViews()
        root.addView(buildAiBar())
        status = TextView(this).apply {
            text = if (secureField) "🔒 Modo seguro — IA desativada" else "Nexo AI pronto"
            setTextColor(Color.rgb(120, 220, 238))
            textSize = 11f
            setPadding(dp(8), dp(3), dp(8), dp(3))
            maxLines = 1
        }
        root.addView(status)
        root.addView(buildAccentBar())
        if (symbols) buildSymbolRows() else buildLetterRows()
    }

    private fun buildAiBar(): View {
        val scroller = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val modes = listOf(AiMode.AUTO, AiMode.REPLY, AiMode.OPTIMIZE, AiMode.PROFESSIONAL, AiMode.CORRECT, AiMode.ORGANIZE, AiMode.SUMMARIZE, AiMode.DIRECT, AiMode.POLITE, AiMode.IMPACTFUL, AiMode.MY_STYLE)
        modes.forEach { mode -> row.addView(chip(mode.label) { runAi(mode) }) }
        listOf(
            config.customLabel1 to config.customPrompt1,
            config.customLabel2 to config.customPrompt2,
            config.customLabel3 to config.customPrompt3
        ).filter { it.second.isNotBlank() }.forEach { (label, prompt) ->
            row.addView(chip("★ $label") { runCustom(prompt) })
        }
        row.addView(chip("↶ Desfazer") { undoAi() })
        row.addView(chip("⚙") { startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
        scroller.addView(row)
        return scroller
    }

    private fun buildAccentBar(): View {
        val scroller = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("á","é","í","ó","ú","ã","õ","â","ê","ô","ç","?","!",":",";").forEach { s ->
            row.addView(chip(s) { commit(s) })
        }
        scroller.addView(row)
        return scroller
    }

    private fun buildLetterRows() {
        addKeyRow(listOf("q","w","e","r","t","y","u","i","o","p"))
        addKeyRow(listOf("a","s","d","f","g","h","j","k","l","ç"))
        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(key("⇧", 1.3f) { caps = !caps; rebuildKeyboard() })
        listOf("z","x","c","v","b","n","m").forEach { row3.addView(key(if (caps) it.uppercase() else it) { commit(if (caps) it.uppercase() else it) }) }
        row3.addView(key("⌫", 1.3f) { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL) })
        root.addView(row3)
        addBottomRow()
    }

    private fun buildSymbolRows() {
        addKeyRow(listOf("1","2","3","4","5","6","7","8","9","0"))
        addKeyRow(listOf("@","#","$","%","&","*","(",")","-","_"))
        addKeyRow(listOf("+","=","/","\\","[","]","{","}","<",">"))
        addBottomRow()
    }

    private fun addKeyRow(values: List<String>) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        values.forEach { value -> row.addView(key(if (caps && !symbols) value.uppercase() else value) { commit(if (caps && !symbols) value.uppercase() else value) }) }
        root.addView(row)
    }

    private fun addBottomRow() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(key(if (symbols) "ABC" else "123", 1.25f) { symbols = !symbols; rebuildKeyboard() })
        row.addView(key(",") { commit(",") })
        row.addView(key("espaço", 4.3f) { commit(" ") })
        row.addView(key(".") { commit(".") })
        row.addView(key("↵", 1.2f) { handleEnter() })
        row.addView(key("🌐", 1.1f) { switchToNextInputMethod(false) })
        root.addView(row)
    }

    private fun key(label: String, weight: Float = 1f, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = if (label.length > 2) 12f else 18f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(24, 48, 70))
        setPadding(0, 0, 0, 0)
        minWidth = 0
        minimumWidth = 0
        layoutParams = LinearLayout.LayoutParams(0, dp(46), weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        setOnClickListener { action() }
    }

    private fun chip(label: String, action: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 12f
        setTextColor(Color.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER
        setPadding(dp(12), dp(8), dp(12), dp(8))
        setBackgroundColor(Color.rgb(13, 83, 108))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        setOnClickListener { action() }
    }

    private fun commit(value: String) {
        currentInputConnection?.commitText(value, 1)
        if (caps && value.length == 1 && value[0].isLetter()) {
            caps = false
            rebuildKeyboard()
        }
    }

    private fun handleEnter() {
        val info = currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            currentInputConnection?.performEditorAction(action)
        } else {
            sendKeyChar('\n')
        }
    }

    private fun runAi(mode: AiMode) {
        if (secureField) { status.text = "🔒 IA bloqueada em campo de senha"; return }
        val snapshot = snapshotTarget() ?: run { status.text = "Digite ou selecione um texto primeiro"; return }
        if (snapshot.text.length > config.maxChars) { status.text = "Texto acima de ${config.maxChars} caracteres"; return }
        val id = requestSeq.incrementAndGet()
        status.text = "${mode.label}: pensando…"
        executor.execute {
            val result = AiApiClient(this).transform(snapshot.text, mode)
            main.post {
                if (id != requestSeq.get()) return@post
                result.onSuccess { applyResult(snapshot, it, mode.label) }
                    .onFailure { status.text = "Erro: ${it.message ?: "falha na IA"}" }
            }
        }
    }

    private fun snapshotTarget(): TargetSnapshot? {
        val ic = currentInputConnection ?: return null
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return null
        val full = extracted.text?.toString().orEmpty()
        if (full.isBlank()) {
            val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cb.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim().orEmpty()
            if (clip.isBlank()) return null
            status.text = "Usando o texto copiado"
            return TargetSnapshot(clip, full, extracted.startOffset, 0, 0, false, currentInputEditorInfo?.packageName, true)
        }
        val s = extracted.selectionStart.coerceIn(0, full.length)
        val e = extracted.selectionEnd.coerceIn(0, full.length)
        val lo = minOf(s, e)
        val hi = maxOf(s, e)
        val selected = hi > lo
        val target = if (selected) full.substring(lo, hi) else full
        return TargetSnapshot(target, full, extracted.startOffset, lo, hi, selected, currentInputEditorInfo?.packageName)
    }

    private fun runCustom(prompt: String) {
        if (secureField) { status.text = "🔒 IA bloqueada em campo de senha"; return }
        val snapshot = snapshotTarget() ?: run { status.text = "Digite, selecione ou copie um texto"; return }
        val id = requestSeq.incrementAndGet()
        status.text = "Atalho personalizado: pensando…"
        executor.execute {
            val result = AiApiClient(this).transform(snapshot.text, AiMode.CUSTOM, customInstruction = prompt)
            main.post {
                if (id != requestSeq.get()) return@post
                result.onSuccess { applyResult(snapshot, it, "Personalizado") }
                    .onFailure { status.text = "Erro: ${it.message ?: "falha na IA"}" }
            }
        }
    }

    private fun applyResult(snapshot: TargetSnapshot, result: String, modeLabel: String) {
        val ic = currentInputConnection ?: return
        if (currentInputEditorInfo?.packageName != snapshot.packageName) {
            copyResult(result); status.text = "Campo mudou; resultado copiado"; return
        }
        val current = ic.getExtractedText(ExtractedTextRequest(), 0)
        val currentFull = current?.text?.toString().orEmpty()
        if (snapshot.fromClipboard) {
            if (current == null || currentFull.isNotBlank()) { copyResult(result); status.text = "Campo mudou; resultado copiado"; return }
            ic.commitText(result, 1)
            lastUndo = UndoState(result, "", snapshot.packageName)
            HistoryStore(this).add(modeLabel, snapshot.text, result)
            status.text = "✓ Resposta inserida a partir do texto copiado"
            return
        }
        if (current == null || currentFull != snapshot.fullExtractedText || current.startOffset != snapshot.startOffset) {
            copyResult(result); status.text = "Texto mudou durante o processamento; resultado copiado"; return
        }
        val absoluteStart = snapshot.startOffset + if (snapshot.selectedOnly) snapshot.selectionStart else 0
        val absoluteEnd = snapshot.startOffset + if (snapshot.selectedOnly) snapshot.selectionEnd else snapshot.fullExtractedText.length
        if (!ic.setSelection(absoluteStart, absoluteEnd)) {
            copyResult(result); status.text = "Não foi possível substituir; resultado copiado"; return
        }
        ic.commitText(result, 1)
        lastUndo = UndoState(result, snapshot.text, snapshot.packageName)
        HistoryStore(this).add(modeLabel, snapshot.text, result)
        status.text = "✓ Pronto — toque em Desfazer se quiser voltar"
    }

    private fun undoAi() {
        val undo = lastUndo ?: run { status.text = "Nada para desfazer"; return }
        if (currentInputEditorInfo?.packageName != undo.packageName) { status.text = "O campo mudou"; return }
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(undo.inserted.length, 0)?.toString().orEmpty()
        if (before != undo.inserted) { status.text = "O texto foi alterado; desfazer cancelado"; return }
        ic.deleteSurroundingText(undo.inserted.length, 0)
        ic.commitText(undo.original, 1)
        lastUndo = null
        status.text = "↶ Texto original restaurado"
    }

    private fun copyResult(result: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("Nexo AI", result))
    }

    override fun onDestroy() {
        requestSeq.incrementAndGet()
        executor.shutdownNow()
        super.onDestroy()
    }
}
