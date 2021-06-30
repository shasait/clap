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

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPNode;
import de.hasait.clap.CLAPValue;

/**
 * A list of nodes.
 */
public class CLAPGroupNode extends AbstractCLAPNode implements CLAPNode {

    public CLAPGroupNode(CLAP clap) {
        super(clap);
    }

    @Override
    public final void setUsageCategoryTitle(String usageCategoryTitle) {
        internalSetUsageCategoryTitle(usageCategoryTitle);
    }

    @Override
    public final void setUsageCategoryOrder(int usageCategoryOrder) {
        internalSetUsageCategoryOrder(usageCategoryOrder);
    }

    @Override
    public final <V> CLAPValue<V> addClass(Class<V> clazz) {
        return internalAddClass(clazz);
    }

    @Override
    public final CLAPNode addDecision() {
        return internalAddDecision();
    }

    @Override
    public final <V> CLAPValue<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses) {
        return internalAddDecision(resultClass, branchClasses);
    }

    @Override
    public final void addKeyword(String keyword) {
        internalAddKeyword(keyword);
    }

    @Override
    public final CLAPNode addGroup() {
        return internalAddNodeList();
    }

    @Override
    public final <V> CLAPValue<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn, boolean password) {
        return internalAddOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey,
                                 immediateReturn, password
        );
    }

}
