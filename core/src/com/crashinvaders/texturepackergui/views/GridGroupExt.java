/*
 * Copyright 2014-2017 See AUTHORS file.
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

package com.crashinvaders.texturepackergui.views;

import com.kotcrab.vis.ui.layout.GridGroup;

//TODO Remove when the related issue is resolved in VisUI
/**
 * Same old VisUI's {@link com.kotcrab.vis.ui.layout.GridGroup}, but has a fix for proper layout scaling.
 * @see <a href="https://github.com/kotcrab/vis-ui/issues/218">The related issue</a>
 */
public class GridGroupExt extends GridGroup {

	@Override
	public float getMinWidth() {
		return 0f;
	}

	@Override
	public float getMinHeight() {
		return 0f;
	}
}
