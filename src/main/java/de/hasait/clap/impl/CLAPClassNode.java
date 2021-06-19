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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

    private static <A extends Annotation> A findAnnotation(Class<?> pClass, Class<A> pAnnotationClass) {
        final LinkedList<Class<?>> queue = new LinkedList<>();
        queue.add(pClass);
        while (!queue.isEmpty()) {
            final Class<?> clazz = queue.removeFirst();
            if (clazz != null) {
                final A result = clazz.getAnnotation(pAnnotationClass);
                if (result != null) {
                    return result;
                }
                queue.add(clazz.getSuperclass());
                queue.addAll(Arrays.asList(clazz.getInterfaces()));
            }
        }
        return null;
    }

    private static <T extends Annotation> T getAnnotation(PropertyDescriptor pPropertyDescriptor, Class<T> pAnnotationClass) {
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
            return annotation;
        }

        return null;
    }

    private final Class<T> _class;

    private final Map<CLAPValue<?>, PropertyDescriptor> _propertyDescriptorByOptionMap;

    private final Set<CLAPKeywordNode> _keywordNodes;

    public CLAPClassNode(CLAP pCLAP, Class<T> pClass) {
        super(pCLAP);

        _class = pClass;

        _propertyDescriptorByOptionMap = new HashMap<>();
        _keywordNodes = new HashSet<>();

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(pClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        final List<Item> annotations = new ArrayList<>();

        final CLAPKeywords classKeywords = findAnnotation(pClass, CLAPKeywords.class);
        if (classKeywords != null) {
            for (CLAPKeyword classKeyword : classKeywords.value()) {
                annotations.add(new Item(classKeyword.order(), classKeyword, null, null, null));
            }
        }

        final CLAPHelpCategory classHelpCategory = findAnnotation(pClass, CLAPHelpCategory.class);
        if (classHelpCategory != null) {
            setHelpCategory(classHelpCategory.order(), classHelpCategory.titleNLSKey());
        }

        final CLAPUsageCategory classUsageCategory = findAnnotation(pClass, CLAPUsageCategory.class);
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
                throw new IllegalArgumentException();
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

        annotations.sort(Comparator.comparing(pPO -> pPO._order));

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
    public final boolean fillResult(CLAPParseContext pContext, CLAPResultImpl pResult) {
        internalFillResult(pContext, pResult);

        T value;
        try {
            value = _class.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean anySet = false;
        for (Entry<CLAPValue<?>, PropertyDescriptor> entry : _propertyDescriptorByOptionMap.entrySet()) {
            final CLAPValue<?> node = entry.getKey();
            if (pResult.getCount(node) > 0) {
                anySet = true;
                final PropertyDescriptor propertyDescriptor = entry.getValue();
                final Object nodeValue = pResult.getValue(node);
                try {
                    propertyDescriptor.getWriteMethod().invoke(value, nodeValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (CLAPKeywordNode node : _keywordNodes) {
            if (pContext.getNodeCount(node) > 0) {
                anySet = true;
            }
        }
        if (anySet) {
            pResult.setCount(this, 1);
            pResult.setValue(this, value);
        }

        return anySet;
    }

    @Override
    public final CLAPParseContext[] parse(CLAPParseContext pContext) {
        return internalParse(pContext);
    }

    @Override
    public void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> pCategories, CLAPUsageCategoryImpl pCurrentCategory, StringBuilder pResult) {
        final Pair<CLAPUsageCategoryImpl, StringBuilder> pair = handleUsageCategory(pCategories, pCurrentCategory, pResult);
        if (pair != null) {
            final CLAPUsageCategoryImpl currentCategory = pair.getLeft();
            final StringBuilder result = pair.getRight();
            internalPrintUsage(pCategories, currentCategory, result, " ");
        }
    }

    @Override
    public final void validate(CLAPParseContext pContext, List<String> pErrorMessages) {
        internalValidate(pContext, pErrorMessages);
    }

    private <V> CLAPTypedDecisionNode<V> processCLAPDecision(Class<V> pPropertyType, PropertyDescriptor pPropertyDescriptor, CLAPDecision pClapDecision) {
        final CLAPTypedDecisionNode<V> decisionNode = internalAddDecision(pPropertyType);
        final Class<?>[] branchClasses = pClapDecision.branches();
        for (Class<?> branchClassUncasted : branchClasses) {
            if (!pPropertyType.isAssignableFrom(branchClassUncasted)) {
                throw new IllegalArgumentException(
                        "Type of decisionBranch is not assignable to decisionProperty: " + pPropertyType + " vs. " + branchClassUncasted);
            }
            @SuppressWarnings("unchecked") final Class<? extends V> branchClass = (Class<? extends V>) branchClassUncasted;
            decisionNode.addClass(branchClass);
            _propertyDescriptorByOptionMap.put(decisionNode, pPropertyDescriptor);
        }
        return decisionNode;
    }

    private <V> CLAPClassNode<V> processCLAPDelegate(Class<V> pPropertyType, PropertyDescriptor pPropertyDescriptor, CLAPDelegate pClapDelegate) {
        final CLAPClassNode<V> classNode = internalAddClass(pPropertyType);
        _propertyDescriptorByOptionMap.put(classNode, pPropertyDescriptor);
        return classNode;
    }

    private CLAPKeywordNode processCLAPKeyword(CLAPKeyword pClapKeyword) {
        final CLAPKeywordNode keywordNode = internalAddKeyword(pClapKeyword.value());
        _keywordNodes.add(keywordNode);
        return keywordNode;
    }

    private <V> CLAPOptionNode<V> processCLAPOption(Class<V> pPropertyType, PropertyDescriptor pPropertyDescriptor, CLAPOption pClapOption) {
        int sslength = pClapOption.sshortKey().length();
        final Character shortKey;
        if (pClapOption.shortKey() == ' ') {
            if (sslength == 0) {
                shortKey = null;
            } else if (sslength == 1) {
                shortKey = pClapOption.sshortKey().charAt(0);
            } else {
                throw new IllegalArgumentException("sshortKey.length > 1: " + pPropertyDescriptor);
            }
        } else {
            if (sslength == 0) {
                shortKey = pClapOption.shortKey();
            } else {
                throw new IllegalArgumentException("Cannot use both shortKey and sshortKey: " + pPropertyDescriptor);
            }
        }
        final String longKey = pClapOption.longKey().length() == 0 ? null : pClapOption.longKey();
        final boolean required = pClapOption.required();
        final Integer argCount;
        final int argCountA = pClapOption.argCount();
        if (argCountA == CLAPOption.UNLIMITED_ARG_COUNT) {
            argCount = CLAP.UNLIMITED_ARG_COUNT;
        } else if (argCountA == CLAPOption.AUTOMATIC_ARG_COUNT) {
            argCount = null;
        } else {
            argCount = argCountA;
        }
        final Character multiArgSplit = pClapOption.multiArgSplit() == ' ' ? null : pClapOption.multiArgSplit();
        final String descriptionNLSKey = pClapOption.descriptionNLSKey().length() == 0 ? null : pClapOption.descriptionNLSKey();
        final String argUsageNLSKey = pClapOption.argUsageNLSKey().length() == 0 ? null : pClapOption.argUsageNLSKey();

        final CLAPOptionNode<V> optionNode = internalAddOption(pPropertyType, shortKey, longKey, required, argCount, multiArgSplit,
                                                               descriptionNLSKey, argUsageNLSKey
        );
        _propertyDescriptorByOptionMap.put(optionNode, pPropertyDescriptor);
        return optionNode;
    }

    private static class Item {

        public final int _order;
        public final Annotation _annotation;
        public final PropertyDescriptor _propertyDescriptor;
        public final CLAPHelpCategory _helpCategory;
        public final CLAPUsageCategory _usageCategory;

        public Item(int pOrder, Annotation pAnnotation, PropertyDescriptor pPropertyDescriptor, CLAPHelpCategory pHelpCategory, CLAPUsageCategory pUsageCategory) {
            super();
            _order = pOrder;
            _annotation = pAnnotation;
            _propertyDescriptor = pPropertyDescriptor;
            _helpCategory = pHelpCategory;
            _usageCategory = pUsageCategory;
        }

    }

}
