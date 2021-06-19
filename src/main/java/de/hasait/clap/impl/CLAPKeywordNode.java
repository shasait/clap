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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hasait.clap.CLAP;

/**
 * An option node.
 */
public class CLAPKeywordNode extends AbstractCLAPNode {

    private static final String NLSKEY_CLAP_ERROR_KEYWORD_IS_MISSING = "clap.error.keywordIsMissing";

    private final String _keyword;

    public CLAPKeywordNode(CLAP clap, String keyword) {
        super(clap);

        if (keyword == null || keyword.length() == 0) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }

        if (keyword.startsWith(Character.toString(getCLAP().getShortOptPrefix()))) {
            throw new IllegalArgumentException(
                    "Keyword cannot start with shortOptPrefix " + getCLAP().getShortOptPrefix() + ": " + keyword);
        }
        if (keyword.startsWith(getCLAP().getLongOptPrefix())) {
            throw new IllegalArgumentException("Keyword cannot start with longOptPrefix " + getCLAP().getLongOptPrefix() + ": " + keyword);
        }

        _keyword = keyword;
    }

    @Override
    public void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> nodes, CLAPHelpCategoryImpl currentCategory) {
        // none
    }

    @Override
    public boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        return false;
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
        if (_keyword.equals(context.currentArg())) {
            context.consumeCurrent();
            context.addKeyword(this);
            return new CLAPParseContext[]{
                    context
            };
        }
        return null;
    }

    @Override
    public void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> categories, CLAPUsageCategoryImpl currentCategory, StringBuilder result) {
        result.append(_keyword);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[\"{1}\"]", getClass().getSimpleName(), _keyword);
    }

    @Override
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        if (context.getNodeCount(this) == 0) {
            errorMessages.add(nls(NLSKEY_CLAP_ERROR_KEYWORD_IS_MISSING, _keyword));
        }
    }

}
