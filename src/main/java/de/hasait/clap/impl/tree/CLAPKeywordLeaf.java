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

import java.text.MessageFormat;
import java.util.List;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.help.CLAPHelpPrinter;
import de.hasait.clap.impl.parser.CLAPParseContext;
import de.hasait.clap.impl.parser.CLAPResultImpl;

/**
 * Keyword.
 */
public class CLAPKeywordLeaf extends AbstractCLAPLeaf {

    private static final String NLSKEY_CLAP_ERROR_KEYWORD_IS_MISSING = "clap.error.keywordIsMissing";

    private final String keyword;

    public CLAPKeywordLeaf(CLAP clap, String keyword) {
        super(clap);

        if (keyword == null || keyword.length() == 0) {
            throw new IllegalArgumentException("Keyword must not be null or empty");
        }

        if (keyword.startsWith(Character.toString(clap.getShortOptPrefix()))) {
            throw new IllegalArgumentException("Keyword must not start with shortOptPrefix " + clap.getShortOptPrefix() + ": " + keyword);
        }
        if (keyword.startsWith(clap.getLongOptPrefix())) {
            throw new IllegalArgumentException("Keyword must not start with longOptPrefix " + clap.getLongOptPrefix() + ": " + keyword);
        }

        this.keyword = keyword;
    }

    @Override
    public CLAPParseContext[] parse(CLAPParseContext context) {
        if (keyword.equals(context.currentArg())) {
            context.consumeCurrent();
            context.addKeyword(this);
            return new CLAPParseContext[]{
                    context
            };
        }
        return null;
    }

    @Override
    public void validate(CLAPParseContext context, List<String> errorMessages) {
        if (context.getNodeCount(this) == 0) {
            errorMessages.add(nls(NLSKEY_CLAP_ERROR_KEYWORD_IS_MISSING, keyword));
        }
    }

    @Override
    public boolean fillResult(CLAPParseContext context, CLAPResultImpl result) {
        return false;
    }

    @Override
    public void collectHelp(CLAPHelpPrinter helpPrinter) {
        // none
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}[\"{1}\"]", getClass().getSimpleName(), keyword);
    }

    @Override
    protected String buildUsageEntryText() {
        return keyword;
    }

}
