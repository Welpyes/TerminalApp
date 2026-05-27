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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class WebViewManager
private constructor(private val sharedPref: SharedPreferences) {
    private val lock = Any()
    private val listeners = mutableListOf<OnSettingsChangeListener>()

    interface OnSettingsChangeListener {
        fun onSettingsChanged()
    }

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == URL_KEY || key == FONT_SIZE_KEY || key == FONT_FAMILY_KEY || key == CUSTOM_FONT_PATH_KEY || key == CUSTOM_COLORS_PATH_KEY || key == EXTRA_KEYS_CONFIG_KEY) {
            synchronized(lock) {
                listeners.forEach { it.onSettingsChanged() }
            }
        }
    }

    init {
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener)
    }

    fun addListener(listener: OnSettingsChangeListener) {
        synchronized(lock) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: OnSettingsChangeListener) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }

    var webViewUrl: String
        get() =
            synchronized(lock) {
                val url = sharedPref.getString(URL_KEY, null)
                val defaultOption = "http://127.0.0.1:7681"
                return try {
                    url ?: defaultOption
                } catch (_: IllegalArgumentException) {
                    defaultOption
                }
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putString(URL_KEY, value) }
            }

    var extraKeysConfig: String
        get() =
            synchronized(lock) {
                return sharedPref.getString(EXTRA_KEYS_CONFIG_KEY, DEFAULT_EXTRA_KEYS) ?: DEFAULT_EXTRA_KEYS
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putString(EXTRA_KEYS_CONFIG_KEY, value) }
            }

    var fontSize: Int
        get() =
            synchronized(lock) {
                return sharedPref.getInt(FONT_SIZE_KEY, 14)
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putInt(FONT_SIZE_KEY, value) }
            }

    var fontFamily: String
        get() =
            synchronized(lock) {
                return sharedPref.getString(FONT_FAMILY_KEY, "monospace") ?: "monospace"
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putString(FONT_FAMILY_KEY, value) }
            }

    var customFontPath: String?
        get() =
            synchronized(lock) {
                return sharedPref.getString(CUSTOM_FONT_PATH_KEY, null)
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putString(CUSTOM_FONT_PATH_KEY, value) }
            }

    var customColorsPath: String?
        get() =
            synchronized(lock) {
                return sharedPref.getString(CUSTOM_COLORS_PATH_KEY, null)
            }
        set(value) =
            synchronized(lock) {
                sharedPref.edit { putString(CUSTOM_COLORS_PATH_KEY, value) }
            }

    companion object {
        private const val PREFS_NAME = ".WEBVIEW"
        private const val URL_KEY = "url"
        private const val FONT_SIZE_KEY = "font_size"
        private const val FONT_FAMILY_KEY = "font_family"
        private const val CUSTOM_FONT_PATH_KEY = "custom_font_path"
        private const val CUSTOM_COLORS_PATH_KEY = "custom_colors_path"

        @Volatile private var instance: WebViewManager? = null

        @Synchronized
        fun getInstance(context: Context): WebViewManager {
            // Use double-checked locking for thread safety.
            return instance
                ?: synchronized(this) {
                    instance
                        ?: run {
                            val sharedPref =
                                context.getSharedPreferences(
                                    context.packageName + PREFS_NAME,
                                    Context.MODE_PRIVATE,
                                )
                            WebViewManager(sharedPref).also { instance = it }
                        }
                }
        }
    }
}
