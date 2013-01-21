/*
 * Copyright (C) 2013 by HasaIT (hasait at web dot de)
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

package de.hasait.clap;

import de.hasait.clap.CLAPDecisionA;
import de.hasait.clap.CLAPOptionA;

/**
 * Class used by {@link CLAPTest}.
 */
public class CLAPTypeC {

	private Boolean _boolean;

	private Object _object;

	public Boolean getBoolean() {
		return _boolean;
	}

	public Object getObject() {
		return _object;
	}

	@CLAPOptionA(shortKey = 'c', longKey = "cboolean", order = 1)
	public void setBoolean(final Boolean pBoolean) {
		_boolean = pBoolean;
	}

	@CLAPDecisionA(branches = {
			CLAPTypeA.class,
			CLAPTypeB.class
	})
	public void setObject(final Object pObject) {
		_object = pObject;
	}

}
