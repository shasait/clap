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
import de.hasait.clap.CLAPNode;

/**
 * A list of nodes.
 */
public class CLAPNodeList extends AbstractCLAPNodeList implements CLAPNode {

	public CLAPNodeList(final CLAP pCLAP) {
		super(pCLAP);
	}

	@Override
	public final <V> CLAPClassNode<V> addClass(final Class<V> pClass) {
		return internalAddClass(pClass);
	}

	@Override
	public final CLAPDecisionNode addDecision() {
		return internalAddDecision();
	}

	@Override
	public final <V> CLAPTypedDecisionNode<V> addDecision(final Class<V> pResultClass, final Class<? extends V>... pBranchClasses) {
		return internalAddDecision(pResultClass, pBranchClasses);
	}

	@Override
	public final CLAPOptionNode<Boolean> addFlag(final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey,
			final String pArgUsageNLSKey) {
		return internalAddFlag(pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public void addKeyword(final String pKeyword) {
		internalAddKeyword(pKeyword);
	}

	@Override
	public final CLAPNodeList addNodeList() {
		return internalAddNodeList();
	}

	@Override
	public final <V> CLAPOptionNode<V> addOption(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return internalAddOption(pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public final <V> CLAPOptionNode<V> addOption1(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return internalAddOption1(pResultClass, pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public final <V> CLAPOptionNode<V> addOptionU(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return internalAddOptionU(pResultClass, pShortKey, pLongKey, pRequired, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public final void fillResult(final CLAPParseContext pContext, final CLAPResultImpl pResult) {
		internalFillResult(pContext, pResult);
	}

	@Override
	public final CLAPParseContext[] parse(final CLAPParseContext pContext) {
		return internalParse(pContext);
	}

	@Override
	public final String toString() {
		return internalToString(" "); //$NON-NLS-1$
	}

	@Override
	public final void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		internalValidate(pContext, pErrorMessages);
	}

}