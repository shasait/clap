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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import de.hasait.clap.impl.CLAPHelpCategoryImpl;
import de.hasait.clap.impl.CLAPHelpNode;
import de.hasait.clap.impl.CLAPNodeList;
import de.hasait.clap.impl.CLAPParseContext;
import de.hasait.clap.impl.CLAPResultImpl;
import de.hasait.clap.impl.CLAPUsageCategoryImpl;
import de.hasait.clap.impl.SwingDialogUICallback;
import de.hasait.clap.impl.SystemConsoleUICallback;

/**
 * Entry point to CLAP library.
 */
public final class CLAP implements CLAPNode {

	public static final int UNLIMITED_ARG_COUNT = -1;

	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT = "clap.error.errorMessagesSplit"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT = "clap.error.errorMessageSplit"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_VALIDATION_FAILED = "clap.error.validationFailed"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT = "clap.error.ambiguousResult"; //$NON-NLS-1$
	private static final String NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST = "clap.error.invalidTokenList"; //$NON-NLS-1$
	private static final String NLSKEY_ENTER_PASSWORD = "clap.enterpassword"; //$NON-NLS-1$
	private static final String NLSKEY_ENTER_LINE = "clap.enterline"; //$NON-NLS-1$
	private static final String NLSKEY_DEFAULT_HELP_CATEGORY = "clap.defaultHelpCategory"; //$NON-NLS-1$
	private static final String NLSKEY_DEFAULT_USAGE_CATEGORY = "clap.defaultUsageCategory"; //$NON-NLS-1$

	private final ResourceBundle _nls;

	private final char _shortOptPrefix;
	private final String _longOptPrefix;
	private final String _longOptEquals;

	private final Map<Class<?>, CLAPConverter<?>> _converters;

	private final CLAPNodeList _root;

	private CLAPUICallback _uiCallback;

	/**
	 * @param pNLS {@link ResourceBundle} used for messages.
	 */
	public CLAP(final ResourceBundle pNLS) {
		super();

		_nls = pNLS;

		_shortOptPrefix = '-';
		_longOptPrefix = "--"; //$NON-NLS-1$
		_longOptEquals = "="; //$NON-NLS-1$

		_converters = new HashMap<Class<?>, CLAPConverter<?>>();
		initDefaultConverters();

		_root = new CLAPNodeList(this);
		_root.setHelpCategory(1000, NLSKEY_DEFAULT_HELP_CATEGORY);
		_root.setUsageCategory(1000, NLSKEY_DEFAULT_USAGE_CATEGORY);

		if (System.console() != null) {
			_uiCallback = new SystemConsoleUICallback();
		} else {
			_uiCallback = new SwingDialogUICallback(null);
		}
	}

	@Override
	public <V> CLAPValue<V> addClass(final Class<V> pClass) {
		return _root.addClass(pClass);
	}

	public <R> void addConverter(final Class<R> pResultClass, final CLAPConverter<? extends R> pConverter) {
		Class<? super R> currentClass = pResultClass;
		while (currentClass != null) {
			if (pResultClass.equals(currentClass) || !_converters.containsKey(currentClass)) {
				_converters.put(currentClass, pConverter);
			}
			currentClass = currentClass.getSuperclass();
		}
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
	public CLAPValue<Boolean> addFlag(final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey) {
		return _root.addFlag(pShortKey, pLongKey, pRequired, pDescriptionNLSKey);
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

	public <R> CLAPConverter<? extends R> getConverter(final Class<R> pResultClass) {
		if (!_converters.containsKey(pResultClass)) {
			try {
				addStringConstructorConverter(pResultClass);
			} catch (final Exception e) {
				throw new RuntimeException(MessageFormat.format("No converter for {0} found", pResultClass), e); //$NON-NLS-1$
			}
		}

		return (CLAPConverter<? extends R>) _converters.get(pResultClass);
	}

	public <T> T getLineOrReadInteractivly(final T pObject, final String pCancelNLSKey, final boolean pSetAfterRead) {
		final GetOrReadInteractivlyLogic logic = new GetOrReadInteractivlyLogic() {

			@Override
			protected String readInteractivly(final String pPrompt) {
				return _uiCallback.readLine(pPrompt);
			}

		};
		return logic.getOrReadInteractivly(pObject, NLSKEY_ENTER_LINE, pCancelNLSKey, pSetAfterRead);
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
		final GetOrReadInteractivlyLogic logic = new GetOrReadInteractivlyLogic() {

			@Override
			protected String readInteractivly(final String pPrompt) {
				return _uiCallback.readPassword(pPrompt);
			}

		};
		return logic.getOrReadInteractivly(pObject, NLSKEY_ENTER_PASSWORD, pCancelNLSKey, pSetAfterRead);
	}

	public char getShortOptPrefix() {
		return _shortOptPrefix;
	}

	public CLAPUICallback getUICallback() {
		return _uiCallback;
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
			int maxArgIndex = Integer.MIN_VALUE;
			final Set<String> invalidTokensOfBestContexts = new HashSet<String>();
			for (final CLAPParseContext context : contextsWithInvalidToken) {
				final int currentArgIndex = context.getCurrentArgIndex();
				if (currentArgIndex > maxArgIndex) {
					invalidTokensOfBestContexts.clear();
				}
				if (currentArgIndex >= maxArgIndex) {
					maxArgIndex = currentArgIndex;
					invalidTokensOfBestContexts.add(context.currentArg());
				}
			}
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST, StringUtils.join(invalidTokensOfBestContexts, ", "))); //$NON-NLS-1$
		}

		final Map<CLAPParseContext, List<String>> contextErrorMessages = new HashMap<CLAPParseContext, List<String>>();
		final Set<CLAPResultImpl> results = new LinkedHashSet<CLAPResultImpl>();
		for (final CLAPParseContext context : parsedContexts) {
			final List<String> errorMessages = new ArrayList<String>();
			_root.validate(context, errorMessages);
			if (errorMessages.isEmpty()) {
				final CLAPResultImpl result = new CLAPResultImpl();
				_root.fillResult(context, result);
				results.add(result);
			} else {
				contextErrorMessages.put(context, errorMessages);
			}
		}

		if (results.isEmpty()) {
			int minErrorMessages = Integer.MAX_VALUE;
			final List<String> errorMessagesOfBestContexts = new ArrayList<String>();
			for (final Entry<CLAPParseContext, List<String>> entry : contextErrorMessages.entrySet()) {
				final int countErrorMessages = entry.getValue().size();
				if (countErrorMessages < minErrorMessages) {
					errorMessagesOfBestContexts.clear();
				}
				if (countErrorMessages <= minErrorMessages) {
					minErrorMessages = countErrorMessages;
					errorMessagesOfBestContexts.add(StringUtils.join(entry.getValue(), nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT)));
				}
			}
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_VALIDATION_FAILED, StringUtils.join(errorMessagesOfBestContexts, nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT))));
		}

		if (results.size() > 1) {
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT));
		}

		return results.iterator().next();
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

	public void setUICallback(final CLAPUICallback pUICallback) {
		if (pUICallback == null) {
			throw new IllegalArgumentException();
		}
		_uiCallback = pUICallback;
	}

	@Override
	public void setUsageCategory(final int pOrder, final String pTitleNLSKey) {
		_root.setUsageCategory(pOrder, pTitleNLSKey);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + _root; //$NON-NLS-1$
	}

	private <R> void addPrimitiveConverter(final Class<?> pWrapperClass, final Class<R> pPrimitiveClass) throws Exception {
		final Method parseMethod = pWrapperClass.getMethod("parse" + StringUtils.capitalize(pPrimitiveClass.getSimpleName()), String.class); //$NON-NLS-1$
		final CLAPConverter<R> methodConverter = new CLAPConverter<R>() {

			@Override
			public R convert(final String pInput) {
				try {
					return (R) parseMethod.invoke(null, pInput);
				} catch (final Exception e) {
					throw runtimeException(e);
				}
			}

		};
		addConverter(pPrimitiveClass, methodConverter);
	}

	private <R> void addStringConstructorConverter(final Class<R> pStringConstructorClass) throws Exception {
		final Constructor<R> constructor = pStringConstructorClass.getConstructor(String.class);

		final CLAPConverter<R> constructorConverter = new CLAPConverter<R>() {

			@Override
			public R convert(final String pInput) {
				try {
					return constructor.newInstance(pInput);
				} catch (final Exception e) {
					throw runtimeException(e);
				}
			}

		};

		addConverter(pStringConstructorClass, constructorConverter);
	}

	private void initDefaultConverters() {
		try {
			final CLAPConverter<String> stringConverter = new CLAPConverter<String>() {

				@Override
				public String convert(final String pInput) {
					return pInput;
				}

			};
			addConverter(String.class, stringConverter);

			final CLAPConverter<Boolean> booleanConverter = new CLAPConverter<Boolean>() {

				@Override
				public Boolean convert(final String pInput) {
					if (pInput.equalsIgnoreCase("true")) { //$NON-NLS-1$
						return true;
					}
					if (pInput.equalsIgnoreCase("false")) { //$NON-NLS-1$
						return false;
					}
					if (pInput.equalsIgnoreCase("yes")) { //$NON-NLS-1$
						return true;
					}
					if (pInput.equalsIgnoreCase("no")) { //$NON-NLS-1$
						return false;
					}
					if (pInput.equalsIgnoreCase("on")) { //$NON-NLS-1$
						return true;
					}
					if (pInput.equalsIgnoreCase("off")) { //$NON-NLS-1$
						return false;
					}
					if (pInput.equalsIgnoreCase("enable")) { //$NON-NLS-1$
						return true;
					}
					if (pInput.equalsIgnoreCase("disable")) { //$NON-NLS-1$
						return false;
					}
					throw new RuntimeException(pInput);
				}

			};
			addConverter(Boolean.class, booleanConverter);
			addConverter(Boolean.TYPE, booleanConverter);

			final Class<?>[] someWrapperClasses = new Class<?>[] {
					Byte.class,
					Short.class,
					Integer.class,
					Long.class,
					Float.class,
					Double.class
			};
			for (int i = 0; i < someWrapperClasses.length; i++) {
				final Class<?> wrapperClass = someWrapperClasses[i];
				addStringConstructorConverter(wrapperClass);
				final Class<?> primitiveClass = ClassUtils.wrapperToPrimitive(wrapperClass);
				if (primitiveClass != null) {
					addPrimitiveConverter(wrapperClass, primitiveClass);
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private RuntimeException runtimeException(final Throwable pThrowable) {
		if (pThrowable instanceof InvocationTargetException) {
			return runtimeException(((InvocationTargetException) pThrowable).getTargetException());
		}
		if (pThrowable instanceof RuntimeException) {
			return (RuntimeException) pThrowable;
		}
		return new RuntimeException(pThrowable);
	}

	private abstract class GetOrReadInteractivlyLogic {

		public <T> T getOrReadInteractivly(final T pObject, final String pPromptNLSKey, final String pCancelNLSKey, final boolean pSetAfterRead) {
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
					public Object invoke(final Object pSelf, final Method pMethod, final Method pProceed, final Object[] pArgs) throws Throwable {
						final String result = (String) pMethod.invoke(pObject, pArgs); // execute the original method.
						if (result == null) {
							final String description = readMethodToDescriptionMap.get(pMethod);
							final String prompt = nls(pPromptNLSKey, description);
							final String newResult = readInteractivly(prompt);
							if (newResult == null) {
								throw new RuntimeException(nls(pCancelNLSKey));
							}
							if (pSetAfterRead) {
								readMethodToWriteMethodMap.get(pMethod).invoke(pObject, newResult);
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

		protected abstract String readInteractivly(String pPrompt);

	}

}
