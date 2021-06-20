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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.hasait.clap.CLAP;

/**
 * Context used while parsing arguments.
 */
public class CLAPParseContext implements Cloneable {

    private final CLAP _clap;
    private final List<Pair<? extends AbstractCLAPNode, ?>> _nodeContextMap;
    private final String[] _args;
    private int _currentArgIndex;
    private String _currentArg;
    private boolean _immediateReturn;

    public CLAPParseContext(CLAP clap, String[] args) {
        super();

        _clap = clap;

        _args = args.clone();

        _nodeContextMap = new ArrayList<>();

        _currentArgIndex = -1;
        _currentArg = null;
        consumeCurrent();
    }

    private CLAPParseContext(CLAPParseContext other) {
        super();

        _clap = other._clap;

        // used read only - so can use reference
        _args = other._args;

        _nodeContextMap = new ArrayList<>(other._nodeContextMap);

        _currentArgIndex = other._currentArgIndex;
        _currentArg = other._currentArg;
        _immediateReturn = other._immediateReturn;
    }

    public <T> void addDecision(AbstractCLAPDecision decisionNode, AbstractCLAPNode branchNode) {
        _nodeContextMap.add(Pair.of(decisionNode, branchNode));
    }

    public void addKeyword(CLAPKeywordNode keywordNode) {
        _nodeContextMap.add(Pair.of(keywordNode, null));
    }

    public void addOption(CLAPOptionNode<?> option, List<String> args) {
        _nodeContextMap.add(Pair.of(option, args));
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
        final String prefix = _clap.getLongOptPrefix() + longKey + _clap.getLongOptAssignment();
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

    public int getArgCount(CLAPOptionNode<?> optionNode) {
        int count = 0;

        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(optionNode)) {
                count += ((List<String>) entry.getRight()).size();
            }
        }

        return count;
    }

    public int getCurrentArgIndex() {
        return _currentArgIndex;
    }

    public AbstractCLAPNode getDecision(AbstractCLAPDecision decisionNode) {
        AbstractCLAPNode lastBranchNode = null;
        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(decisionNode)) {
                lastBranchNode = (AbstractCLAPNode) entry.getRight();
            }
        }
        return lastBranchNode;
    }

    public int getNodeCount(AbstractCLAPNode node) {
        int result = 0;

        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(node)) {
                result++;
            }
        }

        return result;
    }

    public String[] getOptionArgs(CLAPOptionNode<?> optionNode) {
        final List<String> result = new ArrayList<>();
        boolean anyFound = false;
        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
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
        return _currentArg.equals(_clap.getLongOptPrefix() + longKey) || allowEquals && _currentArg
                .startsWith(_clap.getLongOptPrefix() + longKey + _clap.getLongOptAssignment());
    }

    public boolean hasCurrentShortKey(char shortKey) {
        return _currentArg != null
                && _currentArg.length() >= 2
                && _currentArg.charAt(0) == _clap.getShortOptPrefix()
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
