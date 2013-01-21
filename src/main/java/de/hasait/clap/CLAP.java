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

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.hasait.clap.impl.CLAPNodeList;
import de.hasait.clap.impl.CLAPOptionNode;
import de.hasait.clap.impl.CLAPParseContext;
import de.hasait.clap.impl.CLAPResultImpl;

/**
 * Entry point to CLAP library.
 */
public final class CLAP implements CLAPNode {

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
	public <V> CLAPValue<V> addClass(final Class<V> pClass) {
		return _root.addClass(pClass);
	}

	@Override
	public CLAPNode addDecision() {
		return _root.addDecision();
	}

	@Override
	public final <V> CLAPValue<V> addDecision(final Class<V> pResultClass, final Class<? extends V>... pBranchClasses) {
		return _root.addDecision(pResultClass, pBranchClasses);
	}

	@Override
	public CLAPOptionNode<Boolean> addFlag(final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addFlag(pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public void addKeyword(final String pKeyword) {
		_root.addKeyword(pKeyword);
	}

	@Override
	public CLAPNode addNodeList() {
		return _root.addNodeList();
	}

	@Override
	public <V> CLAPValue<V> addOption(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOption(pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V> addOption1(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey,
			final String pArgUsageNLSKey) {
		return _root.addOption1(pResultClass, pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V> addOptionU(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Character pMultiArgSplit,
			final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOptionU(pResultClass, pShortKey, pLongKey, pRequired, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	public String buildUsage() {
		final StringBuilder result = new StringBuilder();
		_root.printUsage(result);
		return result.toString();
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

	public final String nls(final String pKey, final Object... pArguments) {
		String pattern;
		if (_nls != null) {
			pattern = _nls.getString(pKey);
		} else {
			pattern = pKey != null ? pKey : ""; //$NON-NLS-1$
			for (int i = 0; i < pArguments.length; i++) {
				pattern += "{" + i + "}"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		try {
			return MessageFormat.format(pattern, pArguments);
		} catch (final Exception e) {
			return pattern + "!" + StringUtils.join(pArguments, ", ") + "!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public CLAPResult parse(final String... pArgs) {
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

		final Set<CLAPResultImpl> validatedResults = new LinkedHashSet<CLAPResultImpl>();
		for (final CLAPParseContext context : parsedContexts) {
			final List<String> errorMessages = new ArrayList<String>();
			_root.validate(context, errorMessages);
			if (errorMessages.isEmpty()) {
				final CLAPResultImpl result = new CLAPResultImpl();
				_root.fillResult(context, result);
				validatedResults.add(result);
			}
		}
		if (validatedResults.size() != 1) {
			throw new CLAPException();
		}

		return validatedResults.iterator().next();
	}

	public void printHelp(final PrintStream pPrintStream) {
		final List<CLAPOptionNode<?>> optionNodes = new ArrayList<CLAPOptionNode<?>>();
		_root.collectOptionNodes(optionNodes);
		Collections.sort(optionNodes, new Comparator<CLAPOptionNode<?>>() {

			@Override
			public int compare(final CLAPOptionNode<?> pO1, final CLAPOptionNode<?> pO2) {
				final Character sk1 = pO1.getShortKey();
				final String lk1 = pO1.getLongKey();
				final Character sk2 = pO2.getShortKey();
				final String lk2 = pO2.getLongKey();
				final String or1 = sk1 != null ? sk1.toString() : lk1 != null ? lk1 : ""; //$NON-NLS-1$
				final String or2 = sk2 != null ? sk2.toString() : lk2 != null ? lk2 : ""; //$NON-NLS-1$
				final int r = or1.compareTo(or2);
				return r;
			}

		});

		int minKeyLength = 0;
		for (final CLAPOptionNode<?> node : optionNodes) {
			int keyLength = 0;
			final Character shortKey = node.getShortKey();
			final String longKey = node.getLongKey();
			if (shortKey != null) {
				keyLength += 1 + 1; // <SKPREFIX><SK>
				if (longKey != null) {
					keyLength += 2; // <COMMA><SPACE>
				}
			}
			if (longKey != null) {
				keyLength += getLongOptPrefix().length() + longKey.length();
			}
			keyLength += 4; // space to description
			if (keyLength > minKeyLength) {
				minKeyLength = keyLength;
			}
		}

		for (final CLAPOptionNode<?> node : optionNodes) {
			final StringBuilder keySB = new StringBuilder();
			final Character shortKey = node.getShortKey();
			final String longKey = node.getLongKey();
			if (shortKey != null) {
				keySB.append(getShortOptPrefix()).append(shortKey); // <SKPREFIX><SK>
				if (longKey != null) {
					keySB.append(", "); // <COMMA><SPACE> //$NON-NLS-1$
				}
			}
			if (longKey != null) {
				keySB.append(getLongOptPrefix()).append(longKey);
			}
			pPrintStream.print(StringUtils.rightPad(keySB.toString(), minKeyLength));
			pPrintStream.println(nls(node.getDescriptionNLSKey()));
			pPrintStream.print(StringUtils.repeat(' ', minKeyLength + 4));
			pPrintStream.print('(');
			pPrintStream.print(nls(node.isRequired() ? "clap.required" : "clap.optional")); //$NON-NLS-1$ //$NON-NLS-2$
			pPrintStream.print(')');
			pPrintStream.println();
		}
	}

	public void printUsage(final PrintStream pPrintStream) {
		pPrintStream.println(buildUsage());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + _root; //$NON-NLS-1$
	}

}
