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

import org.apache.commons.lang3.StringUtils;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPDecision;
import de.hasait.clap.CLAPDelegate;
import de.hasait.clap.CLAPHelpCategory;
import de.hasait.clap.CLAPKeyword;
import de.hasait.clap.CLAPKeywords;
import de.hasait.clap.CLAPOption;
import de.hasait.clap.CLAPUsageCategory;
import de.hasait.clap.CLAPValue;
import de.hasait.clap.impl.parser.CLAPParseContext;
import de.hasait.clap.impl.parser.CLAPResultImpl;

/**
 * CLAPNode for handling annotated classes.
 */
public class CLAPClassNode<T> extends AbstractCLAPNode implements CLAPValue<T> {

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

    private static Character useCharOrString(char charValue, String stringValue, String name, PropertyDescriptor propertyDescriptor) {
        int stringLength = stringValue.length();
        Character result;
        if (charValue == ' ') {
            if (stringLength == 0) {
                result = null;
            } else if (stringLength == 1) {
                result = stringValue.charAt(0);
            } else {
                throw new IllegalArgumentException("s" + name + ".length > 1: " + propertyDescriptor);
            }
        } else {
            if (stringLength == 0) {
                result = charValue;
            } else {
                throw new IllegalArgumentException("Cannot use both " + name + " and s" + name + ": " + propertyDescriptor);
            }
        }
        return result;
    }

    private final Class<T> _class;

    private final Map<CLAPValue<?>, PropertyDescriptor> _propertyDescriptorByOptionMap;

    private final Set<CLAPKeywordLeaf> _keywordNodes;

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
                annotations.add(new Item(classKeyword.order(), classKeyword, null, null));
            }
        }

        final CLAPKeyword classKeyword = findAnnotation(clazz, CLAPKeyword.class);
        if (classKeyword != null) {
            annotations.add(new Item(classKeyword.order(), classKeyword, null, null));
        }

        processCLAPHelpCategory(this, findAnnotation(clazz, CLAPHelpCategory.class));
        processCLAPUsageCategory(this, findAnnotation(clazz, CLAPUsageCategory.class));

        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            final CLAPOption pdOption = getAnnotation(propertyDescriptor, CLAPOption.class);
            final CLAPDelegate pdDelegate = getAnnotation(propertyDescriptor, CLAPDelegate.class);
            final CLAPDecision pdDecision = getAnnotation(propertyDescriptor, CLAPDecision.class);
            final CLAPHelpCategory pdHelpCategory = getAnnotation(propertyDescriptor, CLAPHelpCategory.class);
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
                annotations.add(new Item(pdOption.order(), pdOption, propertyDescriptor, pdHelpCategory));
            }
            if (pdDelegate != null) {
                annotations.add(new Item(pdDelegate.order(), pdDelegate, propertyDescriptor, pdHelpCategory));
            }
            if (pdDecision != null) {
                annotations.add(new Item(pdDecision.order(), pdDecision, propertyDescriptor, pdHelpCategory));
            }
        }

        annotations.sort(Comparator.comparing(a -> a.order));

        for (Item entry : annotations) {
            final Object annotation = entry.annotation;

            final AbstractCLAPLeafOrNode node;
            if (annotation instanceof CLAPKeyword) {
                node = processCLAPKeyword((CLAPKeyword) annotation);
            } else if (annotation instanceof CLAPOption) {
                node = processCLAPOption(entry.propertyDescriptor.getPropertyType(), entry.propertyDescriptor, (CLAPOption) annotation);
            } else if (annotation instanceof CLAPDelegate) {
                node = processCLAPDelegate(entry.propertyDescriptor.getPropertyType(), entry.propertyDescriptor, (CLAPDelegate) annotation);
            } else if (annotation instanceof CLAPDecision) {
                node = processCLAPDecision(entry.propertyDescriptor.getPropertyType(), entry.propertyDescriptor, (CLAPDecision) annotation);
            } else {
                throw new RuntimeException();
            }

            processCLAPHelpCategory(node, entry.helpCategory);
        }

    }

    @Override
    public final boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        super.fillResult(context, result);

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
        for (CLAPKeywordLeaf node : _keywordNodes) {
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
    protected String getDistinctionKey() {
        return _class.getName();
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

    private CLAPKeywordLeaf processCLAPKeyword(CLAPKeyword clapKeyword) {
        final CLAPKeywordLeaf keywordNode = internalAddKeyword(clapKeyword.value());
        _keywordNodes.add(keywordNode);
        return keywordNode;
    }

    private <V> CLAPOptionLeaf<V> processCLAPOption(Class<V> propertyType, PropertyDescriptor propertyDescriptor, CLAPOption clapOption) {
        Character shortKey = useCharOrString(clapOption.shortKey(), clapOption.sshortKey(), "shortKey", propertyDescriptor);
        String longKey = clapOption.longKey().length() == 0 ? null : clapOption.longKey();
        boolean required = clapOption.required();
        Integer argCount;
        int argCountA = clapOption.argCount();
        if (argCountA == CLAPOption.UNLIMITED_ARG_COUNT) {
            argCount = CLAP.UNLIMITED_ARG_COUNT;
        } else if (argCountA == CLAPOption.AUTOMATIC_ARG_COUNT) {
            argCount = null;
        } else {
            argCount = argCountA;
        }
        Character multiArgSplit = useCharOrString(clapOption.multiArgSplit(), clapOption.smultiArgSplit(), "multiArgSplit",
                                                  propertyDescriptor
        );
        String descriptionNLSKey = clapOption.descriptionNLSKey().length() == 0 ? null : clapOption.descriptionNLSKey();
        String argUsageNLSKey = clapOption.argUsageNLSKey().length() == 0 ? null : clapOption.argUsageNLSKey();
        boolean password = clapOption.password();

        CLAPOptionLeaf<V> optionNode = internalAddOption(propertyType, shortKey, longKey, required, argCount, multiArgSplit,
                                                         descriptionNLSKey, argUsageNLSKey, false, password
        );
        _propertyDescriptorByOptionMap.put(optionNode, propertyDescriptor);
        return optionNode;
    }

    private void processCLAPHelpCategory(AbstractCLAPLeafOrNode node, CLAPHelpCategory clapHelpCategory) {
        if (clapHelpCategory != null) {
            String helpCategory = clapHelpCategory.value();
            node.setHelpCategoryTitle(StringUtils.isEmpty(helpCategory) ? null : helpCategory);
            node.setHelpCategoryOrder(clapHelpCategory.categoryOrder());
            node.setHelpEntryOrder(clapHelpCategory.entryOrder());
        }
    }

    private void processCLAPUsageCategory(AbstractCLAPNode node, CLAPUsageCategory clapUsageCategory) {
        if (clapUsageCategory != null) {
            String usageCategory = clapUsageCategory.value();
            node.internalSetUsageCategoryTitle(StringUtils.isEmpty(usageCategory) ? null : usageCategory);
            node.internalSetUsageCategoryOrder(clapUsageCategory.categoryOrder());
        }
    }

    private static class Item {

        public final int order;
        public final Annotation annotation;
        public final PropertyDescriptor propertyDescriptor;
        public final CLAPHelpCategory helpCategory;

        public Item(int order, Annotation annotation, PropertyDescriptor propertyDescriptor, CLAPHelpCategory helpCategory) {
            super();
            this.order = order;
            this.annotation = annotation;
            this.propertyDescriptor = propertyDescriptor;
            this.helpCategory = helpCategory;
        }

    }

}
