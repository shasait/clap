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

package de.hasait.clap.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPDecisionA;
import de.hasait.clap.CLAPOptionA;
import de.hasait.clap.CLAPValue;

/**
 * CLAPNode for handling annotated classes.
 */
public class CLAPClass<T> extends AbstractCLAPNodeList implements CLAPValue<T> {

	private final Class<T> _class;

	private final Map<CLAPValue<?>, PropertyDescriptor> _propertyDescriptorByOptionMap;

	public CLAPClass(final CLAP pCLAP, final Class<T> pClass) {
		super(pCLAP);

		_class = pClass;

		_propertyDescriptorByOptionMap = new HashMap<CLAPValue<?>, PropertyDescriptor>();

		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(pClass);
		} catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}

		for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
			final CLAPOptionA clapOptionA = getAnnotation(propertyDescriptor, CLAPOptionA.class);
			final CLAPDecisionA clapDecisionA = getAnnotation(propertyDescriptor, CLAPDecisionA.class);
			if (clapOptionA != null && clapDecisionA != null) {
				throw new IllegalArgumentException();
			}
			if (clapOptionA != null) {
				processCLAPOptionA(propertyDescriptor.getPropertyType(), propertyDescriptor, clapOptionA);
			}
			if (clapDecisionA != null) {
				processCLAPDecisionA(propertyDescriptor.getPropertyType(), propertyDescriptor, clapDecisionA);
			}
		}
	}

	private static <T extends Annotation> T getAnnotation(final PropertyDescriptor pPropertyDescriptor, final Class<T> pAnnotationClass) {
		final Method writeMethod = pPropertyDescriptor.getWriteMethod();
		if (writeMethod != null) {
			final T annotation = writeMethod.getAnnotation(pAnnotationClass);
			if (annotation != null) {
				return annotation;
			}
		}

		final Method readMethod = pPropertyDescriptor.getReadMethod();
		if (readMethod != null) {
			final T annotation = readMethod.getAnnotation(pAnnotationClass);
			if (annotation != null) {
				return annotation;
			}
		}

		return null;
	}

	@Override
	public final void fillResult(final CLAPParseContext pContext, final CLAPResultImpl pResult) {
		internalFillResult(pContext, pResult);

		T value;
		try {
			value = _class.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		boolean anySet = false;
		for (final Entry<CLAPValue<?>, PropertyDescriptor> entry : _propertyDescriptorByOptionMap.entrySet()) {
			final CLAPValue<?> node = entry.getKey();
			if (pResult.getCount(node) > 0) {
				anySet = true;
				final PropertyDescriptor propertyDescriptor = entry.getValue();
				final Object nodeValue = pResult.getValue(node);
				try {
					propertyDescriptor.getWriteMethod().invoke(value, nodeValue);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		if (anySet) {
			pResult.setCount(this, 1);
			pResult.setValue(this, value);
		}
	}

	@Override
	public final CLAPParseContext[] parse(final CLAPParseContext pContext) {
		return internalParse(pContext);
	}

	@Override
	public String toString() {
		return internalToString(" "); //$NON-NLS-1$
	}

	@Override
	public final void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		internalValidate(pContext, pErrorMessages);
	}

	@SuppressWarnings("unchecked")
	private <V> void processCLAPDecisionA(final Class<V> pPropertyType, final PropertyDescriptor pPropertyDescriptor, final CLAPDecisionA clapDecisionA) {
		final CLAPTypedDecision<V> decision = internalAddDecision(pPropertyType);
		final Class<? extends Object>[] branchClasses = clapDecisionA.branches();
		for (final Class<? extends Object> branchClass : branchClasses) {
			if (!pPropertyType.isAssignableFrom(branchClass)) {
				throw new IllegalArgumentException();
			}
			decision.addClass((Class<? extends V>) branchClass);
			_propertyDescriptorByOptionMap.put(decision, pPropertyDescriptor);
		}
	}

	private <V> void processCLAPOptionA(final Class<V> pPropertyType, final PropertyDescriptor pPropertyDescriptor, final CLAPOptionA clapOptionA) {
		final Character shortKey = clapOptionA.shortKey() == ' ' ? null : clapOptionA.shortKey();
		final String longKey = clapOptionA.longKey().length() == 0 ? null : clapOptionA.longKey();
		final boolean required = clapOptionA.required();
		final Integer argCount;
		final int argCountA = clapOptionA.argCount();
		if (argCountA == CLAPOptionA.UNLIMITED_ARG_COUNT) {
			argCount = CLAPOption.UNLIMITED_ARG_COUNT;
		} else if (argCountA == CLAPOptionA.AUTOMATIC_ARG_COUNT) {
			argCount = null;
		} else {
			argCount = argCountA;
		}
		final Character multiArgSplit = clapOptionA.multiArgSplit() == ' ' ? null : clapOptionA.multiArgSplit();
		final String descriptionNLSKey = clapOptionA.descriptionNLSKey().length() == 0 ? null : clapOptionA.descriptionNLSKey();
		final String argUsageNLSKey = clapOptionA.argUsageNLSKey().length() == 0 ? null : clapOptionA.argUsageNLSKey();

		final CLAPOption<?> option = internalAddOption(pPropertyType, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey);
		_propertyDescriptorByOptionMap.put(option, pPropertyDescriptor);
	}

}
