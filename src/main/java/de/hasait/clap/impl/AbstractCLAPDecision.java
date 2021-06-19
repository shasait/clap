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

/**
 * Base class for all decision (XOR) nodes.
 */
public abstract class AbstractCLAPDecision extends AbstractCLAPNodeList {

    protected AbstractCLAPDecision(CLAP clap) {
        super(clap);
    }

    @Override
    public final CLAPParseContext[] parse(CLAPParseContext context) {
        if (list().isEmpty()) {
            return null;
        }

        final AbstractCLAPNode decision = context.getDecision(this);
        if (decision == null) {
            final CLAPParseContext[] result = new CLAPParseContext[list().size()];
            for (int i = 0; i < list().size(); i++) {
                result[i] = context.clone();
                result[i].addDecision(this, list().get(i));
            }
            return result;
        } else {
            return decision.parse(context);
        }
    }

    @Override
    public final void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result) {
        final Pair<CLAPUsageCategoryImpl, StringBuilder> pair = handleUsageCategory(categories, currentCategory, result);
        if (pair != null) {
            final CLAPUsageCategoryImpl nodeCategory = pair.getLeft();
            final StringBuilder nodeResult = pair.getRight();
            if (list().size() > 1) {
                nodeResult.append("{ ");
            }
            internalPrintUsage(categories, nodeCategory, nodeResult, " | ");
            if (list().size() > 1) {
                nodeResult.append(" }");
            }
        }
    }

    @Override
    public final void validate(CLAPParseContext context, List<String> errorMessages) {
        final AbstractCLAPNode decision = context.getDecision(this);
        if (decision == null) {
            for (int i = 0; i < list().size(); i++) {
                list().get(i).validate(context, errorMessages);
            }
        } else {
            decision.validate(context, errorMessages);
        }
    }

}
