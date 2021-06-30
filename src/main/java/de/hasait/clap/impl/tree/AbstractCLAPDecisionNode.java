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
import de.hasait.clap.impl.parser.CLAPParseContext;

/**
 * Base class for all decision (XOR) nodes.
 */
public abstract class AbstractCLAPDecisionNode extends AbstractCLAPNode {

    protected AbstractCLAPDecisionNode(CLAP clap) {
        super(clap);
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
        if (unmodifiableChildren.isEmpty()) {
            return null;
        }

        AbstractCLAPLeafOrNode branchLeafOrNode = context.getDecisionBranch(this);
        if (branchLeafOrNode != null) {
            return branchLeafOrNode.parse(context);
        }

        CLAPParseContext[] nextContexts = new CLAPParseContext[unmodifiableChildren.size()];
        for (int i = 0; i < unmodifiableChildren.size(); i++) {
            nextContexts[i] = context.clone();
            nextContexts[i].addDecisionBranch(this, unmodifiableChildren.get(i));
        }
        return nextContexts;
    }

    @Override
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        AbstractCLAPLeafOrNode branchLeafOrNode = context.getDecisionBranch(this);
        if (branchLeafOrNode != null) {
            branchLeafOrNode.validate(context, errorMessages);
        } else {
            super.validate(context, errorMessages);
        }
    }

    protected String getUsageNodePrefix() {
        boolean multipleChildren = unmodifiableChildren.size() > 1;
        return multipleChildren ? "{ " : "";
    }

    protected String getUsageNodeSuffix() {
        boolean multipleChildren = unmodifiableChildren.size() > 1;
        return multipleChildren ? " }" : "";
    }

    protected String getUsageNodeSeparator() {
        return " | ";
    }

    protected boolean isDecision() {
        return true;
    }

}
