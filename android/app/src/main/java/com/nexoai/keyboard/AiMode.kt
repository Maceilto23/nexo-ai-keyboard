package com.nexoai.keyboard

enum class AiMode(val code: String, val label: String) {
    AUTO("auto", "✨ Auto"),
    REPLY("reply", "↩ Responder"),
    QUICK("quick", "⚡ Rápido"),
    OPTIMIZE("optimize", "✦ Otimizar"),
    PROFESSIONAL("professional", "💼 Profissional"),
    POLITE("polite", "🙂 Educado"),
    DIRECT("direct", "➜ Direto"),
    PERSUASIVE("persuasive", "🎯 Convincente"),
    NATURAL("natural", "💬 Natural"),
    FIRM("firm", "🛡 Firme"),
    CORRECT("correct", "✓ Corrigir"),
    ORGANIZE("organize", "☷ Organizar"),
    EXPAND("expand", "＋ Aumentar"),
    SUMMARIZE("summarize", "− Resumir"),
    FUNNY("funny", "😄 Engraçado"),
    IMPACTFUL("impactful", "⚡ Impactante"),
    AFFECTIONATE("affectionate", "❤ Carinhoso"),
    MY_STYLE("my_style", "◉ Minha Cara"),
    CUSTOM("custom", "✎ Personalizar");

    companion object {
        fun fromCode(code: String): AiMode = entries.firstOrNull { it.code == code } ?: AUTO
    }
}
