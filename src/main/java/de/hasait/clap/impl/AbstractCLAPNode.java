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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.hasait.clap.CLAP;

/**
 * Base class of all nodes forming the option tree.
 */
public abstract class AbstractCLAPNode {

	private final CLAP _clap;

	private CLAPHelpCategoryImpl _helpCategory;

	protected AbstractCLAPNode(final CLAP pCLAP) {
		super();

		_clap = pCLAP;
	}

	protected static final void addHelpNode(final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pOptionNodes, final CLAPHelpCategoryImpl pCurrentCategory, final CLAPHelpNode pNode) {
		final CLAPHelpCategoryImpl currentCategory = pNode.getHelpCategory() != null ? pNode.getHelpCategory() : pCurrentCategory;
		if (!pOptionNodes.containsKey(currentCategory)) {
			pOptionNodes.put(currentCategory, new TreeSet<CLAPHelpNode>(new Comparator<CLAPHelpNode>() {

				@Override
				public int compare(final CLAPHelpNode pO1, final CLAPHelpNode pO2) {
					return pO1.getHelpID().compareTo(pO2.getHelpID());
				}

			}));
		}
		pOptionNodes.get(currentCategory).add(pNode);
	}

	public abstract void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pOptionNodes, CLAPHelpCategoryImpl pCurrentCategory);

	public abstract void fillResult(CLAPParseContext pContext, CLAPResultImpl pResult);

	public CLAP getCLAP() {
		return _clap;
	}

	public final CLAPHelpCategoryImpl getHelpCategory() {
		return _helpCategory;
	}

	public final String nls(final String pKey) {
		return _clap.nls(pKey);
	}

	public abstract CLAPParseContext[] parse(CLAPParseContext pContext);

	public abstract void printUsage(StringBuilder pResult);

	public final void setHelpCategory(final int pOrder, final String pTitleNLSKey) {
		_helpCategory = new CLAPHelpCategoryImpl(pOrder, pTitleNLSKey);
	}

	public abstract void validate(CLAPParseContext pContext, List<String> pErrorMessages);

}
