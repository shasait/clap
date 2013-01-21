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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Entry point to CLAP library.
 */
public final class CLAP implements ICLAPNode {

	private final ResourceBundle _nls;

	private final char _shortOptPrefix;
	private final String _longOptPrefix;
	private final String _longOptEquals;

	private final CLAPNodeList _root;

	/**
	 * 
	 */
	public CLAP(final ResourceBundle pNLS) {
		super();

		_nls = pNLS;

		_shortOptPrefix = '-';
		_longOptPrefix = "--"; //$NON-NLS-1$
		_longOptEquals = "="; //$NON-NLS-1$

		_root = new CLAPNodeList(this);
	}

	@Override
	public <V> ICLAPHasResult<V> addClass(final Class<V> pClass) {
		return _root.addClass(pClass);
	}

	@Override
	public ICLAPNode addDecision() {
		return _root.addDecision();
	}

	@Override
	public CLAPOption<Boolean> addFlag(final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addFlag(pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public ICLAPNode addNodeList() {
		return _root.addNodeList();
	}

	@Override
	public <V> ICLAPHasResult<V> addOption(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOption(pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> ICLAPHasResult<V> addOption1(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey,
			final String pArgUsageNLSKey) {
		return _root.addOption1(pResultClass, pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> ICLAPHasResult<V> addOptionU(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Character pMultiArgSplit,
			final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOptionU(pResultClass, pShortKey, pLongKey, pRequired, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	public String getLongOptEquals() {
		return _longOptEquals;
	}

	public String getLongOptPrefix() {
		return _longOptPrefix;
	}

	public ResourceBundle getNLS() {
		return _nls;
	}

	public char getShortOptPrefix() {
		return _shortOptPrefix;
	}

	public ICLAPResult parse(final String... pArgs) {
		final List<CLAPParseContext> parsedContexts = new ArrayList<CLAPParseContext>();
		final LinkedList<CLAPParseContext> activeContexts = new LinkedList<CLAPParseContext>();
		activeContexts.add(new CLAPParseContext(this, pArgs));
		while (!activeContexts.isEmpty()) {
			final CLAPParseContext context = activeContexts.removeFirst();
			if (context.hasMoreTokens()) {
				final CLAPParseContext[] result = _root.parse(context);
				if (result != null) {
					for (final CLAPParseContext nextContext : result) {
						activeContexts.add(nextContext);
					}
				}
			} else {
				parsedContexts.add(context);
			}
		}
		if (parsedContexts.isEmpty()) {
			throw new CLAPException();
		}

		final Set<CLAPResult> validatedResults = new LinkedHashSet<CLAPResult>();
		for (final CLAPParseContext context : parsedContexts) {
			final List<String> errorMessages = new ArrayList<String>();
			_root.validate(context, errorMessages);
			if (errorMessages.isEmpty()) {
				final CLAPResult result = new CLAPResult();
				_root.fillResult(context, result);
				validatedResults.add(result);
			}
		}
		// TODO remove duplicate results, e.g. resulting from two branches where all options are optional and none matched
		// Implement equals and hashcode for CLAPParseResult
		if (validatedResults.size() != 1) {
			throw new CLAPException();
		}

		return validatedResults.iterator().next();
	}

}
