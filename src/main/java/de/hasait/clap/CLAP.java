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
import java.lang.reflect.Array;
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
    public static final String NLSKEY_DEFAULT_HELP_CATEGORY = "clap.defaultHelpCategory";
    public static final String NLSKEY_DEFAULT_USAGE_CATEGORY = "clap.defaultUsageCategory";

    private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT = "clap.error.errorMessagesSplit";
    private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT = "clap.error.errorMessageSplit";
    private static final String NLSKEY_CLAP_ERROR_VALIDATION_FAILED = "clap.error.validationFailed";
    private static final String NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT = "clap.error.ambiguousResult";
    private static final String NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST = "clap.error.invalidTokenList";
    private static final String NLSKEY_ENTER_PASSWORD = "clap.enterpassword";
    private static final String NLSKEY_ENTER_LINE = "clap.enterline";

    @SuppressWarnings("unchecked")
    public static <T> Class<T[]> asArrayClass(Class<T> type) {
        return (Class<T[]>) Array.newInstance(type, 0).getClass();
    }

    private final ResourceBundle nls;

    private final char shortOptPrefix;
    private final String longOptPrefix;
    private final String longOptAssignment;

    private final Map<Class<?>, CLAPConverter<?>> converters;

    private final CLAPNodeList root;

    private CLAPUICallback uiCallback;

    /**
     * Default constructor to use the default resource bundle.
     * All NLS keys for options should be plain messages.
     */
    public CLAP() {
        this(ResourceBundle.getBundle("clap-msg"));
    }

    /**
     * @param nls {@link ResourceBundle} used for messages.
     */
    public CLAP(ResourceBundle nls) {
        super();

        this.nls = nls;

        this.shortOptPrefix = '-';
        this.longOptPrefix = "--";
        this.longOptAssignment = "=";

        this.converters = new HashMap<>();
        initDefaultConverters();

        this.root = new CLAPNodeList(this);
        this.root.setHelpCategory(1000, null);
        this.root.setUsageCategory(1000, null);

        if (System.console() != null) {
            this.uiCallback = new SystemConsoleUICallback();
        } else {
            this.uiCallback = new SwingDialogUICallback(null);
        }
    }

    @Override
    public <V> CLAPValue<V> addClass(Class<V> clazz) {
        return root.addClass(clazz);
    }

    public <R> void addConverter(Class<R> resultClass, CLAPConverter<? extends R> converter) {
        Class<? super R> currentClass = resultClass;
        while (currentClass != null) {
            if (resultClass.equals(currentClass) || !converters.containsKey(currentClass)) {
                converters.put(currentClass, converter);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    @Override
    public CLAPNode addDecision() {
        return root.addDecision();
    }

    @Override
    public final <V> CLAPValue<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses) {
        return root.addDecision(resultClass, branchClasses);
    }

    @Override
    public CLAPValue<Boolean> addFlag(Character shortKey, String longKey, boolean required, String descriptionNLSKey) {
        return root.addFlag(shortKey, longKey, required, descriptionNLSKey);
    }

    @Override
    public void addKeyword(String keyword) {
        root.addKeyword(keyword);
    }

    @Override
    public CLAPNode addNodeList() {
        return root.addNodeList();
    }

    @Override
    public <V> CLAPValue<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn) {
        return root.addOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey,
                              immediateReturn
        );
    }

    public <R> CLAPConverter<? extends R> getConverter(Class<R> resultClass) {
        if (!converters.containsKey(resultClass)) {
            try {
                addStringConstructorConverter(resultClass);
            } catch (Exception e) {
                throw new RuntimeException(MessageFormat.format("No converter for {0} found", resultClass), e);
            }
        }

        return (CLAPConverter<? extends R>) converters.get(resultClass);
    }

    public <T> T getLineOrReadInteractivly(T object, String cancelNLSKey, boolean setAfterRead) {
        final GetOrReadInteractivlyLogic logic = new GetOrReadInteractivlyLogic() {

            @Override
            protected String readInteractivly(String prompt) {
                return uiCallback.readLine(prompt);
            }

        };
        return logic.getOrReadInteractivly(object, NLSKEY_ENTER_LINE, cancelNLSKey, setAfterRead);
    }

    public String getLongOptAssignment() {
        return longOptAssignment;
    }

    public String getLongOptPrefix() {
        return longOptPrefix;
    }

    public ResourceBundle getNLS() {
        return nls;
    }

    public <T> T getPasswordOrReadInteractivly(T object, String cancelNLSKey, boolean setAfterRead) {
        final GetOrReadInteractivlyLogic logic = new GetOrReadInteractivlyLogic() {

            @Override
            protected String readInteractivly(String prompt) {
                return uiCallback.readPassword(prompt);
            }

        };
        return logic.getOrReadInteractivly(object, NLSKEY_ENTER_PASSWORD, cancelNLSKey, setAfterRead);
    }

    public char getShortOptPrefix() {
        return shortOptPrefix;
    }

    public CLAPUICallback getUICallback() {
        return uiCallback;
    }

    public final String nls(String key, Object... arguments) {
        String pattern = null;
        if (nls != null && key != null) {
            try {
                pattern = nls.getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        if (pattern == null) {
            pattern = key != null ? key : "";
            StringBuilder appendPatternVars = new StringBuilder();
            for (int i = 0; i < arguments.length; i++) {
                String patternVar = "{" + i + "}";
                if (!pattern.contains(patternVar)) {
                    appendPatternVars.append(' ').append(patternVar);
                }
            }
            pattern += appendPatternVars.toString();
        }
        try {
            return MessageFormat.format(pattern, arguments);
        } catch (Exception e) {
            return pattern + "!" + StringUtils.join(arguments, ", ") + "!";
        }
    }

    public CLAPResult parse(String... args) {
        final Set<CLAPParseContext> contextsWithInvalidToken = new HashSet<>();
        final List<CLAPParseContext> parsedContexts = new ArrayList<>();
        final LinkedList<CLAPParseContext> activeContexts = new LinkedList<>();
        activeContexts.add(new CLAPParseContext(this, args));
        while (!activeContexts.isEmpty()) {
            final CLAPParseContext context = activeContexts.removeFirst();
            if (context.containsImmediateReturn()) {
                parsedContexts.clear();
                parsedContexts.add(context);
                break;
            }
            if (context.hasMoreTokens()) {
                final CLAPParseContext[] result = root.parse(context);
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
            for (CLAPParseContext context : contextsWithInvalidToken) {
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
        for (CLAPParseContext context : parsedContexts) {
            final List<String> errorMessages = new ArrayList<>();
            if (!context.containsImmediateReturn()) {
                root.validate(context, errorMessages);
            }
            if (errorMessages.isEmpty()) {
                final CLAPResultImpl result = new CLAPResultImpl();
                root.fillResult(context, result);
                results.add(result);
            } else {
                contextErrorMessages.put(context, errorMessages);
            }
        }

        if (results.isEmpty()) {
            int minErrorMessages = Integer.MAX_VALUE;
            final List<String> errorMessagesOfBestContexts = new ArrayList<>();
            for (Entry<CLAPParseContext, List<String>> entry : contextErrorMessages.entrySet()) {
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

    public void printUsageAndHelp(PrintStream printStream) {
        printUsage(printStream);
        printHelp(printStream);
    }

    public void printHelp(PrintStream printStream) {
        final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> nodes = new TreeMap<>();
        root.collectHelpNodes(nodes, null);

        int maxLength = 0;
        for (Entry<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> entry : nodes.entrySet()) {
            for (CLAPHelpNode node : entry.getValue()) {
                final int length = node.getHelpID().length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
        }
        maxLength += 6; // space to description

        for (Entry<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> entry : nodes.entrySet()) {
            printStream.println();
            printStream.println(nls(entry.getKey().getTitleNLSKey()));
            for (CLAPHelpNode node : entry.getValue()) {
                printStream.println();
                printStream.print("  ");
                printStream.print(StringUtils.rightPad(node.getHelpID(), maxLength - 2));
                final String descriptionNLSKey = node.getDescriptionNLSKey();
                if (descriptionNLSKey != null) {
                    printStream.println(nls(descriptionNLSKey));
                } else {
                    printStream.println();
                }
            }
        }
    }

    public void printUsage(PrintStream printStream) {
        final Map<CLAPUsageCategoryImpl, StringBuilder> categories = new TreeMap<>();
        root.printUsage(categories, null, null);
        for (Entry<CLAPUsageCategoryImpl, StringBuilder> entry : categories.entrySet()) {
            printStream.print(nls(entry.getKey().getTitleNLSKey()));
            printStream.print(": ");
            printStream.print(entry.getValue().toString());
            printStream.println();
        }
    }

    @Override
    public void setHelpCategory(int order, String titleNLSKey) {
        root.setHelpCategory(order, titleNLSKey);
    }

    public void setUICallback(CLAPUICallback uICallback) {
        if (uICallback == null) {
            throw new IllegalArgumentException("UICallback cannot be null");
        }
        uiCallback = uICallback;
    }

    @Override
    public void setUsageCategory(int order, String titleNLSKey) {
        root.setUsageCategory(order, titleNLSKey);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + root;
    }

    private <R> void addPrimitiveConverter(Class<?> wrapperClass, Class<R> primitiveClass) throws Exception {
        final Method parseMethod = wrapperClass.getMethod("parse" + StringUtils.capitalize(primitiveClass.getSimpleName()), String.class);
        final CLAPConverter<R> methodConverter = input -> {
            try {
                return (R) parseMethod.invoke(null, input);
            } catch (Exception e) {
                throw runtimeException(e);
            }
        };
        addConverter(primitiveClass, methodConverter);
    }

    private <R> void addStringConstructorConverter(Class<R> stringConstructorClass) throws Exception {
        final Constructor<R> constructor = stringConstructorClass.getConstructor(String.class);

        final CLAPConverter<R> constructorConverter = input -> {
            try {
                return constructor.newInstance(input);
            } catch (Exception e) {
                throw runtimeException(e);
            }
        };

        addConverter(stringConstructorClass, constructorConverter);
    }

    private void initDefaultConverters() {
        try {
            final CLAPConverter<String> stringConverter = input -> input;
            addConverter(String.class, stringConverter);

            final CLAPConverter<Boolean> booleanConverter = input -> {
                if (input.equalsIgnoreCase("true")) {
                    return true;
                }
                if (input.equalsIgnoreCase("false")) {
                    return false;
                }
                if (input.equalsIgnoreCase("yes")) {
                    return true;
                }
                if (input.equalsIgnoreCase("no")) {
                    return false;
                }
                if (input.equalsIgnoreCase("on")) {
                    return true;
                }
                if (input.equalsIgnoreCase("off")) {
                    return false;
                }
                if (input.equalsIgnoreCase("enable")) {
                    return true;
                }
                if (input.equalsIgnoreCase("disable")) {
                    return false;
                }
                throw new RuntimeException(input);
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
            for (Class<?> wrapperClass : someWrapperClasses) {
                addStringConstructorConverter(wrapperClass);
                final Class<?> primitiveClass = ClassUtils.wrapperToPrimitive(wrapperClass);
                if (primitiveClass != null) {
                    addPrimitiveConverter(wrapperClass, primitiveClass);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RuntimeException runtimeException(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            return runtimeException(((InvocationTargetException) throwable).getTargetException());
        }
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

    private abstract class GetOrReadInteractivlyLogic {

        public <T> T getOrReadInteractivly(T object, String promptNLSKey, String cancelNLSKey, boolean setAfterRead) {
            try {
                final BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
                final Map<Method, String> readMethodToDescriptionMap = new HashMap<>();
                final Map<Method, Method> readMethodToWriteMethodMap = new HashMap<>();
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
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
                proxyFactory.setSuperclass(object.getClass());
                proxyFactory.setFilter(readMethodToDescriptionMap::containsKey);
                final MethodHandler handler = (self, method, proceed, args) -> {
                    final String result = (String) method.invoke(object, args); // execute the original method.
                    if (result == null) {
                        final String description = readMethodToDescriptionMap.get(method);
                        final String prompt = nls(promptNLSKey, description);
                        final String newResult = readInteractivly(prompt);
                        if (newResult == null) {
                            throw new RuntimeException(nls(cancelNLSKey));
                        }
                        if (setAfterRead) {
                            readMethodToWriteMethodMap.get(method).invoke(object, newResult);
                        }
                        return newResult;
                    }
                    return result;
                };
                final Class<T> proxyClass = proxyFactory.createClass();
                final T proxy = proxyClass.newInstance();
                ((Proxy) proxy).setHandler(handler);
                return proxy;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract String readInteractivly(String prompt);

    }

}
