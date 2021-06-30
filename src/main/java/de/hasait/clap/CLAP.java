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

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import de.hasait.clap.impl.SwingDialogUICallback;
import de.hasait.clap.impl.SystemConsoleUICallback;
import de.hasait.clap.impl.UserInteractionInterceptor;
import de.hasait.clap.impl.help.CLAPHelpPrinter;
import de.hasait.clap.impl.parser.CLAPParser;
import de.hasait.clap.impl.tree.CLAPGroupNode;
import de.hasait.clap.impl.usage.CLAPUsagePrinter;

/**
 * Entry point to CLAP library.
 */
public final class CLAP implements CLAPNode {

    public static final int UNLIMITED_ARG_COUNT = -1;
    public static final String NLSKEY_DEFAULT_HELP_CATEGORY = "clap.defaultHelpCategory";
    public static final String NLSKEY_DEFAULT_USAGE_CATEGORY = "clap.defaultUsageCategory";

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

    private final CLAPGroupNode root;

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

        this.root = new CLAPGroupNode(this);
        root.setHelpCategory(Integer.MIN_VALUE, NLSKEY_DEFAULT_HELP_CATEGORY);
        root.setUsageCategory(Integer.MIN_VALUE, NLSKEY_DEFAULT_USAGE_CATEGORY);

        if (System.console() != null) {
            this.uiCallback = new SystemConsoleUICallback();
        } else {
            this.uiCallback = new SwingDialogUICallback(null);
        }
    }

    public char getShortOptPrefix() {
        return shortOptPrefix;
    }

    public String getLongOptPrefix() {
        return longOptPrefix;
    }

    public String getLongOptAssignment() {
        return longOptAssignment;
    }

    public CLAPUICallback getUICallback() {
        return uiCallback;
    }

    public void setUICallback(CLAPUICallback uiCallback) {
        this.uiCallback = Objects.requireNonNull(uiCallback, "uiCallback must not be null");
    }

    @Override
    public void setHelpCategoryTitle(String helpCategory) {
        root.setHelpCategoryTitle(helpCategory);
    }

    @Override
    public void setHelpCategoryOrder(int helpCategoryOrder) {
        root.setHelpCategoryOrder(helpCategoryOrder);
    }

    @Override
    public void setHelpEntryOrder(int helpEntryOrder) {
        root.setHelpEntryOrder(helpEntryOrder);
    }

    @Override
    public void setUsageCategoryTitle(String usageCategory) {
        root.setUsageCategoryTitle(usageCategory);
    }

    @Override
    public void setUsageCategoryOrder(int usageCategoryOrder) {
        root.setUsageCategoryOrder(usageCategoryOrder);
    }

    @Override
    public <V> CLAPValue<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn, boolean password) {
        return root.addOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey,
                              immediateReturn, password
        );
    }

    @Override
    public void addKeyword(String keyword) {
        root.addKeyword(keyword);
    }

    @Override
    public CLAPNode addGroup() {
        return root.addGroup();
    }

    @Override
    public CLAPNode addDecision() {
        return root.addDecision();
    }

    @Override
    public <V> CLAPValue<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses) {
        return root.addDecision(resultClass, branchClasses);
    }

    @Override
    public <V> CLAPValue<V> addClass(Class<V> clazz) {
        return root.addClass(clazz);
    }

    public CLAPResult parse(String... args) {
        return new CLAPParser(this, root).parse(args);
    }

    public void printUsageAndHelp(PrintStream printStream) {
        printUsage(printStream);
        printHelp(printStream);
    }

    public void printHelp(PrintStream printStream) {
        CLAPHelpPrinter helpPrinter = new CLAPHelpPrinter(this);
        root.collectHelp(helpPrinter);
        helpPrinter.print(printStream);
    }

    public void printUsage(PrintStream printStream) {
        CLAPUsagePrinter usagePrinter = new CLAPUsagePrinter(this);
        root.collectUsage(usagePrinter);
        usagePrinter.print(printStream);
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

    public String nls(String key, Object... arguments) {
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

    /**
     * @see UserInteractionInterceptor
     */
    public <T> T userInteractionInterceptor(T object, String prompt, String cancelMessage, boolean setAfterRead) {
        @SuppressWarnings("unchecked") Class<T> tClass = (Class<T>) object.getClass();
        UserInteractionInterceptor<T> interceptor = new UserInteractionInterceptor<>(this, tClass, uiCallback);
        interceptor.setPrompt(prompt);
        interceptor.setCancelMessage(cancelMessage);
        interceptor.setSetAfterRead(setAfterRead);
        return interceptor.intercept(object);
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

}
