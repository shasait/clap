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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPDecision;
import de.hasait.clap.CLAPDelegate;
import de.hasait.clap.CLAPKeyword;
import de.hasait.clap.CLAPKeywords;
import de.hasait.clap.CLAPOption;
import de.hasait.clap.CLAPValue;

/**
 * CLAPNode for handling annotated classes.
 */
public class CLAPClassNode<T> extends AbstractCLAPNodeList implements CLAPValue<T> {

	private final Class<T> _class;

	private final Map<CLAPValue<?>, PropertyDescriptor> _propertyDescriptorByOptionMap;

	public CLAPClassNode(final CLAP pCLAP, final Class<T> pClass) {
		super(pCLAP);

		_class = pClass;

		_propertyDescriptorByOptionMap = new HashMap<CLAPValue<?>, PropertyDescriptor>();

		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(pClass);
		} catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}

		final List<Item> annotations = new ArrayList<Item>();

		final CLAPKeywords clapKeywords = pClass.getAnnotation(CLAPKeywords.class);
		if (clapKeywords != null) {
			for (final CLAPKeyword clapKeyword : clapKeywords.value()) {
				annotations.add(new Item(clapKeyword.order(), clapKeyword, null));
			}
		}

		for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
			final CLAPOption clapOption = getAnnotation(propertyDescriptor, CLAPOption.class);
			final CLAPDelegate clapDelegate = getAnnotation(propertyDescriptor, CLAPDelegate.class);
			final CLAPDecision clapDecision = getAnnotation(propertyDescriptor, CLAPDecision.class);
			if ((clapOption != null ? 1 : 0) + (clapDelegate != null ? 1 : 0) + (clapDecision != null ? 1 : 0) > 1) {
				throw new IllegalArgumentException();
			}
			if (clapOption != null) {
				annotations.add(new Item(clapOption.order(), clapOption, propertyDescriptor));
			}
			if (clapDelegate != null) {
				annotations.add(new Item(clapDelegate.order(), clapDelegate, propertyDescriptor));
			}
			if (clapDecision != null) {
				annotations.add(new Item(clapDecision.order(), clapDecision, propertyDescriptor));
			}
		}

		Collections.sort(annotations, new Comparator<Item>() {

			@Override
			public int compare(final Item pO1, final Item pO2) {
				return Integer.valueOf(pO1._order).compareTo(Integer.valueOf(pO2._order));
			}

		});

		for (final Item entry : annotations) {
			final Object annotation = entry._annotation;
			if (annotation instanceof CLAPKeyword) {
				internalAddKeyword(((CLAPKeyword) annotation).value());
			}
			if (annotation instanceof CLAPOption) {
				processCLAPOption(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor, (CLAPOption) annotation);
			}
			if (annotation instanceof CLAPDelegate) {
				processCLAPDelegate(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor, (CLAPDelegate) annotation);
			}
			if (annotation instanceof CLAPDecision) {
				processCLAPDecision(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor, (CLAPDecision) annotation);
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
	public void printUsage(final StringBuilder pResult) {
		internalPrintUsage(pResult, " "); //$NON-NLS-1$
	}

	@Override
	public final void validate(final CLAPParseContext pContext, final List<String> pErrorMessages) {
		internalValidate(pContext, pErrorMessages);
	}

	private <V> void processCLAPDecision(final Class<V> pPropertyType, final PropertyDescriptor pPropertyDescriptor, final CLAPDecision pClapDecision) {
		final CLAPTypedDecisionNode<V> decisionNode = internalAddDecision(pPropertyType);
		final Class<? extends Object>[] branchClasses = pClapDecision.branches();
		for (final Class<? extends Object> branchClassUncasted : branchClasses) {
			if (!pPropertyType.isAssignableFrom(branchClassUncasted)) {
				throw new IllegalArgumentException();
			}
			@SuppressWarnings("unchecked")
			final Class<? extends V> branchClass = (Class<? extends V>) branchClassUncasted;
			decisionNode.addClass(branchClass);
			_propertyDescriptorByOptionMap.put(decisionNode, pPropertyDescriptor);
		}
	}

	private <V> void processCLAPDelegate(final Class<V> pPropertyType, final PropertyDescriptor pPropertyDescriptor, final CLAPDelegate pClapDelegate) {
		final CLAPClassNode<V> classNode = internalAddClass(pPropertyType);
		_propertyDescriptorByOptionMap.put(classNode, pPropertyDescriptor);
	}

	private <V> void processCLAPOption(final Class<V> pPropertyType, final PropertyDescriptor pPropertyDescriptor, final CLAPOption pClapOption) {
		final Character shortKey = pClapOption.shortKey() == ' ' ? null : pClapOption.shortKey();
		final String longKey = pClapOption.longKey().length() == 0 ? null : pClapOption.longKey();
		final boolean required = pClapOption.required();
		final Integer argCount;
		final int argCountA = pClapOption.argCount();
		if (argCountA == CLAPOption.UNLIMITED_ARG_COUNT) {
			argCount = CLAPOptionNode.UNLIMITED_ARG_COUNT;
		} else if (argCountA == CLAPOption.AUTOMATIC_ARG_COUNT) {
			argCount = null;
		} else {
			argCount = argCountA;
		}
		final Character multiArgSplit = pClapOption.multiArgSplit() == ' ' ? null : pClapOption.multiArgSplit();
		final String descriptionNLSKey = pClapOption.descriptionNLSKey().length() == 0 ? null : pClapOption.descriptionNLSKey();
		final String argUsageNLSKey = pClapOption.argUsageNLSKey().length() == 0 ? null : pClapOption.argUsageNLSKey();

		final CLAPOptionNode<?> optionNode = internalAddOption(pPropertyType, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey);
		_propertyDescriptorByOptionMap.put(optionNode, pPropertyDescriptor);
	}

	private static class Item {

		public final int _order;
		public final Annotation _annotation;
		public final PropertyDescriptor _propertyDescriptor;

		public Item(final int pOrder, final Annotation pAnnotation, final PropertyDescriptor pPropertyDescriptor) {
			super();
			_order = pOrder;
			_annotation = pAnnotation;
			_propertyDescriptor = pPropertyDescriptor;
		}

	}

}
