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

/**
 * A list of nodes.
 */
public class CLAPNodeList extends AbstractCLAPNodeList implements CLAPNode {

    public CLAPNodeList(CLAP clap) {
        super(clap);
    }

    @Override
    public <V> CLAPClassNode<V> addClass(Class<V> clazz) {
        return internalAddClass(clazz);
    }

    @Override
    public CLAPDecisionNode addDecision() {
        return internalAddDecision();
    }

    @Override
    public <V> CLAPTypedDecisionNode<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses) {
        return internalAddDecision(resultClass, branchClasses);
    }

    @Override
    public void addKeyword(String keyword) {
        internalAddKeyword(keyword);
    }

    @Override
    public CLAPNodeList addNodeList() {
        return internalAddNodeList();
    }

    @Override
    public <V> CLAPOptionNode<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn) {
        return internalAddOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey,
                                 immediateReturn
        );
    }

    @Override
    public boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        return internalFillResult(context, result);
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
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
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        internalValidate(context, errorMessages);
    }

}
