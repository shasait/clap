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

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hasait.clap.CLAP;

/**
 * Base class for nodes containing multiple child nodes.
 */
public abstract class AbstractCLAPNodeList extends AbstractCLAPNode {

    @SuppressWarnings("unchecked")
    private static <T> Class<T[]> asArrayClass(Class<T> type) {
        return (Class<T[]>) Array.newInstance(type, 0).getClass();
    }

    private final List<AbstractCLAPNode> _list = new ArrayList<>();
    private final List<AbstractCLAPNode> _ulist = Collections.unmodifiableList(_list);

    protected AbstractCLAPNodeList(CLAP clap) {
        super(clap);
    }

    @Override
    public final void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> nodes, CLAPHelpCategoryImpl currentCategory) {
        final CLAPHelpCategoryImpl nodeCategory = getHelpCategory() != null ? getHelpCategory() : currentCategory;
        for (AbstractCLAPNode node : list()) {
            node.collectHelpNodes(nodes, nodeCategory);
        }
    }

    @Override
    public final String toString() {
        return MessageFormat.format("{0}[{1}]", getClass().getSimpleName(), internalToString(", "));
    }

    protected final <V> CLAPClassNode<V> internalAddClass(Class<V> clazz) {
        final CLAPClassNode<V> node = new CLAPClassNode<>(getCLAP(), clazz);
        _list.add(node);
        return node;
    }

    protected final CLAPDecisionNode internalAddDecision() {
        final CLAPDecisionNode node = new CLAPDecisionNode(getCLAP());
        _list.add(node);
        return node;
    }

    protected final <V> CLAPTypedDecisionNode<V> internalAddDecision(@SuppressWarnings("unused") final Class<V> resultClass, Class<? extends V>... branchClasses) {
        final CLAPTypedDecisionNode<V> node = new CLAPTypedDecisionNode<>(getCLAP());
        _list.add(node);
        for (Class<? extends V> branchClass : branchClasses) {
            node.addClass(branchClass);
        }
        return node;
    }

    protected final CLAPOptionNode<Boolean> internalAddFlag(Character shortKey, String longKey, boolean required, String descriptionNLSKey) {
        final CLAPOptionNode<Boolean> node = CLAPOptionNode
                .create(getCLAP(), Boolean.class, shortKey, longKey, required, 0, null, descriptionNLSKey, null);
        _list.add(node);
        return node;
    }

    protected final CLAPKeywordNode internalAddKeyword(String keyword) {
        final CLAPKeywordNode node = new CLAPKeywordNode(getCLAP(), keyword);
        _list.add(node);
        return node;
    }

    protected final CLAPNodeList internalAddNodeList() {
        final CLAPNodeList list = new CLAPNodeList(getCLAP());
        _list.add(list);
        return list;
    }

    protected final <V> CLAPOptionNode<V> internalAddOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey) {
        final CLAPOptionNode<V> node = CLAPOptionNode
                .create(getCLAP(), resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey,
                        argUsageNLSKey
                );
        _list.add(node);
        return node;
    }

    protected final <V> CLAPOptionNode<V> internalAddOption1(Class<V> resultClass, Character shortKey, String longKey, boolean required, String descriptionNLSKey, String argUsageNLSKey) {
        final CLAPOptionNode<V> node = CLAPOptionNode
                .create(getCLAP(), resultClass, shortKey, longKey, required, 1, null, descriptionNLSKey, argUsageNLSKey);
        _list.add(node);
        return node;
    }

    protected final <V> CLAPOptionNode<V[]> internalAddOptionU(Class<V> resultClass, Character shortKey, String longKey, boolean required, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey) {
        final CLAPOptionNode<V[]> node = CLAPOptionNode
                .create(getCLAP(), asArrayClass(resultClass), shortKey, longKey, required, CLAP.UNLIMITED_ARG_COUNT, multiArgSplit,
                        descriptionNLSKey, argUsageNLSKey
                );
        _list.add(node);
        return node;
    }

    protected final boolean internalFillResult(CLAPParseContext context, CLAPResultImpl result) {
        boolean anyFilled = false;
        for (AbstractCLAPNode node : list()) {
            if (node.fillResult(context, result)) {
                anyFilled = true;
            }
        }
        return anyFilled;
    }

    protected final CLAPParseContext[] internalParse(CLAPParseContext context) {
        for (AbstractCLAPNode node : list()) {
            final CLAPParseContext[] result = node.parse(context);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    protected final void internalPrintUsage(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result, String separator) {
        boolean first = true;
        for (AbstractCLAPNode node : _list) {
            if (first) {
                first = false;
            } else {
                result.append(separator);
            }
            node.printUsage(categories, currentCategory, result);
        }
    }

    protected final String internalToString(String separator) {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (AbstractCLAPNode node : _list) {
            if (first) {
                first = false;
            } else {
                result.append(separator);
            }
            result.append(node);
        }
        return result.toString();
    }

    protected final void internalValidate(CLAPParseContext context, List<String> errorMessages) {
        for (AbstractCLAPNode node : list()) {
            node.validate(context, errorMessages);
        }
    }

    protected final List<AbstractCLAPNode> list() {
        return _ulist;
    }

}
