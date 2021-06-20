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

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPNode;
import de.hasait.clap.CLAPValue;

/**
 * Decision node (XOR).
 */
public class CLAPDecisionNode extends AbstractCLAPDecision implements CLAPNode {

    public CLAPDecisionNode(CLAP clap) {
        super(clap);
    }

    @Override
    public final <V> CLAPClassNode<V> addClass(Class<V> clazz) {
        return internalAddClass(clazz);
    }

    @Override
    public final CLAPDecisionNode addDecision() {
        return internalAddDecision();
    }

    @Override
    public final <V> CLAPTypedDecisionNode<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses) {
        return internalAddDecision(resultClass, branchClasses);
    }

    @Override
    public void addKeyword(String keyword) {
        internalAddKeyword(keyword);
    }

    @Override
    public final CLAPNodeList addNodeList() {
        return internalAddNodeList();
    }

    @Override
    public final <V> CLAPOptionNode<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn) {
        return internalAddOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey,
                                 immediateReturn);
    }

    @Override
    public final boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        final AbstractCLAPNode decision = context.getDecision(this);
        if (decision != null) {
            if (decision.fillResult(context, result)) {
                result.setCount(decision, 1);
                return true;
            }
        }
        return false;
    }

}
