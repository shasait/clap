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

package de.hasait.util.clap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for nodes containing multiple child nodes.
 */
public abstract class AbstractCLAPNodeList extends AbstractCLAPNode {

	private final List<AbstractCLAPNode> _list = new ArrayList<AbstractCLAPNode>();
	private final List<AbstractCLAPNode> _ulist = Collections.unmodifiableList(_list);

	protected AbstractCLAPNodeList(final CLAP pCLAP) {
		super(pCLAP);
	}

	protected final <V> CLAPClass<V> internalAddClass(final Class<V> pClass) {
		final CLAPClass<V> clapClass = new CLAPClass<V>(getCLAP(), pClass);
		_list.add(clapClass);
		return clapClass;
	}

	protected final CLAPDecision internalAddDecision() {
		final CLAPDecision node = new CLAPDecision(getCLAP());
		_list.add(node);
		return node;
	}

	protected final <V> CLAPTypedDecision<V> internalAddDecision(@SuppressWarnings("unused") final Class<V> pResultClass) {
		final CLAPTypedDecision<V> node = new CLAPTypedDecision<V>(getCLAP());
		_list.add(node);
		return node;
	}

	protected final CLAPOption<Boolean> internalAddFlag(final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey,
			final String pArgUsageNLSKey) {
		final CLAPOption<Boolean> option = CLAPOption.create(getCLAP(), Boolean.class, pShortKey, pLongKey, pRequired, 0, null, pDescriptionNLSKey, pArgUsageNLSKey);
		_list.add(option);
		return option;
	}

	protected final CLAPNodeList internalAddNodeList() {
		final CLAPNodeList list = new CLAPNodeList(getCLAP());
		_list.add(list);
		return list;
	}

	protected final <V> CLAPOption<V> internalAddOption(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final Integer pArgCount, final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		final CLAPOption<V> option = CLAPOption.create(getCLAP(), pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
		_list.add(option);
		return option;
	}

	protected final <V> CLAPOption<V> internalAddOption1(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		final CLAPOption<V> option = CLAPOption.create(getCLAP(), pResultClass, pShortKey, pLongKey, pRequired, 1, null, pDescriptionNLSKey, pArgUsageNLSKey);
		_list.add(option);
		return option;
	}

	protected final <V> CLAPOption<V> internalAddOptionU(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		final CLAPOption<V> option = CLAPOption.create(getCLAP(), pResultClass, pShortKey, pLongKey, pRequired, CLAPOption.UNLIMITED_ARG_COUNT, pMultiArgSplit, pDescriptionNLSKey,
				pArgUsageNLSKey);
		_list.add(option);
		return option;
	}

	protected final void internalFillResult(final CLAPParseContext pContext, final CLAPResult pResult) {
		for (final AbstractCLAPNode node : list()) {
			node.fillResult(pContext, pResult);
		}
	}

	protected final CLAPParseContext[] internalParse(final CLAPParseContext pContext) {
		for (final AbstractCLAPNode node : list()) {
			final CLAPParseContext[] result = node.parse(pContext);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	protected final String internalToString(final String pSeparator) {
		final StringBuilder result = new StringBuilder();
		boolean first = true;
		for (final AbstractCLAPNode node : _list) {
			if (first) {
				first = false;
			} else {
				result.append(pSeparator);
			}
			result.append(node);
		}
		return result.toString();
	}

	protected final void internalValidate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		for (final AbstractCLAPNode node : list()) {
			node.validate(pContext, pErrorMessages);
		}
	}

	protected final List<AbstractCLAPNode> list() {
		return _ulist;
	}

}
