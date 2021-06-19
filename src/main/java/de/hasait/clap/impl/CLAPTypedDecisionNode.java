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
import de.hasait.clap.CLAPValue;

/**
 * Decision (XOR) node only used by annotation.
 */
public class CLAPTypedDecisionNode<T> extends AbstractCLAPDecision implements CLAPValue<T> {

    public CLAPTypedDecisionNode(CLAP clap) {
        super(clap);
    }

    public final <V extends T> CLAPClassNode<V> addClass(Class<V> clazz) {
        return internalAddClass(clazz);
    }

    @Override
    public final boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        final AbstractCLAPNode decision = context.getDecision(this);
        if (decision != null) {
            boolean anyFilled = decision.fillResult(context, result);
            if (decision instanceof CLAPValue) {
                @SuppressWarnings("unchecked") final CLAPValue<? extends T> decisionWithResult = (CLAPValue<? extends T>) decision;
                final int count = result.getCount(decisionWithResult);
                if (count > 0) {
                    result.setCount(this, count);
                    result.setValue(this, result.getValue(decisionWithResult));
                }
            } else if (anyFilled) {
                result.setCount(decision, 1);
            }
            return anyFilled;
        }
        return false;
    }

}
