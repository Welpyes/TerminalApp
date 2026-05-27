/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.webview.terminal

import android.view.KeyEvent
import org.json.JSONArray
import org.json.JSONException

data class ExtraKey(
    val key: String,
    val display: String? = null,
    val popup: Any? = null,
    val macro: String? = null
)

object ExtraKeysHelper {
    private val KEY_MAP = mapOf(
        "ESC" to KeyEvent.KEYCODE_ESCAPE,
        "TAB" to KeyEvent.KEYCODE_TAB,
        "CTRL" to -1, // Special handling
        "ALT" to KeyEvent.KEYCODE_ALT_LEFT,
        "UP" to KeyEvent.KEYCODE_DPAD_UP,
        "DOWN" to KeyEvent.KEYCODE_DPAD_DOWN,
        "LEFT" to KeyEvent.KEYCODE_DPAD_LEFT,
        "RIGHT" to KeyEvent.KEYCODE_DPAD_RIGHT,
        "HOME" to KeyEvent.KEYCODE_MOVE_HOME,
        "END" to KeyEvent.KEYCODE_MOVE_END,
        "PGUP" to KeyEvent.KEYCODE_PAGE_UP,
        "PGDN" to KeyEvent.KEYCODE_PAGE_DOWN,
        "F1" to KeyEvent.KEYCODE_F1,
        "F2" to KeyEvent.KEYCODE_F2,
        "F3" to KeyEvent.KEYCODE_F3,
        "F4" to KeyEvent.KEYCODE_F4,
        "F5" to KeyEvent.KEYCODE_F5,
        "F6" to KeyEvent.KEYCODE_F6,
        "F7" to KeyEvent.KEYCODE_F7,
        "F8" to KeyEvent.KEYCODE_F8,
        "F9" to KeyEvent.KEYCODE_F9,
        "F10" to KeyEvent.KEYCODE_F10,
        "F11" to KeyEvent.KEYCODE_F11,
        "F12" to KeyEvent.KEYCODE_F12,
        "BKSP" to KeyEvent.KEYCODE_DEL,
        "DEL" to KeyEvent.KEYCODE_FORWARD_DEL,
        "INS" to KeyEvent.KEYCODE_INSERT,
        "ENTER" to KeyEvent.KEYCODE_ENTER,
        "SPACE" to KeyEvent.KEYCODE_SPACE
    )

    fun parse(config: String): List<List<ExtraKey>> {
        val result = mutableListOf<List<ExtraKey>>()
        try {
            val root = JSONArray(config.replace("'", "\""))
            for (i in 0 until root.length()) {
                val rowArray = root.getJSONArray(i)
                val row = mutableListOf<ExtraKey>()
                for (j in 0 until rowArray.length()) {
                    val obj = rowArray.get(j)
                    if (obj is String) {
                        row.add(ExtraKey(key = obj))
                    } else if (obj is org.json.JSONObject) {
                        row.add(ExtraKey(
                            key = obj.optString("key"),
                            display = obj.optString("display", null),
                            popup = obj.opt("popup"),
                            macro = obj.optString("macro", null)
                        ))
                    }
                }
                result.add(row)
            }
        } catch (e: JSONException) {
            // Fallback to minimal keys on error
            return listOf(listOf(ExtraKey("ESC"), ExtraKey("TAB"), ExtraKey("CTRL"), ExtraKey("ALT")))
        }
        return result
    }

    fun getKeyCode(keyName: String): Int? = KEY_MAP[keyName]
}
