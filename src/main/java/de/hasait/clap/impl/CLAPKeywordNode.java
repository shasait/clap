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

    public CLAPKeywordNode(CLAP pCLAP, String pKeyword) {
        super(pCLAP);

        if (pKeyword == null || pKeyword.length() == 0) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }

        if (pKeyword.startsWith(Character.toString(getCLAP().getShortOptPrefix()))) {
            throw new IllegalArgumentException(
                    "Keyword cannot start with shortOptPrefix " + getCLAP().getShortOptPrefix() + ": " + pKeyword);
        }
        if (pKeyword.startsWith(getCLAP().getLongOptPrefix())) {
            throw new IllegalArgumentException("Keyword cannot start with longOptPrefix " + getCLAP().getLongOptPrefix() + ": " + pKeyword);
        }

        _keyword = pKeyword;
    }

    @Override
    public void collectHelpNodes(Map<CLAPHelpCategoryImpl, Set<CLAPHelpNode>> pNodes, CLAPHelpCategoryImpl pCurrentCategory) {
        // none
    }

    @Override
    public boolean fillResult(CLAPParseContext pContext, CLAPResultImpl pResult) {
        return false;
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext pContext) {
        if (_keyword.equals(pContext.currentArg())) {
            pContext.consumeCurrent();
            pContext.addKeyword(this);
            return new CLAPParseContext[]{
                    pContext
            };
        }
        return null;
    }

    @Override
    public void printUsage(Map<CLAPUsageCategoryImpl, StringBuilder> pCategories, CLAPUsageCategoryImpl pCurrentCategory, StringBuilder pResult) {
        pResult.append(_keyword);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[\"{1}\"]", getClass().getSimpleName(), _keyword);
    }

    @Override
    public void validate(CLAPParseContext pContext, List<String> pErrorMessages) {
        if (pContext.getNodeCount(this) == 0) {
            pErrorMessages.add(nls(NLSKEY_CLAP_ERROR_KEYWORD_IS_MISSING, _keyword));
        }
    }

}
