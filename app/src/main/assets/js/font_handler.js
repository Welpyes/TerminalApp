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
  window.applyFontSettings = function(fontSize, fontFamily, customFontBase64, theme) {
    const styleId = 'terminal-font-settings';
    let style = document.getElementById(styleId);
    if (!style) {
      style = document.createElement('style');
      style.id = styleId;
      document.head.appendChild(style);
    }

    let fontFace = '';
    let finalFontFamily = fontFamily;

    if (customFontBase64) {
      finalFontFamily = 'CustomTerminalFont';
      fontFace = `
        @font-face {
          font-family: 'CustomTerminalFont';
          src: url(data:font/ttf;base64,${customFontBase64});
        }
      `;
    }

    style.textContent = `
      ${fontFace}
      .xterm-rows {
        font-family: ${finalFontFamily} !important;
        font-size: ${fontSize}px !important;
      }
      .xterm-helper-textarea {
        font-family: ${finalFontFamily} !important;
        font-size: ${fontSize}px !important;
      }
      .xterm {
        font-family: ${finalFontFamily} !important;
      }
    `;

    // xterm.js specific update via window.term
    if (window.term) {
      window.term.options.fontSize = fontSize;
      window.term.options.fontFamily = finalFontFamily;
      
      if (theme) {
        window.term.options.theme = theme;
      }

      // Force refresh of the terminal
      if (window.term.refresh) {
          window.term.refresh(0, window.term.rows - 1);
      }
    }

    window.dispatchEvent(new Event('resize'));
  };
})();
