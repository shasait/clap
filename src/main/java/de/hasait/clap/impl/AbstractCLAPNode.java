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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

import de.hasait.clap.CLAP;

/**
 * Base class of all nodes forming the option tree.
 */
public abstract class AbstractCLAPNode {

    private final CLAP _clap;

    private CLAPUsageCategoryImpl _usageCategory;

    private CLAPHelpCategoryImpl _helpCategory;

    protected AbstractCLAPNode(CLAP clap) {
        super();

        _clap = clap;
    }

    protected static void addHelpNode(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> optionNodes, CLAPHelpCategoryImpl currentCategory, CLAPHelpNode node) {
        final CLAPHelpCategoryImpl nodeCategory = node.getHelpCategory() != null ? node.getHelpCategory() : currentCategory;
        if (!optionNodes.containsKey(nodeCategory)) {
            optionNodes.put(nodeCategory, new TreeSet<>(Comparator.comparing(CLAPHelpNode::getHelpID)));
        }
        optionNodes.get(nodeCategory).add(node);
    }

    public abstract void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> optionNodes, CLAPHelpCategoryImpl currentCategory);

    public abstract boolean fillResult(CLAPParseContext context, CLAPResultImpl result);

    public CLAP getCLAP() {
        return _clap;
    }

    public final CLAPHelpCategoryImpl getHelpCategory() {
        return _helpCategory;
    }

    public CLAPUsageCategoryImpl getUsageCategory() {
        return _usageCategory;
    }

    public final String nls(String key, Object... arguments) {
        return _clap.nls(key, arguments);
    }

    public abstract CLAPParseContext[] parse(CLAPParseContext context);

    public abstract void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result);

    public final void setHelpCategory(int order, String titleNLSKey) {
        _helpCategory = new CLAPHelpCategoryImpl(order, titleNLSKey);
    }

    public final void setUsageCategory(int order, String titleNLSKey) {
        _usageCategory = new CLAPUsageCategoryImpl(order, titleNLSKey);
    }

    public abstract void validate(CLAPParseContext context, List<String> errorMessages);

    protected final Pair<CLAPUsageCategoryImpl, StringBuilder> handleUsageCategory(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result) {
        final CLAPUsageCategoryImpl nodeCategory = getUsageCategory() != null ? getUsageCategory() : currentCategory;
        StringBuilder nodeResult;
        if (!nodeCategory.equals(currentCategory)) {
            if (result != null) {
                result.append(nls(nodeCategory.getTitleNLSKey()));
            }
            if (categories.containsKey(nodeCategory)) {
                return null;
            } else {
                nodeResult = new StringBuilder();
                categories.put(nodeCategory, nodeResult);
            }
        } else {
            nodeResult = result;
        }
        return Pair.of(nodeCategory, nodeResult);
    }

}
