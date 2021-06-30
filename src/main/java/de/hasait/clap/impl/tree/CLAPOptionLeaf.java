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

package de.hasait.clap.impl.tree;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPConverter;
import de.hasait.clap.CLAPValue;
import de.hasait.clap.impl.help.CLAPHelpPrinter;
import de.hasait.clap.impl.parser.CLAPParseContext;
import de.hasait.clap.impl.parser.CLAPResultImpl;

/**
 * An option node.
 */
public final class CLAPOptionLeaf<T> extends AbstractCLAPLeaf implements CLAPValue<T> {

    private static final String NLSKEY_CLAP_ERROR_OPTION_IS_MISSING = "clap.error.optionIsMissing";
    private static final String NLSKEY_CLAP_ERROR_INCORRECT_NUMBER_OF_ARGUMENTS = "clap.error.incorrectNumberOfArguments";
    private static final String NLSKEY_CLAP_DEFAULT_ARG = "clap.defaultArg";

    private final Class<T> _resultClass;

    private final Character _shortKey;

    private final String _longKey;

    private final boolean _required;

    private final int _argCount;

    private final Character _multiArgSplit;

    private final String helpDescription;

    private final String argUsageTitle;

    private final boolean _immediateReturn;

    private final boolean password;

    private final Mapper<T> _mapper;

    public CLAPOptionLeaf(CLAP clap, Class<T> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String helpDescription, String argUsageTitle, boolean immediateReturn, boolean password) {
        super(clap);

        _resultClass = resultClass;

        if (shortKey != null && shortKey == clap.getShortOptPrefix()) {
            throw new IllegalArgumentException("ShortKey " + shortKey + " cannot be shortOptPrefix " + clap.getShortOptPrefix());
        }
        _shortKey = shortKey;

        if (longKey != null && longKey.contains(clap.getLongOptAssignment())) {
            throw new IllegalArgumentException("LongKey " + longKey + " cannot contain longOptAssignment " + clap.getLongOptAssignment());
        }
        _longKey = longKey;

        _required = required;


        if (argCount == null) {
            // autodetect using resultClass
            if (resultClass.isArray() || Collection.class.isAssignableFrom(resultClass)) {
                _argCount = CLAP.UNLIMITED_ARG_COUNT;
            } else if (resultClass.equals(Boolean.class) || resultClass.equals(Boolean.TYPE)) {
                _argCount = 0;
            } else {
                _argCount = 1;
            }
        } else {
            if (argCount < 0 && argCount != CLAP.UNLIMITED_ARG_COUNT) {
                throw new IllegalArgumentException("Invalid argCount: " + argCount);
            }

            if (resultClass.isArray() || Collection.class.isAssignableFrom(resultClass)) {
                if (argCount == 0) {
                    throw new IllegalArgumentException(
                            "Invalid argCount for array or collection type: " + argCount + " vs. " + resultClass);
                }
            } else if (resultClass.equals(Boolean.class) || resultClass.equals(Boolean.TYPE)) {
                if (argCount != 0 && argCount != 1) {
                    throw new IllegalArgumentException("Invalid argCount for boolean: " + argCount);
                }
            } else {
                if (argCount != 1) {
                    throw new IllegalArgumentException("Expected argCount 1 for single value type: " + argCount + " vs. " + resultClass);
                }
            }

            _argCount = argCount;
        }

        if (shortKey == null && longKey == null && _argCount == 0) {
            throw new IllegalArgumentException("Nameless options need an argCount > 0");
        }

        _multiArgSplit = multiArgSplit;
        this.helpDescription = helpDescription;
        this.argUsageTitle = argUsageTitle;
        _immediateReturn = immediateReturn;
        this.password = password;

        if (resultClass.isArray()) {
            final Class<?> componentType = resultClass.getComponentType();
            final CLAPConverter<?> converter = clap.getConverter(componentType);
            _mapper = stringValues -> {
                final T result = (T) Array.newInstance(componentType, stringValues.length);
                for (int i = 0; i < stringValues.length; i++) {
                    try {
                        Array.set(result, i, converter.convert(stringValues[i]));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return result;
            };
        } else if (Collection.class.isAssignableFrom(resultClass)) {
            _mapper = null;
        } else {
            final CLAPConverter<? extends T> converter = clap.getConverter(resultClass);
            _mapper = stringValues -> {
                if (stringValues.length == 0 && (resultClass.equals(Boolean.class) || resultClass.equals(Boolean.TYPE))) {
                    return (T) Boolean.TRUE;
                }
                return converter.convert(stringValues[0]);
            };
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_shortKey).append(_longKey).append(_required).append(_argCount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        final CLAPOptionLeaf<?> casted = (CLAPOptionLeaf<?>) other;

        if (!ObjectUtils.equals(_resultClass, casted._resultClass)) {
            return false;
        }

        if (_shortKey != casted._shortKey) {
            return false;
        }

        if (!StringUtils.equals(_longKey, casted._longKey)) {
            return false;
        }

        if (_required != casted._required) {
            return false;
        }

        if (_argCount != casted._argCount) {
            return false;
        }

        if (!ObjectUtils.equals(_multiArgSplit, casted._multiArgSplit)) {
            return false;
        }

        if (!StringUtils.equals(helpDescription, casted.helpDescription)) {
            return false;
        }

        if (!StringUtils.equals(argUsageTitle, casted.argUsageTitle)) {
            return false;
        }

        if (_immediateReturn != casted._immediateReturn) {
            return false;
        }

        // _mapper depends on _resultClass

        return true;
    }

    public Character getShortKey() {
        return _shortKey;
    }

    public String getLongKey() {
        return _longKey;
    }

    public boolean isRequired() {
        return _required;
    }

    public String getHelpDescription() {
        return helpDescription;
    }

    public String getArgUsageTitle() {
        return argUsageTitle;
    }

    public boolean isImmediateReturn() {
        return _immediateReturn;
    }

    public boolean isPassword() {
        return password;
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
        final List<String> args = parseArgs(context);
        if (args == null) {
            return null;
        }
        context.addOption(this, args);
        return new CLAPParseContext[]{
                context
        };
    }

    @Override
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        if (context.getNodeCount(this) == 0) {
            if (_required) {
                errorMessages.add(nls(NLSKEY_CLAP_ERROR_OPTION_IS_MISSING, buildHelpEntryTitle()));
            }
        } else if (_argCount != CLAP.UNLIMITED_ARG_COUNT && _argCount != context.getArgCount(this)) {
            errorMessages
                    .add(nls(NLSKEY_CLAP_ERROR_INCORRECT_NUMBER_OF_ARGUMENTS, buildHelpEntryTitle(), _argCount, context.getArgCount(this)));
        }
    }

    @Override
    public boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        final int optionCount = context.getNodeCount(this);
        if (optionCount > 0) {
            result.setCount(this, optionCount);
            final String[] stringValues = context.getOptionArgs(this);
            result.setValue(this, _mapper.transform(stringValues));
            return true;
        }
        return false;
    }

    @Override
    public void collectHelp(CLAPHelpPrinter helpPrinter) {
        helpPrinter.addEntry(getHelpCategoryTitle(), getHelpCategoryOrder(), buildHelpEntryTitle(), getHelpDescription(), getHelpEntryOrder());
    }

    @Override
    protected String buildUsageEntryText() {
        StringBuilder result = new StringBuilder();
        if (_required) {
            if (_shortKey != null && _longKey != null) {
                result.append('{');
            }
        } else {
            result.append('[');
        }
        if (_shortKey != null) {
            result.append(clap.getShortOptPrefix());
            result.append(_shortKey);
            if (_longKey != null) {
                result.append('|');
            }
        }
        if (_longKey != null) {
            result.append(clap.getLongOptPrefix());
            result.append(_longKey);
        }
        if (_argCount != 0) {
            final int count = _argCount == CLAP.UNLIMITED_ARG_COUNT ? 2 : _argCount;
            for (int i = 0; i < count; i++) {
                if (_multiArgSplit != null) {
                    if (i == 0) {
                        result.append(clap.getLongOptAssignment());
                    } else {
                        result.append(_multiArgSplit);
                    }
                } else {
                    result.append(' ');
                }
                if (_argCount == CLAP.UNLIMITED_ARG_COUNT && i == count - 1) {
                    result.append("...");
                } else {
                    result.append('<');
                    result.append(argUsageTitle != null ? nls(argUsageTitle) : nls(NLSKEY_CLAP_DEFAULT_ARG));
                    if (count > 1 + (_argCount == CLAP.UNLIMITED_ARG_COUNT ? 1 : 0)) {
                        result.append(i + 1);
                    }
                    result.append('>');
                }
            }
        }
        if (_required) {
            if (_shortKey != null && _longKey != null) {
                result.append('}');
            }
        } else {
            result.append(']');
        }

        return result.toString();
    }

    private String buildHelpEntryTitle() {
        final StringBuilder helpIDSB = new StringBuilder();
        if (_shortKey != null) {
            helpIDSB.append(clap.getShortOptPrefix()).append(_shortKey);
            if (_longKey != null) {
                helpIDSB.append(", ");
            }
        }
        if (_longKey != null) {
            helpIDSB.append(clap.getLongOptPrefix()).append(_longKey);
        }
        if (_shortKey == null && _longKey == null) {
            final String text = argUsageTitle != null ? nls(argUsageTitle) : nls(CLAPOptionLeaf.NLSKEY_CLAP_DEFAULT_ARG);
            helpIDSB.append('<').append(text).append('>');
        }
        return helpIDSB.toString();
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[''{1}'', \"{2}\"]", getClass().getSimpleName(), _shortKey, _longKey);
    }

    private List<String> handleArgCount(CLAPParseContext context, boolean allWithSplit) {
        final List<String> args = new ArrayList<>();
        if (_argCount == CLAP.UNLIMITED_ARG_COUNT) {
            if (allWithSplit) {
                if (context.hasMoreTokens()) {
                    final String argsUnsplitted = context.consumeCurrent();
                    args.addAll(Arrays.asList(argsUnsplitted.split(_multiArgSplit.toString())));
                }
            } else {
                while (context.hasMoreTokens()) {
                    args.add(context.consumeCurrent());
                }
            }
        } else {
            if (allWithSplit && _argCount != 0 && _argCount != 1) {
                if (context.hasMoreTokens()) {
                    final String argsUnsplitted = context.consumeCurrent();
                    args.addAll(Arrays.asList(argsUnsplitted.split(_multiArgSplit.toString())));
                }
            } else {
                int i = 0;
                while (i++ < _argCount) {
                    args.add(context.consumeCurrent());
                }
            }
        }
        return args;
    }

    private List<String> parseArgs(CLAPParseContext context) {
        final boolean hasArg = _argCount != 0;
        final boolean allowEqualsForLongOpt = _argCount == 1 || _multiArgSplit != null;
        if (_shortKey != null && context.hasCurrentShortKey(_shortKey)) {
            final boolean hasDirectFollower = context.consumeCurrentShortKey(_shortKey, hasArg);
            return handleArgCount(context, hasDirectFollower);
        } else if (_longKey != null && context.hasCurrentLongKey(_longKey, allowEqualsForLongOpt)) {
            final boolean wasEquals = context.consumeCurrentLongKey(_longKey, allowEqualsForLongOpt);
            return handleArgCount(context, wasEquals);
        } else if (_shortKey == null && _longKey == null) {
            return handleArgCount(context, false);
        } else {
            return null;
        }
    }

    public interface Mapper<T> {

        T transform(String[] stringValues);

    }

}
