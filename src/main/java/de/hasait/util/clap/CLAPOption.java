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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An option node.
 */
public final class CLAPOption<T> extends AbstractCLAPNode implements ICLAPHasResult<T> {

	public static final int UNLIMITED_ARG_COUNT = -1;

	private final Character _shortKey;

	private final String _longKey;

	private final boolean _required;

	private final int _argCount;

	private final Character _multiArgSplit;

	private final String _descriptionNLSKey;

	private final String _argUsageNLSKey;

	private final Mapper<T> _mapper;

	private CLAPOption(final CLAP pCLAP, final Class<T> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		super(pCLAP);

		if (pShortKey != null && pShortKey == getCLAP().getShortOptPrefix()) {
			throw new IllegalArgumentException();
		}
		if (pLongKey != null && pLongKey.contains(getCLAP().getLongOptEquals())) {
			throw new IllegalArgumentException();
		}

		if (pArgCount == null) {
			// autodetect using resultClass
			if (pResultClass.isArray() || Collection.class.isAssignableFrom(pResultClass)) {
				_argCount = CLAPOption.UNLIMITED_ARG_COUNT;
			} else if (pResultClass.equals(Boolean.class) || pResultClass.equals(Boolean.TYPE)) {
				_argCount = 0;
			} else {
				_argCount = 1;
			}
		} else {
			if (pArgCount < 0 && pArgCount != UNLIMITED_ARG_COUNT) {
				throw new IllegalArgumentException();
			}

			if (pResultClass.isArray() || Collection.class.isAssignableFrom(pResultClass)) {
				if (pArgCount == 0) {
					throw new IllegalArgumentException();
				}
			} else if (pResultClass.equals(Boolean.class) || pResultClass.equals(Boolean.TYPE)) {
				if (pArgCount != 0 && pArgCount != 1) {
					throw new IllegalArgumentException();
				}
			} else {
				if (pArgCount != 1) {
					throw new IllegalArgumentException();
				}
			}

			_argCount = pArgCount;
		}

		if (pShortKey == null && pLongKey == null && _argCount == 0) {
			throw new IllegalArgumentException();
		}

		if (pResultClass.isArray()) {
			final Class<?> componentType = pResultClass.getComponentType();
			if (componentType.equals(String.class)) {
				_mapper = (Mapper<T>) new Mapper<String[]>() {

					@Override
					public String[] transform(final String[] pStringValues) {
						return pStringValues;
					}

				};
			} else {
				final Constructor<?> constructor;
				try {
					constructor = componentType.getConstructor(String.class);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
				_mapper = new Mapper<T>() {

					@Override
					public T transform(final String[] pStringValues) {
						final T result = (T) Array.newInstance(componentType, pStringValues.length);
						for (int i = 0; i < pStringValues.length; i++) {
							try {
								Array.set(result, i, constructor.newInstance(pStringValues[i]));
							} catch (final Exception e) {
								throw new RuntimeException(e);
							}
						}
						return result;
					}

				};
			}
		} else if (Collection.class.isAssignableFrom(pResultClass)) {
			_mapper = null;
		} else if (pResultClass.equals(Boolean.class) || pResultClass.equals(Boolean.TYPE)) {
			_mapper = (Mapper<T>) new Mapper<Boolean>() {

				@Override
				public Boolean transform(final String[] pStringValues) {
					return pStringValues.length == 0 ? true : pStringValues[0].equalsIgnoreCase("true") || pStringValues[0].equalsIgnoreCase("yes") ||
							pStringValues[0].equals("on");
				}

			};
		} else {
			if (pResultClass.equals(String.class)) {
				_mapper = (Mapper<T>) new Mapper<String>() {

					@Override
					public String transform(final String[] pStringValues) {
						return pStringValues[0];
					}

				};
			} else {
				final Constructor<T> constructor;
				try {
					constructor = pResultClass.getConstructor(String.class);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
				_mapper = new Mapper<T>() {

					@Override
					public T transform(final String[] pStringValues) {
						try {
							return constructor.newInstance(pStringValues[0]);
						} catch (final Exception e) {
							throw new RuntimeException(e);
						}
					}

				};
			}
		}

		_shortKey = pShortKey;
		_longKey = pLongKey;
		_required = pRequired;
		_multiArgSplit = pMultiArgSplit;
		_descriptionNLSKey = pDescriptionNLSKey;
		_argUsageNLSKey = pArgUsageNLSKey;

	}

	static <V> CLAPOption<V> create(final CLAP pCLAP, final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
			final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return new CLAPOption<V>(pCLAP, pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public void fillResult(final CLAPParseContext pContext, final CLAPResult pResult) {
		final int optionCount = pContext.getOptionCount(this);
		if (optionCount > 0) {
			pResult.setCount(this, optionCount);
			final String[] stringValues = pContext.getStringValues(this);
			pResult.setValue(this, _mapper.transform(stringValues));
		}
	}

	@Override
	public CLAPParseContext[] parse(final CLAPParseContext pContext) {
		final List<String> args = parseArgs(pContext);
		if (args == null) {
			return null;
		}
		pContext.addOptionToResult(this, args);
		return new CLAPParseContext[] {
			pContext
		};
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		if (_required) {
			if (_shortKey != null && _longKey != null) {
				result.append('{');
			}
		} else {
			result.append('[');
		}
		if (_shortKey != null) {
			result.append(getCLAP().getShortOptPrefix());
			result.append(_shortKey);
			if (_longKey != null) {
				result.append('|');
			}
		}
		if (_longKey != null) {
			result.append(getCLAP().getLongOptPrefix());
			result.append(_longKey);
		}
		if (_argCount != 0) {
			final int count = _argCount == UNLIMITED_ARG_COUNT ? 3 : _argCount;
			for (int i = 0; i < count; i++) {
				if (_multiArgSplit != null) {
					if (i == 0) {
						result.append(getCLAP().getLongOptEquals());
					} else {
						result.append(_multiArgSplit);
					}
				} else {
					result.append(' ');
				}
				if (_argCount == UNLIMITED_ARG_COUNT && i == 2) {
					result.append("..."); //$NON-NLS-1$
				} else {
					result.append('<');
					result.append(_argUsageNLSKey);
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

	@Override
	public void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		if (pContext.getOptionCount(this) == 0) {
			if (_required) {
				pErrorMessages.add(this + " is missing"); //$NON-NLS-1$
			}
		} else if (_argCount != UNLIMITED_ARG_COUNT && _argCount != pContext.getArgCount(this)) {
			pErrorMessages.add(this + " has incorrect number of arguments"); //$NON-NLS-1$
		}
	}

	private List<String> handleArgCount(final CLAPParseContext pContext, final boolean pAllWithSplit) {
		final List<String> args = new ArrayList<String>();
		if (_argCount == UNLIMITED_ARG_COUNT) {
			if (pAllWithSplit) {
				if (pContext.hasMoreTokens()) {
					final String argsUnsplitted = pContext.consumeCurrent();
					for (final String arg : argsUnsplitted.split(_multiArgSplit.toString())) {
						args.add(arg);
					}
				}
			} else {
				while (pContext.hasMoreTokens()) {
					args.add(pContext.consumeCurrent());
				}
			}
		} else {
			if (pAllWithSplit && _argCount != 0 && _argCount != 1) {
				if (pContext.hasMoreTokens()) {
					final String argsUnsplitted = pContext.consumeCurrent();
					for (final String arg : argsUnsplitted.split(_multiArgSplit.toString())) {
						args.add(arg);
					}
				}
			} else {
				int i = 0;
				while (i++ < _argCount) {
					args.add(pContext.consumeCurrent());
				}
			}
		}
		return args;
	}

	private List<String> parseArgs(final CLAPParseContext pContext) {
		final boolean hasArg = _argCount != 0;
		final boolean allowEqualsForLongOpt = _argCount == 1 || _multiArgSplit != null;
		if (_shortKey != null && pContext.hasCurrentShortKey(_shortKey)) {
			final boolean hasDirectFollower = pContext.consumeCurrentShortKey(_shortKey, hasArg);
			return handleArgCount(pContext, hasDirectFollower);
		} else if (_longKey != null && pContext.hasCurrentLongKey(_longKey, allowEqualsForLongOpt)) {
			final boolean wasEquals = pContext.consumeCurrentLongKey(_longKey, allowEqualsForLongOpt);
			return handleArgCount(pContext, wasEquals);
		} else if (_shortKey == null && _longKey == null) {
			return handleArgCount(pContext, false);
		} else {
			return null;
		}
	}

	public static interface Mapper<T> {

		T transform(String[] pStringValues);

	}

}
