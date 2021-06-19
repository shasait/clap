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

package de.hasait.clap.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPDecision;
import de.hasait.clap.CLAPDelegate;
import de.hasait.clap.CLAPHelpCategory;
import de.hasait.clap.CLAPKeyword;
import de.hasait.clap.CLAPKeywords;
import de.hasait.clap.CLAPOption;
import de.hasait.clap.CLAPUsageCategory;
import de.hasait.clap.CLAPValue;

/**
 * CLAPNode for handling annotated classes.
 */
public class CLAPClassNode<T> extends AbstractCLAPNodeList implements CLAPValue<T> {

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationClass) {
        final Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            final Class<?> currentClass = queue.removeFirst();
            final A result = currentClass.getAnnotation(annotationClass);
            if (result != null) {
                return result;
            }
            Class<?> superclass = currentClass.getSuperclass();
            if (superclass != null) {
                queue.add(superclass);
            }
            queue.addAll(Arrays.asList(currentClass.getInterfaces()));
        }
        return null;
    }

    private static <T extends Annotation> T getAnnotation(PropertyDescriptor propertyDescriptor, Class<T> annotationClass) {
        final Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod != null) {
            final T annotation = writeMethod.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }

        final Method readMethod = propertyDescriptor.getReadMethod();
        if (readMethod != null) {
            final T annotation = readMethod.getAnnotation(annotationClass);
            return annotation;
        }

        return null;
    }

    private final Class<T> _class;

    private final Map<CLAPValue<?>, PropertyDescriptor> _propertyDescriptorByOptionMap;

    private final Set<CLAPKeywordNode> _keywordNodes;

    public CLAPClassNode(CLAP clap, Class<T> clazz) {
        super(clap);

        _class = clazz;

        _propertyDescriptorByOptionMap = new HashMap<>();
        _keywordNodes = new HashSet<>();

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        final List<Item> annotations = new ArrayList<>();

        final CLAPKeywords classKeywords = findAnnotation(clazz, CLAPKeywords.class);
        if (classKeywords != null) {
            for (CLAPKeyword classKeyword : classKeywords.value()) {
                annotations.add(new Item(classKeyword.order(), classKeyword, null, null, null));
            }
        }

        final CLAPHelpCategory classHelpCategory = findAnnotation(clazz, CLAPHelpCategory.class);
        if (classHelpCategory != null) {
            setHelpCategory(classHelpCategory.order(), classHelpCategory.titleNLSKey());
        }

        final CLAPUsageCategory classUsageCategory = findAnnotation(clazz, CLAPUsageCategory.class);
        if (classUsageCategory != null) {
            setUsageCategory(classUsageCategory.order(), classUsageCategory.titleNLSKey());
        }

        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            final CLAPOption pdOption = getAnnotation(propertyDescriptor, CLAPOption.class);
            final CLAPDelegate pdDelegate = getAnnotation(propertyDescriptor, CLAPDelegate.class);
            final CLAPDecision pdDecision = getAnnotation(propertyDescriptor, CLAPDecision.class);
            final CLAPHelpCategory pdHelpCategory = getAnnotation(propertyDescriptor, CLAPHelpCategory.class);
            final CLAPUsageCategory pdUsageCategory = getAnnotation(propertyDescriptor, CLAPUsageCategory.class);
            if ((pdOption != null ? 1 : 0) + (pdDelegate != null ? 1 : 0) + (pdDecision != null ? 1 : 0) > 1) {
                throw new IllegalArgumentException("Cannot mix annotations "
                                                           + CLAPOption.class.getSimpleName()
                                                           + ", "
                                                           + CLAPDelegate.class.getSimpleName()
                                                           + ", "
                                                           + CLAPDecision.class.getSimpleName()
                                                           + " on a property: "
                                                           + propertyDescriptor);
            }
            if (pdOption != null) {
                annotations.add(new Item(pdOption.order(), pdOption, propertyDescriptor, pdHelpCategory, pdUsageCategory));
            }
            if (pdDelegate != null) {
                annotations.add(new Item(pdDelegate.order(), pdDelegate, propertyDescriptor, pdHelpCategory, pdUsageCategory));
            }
            if (pdDecision != null) {
                annotations.add(new Item(pdDecision.order(), pdDecision, propertyDescriptor, pdHelpCategory, pdUsageCategory));
            }
        }

        annotations.sort(Comparator.comparing(a -> a._order));

        for (Item entry : annotations) {
            final Object annotation = entry._annotation;

            final AbstractCLAPNode node;
            if (annotation instanceof CLAPKeyword) {
                node = processCLAPKeyword((CLAPKeyword) annotation);
            } else if (annotation instanceof CLAPOption) {
                node = processCLAPOption(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor, (CLAPOption) annotation);
            } else if (annotation instanceof CLAPDelegate) {
                node = processCLAPDelegate(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor,
                                           (CLAPDelegate) annotation
                );
            } else if (annotation instanceof CLAPDecision) {
                node = processCLAPDecision(entry._propertyDescriptor.getPropertyType(), entry._propertyDescriptor,
                                           (CLAPDecision) annotation
                );
            } else {
                throw new RuntimeException();
            }

            if (entry._helpCategory != null) {
                node.setHelpCategory(entry._helpCategory.order(), entry._helpCategory.titleNLSKey());
            }
            if (entry._usageCategory != null) {
                node.setUsageCategory(entry._usageCategory.order(), entry._usageCategory.titleNLSKey());
            }
        }

    }

    @Override
    public final boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        internalFillResult(context, result);

        T value;
        try {
            value = _class.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean anySet = false;
        for (Entry<CLAPValue<?>, PropertyDescriptor> entry : _propertyDescriptorByOptionMap.entrySet()) {
            final CLAPValue<?> node = entry.getKey();
            if (result.getCount(node) > 0) {
                anySet = true;
                final PropertyDescriptor propertyDescriptor = entry.getValue();
                final Object nodeValue = result.getValue(node);
                try {
                    propertyDescriptor.getWriteMethod().invoke(value, nodeValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (CLAPKeywordNode node : _keywordNodes) {
            if (context.getNodeCount(node) > 0) {
                anySet = true;
            }
        }
        if (anySet) {
            result.setCount(this, 1);
            result.setValue(this, value);
        }

        return anySet;
    }

    @Override
    public final CLAPParseContext[] parse(CLAPParseContext context) {
        return internalParse(context);
    }

    @Override
    public void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result) {
        final Pair<CLAPUsageCategoryImpl, StringBuilder> pair = handleUsageCategory(categories, currentCategory, result);
        if (pair != null) {
            final CLAPUsageCategoryImpl nodeCategory = pair.getLeft();
            final StringBuilder nodeResult = pair.getRight();
            internalPrintUsage(categories, nodeCategory, nodeResult, " ");
        }
    }

    @Override
    public final void validate(CLAPParseContext context, List<String> errorMessages) {
        internalValidate(context, errorMessages);
    }

    private <V> CLAPTypedDecisionNode<V> processCLAPDecision(Class<V> propertyType, PropertyDescriptor propertyDescriptor, CLAPDecision clapDecision) {
        final CLAPTypedDecisionNode<V> decisionNode = internalAddDecision(propertyType);
        final Class<?>[] branchClasses = clapDecision.branches();
        for (Class<?> branchClassUncasted : branchClasses) {
            if (!propertyType.isAssignableFrom(branchClassUncasted)) {
                throw new IllegalArgumentException(
                        "Type of decisionBranch is not assignable to decisionProperty: " + propertyType + " vs. " + branchClassUncasted);
            }
            @SuppressWarnings("unchecked") final Class<? extends V> branchClass = (Class<? extends V>) branchClassUncasted;
            decisionNode.addClass(branchClass);
            _propertyDescriptorByOptionMap.put(decisionNode, propertyDescriptor);
        }
        return decisionNode;
    }

    private <V> CLAPClassNode<V> processCLAPDelegate(Class<V> propertyType, PropertyDescriptor propertyDescriptor, CLAPDelegate clapDelegate) {
        final CLAPClassNode<V> classNode = internalAddClass(propertyType);
        _propertyDescriptorByOptionMap.put(classNode, propertyDescriptor);
        return classNode;
    }

    private CLAPKeywordNode processCLAPKeyword(CLAPKeyword clapKeyword) {
        final CLAPKeywordNode keywordNode = internalAddKeyword(clapKeyword.value());
        _keywordNodes.add(keywordNode);
        return keywordNode;
    }

    private <V> CLAPOptionNode<V> processCLAPOption(Class<V> propertyType, PropertyDescriptor propertyDescriptor, CLAPOption clapOption) {
        int sslength = clapOption.sshortKey().length();
        final Character shortKey;
        if (clapOption.shortKey() == ' ') {
            if (sslength == 0) {
                shortKey = null;
            } else if (sslength == 1) {
                shortKey = clapOption.sshortKey().charAt(0);
            } else {
                throw new IllegalArgumentException("sshortKey.length > 1: " + propertyDescriptor);
            }
        } else {
            if (sslength == 0) {
                shortKey = clapOption.shortKey();
            } else {
                throw new IllegalArgumentException("Cannot use both shortKey and sshortKey: " + propertyDescriptor);
            }
        }
        final String longKey = clapOption.longKey().length() == 0 ? null : clapOption.longKey();
        final boolean required = clapOption.required();
        final Integer argCount;
        final int argCountA = clapOption.argCount();
        if (argCountA == CLAPOption.UNLIMITED_ARG_COUNT) {
            argCount = CLAP.UNLIMITED_ARG_COUNT;
        } else if (argCountA == CLAPOption.AUTOMATIC_ARG_COUNT) {
            argCount = null;
        } else {
            argCount = argCountA;
        }
        final Character multiArgSplit = clapOption.multiArgSplit() == ' ' ? null : clapOption.multiArgSplit();
        final String descriptionNLSKey = clapOption.descriptionNLSKey().length() == 0 ? null : clapOption.descriptionNLSKey();
        final String argUsageNLSKey = clapOption.argUsageNLSKey().length() == 0 ? null : clapOption.argUsageNLSKey();

        final CLAPOptionNode<V> optionNode = internalAddOption(propertyType, shortKey, longKey, required, argCount, multiArgSplit,
                                                               descriptionNLSKey, argUsageNLSKey
        );
        _propertyDescriptorByOptionMap.put(optionNode, propertyDescriptor);
        return optionNode;
    }

    private static class Item {

        public final int _order;
        public final Annotation _annotation;
        public final PropertyDescriptor _propertyDescriptor;
        public final CLAPHelpCategory _helpCategory;
        public final CLAPUsageCategory _usageCategory;

        public Item(int order, Annotation annotation, PropertyDescriptor propertyDescriptor, CLAPHelpCategory helpCategory, CLAPUsageCategory usageCategory) {
            super();
            _order = order;
            _annotation = annotation;
            _propertyDescriptor = propertyDescriptor;
            _helpCategory = helpCategory;
            _usageCategory = usageCategory;
        }

    }

}
