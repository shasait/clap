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

import java.util.List;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPLeafOrNode;
import de.hasait.clap.impl.AbstractCLAPRelated;
import de.hasait.clap.impl.help.CLAPHelpPrinter;
import de.hasait.clap.impl.usage.CLAPUsagePrinter;
import de.hasait.clap.impl.parser.CLAPParseContext;
import de.hasait.clap.impl.parser.CLAPResultImpl;

/**
 * Leaf or Node of the option tree.
 */
public abstract class AbstractCLAPLeafOrNode extends AbstractCLAPRelated implements CLAPLeafOrNode {

    private String helpCategoryTitle;
    private int helpCategoryOrder;
    private int helpEntryOrder;

    protected AbstractCLAPLeafOrNode(CLAP clap) {
        super(clap);
    }

    public final String getHelpCategoryTitle() {
        return helpCategoryTitle;
    }

    @Override
    public final void setHelpCategoryTitle(String helpCategoryTitle) {
        this.helpCategoryTitle = helpCategoryTitle;
    }

    public final int getHelpCategoryOrder() {
        return helpCategoryOrder;
    }

    @Override
    public final void setHelpCategoryOrder(int helpCategoryOrder) {
        this.helpCategoryOrder = helpCategoryOrder;
    }

    public final int getHelpEntryOrder() {
        return helpEntryOrder;
    }

    @Override
    public final void setHelpEntryOrder(int helpEntryOrder) {
        this.helpEntryOrder = helpEntryOrder;
    }

    public abstract CLAPParseContext[] parse(CLAPParseContext context);

    public abstract void validate(CLAPParseContext context, List<String> errorMessages);

    public abstract boolean fillResult(CLAPParseContext context, CLAPResultImpl result);

    public abstract void collectHelp(CLAPHelpPrinter helpPrinter);

    public abstract void collectUsage(CLAPUsagePrinter usagePrinter);

}
