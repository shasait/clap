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

package de.hasait.clap.impl.usage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hasait.clap.CLAP;

class UsageNode extends AbstractUsageLeafOrNode {

    private final String prefix;
    private final String suffix;
    private final String separator;

    private final boolean decision;

    private final String distinctionKey;

    private final List<AbstractUsageLeafOrNode> children = new ArrayList<>();

    private final Set<String> distinctionKeys = new HashSet<>();

    public UsageNode(CLAP clap, UsageNode parent, String prefix, String suffix, String separator, boolean decision, String distinctionKey) {
        super(clap, parent);

        this.prefix = prefix;
        this.suffix = suffix;
        this.separator = separator;

        this.decision = decision;

        this.distinctionKey = distinctionKey;
    }

    public void addChild(AbstractUsageLeafOrNode child) {
        children.add(child);
    }

    public boolean addDistinctionKey(String distinctionKey) {
        if (decision || distinctionKey == null) {
            return true;
        }
        if (parent != null && !parent.decision) {
            return parent.addDistinctionKey(distinctionKey);
        }
        return distinctionKeys.add(distinctionKey);
    }

    @Override
    public void beforePrint() {
        children.forEach(AbstractUsageLeafOrNode::beforePrint);
    }

    @Override
    public void print(StringBuilder stringBuilder) {
        Iterator<AbstractUsageLeafOrNode> childI = children.iterator();
        if (childI.hasNext()) {
            stringBuilder.append(prefix);
            childI.next().print(stringBuilder);
            while (childI.hasNext()) {
                stringBuilder.append(separator);
                childI.next().print(stringBuilder);
            }
            stringBuilder.append(suffix);
        }
    }

}
