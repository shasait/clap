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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hasait.clap.CLAP;

/**
 * An option node.
 */
public class CLAPKeywordNode extends AbstractCLAPNode implements CLAPHelpNode {

	public static final int UNLIMITED_ARG_COUNT = -1;

	private final String _keyword;

	public CLAPKeywordNode(final CLAP pCLAP, final String pKeyword) {
		super(pCLAP);

		if (pKeyword == null) {
			throw new IllegalArgumentException();
		}

		if (pKeyword.startsWith(Character.toString(getCLAP().getShortOptPrefix()))) {
			throw new IllegalArgumentException();
		}
		if (pKeyword.startsWith(getCLAP().getLongOptEquals())) {
			throw new IllegalArgumentException();
		}

		_keyword = pKeyword;
	}

	@Override
	public void collectHelpNodes(final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pNodes, final CLAPHelpCategoryImpl pCurrentCategory) {
		addHelpNode(pNodes, pCurrentCategory, this);
	}

	@Override
	public void fillResult(final CLAPParseContext pContext, final CLAPResultImpl pResult) {
		// none
	}

	@Override
	public String getDescriptionNLSKey() {
		return null;
	}

	@Override
	public String getHelpID() {
		return _keyword;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public CLAPParseContext[] parse(final CLAPParseContext pContext) {
		if (_keyword.equals(pContext.currentArg())) {
			pContext.consumeCurrent();
			pContext.addKeyword(this);
			return new CLAPParseContext[] {
				pContext
			};
		}
		return null;
	}

	@Override
	public void printUsage(final StringBuilder pResult) {
		pResult.append(_keyword);
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}[\"{1}\"]", getClass().getSimpleName(), _keyword); //$NON-NLS-1$ 
	}

	@Override
	public void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		if (pContext.getNodeCount(this) == 0) {
			pErrorMessages.add(this + " is missing"); //$NON-NLS-1$
		}
	}

}
