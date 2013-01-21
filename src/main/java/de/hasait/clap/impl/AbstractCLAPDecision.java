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

package de.hasait.clap.impl;

import java.util.List;

import de.hasait.clap.CLAP;

/**
 * Base class for all decision (XOR) nodes.
 */
public abstract class AbstractCLAPDecision extends AbstractCLAPNodeList {

	protected AbstractCLAPDecision(final CLAP pCLAP) {
		super(pCLAP);
	}

	@Override
	public final CLAPParseContext[] parse(final CLAPParseContext pContext) {
		if (list().isEmpty()) {
			return null;
		}

		final AbstractCLAPNode decision = pContext.getDecision(this);
		if (decision == null) {
			final CLAPParseContext[] result = new CLAPParseContext[list().size()];
			for (int i = 0; i < list().size(); i++) {
				result[i] = pContext.clone();
				result[i].addDecision(this, list().get(i));
			}
			return result;
		} else {
			return decision.parse(pContext);
		}
	}

	@Override
	public final String toString() {
		final StringBuilder result = new StringBuilder();
		if (list().size() > 1) {
			result.append("{ "); //$NON-NLS-1$
		}
		result.append(internalToString(" | ")); //$NON-NLS-1$
		if (list().size() > 1) {
			result.append(" }"); //$NON-NLS-1$
		}
		return result.toString();
	}

	@Override
	public final void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		final AbstractCLAPNode decision = pContext.getDecision(this);
		if (decision == null) {
			for (int i = 0; i < list().size(); i++) {
				list().get(i).validate(pContext, pErrorMessages);
			}
		} else {
			decision.validate(pContext, pErrorMessages);
		}
	}

}
