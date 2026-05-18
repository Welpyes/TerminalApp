/*
 * Copyright (C) 2026 The Android Open Source Project
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

(function() {
  window.applyFontSettings = function(fontSize, fontFamily) {
    const styleId = 'terminal-font-settings';
    let style = document.getElementById(styleId);
    if (!style) {
      style = document.createElement('style');
      style.id = styleId;
      document.head.appendChild(style);
    }
    style.textContent = `
      .xterm-rows {
        font-family: ${fontFamily} !important;
        font-size: ${fontSize}px !important;
      }
      .xterm-helper-textarea {
        font-family: ${fontFamily} !important;
        font-size: ${fontSize}px !important;
      }
      /* Ensure the cursor and other elements also use the font */
      .xterm {
        font-family: ${fontFamily} !important;
      }
    `;
    // Trigger window resize to help xterm.js recalculate dimensions if it's listening
    window.dispatchEvent(new Event('resize'));
  };
})();
