/*
 * Copyright (C) 2025 The Android Open Source Project
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
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper

class ModifierKeysController(val activity: MainActivity, val parent: ViewGroup) {
    private val window = activity.window
    private var activeTerminalView: TerminalView? = null
    private val webViewManager = WebViewManager.getInstance(activity)

    init {
        // Setup for the update to be called when needed
        window.decorView.rootView.setOnApplyWindowInsetsListener { _: View?, insets: WindowInsets ->
            update()
            insets
        }
    }

    fun addTerminalView(terminalView: TerminalView) {
        terminalView.setOnFocusChangeListener { _: View, onFocus: Boolean ->
            if (onFocus) {
                activeTerminalView = terminalView
            } else {
                activeTerminalView = null
                terminalView.disableCtrlKey()
            }
            update()
        }
    }

    fun update() {
        parent.removeAllViews()
        
        // Pass if no TerminalView focused.
        if (activeTerminalView == null) {
            parent.visibility = View.GONE
            return
        }

        val config = webViewManager.extraKeysConfig
        val rows = ExtraKeysHelper.parse(config)
        
        val container = LinearLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        for (rowKeys in rows) {
            val rowLayout = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            for (extraKey in rowKeys) {
                val btn = createButton(extraKey)
                rowLayout.addView(btn)
            }
            container.addView(rowLayout)
        }

        parent.addView(container)
        parent.visibility = if (needToShowKeys()) View.VISIBLE else View.GONE
    }

    private fun createButton(extraKey: ExtraKey): Button {
        // Use ContextThemeWrapper to apply ModifierKeyStyle
        val btn = Button(ContextThemeWrapper(activity, R.style.ModifierKeyStyle), null, 0)
        
        btn.text = extraKey.display ?: extraKey.key
        btn.textSize = 10f
        
        btn.setOnClickListener {
            handleKeyPress(extraKey)
        }
        
        return btn
    }

    private fun handleKeyPress(extraKey: ExtraKey) {
        val terminal = activeTerminalView ?: return
        
        if (extraKey.macro != null) {
            // TODO: Implement macro handling
            // For now, treat as single key if macro is just one word
            dispatchKey(extraKey.key)
            return
        }

        if (extraKey.key == "CTRL") {
            terminal.mapCtrlKey()
            terminal.enableCtrlKey()
        } else {
            dispatchKey(extraKey.key)
        }
    }

    private fun dispatchKey(keyName: String) {
        val terminal = activeTerminalView ?: return
        val keyCode = ExtraKeysHelper.getKeyCode(keyName)
        
        if (keyCode != null && keyCode != -1) {
            terminal.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            terminal.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } else if (keyName.length == 1) {
            // Handle literal characters
            val event = KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_UNKNOWN, 0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD)
            // WebView should handle characters. For now, we might need a better way to send literals.
            // Termux usually sends characters via the terminal emulator. 
            // In WebView, we might need to use evaluateJavascript to trigger input events.
        }
    }

    // Modifier keys are required only when IME is shown and the HW qwerty keyboard is not present
    private fun needToShowKeys(): Boolean {
        // For debugging/MVP, always show if terminal is focused
        return activeTerminalView != null
    }
}
