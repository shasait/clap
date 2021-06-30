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
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.AbstractCLAPRelated;
import de.hasait.clap.impl.tree.AbstractCLAPDecisionNode;
import de.hasait.clap.impl.tree.AbstractCLAPLeafOrNode;
import de.hasait.clap.impl.tree.CLAPKeywordLeaf;
import de.hasait.clap.impl.tree.CLAPOptionLeaf;

/**
 * Context used while parsing arguments.
 */
public class CLAPParseContext extends AbstractCLAPRelated implements Cloneable {

    private final List<Pair<? extends AbstractCLAPLeafOrNode, ?>> nodeOrLeafContext;
    private final String[] _args;
    private int _currentArgIndex;
    private String _currentArg;
    private boolean _immediateReturn;

    public CLAPParseContext(CLAP clap, String[] args) {
        super(clap);

        _args = args.clone();

        nodeOrLeafContext = new ArrayList<>();

        _currentArgIndex = -1;
        _currentArg = null;
        consumeCurrent();
    }

    private CLAPParseContext(CLAPParseContext other) {
        super(other.clap);

        // used read only - so can use reference
        _args = other._args;

        nodeOrLeafContext = new ArrayList<>(other.nodeOrLeafContext);

        _currentArgIndex = other._currentArgIndex;
        _currentArg = other._currentArg;
        _immediateReturn = other._immediateReturn;
    }

    public void addDecisionBranch(AbstractCLAPDecisionNode decisionNode, AbstractCLAPLeafOrNode branch) {
        nodeOrLeafContext.add(Pair.of(decisionNode, branch));
    }

    public AbstractCLAPLeafOrNode getDecisionBranch(AbstractCLAPDecisionNode decisionNode) {
        AbstractCLAPLeafOrNode lastBranch = null;
        for (Pair<? extends AbstractCLAPLeafOrNode, ?> entry : nodeOrLeafContext) {
            if (entry.getLeft().equals(decisionNode)) {
                lastBranch = (AbstractCLAPLeafOrNode) entry.getRight();
            }
        }
        return lastBranch;
    }

    public void addKeyword(CLAPKeywordLeaf keywordNode) {
        nodeOrLeafContext.add(Pair.of(keywordNode, null));
    }

    public void addOption(CLAPOptionLeaf<?> option, List<String> args) {
        nodeOrLeafContext.add(Pair.of(option, args));
        if (option.isImmediateReturn()) {
            _immediateReturn = true;
        }
    }

    @Override
    public CLAPParseContext clone() {
        return new CLAPParseContext(this);
    }

    public String consumeCurrent() {
        final String result = _currentArg;
        _currentArgIndex++;
        _currentArg = _currentArgIndex < _args.length ? _args[_currentArgIndex] : null;
        return result;
    }

    public boolean consumeCurrentLongKey(String longKey, boolean allowEquals) {
        if (!hasCurrentLongKey(longKey, allowEquals)) {
            throw new IllegalStateException();
        }
        final String prefix = clap.getLongOptPrefix() + longKey + clap.getLongOptAssignment();
        if (allowEquals && _currentArg.startsWith(prefix)) {
            _currentArg = _currentArg.substring(prefix.length());
            return true;
        } else {
            consumeCurrent();
            return false;
        }
    }

    public boolean consumeCurrentShortKey(char shortKey, boolean hasArg) {
        if (!hasCurrentShortKey(shortKey)) {
            throw new IllegalStateException();
        }
        if (_currentArg.length() > 2) {
            _currentArg = (hasArg ? "" : _currentArg.charAt(0)) + _currentArg.substring(2);
            return true;
        } else {
            consumeCurrent();
            return false;
        }
    }

    public String currentArg() {
        return _currentArg;
    }

    public int getArgCount(CLAPOptionLeaf<?> optionNode) {
        int count = 0;

        for (Pair<? extends AbstractCLAPLeafOrNode, ?> entry : nodeOrLeafContext) {
            if (entry.getLeft().equals(optionNode)) {
                count += ((List<String>) entry.getRight()).size();
            }
        }

        return count;
    }

    public int getCurrentArgIndex() {
        return _currentArgIndex;
    }

    public int getNodeCount(AbstractCLAPLeafOrNode node) {
        int result = 0;

        for (Pair<? extends AbstractCLAPLeafOrNode, ?> entry : nodeOrLeafContext) {
            if (entry.getLeft().equals(node)) {
                result++;
            }
        }

        return result;
    }

    public String[] getOptionArgs(CLAPOptionLeaf<?> optionNode) {
        final List<String> result = new ArrayList<>();
        boolean anyFound = false;
        for (Pair<? extends AbstractCLAPLeafOrNode, ?> entry : nodeOrLeafContext) {
            if (entry.getLeft().equals(optionNode)) {
                result.addAll((List<String>) entry.getRight());
                anyFound = true;
            }
        }
        return anyFound ? result.toArray(new String[0]) : null;
    }

    public boolean hasCurrentLongKey(String longKey, boolean allowEquals) {
        if (longKey == null) {
            throw new IllegalArgumentException("longKey == null");
        }
        if (_currentArg == null) {
            return false;
        }
        return _currentArg.equals(clap.getLongOptPrefix() + longKey) || allowEquals && _currentArg
                .startsWith(clap.getLongOptPrefix() + longKey + clap.getLongOptAssignment());
    }

    public boolean hasCurrentShortKey(char shortKey) {
        return _currentArg != null
                && _currentArg.length() >= 2
                && _currentArg.charAt(0) == clap.getShortOptPrefix()
                && _currentArg.charAt(1) == shortKey;
    }

    public boolean hasMoreTokens() {
        return _currentArg != null;
    }

    public boolean containsImmediateReturn() {
        return _immediateReturn;
    }

    @Override
    public String toString() {
        return _currentArg + " " + _currentArgIndex;
    }

}
