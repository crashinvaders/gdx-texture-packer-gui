/*
 * Copyright 2014-2016 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.ui.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.utils.FloatArray;

/**
 * {@link VisTextField} with extended functionality/fixes (search for "//PATCH:" for details).
 */
public class PatchedVisTextField extends VisTextField {
	static private final char BULLET = 8226;

	private StringBuilder passwordBuffer;
	private char passwordCharacter = BULLET;

	public PatchedVisTextField() {
		super();
	}

	public PatchedVisTextField(String text) {
		super(text);
	}

	public PatchedVisTextField(String text, String styleName) {
		super(text, styleName);
	}

	public PatchedVisTextField(String text, VisTextFieldStyle style) {
		super(text, style);
	}

	@Override
	public void setPasswordCharacter(char passwordCharacter) {
		this.passwordCharacter = passwordCharacter;
		super.setPasswordCharacter(passwordCharacter);
	}

	@Override
	void updateDisplayText () {
		BitmapFont font = style.font;
		BitmapFontData data = font.getData();
		String text = this.text;
		int textLength = text.length();

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < textLength; i++) {
			char c = text.charAt(i);
			buffer.append(data.hasGlyph(c) ? c : ' ');
		}
		String newDisplayText = buffer.toString();

		if (passwordMode && data.hasGlyph(passwordCharacter)) {
			if (passwordBuffer == null) passwordBuffer = new StringBuilder(newDisplayText.length());
			if (passwordBuffer.length() > textLength)
				passwordBuffer.setLength(textLength);
			else {
				for (int i = passwordBuffer.length(); i < textLength; i++)
					passwordBuffer.append(passwordCharacter);
			}
			displayText = passwordBuffer;
		} else
			displayText = newDisplayText;

		//PATCH: Disabled color markup for BitmapFont when updating GlyphLayout https://github.com/libgdx/libgdx/issues/4576
		BitmapFontData fontData = font.getData();
		boolean markupEnabled = fontData.markupEnabled;
		fontData.markupEnabled = false;
		layout.setText(font, displayText);
		fontData.markupEnabled = markupEnabled;

		glyphPositions.clear();
		float x = 0;
		if (layout.runs.size > 0) {
			GlyphRun run = layout.runs.first();
			fontOffset = run.xAdvances.first();

			for (GlyphRun glyphRun : layout.runs) {
				FloatArray xAdvances = glyphRun.xAdvances;
				for (int i = 1, n = xAdvances.size; i < n; i++) {
					glyphPositions.add(x);
					x += xAdvances.get(i);
				}
				glyphPositions.add(x);
			}
		} else {
			fontOffset = 0;
		}
		glyphPositions.add(x);

		if (selectionStart > newDisplayText.length()) selectionStart = textLength;
	}

	@Override
	protected void drawText(Batch batch, BitmapFont font, float x, float y) {
		//PATCH: Disabled color markup for BitmapFont when updating GlyphLayout https://github.com/libgdx/libgdx/issues/4576
		BitmapFontData fontData = font.getData();
		boolean markupEnabled = fontData.markupEnabled;
		fontData.markupEnabled = false;
		super.drawText(batch, font, x, y);
		fontData.markupEnabled = markupEnabled;
	}
}
