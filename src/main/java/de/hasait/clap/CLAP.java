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

package de.hasait.clap;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import de.hasait.clap.impl.CLAPHelpCategoryImpl;
import de.hasait.clap.impl.CLAPHelpNode;
import de.hasait.clap.impl.CLAPNodeList;
import de.hasait.clap.impl.CLAPOptionNode;
import de.hasait.clap.impl.CLAPParseContext;
import de.hasait.clap.impl.CLAPResultImpl;
import de.hasait.clap.impl.CLAPUsageCategoryImpl;
import de.hasait.clap.impl.ConsoleReadPasswordCallback;
import de.hasait.clap.impl.SwingDialogReadPasswordCallback;

/**
 * Entry point to CLAP library.
 */
public final class CLAP implements CLAPNode {

	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT = "clap.error.errorMessagesSplit"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT = "clap.error.errorMessageSplit"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_VALIDATION_FAILED = "clap.error.validationFailed"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST = "clap.error.invalidTokenList"; //$NON-NLS-1$
	private static final String NLSKEY_ENTER_PASSWORD = "clap.enterpassword"; //$NON-NLS-1$
	private static final String NLSKEY_DEFAULT_HELP_CATEGORY = "clap.defaultHelpCategory"; //$NON-NLS-1$
	private static final String NLSKEY_DEFAULT_USAGE_CATEGORY = "clap.defaultUsageCategory"; //$NON-NLS-1$

	private final ResourceBundle _nls;

	private final char _shortOptPrefix;
	private final String _longOptPrefix;
	private final String _longOptEquals;

	private final CLAPNodeList _root;

	private CLAPReadPasswordCallback _readPasswordCallback;

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
		_root.setHelpCategory(1000, NLSKEY_DEFAULT_HELP_CATEGORY);
		_root.setUsageCategory(1000, NLSKEY_DEFAULT_USAGE_CATEGORY);

		if (System.console() != null) {
			_readPasswordCallback = new ConsoleReadPasswordCallback();
		} else {
			_readPasswordCallback = new SwingDialogReadPasswordCallback();
		}
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

	public String getLongOptEquals() {
		return _longOptEquals;
	}

	public String getLongOptPrefix() {
		return _longOptPrefix;
	}

	public ResourceBundle getNLS() {
		return _nls;
	}

	public <T> T getPasswordOrReadInteractivly(final T pObject, final String pCancelNLSKey, final boolean pSetAfterRead) {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(pObject.getClass());
			final Map<Method, String> readMethodToDescriptionMap = new HashMap<Method, String>();
			final Map<Method, Method> readMethodToWriteMethodMap = new HashMap<Method, Method>();
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final Method readMethod = propertyDescriptor.getReadMethod();
				final Method writeMethod = propertyDescriptor.getWriteMethod();
				if (readMethod != null && writeMethod != null && propertyDescriptor.getPropertyType().equals(String.class)) {
					final String description;
					final CLAPOption clapOption = writeMethod.getAnnotation(CLAPOption.class);
					final String descriptionNLSKey = clapOption == null ? null : clapOption.descriptionNLSKey();
					if (descriptionNLSKey != null && descriptionNLSKey.trim().length() != 0) {
						description = nls(descriptionNLSKey);
					} else {
						description = propertyDescriptor.getDisplayName();
					}
					readMethodToDescriptionMap.put(readMethod, description);
					readMethodToWriteMethodMap.put(readMethod, writeMethod);
				}
			}
			final ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(pObject.getClass());
			proxyFactory.setFilter(new MethodFilter() {
				@Override
				public boolean isHandled(final Method m) {
					return readMethodToDescriptionMap.containsKey(m);
				}
			});
			final MethodHandler handler = new MethodHandler() {
				@Override
				public Object invoke(final Object self, final Method m, final Method proceed, final Object[] args) throws Throwable {
					final String result = (String) m.invoke(pObject, args); // execute the original method.
					if (result == null) {
						final String description = readMethodToDescriptionMap.get(m);
						final String prompt = nls(NLSKEY_ENTER_PASSWORD, description);
						final String newResult = _readPasswordCallback.readPassword(prompt);
						if (newResult == null) {
							throw new RuntimeException(nls(pCancelNLSKey));
						}
						if (pSetAfterRead) {
							readMethodToWriteMethodMap.get(m).invoke(pObject, newResult);
						}
						return newResult;
					}
					return result;
				}
			};
			final Class<T> proxyClass = proxyFactory.createClass();
			final T proxy = proxyClass.newInstance();
			((Proxy) proxy).setHandler(handler);
			return proxy;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public CLAPReadPasswordCallback getReadPasswordCallback() {
		return _readPasswordCallback;
	}

	public char getShortOptPrefix() {
		return _shortOptPrefix;
	}

	public final String nls(final String pKey, final Object... pArguments) {
		String pattern = null;
		if (_nls != null && pKey != null) {
			try {
				pattern = _nls.getString(pKey);
			} catch (final MissingResourceException e) {
				pattern = null;
			}
		}
		if (pattern == null) {
			pattern = pKey != null ? pKey : ""; //$NON-NLS-1$
			for (int i = 0; i < pArguments.length; i++) {
				pattern += " {" + i + "}"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		try {
			return MessageFormat.format(pattern, pArguments);
		} catch (final Exception e) {
			return pattern + "!" + StringUtils.join(pArguments, ", ") + "!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public CLAPResult parse(final String... pArgs) {
		final Set<CLAPParseContext> contextsWithInvalidToken = new HashSet<CLAPParseContext>();
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
				} else {
					contextsWithInvalidToken.add(context);
				}
			} else {
				parsedContexts.add(context);
			}
		}
		if (parsedContexts.isEmpty()) {
			final Set<String> invalidTokens = new HashSet<String>();
			for (final CLAPParseContext context : contextsWithInvalidToken) {
				invalidTokens.add(context.currentArg());
			}
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST, StringUtils.join(invalidTokens, ", "))); //$NON-NLS-1$
		}

		final Map<CLAPParseContext, List<String>> contextErrorMessages = new HashMap<CLAPParseContext, List<String>>();
		final Set<CLAPResultImpl> validatedResults = new LinkedHashSet<CLAPResultImpl>();
		for (final CLAPParseContext context : parsedContexts) {
			final List<String> errorMessages = new ArrayList<String>();
			_root.validate(context, errorMessages);
			if (errorMessages.isEmpty()) {
				final CLAPResultImpl result = new CLAPResultImpl();
				_root.fillResult(context, result);
				validatedResults.add(result);
			} else {
				contextErrorMessages.put(context, errorMessages);
			}
		}
		if (validatedResults.size() != 1) {
			final List<String> errorMessages = new ArrayList<String>();
			for (final Entry<CLAPParseContext, List<String>> entry : contextErrorMessages.entrySet()) {
				errorMessages.add(StringUtils.join(entry.getValue(), nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT)));
			}
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_VALIDATION_FAILED, StringUtils.join(errorMessages, nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT))));
		}

		return validatedResults.iterator().next();
	}

	public void printHelp(final PrintStream pPrintStream) {
		final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> nodes = new TreeMap<CLAPHelpCategoryImpl, Set<CLAPHelpNode>>();
		_root.collectHelpNodes(nodes, null);

		int maxLength = 0;
		for (final Entry<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> entry : nodes.entrySet()) {
			for (final CLAPHelpNode node : entry.getValue()) {
				final int length = node.getHelpID().length();
				if (length > maxLength) {
					maxLength = length;
				}
			}
		}
		maxLength += 6; // space to description

		for (final Entry<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> entry : nodes.entrySet()) {
			pPrintStream.println();
			pPrintStream.println(nls(entry.getKey().getTitleNLSKey()));
			for (final CLAPHelpNode node : entry.getValue()) {
				pPrintStream.println();
				pPrintStream.print("  "); //$NON-NLS-1$
				pPrintStream.print(StringUtils.rightPad(node.getHelpID(), maxLength - 2));
				final String descriptionNLSKey = node.getDescriptionNLSKey();
				if (descriptionNLSKey != null) {
					pPrintStream.println(nls(descriptionNLSKey));
				} else {
					pPrintStream.println();
				}
			}
		}
	}

	public void printUsage(final PrintStream pPrintStream) {
		final Map<CLAPUsageCategoryImpl, StringBuilder> categories = new TreeMap<CLAPUsageCategoryImpl, StringBuilder>();
		_root.printUsage(categories, null, null);
		for (final Entry<CLAPUsageCategoryImpl, StringBuilder> entry : categories.entrySet()) {
			pPrintStream.print(nls(entry.getKey().getTitleNLSKey()));
			pPrintStream.print(": "); //$NON-NLS-1$
			pPrintStream.print(entry.getValue().toString());
			pPrintStream.println();
		}
	}

	@Override
	public void setHelpCategory(final int pOrder, final String pTitleNLSKey) {
		_root.setHelpCategory(pOrder, pTitleNLSKey);
	}

	public void setReadPasswordCallback(final CLAPReadPasswordCallback pReadPasswordCallback) {
		if (pReadPasswordCallback == null) {
			throw new IllegalArgumentException();
		}
		_readPasswordCallback = pReadPasswordCallback;
	}

	@Override
	public void setUsageCategory(final int pOrder, final String pTitleNLSKey) {
		_root.setUsageCategory(pOrder, pTitleNLSKey);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + _root; //$NON-NLS-1$
	}

}
