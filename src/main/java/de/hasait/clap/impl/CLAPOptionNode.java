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

package de.hasait.clap.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPValue;

/**
 * An option node.
 */
public final class CLAPOptionNode<T> extends AbstractCLAPNode implements CLAPValue<T>, CLAPHelpNode {

	private static final String NLSKEY_CLAP_DEFAULT_ARG = "clap.defaultArg"; //$NON-NLS-1$

	public static final int UNLIMITED_ARG_COUNT = -1;

	private final Character _shortKey;

	private final String _longKey;

	private final boolean _required;

	private final int _argCount;

	private final Character _multiArgSplit;

	private final String _descriptionNLSKey;

	private final String _argUsageNLSKey;

	private final Class<T> _resultClass;

	private final Mapper<T> _mapper;

	private CLAPOptionNode(final CLAP pCLAP, final Class<T> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired, final Integer pArgCount,
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
				_argCount = CLAPOptionNode.UNLIMITED_ARG_COUNT;
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
					if (pStringValues.length == 0) {
						return true;
					}
					if (pStringValues[0].equalsIgnoreCase("true")) {
						return true;
					}
					if (pStringValues[0].equalsIgnoreCase("yes")) {
						return true;
					}
					if (pStringValues[0].equalsIgnoreCase("on")) {
						return true;
					}
					if (pStringValues[0].equalsIgnoreCase("enable")) {
						return true;
					}
					return false;
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
		_resultClass = pResultClass;

	}

	public static <V> CLAPOptionNode<V> create(final CLAP pCLAP, final Class<V> pResultClass, final Character pShortKey, final String pLongKey, final boolean pRequired,
			final Integer pArgCount, final Character pMultiArgSplit, final String pDescriptionNLSKey, final String pArgUsageNLSKey) {
		return new CLAPOptionNode<V>(pCLAP, pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
	}

	@Override
	public void collectHelpNodes(final Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pNodes, final CLAPHelpCategoryImpl pCurrentCategory) {
		addHelpNode(pNodes, pCurrentCategory, this);
	}

	@Override
	public boolean equals(final Object pOther) {
		if (pOther == this) {
			return true;
		}

		if (pOther == null) {
			return false;
		}

		if (getClass() != pOther.getClass()) {
			return false;
		}

		final CLAPOptionNode<?> other = (CLAPOptionNode<?>) pOther;

		if (_shortKey != other._shortKey) {
			return false;
		}

		if (!StringUtils.equals(_longKey, other._longKey)) {
			return false;
		}

		if (_required != other._required) {
			return false;
		}

		if (_argCount != other._argCount) {
			return false;
		}

		if (!ObjectUtils.equals(_multiArgSplit, other._multiArgSplit)) {
			return false;
		}

		if (!StringUtils.equals(_descriptionNLSKey, other._descriptionNLSKey)) {
			return false;
		}

		if (!StringUtils.equals(_argUsageNLSKey, other._argUsageNLSKey)) {
			return false;
		}

		if (!ObjectUtils.equals(_resultClass, other._resultClass)) {
			return false;
		}

		// _mapper depends on _resultClass 

		return true;
	}

	@Override
	public void fillResult(final CLAPParseContext pContext, final CLAPResultImpl pResult) {
		final int optionCount = pContext.getNodeCount(this);
		if (optionCount > 0) {
			pResult.setCount(this, optionCount);
			final String[] stringValues = pContext.getOptionArgs(this);
			pResult.setValue(this, _mapper.transform(stringValues));
		}
	}

	public String getArgUsageNLSKey() {
		return _argUsageNLSKey;
	}

	@Override
	public String getDescriptionNLSKey() {
		return _descriptionNLSKey;
	}

	@Override
	public String getHelpID() {
		final StringBuilder helpIDSB = new StringBuilder();
		if (_shortKey != null) {
			helpIDSB.append(getCLAP().getShortOptPrefix()).append(_shortKey);
			if (_longKey != null) {
				helpIDSB.append(", "); //$NON-NLS-1$
			}
		}
		if (_longKey != null) {
			helpIDSB.append(getCLAP().getLongOptPrefix()).append(_longKey);
		}
		if (_shortKey == null && _longKey == null) {
			final String text = _argUsageNLSKey != null ? nls(_argUsageNLSKey) : nls(CLAPOptionNode.NLSKEY_CLAP_DEFAULT_ARG);
			helpIDSB.append('<').append(text).append('>');
		}
		return helpIDSB.toString();
	}

	public String getLongKey() {
		return _longKey;
	}

	public Character getShortKey() {
		return _shortKey;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(_shortKey).append(_longKey).append(_required).append(_argCount).toHashCode();
	}

	public boolean isRequired() {
		return _required;
	}

	@Override
	public CLAPParseContext[] parse(final CLAPParseContext pContext) {
		final List<String> args = parseArgs(pContext);
		if (args == null) {
			return null;
		}
		pContext.addOption(this, args);
		return new CLAPParseContext[] {
			pContext
		};
	}

	@Override
	public void printUsage(final Map<CLAPUsageCategoryImpl, StringBuilder> pCategories, final CLAPUsageCategoryImpl pCurrentCategory, final StringBuilder pResult) {
		if (_required) {
			if (_shortKey != null && _longKey != null) {
				pResult.append('{');
			}
		} else {
			pResult.append('[');
		}
		if (_shortKey != null) {
			pResult.append(getCLAP().getShortOptPrefix());
			pResult.append(_shortKey);
			if (_longKey != null) {
				pResult.append('|');
			}
		}
		if (_longKey != null) {
			pResult.append(getCLAP().getLongOptPrefix());
			pResult.append(_longKey);
		}
		if (_argCount != 0) {
			final int count = _argCount == UNLIMITED_ARG_COUNT ? 2 : _argCount;
			for (int i = 0; i < count; i++) {
				if (_multiArgSplit != null) {
					if (i == 0) {
						pResult.append(getCLAP().getLongOptEquals());
					} else {
						pResult.append(_multiArgSplit);
					}
				} else {
					pResult.append(' ');
				}
				if (_argCount == UNLIMITED_ARG_COUNT && i == count - 1) {
					pResult.append("..."); //$NON-NLS-1$
				} else {
					pResult.append('<');
					pResult.append(_argUsageNLSKey != null ? nls(_argUsageNLSKey) : nls(NLSKEY_CLAP_DEFAULT_ARG));
					if (count > 1 + (_argCount == UNLIMITED_ARG_COUNT ? 1 : 0)) {
						pResult.append(i + 1);
					}
					pResult.append('>');
				}
			}
		}
		if (_required) {
			if (_shortKey != null && _longKey != null) {
				pResult.append('}');
			}
		} else {
			pResult.append(']');
		}
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}[''{1}'', \"{2}\"]", getClass().getSimpleName(), _shortKey, _longKey); //$NON-NLS-1$ 
	}

	@Override
	public void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		if (pContext.getNodeCount(this) == 0) {
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
