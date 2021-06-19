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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPNode;
import de.hasait.clap.CLAPValue;

/**
 * A list of nodes.
 */
public class CLAPNodeList extends AbstractCLAPNodeList implements CLAPNode {

    public CLAPNodeList(CLAP pCLAP) {
        super(pCLAP);
    }

    @Override
    public <V> CLAPClassNode<V> addClass(Class<V> pClass) {
        return internalAddClass(pClass);
    }

    @Override
    public CLAPDecisionNode addDecision() {
        return internalAddDecision();
    }

    @Override
    public <V> CLAPTypedDecisionNode<V> addDecision(Class<V> pResultClass, Class<? extends V>... pBranchClasses) {
        return internalAddDecision(pResultClass, pBranchClasses);
    }

    @Override
    public CLAPOptionNode<Boolean> addFlag(Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey) {
        return internalAddFlag(pShortKey, pLongKey, pRequired, pDescriptionNLSKey);
    }

    @Override
    public void addKeyword(String pKeyword) {
        internalAddKeyword(pKeyword);
    }

    @Override
    public CLAPNodeList addNodeList() {
        return internalAddNodeList();
    }

    @Override
    public <V> CLAPOptionNode<V> addOption(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Integer pArgCount, Character pMultiArgSplit, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        return internalAddOption(pResultClass, pShortKey, pLongKey, pRequired, pArgCount, pMultiArgSplit, pDescriptionNLSKey,
                                 pArgUsageNLSKey
        );
    }

    @Override
    public <V> CLAPOptionNode<V> addOption1(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        return internalAddOption1(pResultClass, pShortKey, pLongKey, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
    }

    @Override
    public <V> CLAPOptionNode<V[]> addOptionU(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Character pMultiArgSplit, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        return internalAddOptionU(pResultClass, pShortKey, pLongKey, pRequired, pMultiArgSplit, pDescriptionNLSKey, pArgUsageNLSKey);
    }

    @Override
    public <V> CLAPValue<V> addNameless1(Class<V> pResultClass, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        return internalAddOption1(pResultClass, null, null, pRequired, pDescriptionNLSKey, pArgUsageNLSKey);
    }

    @Override
    public <V> CLAPValue<V[]> addNamelessU(Class<V> pResultClass, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey) {
        return internalAddOptionU(pResultClass, null, null, pRequired, null, pDescriptionNLSKey, pArgUsageNLSKey);
    }

    @Override
    public boolean fillResult(CLAPParseContext pContext, CLAPResultImpl pResult) {
        return internalFillResult(pContext, pResult);
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext pContext) {
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
    public void validate(CLAPParseContext pContext, List<String> pErrorMessages) {
        internalValidate(pContext, pErrorMessages);
    }

}
