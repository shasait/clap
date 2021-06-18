/*
 * Copyright (C) 2021 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Arrays;
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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

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

	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT = "clap.error.errorMessagesSplit";
	private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT = "clap.error.errorMessageSplit";
	private static final String NLSKEY_CLAP_ERROR_VALIDATION_FAILED = "clap.error.validationFailed";
	private static final String NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT = "clap.error.ambiguousResult";
	private static final String NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST = "clap.error.invalidTokenList";
	private static final String NLSKEY_ENTER_PASSWORD = "clap.enterpassword";
	private static final String NLSKEY_ENTER_LINE = "clap.enterline";
	private static final String NLSKEY_DEFAULT_HELP_CATEGORY = "clap.defaultHelpCategory";
	private static final String NLSKEY_DEFAULT_USAGE_CATEGORY = "clap.defaultUsageCategory";

	private final ResourceBundle _nls;

	private final char _shortOptPrefix;
	private final String _longOptPrefix;
	private final String _longOptEquals;

	private final Map<Class<?>, CLAPConverter<?>> _converters;

	private final CLAPNodeList _root;

	private CLAPUICallback _uiCallback;

	/**
	 * Default constructor to use the default resource bundle.
	 * All NLS keys for options should be plain messages.
	 */
	public CLAP() {
		this(ResourceBundle.getBundle("clap-msg"));
	}

	/**
	 * @param pNLS {@link ResourceBundle} used for messages.
	 */
	public CLAP(final ResourceBundle pNLS) {
		super();

		_nls = pNLS;

		_shortOptPrefix = '-';
		_longOptPrefix = "--";
		_longOptEquals = "=";

		_converters = new HashMap<>();
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
	public <V> CLAPValue<V> addOption(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount, final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root
				.addOption(pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V> addOption1(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOption1(pResultClass, pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V[]> addOptionU(final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addOptionU(pResultClass, pShortKey, pLongKey, pRequired, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V> addNameless1(final Class<V> pResultClass, final boolean pRequired, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addNameless1(pResultClass, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public <V> CLAPValue<V[]> addNamelessU(final Class<V> pResultClass, final boolean pRequired, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return _root.addNamelessU(pResultClass, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	public <R> CLAPConverter<? extends R> getConverter(final Class<R> pResultClass) {
		if (!_converters.containsKey(pResultClass)) {
			try {
				addStringConstructorConverter(pResultClass);
			} catch (final Exception e) {
				throw new RuntimeException(MessageFormat.format("No converter for {0} found", pResultClass), e);
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
				// ignore
			}
		}
		if (pattern == null) {
			pattern = pKey != null ? pKey : "";
			StringBuilder appendPatternVars = new StringBuilder();
			for (int i = 0; i < pArguments.length; i++) {
				String patternVar = "{" + i + "}";
				if (!pattern.contains(patternVar)) {
					appendPatternVars.append(' ').append(patternVar);
				}
			}
			pattern += appendPatternVars.toString();
		}
		try {
			return MessageFormat.format(pattern, pArguments);
		} catch (final Exception e) {
			return pattern + "!" + StringUtils.join(pArguments, ", ") + "!";
		}
	}

	public CLAPResult parse(final String... pArgs) {
		final Set<CLAPParseContext> contextsWithInvalidToken = new HashSet<>();
		final List<CLAPParseContext> parsedContexts = new ArrayList<>();
		final LinkedList<CLAPParseContext> activeContexts = new LinkedList<>();
		activeContexts.add(new CLAPParseContext(this, pArgs));
		while (!activeContexts.isEmpty()) {
			final CLAPParseContext context = activeContexts.removeFirst();
			if (context.hasMoreTokens()) {
				final CLAPParseContext[] result = _root.parse(context);
				if (result != null) {
					activeContexts.addAll(Arrays.asList(result));
				} else {
					contextsWithInvalidToken.add(context);
				}
			} else {
				parsedContexts.add(context);
			}
		}
		if (parsedContexts.isEmpty()) {
			int maxArgIndex = Integer.MIN_VALUE;
			final Set<String> invalidTokensOfBestContexts = new HashSet<>();
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
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST, StringUtils.join(invalidTokensOfBestContexts, ", ")));
		}

		final Map<CLAPParseContext, List<String>> contextErrorMessages = new HashMap<>();
		final Set<CLAPResultImpl> results = new LinkedHashSet<>();
		for (final CLAPParseContext context : parsedContexts) {
			final List<String> errorMessages = new ArrayList<>();
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
			final List<String> errorMessagesOfBestContexts = new ArrayList<>();
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
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_VALIDATION_FAILED,
										StringUtils.join(errorMessagesOfBestContexts, nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT))
			));
		}

		if (results.size() > 1) {
			throw new CLAPException(nls(NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT));
		}

		return results.iterator().next();
	}

	public void printUsageAndHelp(final PrintStream pPrintStream) {
		printUsage(pPrintStream);
		printHelp(pPrintStream);
	}

	public void printHelp(final PrintStream pPrintStream) {
		final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> nodes = new TreeMap<>();
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
				pPrintStream.print("  ");
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
		final Map<CLAPUsageCategoryImpl, StringBuilder> categories = new TreeMap<>();
		_root.printUsage(categories, null, null);
		for (final Entry<CLAPUsageCategoryImpl, StringBuilder> entry : categories.entrySet()) {
			pPrintStream.print(nls(entry.getKey().getTitleNLSKey()));
			pPrintStream.print(": ");
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
		return getClass().getSimpleName() + ":" + _root;
	}

	private <R> void addPrimitiveConverter(final Class<?> pWrapperClass, final Class<R> pPrimitiveClass) throws Exception {
		final Method parseMethod = pWrapperClass.getMethod("parse" + StringUtils.capitalize(pPrimitiveClass.getSimpleName()), String.class);
		final CLAPConverter<R> methodConverter = pInput -> {
			try {
				return (R) parseMethod.invoke(null, pInput);
			} catch (final Exception e) {
				throw runtimeException(e);
			}
		};
		addConverter(pPrimitiveClass, methodConverter);
	}

	private <R> void addStringConstructorConverter(final Class<R> pStringConstructorClass) throws Exception {
		final Constructor<R> constructor = pStringConstructorClass.getConstructor(String.class);

		final CLAPConverter<R> constructorConverter = pInput -> {
			try {
				return constructor.newInstance(pInput);
			} catch (final Exception e) {
				throw runtimeException(e);
			}
		};

		addConverter(pStringConstructorClass, constructorConverter);
	}

	private void initDefaultConverters() {
		try {
			final CLAPConverter<String> stringConverter = pInput -> pInput;
			addConverter(String.class, stringConverter);

			final CLAPConverter<Boolean> booleanConverter = pInput -> {
				if (pInput.equalsIgnoreCase("true")) {
					return true;
				}
				if (pInput.equalsIgnoreCase("false")) {
					return false;
				}
				if (pInput.equalsIgnoreCase("yes")) {
					return true;
				}
				if (pInput.equalsIgnoreCase("no")) {
					return false;
				}
				if (pInput.equalsIgnoreCase("on")) {
					return true;
				}
				if (pInput.equalsIgnoreCase("off")) {
					return false;
				}
				if (pInput.equalsIgnoreCase("enable")) {
					return true;
				}
				if (pInput.equalsIgnoreCase("disable")) {
					return false;
				}
				throw new RuntimeException(pInput);
			};
			addConverter(Boolean.class, booleanConverter);
			addConverter(Boolean.TYPE, booleanConverter);

			final Class<?>[] someWrapperClasses = new Class<?>[]{
					Byte.class,
					Short.class,
					Integer.class,
					Long.class,
					Float.class,
					Double.class
			};
			for (final Class<?> wrapperClass : someWrapperClasses) {
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
				final Map<Method, String> readMethodToDescriptionMap = new HashMap<>();
				final Map<Method, Method> readMethodToWriteMethodMap = new HashMap<>();
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
				proxyFactory.setFilter(readMethodToDescriptionMap::containsKey);
				final MethodHandler handler = (pSelf, pMethod, pProceed, pArgs) -> {
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
