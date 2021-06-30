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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.help.CLAPHelpPrinter;
import de.hasait.clap.impl.parser.CLAPParseContext;
import de.hasait.clap.impl.parser.CLAPResultImpl;
import de.hasait.clap.impl.usage.CLAPUsagePrinter;

/**
 * Node of the option tree.
 */
public abstract class AbstractCLAPNode extends AbstractCLAPLeafOrNode {

    private final List<AbstractCLAPLeafOrNode> children = new ArrayList<>();
    protected final List<AbstractCLAPLeafOrNode> unmodifiableChildren = Collections.unmodifiableList(children);

    private String usageCategoryTitle;
    private int usageCategoryOrder;

    protected AbstractCLAPNode(CLAP clap) {
        super(clap);
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
        for (AbstractCLAPLeafOrNode leafOrNode : unmodifiableChildren) {
            final CLAPParseContext[] result = leafOrNode.parse(context);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        for (AbstractCLAPLeafOrNode leafOrNode : unmodifiableChildren) {
            leafOrNode.validate(context, errorMessages);
        }
    }

    @Override
    public boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        boolean anyFilled = false;
        for (AbstractCLAPLeafOrNode leafOrNode : unmodifiableChildren) {
            if (leafOrNode.fillResult(context, result)) {
                anyFilled = true;
            }
        }
        return anyFilled;
    }

    @Override
    public void collectHelp(CLAPHelpPrinter helpPrinter) {
        helpPrinter.pushCurrentCategory(getHelpCategoryTitle(), getHelpCategoryOrder());
        for (AbstractCLAPLeafOrNode leafOrNode : unmodifiableChildren) {
            leafOrNode.collectHelp(helpPrinter);
        }
        helpPrinter.popCurrentCategory();
    }

    @Override
    public final void collectUsage(CLAPUsagePrinter usagePrinter) {
        boolean collectChildren = usagePrinter.pushNode(internalGetUsageCategoryTitle(), internalGetUsageCategoryOrder(), //
                                                        getUsageNodePrefix(), getUsageNodeSuffix(), getUsageNodeSeparator(), //
                                                        isDecision(), getDistinctionKey()
        );
        if (collectChildren) {
            for (AbstractCLAPLeafOrNode leafOrNode : unmodifiableChildren) {
                leafOrNode.collectUsage(usagePrinter);
            }
            usagePrinter.popNode();
        }
    }

    protected String getUsageNodePrefix() {
        return "";
    }

    protected String getUsageNodeSuffix() {
        return "";
    }

    protected String getUsageNodeSeparator() {
        return " ";
    }

    protected boolean isDecision() {
        return false;
    }

    protected String getDistinctionKey() {
        return null;
    }

    protected final String internalGetUsageCategoryTitle() {
        return usageCategoryTitle;
    }

    protected final void internalSetUsageCategoryTitle(String usageCategoryTitle) {
        this.usageCategoryTitle = usageCategoryTitle;
    }

    protected final int internalGetUsageCategoryOrder() {
        return usageCategoryOrder;
    }

    protected final void internalSetUsageCategoryOrder(int usageCategoryOrder) {
        this.usageCategoryOrder = usageCategoryOrder;
    }

    protected final <V> CLAPClassNode<V> internalAddClass(Class<V> clazz) {
        final CLAPClassNode<V> node = new CLAPClassNode<>(clap, clazz);
        children.add(node);
        return node;
    }

    protected final CLAPDecisionNode internalAddDecision() {
        final CLAPDecisionNode node = new CLAPDecisionNode(clap);
        children.add(node);
        return node;
    }

    protected final <V> CLAPTypedDecisionNode<V> internalAddDecision(@SuppressWarnings("unused") final Class<V> resultClass, Class<? extends V>... branchClasses) {
        final CLAPTypedDecisionNode<V> node = new CLAPTypedDecisionNode<>(clap);
        children.add(node);
        for (Class<? extends V> branchClass : branchClasses) {
            node.addClass(branchClass);
        }
        return node;
    }

    protected final CLAPKeywordLeaf internalAddKeyword(String keyword) {
        final CLAPKeywordLeaf node = new CLAPKeywordLeaf(clap, keyword);
        children.add(node);
        return node;
    }

    protected final CLAPGroupNode internalAddNodeList() {
        final CLAPGroupNode node = new CLAPGroupNode(clap);
        children.add(node);
        return node;
    }

    protected final <V> CLAPOptionLeaf<V> internalAddOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn, boolean password) {
        final CLAPOptionLeaf<V> node = new CLAPOptionLeaf<>(clap, resultClass, shortKey, longKey, required, argCount, multiArgSplit,
                                                            descriptionNLSKey, argUsageNLSKey, immediateReturn, password
        );
        children.add(node);
        return node;
    }

}
