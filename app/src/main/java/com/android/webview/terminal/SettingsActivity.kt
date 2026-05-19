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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent


class SettingsActivity : AppCompatActivity() {

    private val pickFontLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // Persist access to the URI
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                WebViewManager.getInstance(this).customFontPath = uri.toString()
            }
        }
    }

    fun pickFont() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            val mimeTypes = arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        pickFontLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar: MaterialToolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        val settingsItems = mutableListOf<SettingsItem>()

        settingsItems.add(
            SettingsItem(
                resources.getString(R.string.settings_webview_url_title),
                resources.getString(R.string.settings_webview_url_subtitle),
                R.drawable.ic_display,
                SettingsItemEnum.WebViewSettingsItem,
            )
        )

        settingsItems.add(
            SettingsItem(
                resources.getString(R.string.settings_font_size_title),
                resources.getString(R.string.settings_font_size_subtitle),
                R.drawable.ic_display,
                SettingsItemEnum.FontSizeSettingsItem,
            )
        )

        settingsItems.add(
            SettingsItem(
                resources.getString(R.string.settings_font_family_title),
                resources.getString(R.string.settings_font_family_subtitle),
                R.drawable.ic_display,
                SettingsItemEnum.FontFamilySettingsItem,
            )
        )

        settingsItems.add(
            SettingsItem(
                resources.getString(R.string.settings_font_picker_title),
                resources.getString(R.string.settings_font_picker_subtitle),
                R.drawable.baseline_storage_24,
                SettingsItemEnum.FontPickerSettingsItem,
            )
        )

        val settingsListItemAdapter = SettingsItemAdapter(settingsItems)

        val recyclerView: RecyclerView = findViewById(R.id.settings_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = settingsListItemAdapter
    }
}
