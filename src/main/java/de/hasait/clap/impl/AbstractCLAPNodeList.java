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

    protected AbstractCLAPNodeList(CLAP pCLAP) {
        super(pCLAP);
    }

    @Override
    public final void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pNodes, CLAPHelpCategoryImpl pCurrentCategory) {
        final CLAPHelpCategoryImpl currentCategory = getHelpCategory() != null ? getHelpCategory() : pCurrentCategory;
        for (AbstractCLAPNode node : list()) {
            node.collectHelpNodes(pNodes, currentCategory);
        }
    }

    @Override
    public final String toString() {
        return MessageFormat.format("{0}[{1}]", getClass().getSimpleName(), internalToString(", "));
    }

    protected final <V> CLAPClassNode<V> internalAddClass(Class<V> pClass) {
        final CLAPClassNode<V> node = new CLAPClassNode<>(getCLAP(), pClass);
        _list.add(node);
        return node;
    }

    protected final CLAPDecisionNode internalAddDecision() {
        final CLAPDecisionNode node = new CLAPDecisionNode(getCLAP());
        _list.add(node);
        return node;
    }

    protected final <V> CLAPTypedDecisionNode<V> internalAddDecision(@SuppressWarnings("unused") final Class<V> pResultClass, Class<? extends V>... pBranchClasses) {
        final CLAPTypedDecisionNode<V> node = new CLAPTypedDecisionNode<>(getCLAP());
        _list.add(node);
        for (Class<? extends V> branchClass : pBranchClasses) {
            node.addClass(branchClass);
        }
        return node;
    }

    protected final CLAPOptionNode<Boolean> internalAddFlag(Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey) {
        final CLAPOptionNode<Boolean> node = CLAPOptionNode
                .create(getCLAP(), Boolean.class, pShortKey, pLongKey, pRequired, 0, null, pDescriptionNLSKey, null);
        _list.add(node);
        return node;
    }

    protected final CLAPKeywordNode internalAddKeyword(String pKeyword) {
        final CLAPKeywordNode node = new CLAPKeywordNode(getCLAP(), pKeyword);
        _list.add(node);
        return node;
    }

    protected final CLAPNodeList internalAddNodeList() {
        final CLAPNodeList list = new CLAPNodeList(getCLAP());
        _list.add(list);
        return list;
    }

    protected final <V> CLAPOptionNode<V> internalAddOption(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Integer pArgCount, Character pMultiArgSplit, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        final CLAPOptionNode<V> node = CLAPOptionNode
                .create(getCLAP(), pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey,
                        pArgUsageNLSKey
                );
        _list.add(node);
        return node;
    }

    protected final <V> CLAPOptionNode<V> internalAddOption1(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        final CLAPOptionNode<V> node = CLAPOptionNode
                .create(getCLAP(), pResultClass, pShortKey, pLongKey, pRequired, 1, null, pDescriptionNLSKey, pArgUsageNLSKey);
        _list.add(node);
        return node;
    }

    protected final <V> CLAPOptionNode<V[]> internalAddOptionU(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Character pMultiArgSplit, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        final CLAPOptionNode<V[]> node = CLAPOptionNode
                .create(getCLAP(), asArrayClass(pResultClass), pShortKey, pLongKey, pRequired, CLAP.UNLIMITED_ARG_COUNT, pMultiArgSplit,
                        pDescriptionNLSKey, pArgUsageNLSKey
                );
        _list.add(node);
        return node;
    }

    protected final boolean internalFillResult(CLAPParseContext pContext, CLAPResultImpl pResult) {
        boolean result = false;
        for (AbstractCLAPNode node : list()) {
            if (node.fillResult(pContext, pResult)) {
                result = true;
            }
        }
        return result;
    }

    protected final CLAPParseContext[] internalParse(CLAPParseContext pContext) {
        for (AbstractCLAPNode node : list()) {
            final CLAPParseContext[] result = node.parse(pContext);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    protected final void internalPrintUsage(Map<CLAPUsageCategoryImpl, StringBuilder> pCategories, CLAPUsageCategoryImpl pCurrentCategory, StringBuilder pResult, String pSeparator) {
        boolean first = true;
        for (AbstractCLAPNode node : _list) {
            if (first) {
                first = false;
            } else {
                pResult.append(pSeparator);
            }
            node.printUsage(pCategories, pCurrentCategory, pResult);
        }
    }

    protected final String internalToString(String pSeparator) {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (AbstractCLAPNode node : _list) {
            if (first) {
                first = false;
            } else {
                result.append(pSeparator);
            }
            result.append(node);
        }
        return result.toString();
    }

    protected final void internalValidate(CLAPParseContext pContext, List<String> pErrorMessages) {
        for (AbstractCLAPNode node : list()) {
            node.validate(pContext, pErrorMessages);
        }
    }

    protected final List<AbstractCLAPNode> list() {
        return _ulist;
    }

}
