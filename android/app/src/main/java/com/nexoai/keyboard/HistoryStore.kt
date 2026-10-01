package com.nexoai.keyboard

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(val time: Long, val mode: String, val original: String, val result: String)

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("nexo_ai_history", Context.MODE_PRIVATE)
    private val config = ConfigStore(context)

    fun add(mode: String, original: String, result: String) {
        if (!config.historyEnabled) return
        val arr = JSONArray(prefs.getString("items", "[]"))
        val next = JSONArray()
        next.put(JSONObject().apply {
            put("time", System.currentTimeMillis())
            put("mode", mode)
            put("original", original.take(4000))
            put("result", result.take(4000))
        })
        for (i in 0 until minOf(arr.length(), 19)) next.put(arr.optJSONObject(i))
        prefs.edit().putString("items", next.toString()).apply()
    }

    fun recent(): List<HistoryItem> {
        val arr = JSONArray(prefs.getString("items", "[]"))
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(HistoryItem(o.optLong("time"), o.optString("mode"), o.optString("original"), o.optString("result")))
            }
        }
    }

    fun clear() { prefs.edit().remove("items").apply() }
}
