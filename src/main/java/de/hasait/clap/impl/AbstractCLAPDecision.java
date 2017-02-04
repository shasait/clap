/*
 * Copyright (C) 2013 by Sebastian Hasait (sebastian at hasait dot de)
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
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

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
	public final void printUsage(final Map<CLAPUsageCategoryImpl, StringBuilder> pCategories, final CLAPUsageCategoryImpl pCurrentCategory, final StringBuilder pResult) {
		final Pair<CLAPUsageCategoryImpl, StringBuilder> pair = handleUsageCategory(pCategories, pCurrentCategory, pResult);
		if (pair != null) {
			final CLAPUsageCategoryImpl currentCategory = pair.getLeft();
			final StringBuilder result = pair.getRight();
			if (list().size() > 1) {
				result.append("{ "); //$NON-NLS-1$
			}
			internalPrintUsage(pCategories, currentCategory, result, " | "); //$NON-NLS-1$
			if (list().size() > 1) {
				result.append(" }"); //$NON-NLS-1$
			}
		}
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
