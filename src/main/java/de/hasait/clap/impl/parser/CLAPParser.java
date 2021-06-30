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

package de.hasait.clap.impl.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPException;
import de.hasait.clap.CLAPResult;
import de.hasait.clap.impl.AbstractCLAPRelated;
import de.hasait.clap.impl.tree.AbstractCLAPLeafOrNode;

public class CLAPParser extends AbstractCLAPRelated {

    private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT = "clap.error.errorMessagesSplit";
    private static final String NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT = "clap.error.errorMessageSplit";
    private static final String NLSKEY_CLAP_ERROR_VALIDATION_FAILED = "clap.error.validationFailed";
    private static final String NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT = "clap.error.ambiguousResult";
    private static final String NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST = "clap.error.invalidTokenList";

    private final AbstractCLAPLeafOrNode root;

    public CLAPParser(CLAP clap, AbstractCLAPLeafOrNode root) {
        super(clap);

        this.root = Objects.requireNonNull(root, "root must not be null");
    }

    public CLAPResult parse(String... args) {
        Set<CLAPParseContext> contextsWithInvalidToken = new HashSet<>();
        List<CLAPParseContext> parsedContexts = new ArrayList<>();
        parse(parsedContexts, contextsWithInvalidToken, args);

        if (parsedContexts.isEmpty()) {
            throw createInvalidTokenException(contextsWithInvalidToken);
        }

        Set<CLAPResultImpl> results = new LinkedHashSet<>();
        Map<CLAPParseContext, List<String>> contextErrorMessages = new HashMap<>();
        validate(parsedContexts, contextErrorMessages, results);

        Iterator<CLAPResultImpl> resultI = results.iterator();
        if (!resultI.hasNext()) {
            createValidateionException(contextErrorMessages);
        }

        CLAPResultImpl firstResult = resultI.next();

        if (resultI.hasNext()) {
            throw new CLAPException(nls(NLSKEY_CLAP_ERROR_AMBIGUOUS_RESULT));
        }

        return firstResult;
    }

    private void parse(List<CLAPParseContext> parsedContexts, Set<CLAPParseContext> contextsWithInvalidToken, String[] args) {
        LinkedList<CLAPParseContext> activeContexts = new LinkedList<>();
        activeContexts.add(new CLAPParseContext(clap, args));

        while (!activeContexts.isEmpty()) {
            CLAPParseContext context = activeContexts.removeFirst();
            if (context.containsImmediateReturn()) {
                parsedContexts.clear();
                parsedContexts.add(context);
                break;
            }
            if (context.hasMoreTokens()) {
                final CLAPParseContext[] result = root.parse(context);
                if (result != null) {
                    activeContexts.addAll(Arrays.asList(result));
                } else {
                    contextsWithInvalidToken.add(context);
                }
            } else {
                parsedContexts.add(context);
            }
        }
    }

    private CLAPException createInvalidTokenException(Set<CLAPParseContext> contextsWithInvalidToken) {
        int maxArgIndex = Integer.MIN_VALUE;
        final Set<String> invalidTokensOfBestContexts = new HashSet<>();
        for (CLAPParseContext context : contextsWithInvalidToken) {
            final int currentArgIndex = context.getCurrentArgIndex();
            if (currentArgIndex > maxArgIndex) {
                invalidTokensOfBestContexts.clear();
            }
            if (currentArgIndex >= maxArgIndex) {
                maxArgIndex = currentArgIndex;
                invalidTokensOfBestContexts.add(context.currentArg());
            }
        }
        return new CLAPException(nls(NLSKEY_CLAP_ERROR_INVALID_TOKEN_LIST, StringUtils.join(invalidTokensOfBestContexts, ", ")));
    }

    private void validate(List<CLAPParseContext> parsedContexts, Map<CLAPParseContext, List<String>> contextErrorMessages, Set<CLAPResultImpl> results) {
        for (CLAPParseContext context : parsedContexts) {
            final List<String> errorMessages = new ArrayList<>();
            if (!context.containsImmediateReturn()) {
                root.validate(context, errorMessages);
            }
            if (errorMessages.isEmpty()) {
                final CLAPResultImpl result = new CLAPResultImpl();
                root.fillResult(context, result);
                results.add(result);
            } else {
                contextErrorMessages.put(context, errorMessages);
            }
        }
    }

    private void createValidateionException(Map<CLAPParseContext, List<String>> contextErrorMessages) {
        int minErrorMessages = Integer.MAX_VALUE;
        final List<String> errorMessagesOfBestContexts = new ArrayList<>();
        for (Map.Entry<CLAPParseContext, List<String>> entry : contextErrorMessages.entrySet()) {
            final int countErrorMessages = entry.getValue().size();
            if (countErrorMessages < minErrorMessages) {
                errorMessagesOfBestContexts.clear();
            }
            if (countErrorMessages <= minErrorMessages) {
                minErrorMessages = countErrorMessages;
                errorMessagesOfBestContexts.add(StringUtils.join(entry.getValue(), nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGE_SPLIT)));
            }
        }
        throw new CLAPException(nls(NLSKEY_CLAP_ERROR_VALIDATION_FAILED,
                                    StringUtils.join(errorMessagesOfBestContexts, nls(NLSKEY_CLAP_ERROR_ERROR_MESSAGES_SPLIT))
        ));
    }

}
