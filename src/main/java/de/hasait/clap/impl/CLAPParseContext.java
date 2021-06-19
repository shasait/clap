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

    public CLAPParseContext(CLAP pCLAP, String[] pArgs) {
        super();

        _clap = pCLAP;

        _args = pArgs.clone();

        _nodeContextMap = new ArrayList<>();

        _currentArgIndex = -1;
        _currentArg = null;
        consumeCurrent();
    }

    private CLAPParseContext(CLAPParseContext pOther) {
        super();

        _clap = pOther._clap;

        // used read only - so can use reference
        _args = pOther._args;

        _nodeContextMap = new ArrayList<>(pOther._nodeContextMap);

        _currentArgIndex = pOther._currentArgIndex;
        _currentArg = pOther._currentArg;
    }

    public <T> void addDecision(AbstractCLAPDecision pDecisionNode, AbstractCLAPNode pBranchNode) {
        _nodeContextMap.add(Pair.of(pDecisionNode, pBranchNode));
    }

    public void addKeyword(CLAPKeywordNode pKeywordNode) {
        _nodeContextMap.add(Pair.of(pKeywordNode, null));
    }

    public void addOption(CLAPOptionNode<?> pOption, List<String> pArgs) {
        _nodeContextMap.add(Pair.of(pOption, pArgs));
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

    public boolean consumeCurrentLongKey(String pLongKey, boolean pAllowEquals) {
        if (!hasCurrentLongKey(pLongKey, pAllowEquals)) {
            throw new IllegalStateException();
        }
        final String prefix = _clap.getLongOptPrefix() + pLongKey + _clap.getLongOptAssignment();
        if (pAllowEquals && _currentArg.startsWith(prefix)) {
            _currentArg = _currentArg.substring(prefix.length());
            return true;
        } else {
            consumeCurrent();
            return false;
        }
    }

    public boolean consumeCurrentShortKey(char pShortKey, boolean pHasArg) {
        if (!hasCurrentShortKey(pShortKey)) {
            throw new IllegalStateException();
        }
        if (_currentArg.length() > 2) {
            _currentArg = (pHasArg ? "" : _currentArg.charAt(0)) + _currentArg.substring(2);
            return true;
        } else {
            consumeCurrent();
            return false;
        }
    }

    public String currentArg() {
        return _currentArg;
    }

    public int getArgCount(CLAPOptionNode<?> pOptionNode) {
        int count = 0;

        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(pOptionNode)) {
                count += ((List<String>) entry.getRight()).size();
            }
        }

        return count;
    }

    public int getCurrentArgIndex() {
        return _currentArgIndex;
    }

    public AbstractCLAPNode getDecision(AbstractCLAPDecision pDecisionNode) {
        AbstractCLAPNode lastBranchNode = null;
        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(pDecisionNode)) {
                lastBranchNode = (AbstractCLAPNode) entry.getRight();
            }
        }
        return lastBranchNode;
    }

    public int getNodeCount(AbstractCLAPNode pNode) {
        int result = 0;

        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(pNode)) {
                result++;
            }
        }

        return result;
    }

    public String[] getOptionArgs(CLAPOptionNode<?> pOptionNode) {
        final List<String> result = new ArrayList<>();
        boolean anyFound = false;
        for (Pair<? extends AbstractCLAPNode, ?> entry : _nodeContextMap) {
            if (entry.getLeft().equals(pOptionNode)) {
                result.addAll((List<String>) entry.getRight());
                anyFound = true;
            }
        }
        return anyFound ? result.toArray(new String[0]) : null;
    }

    public boolean hasCurrentLongKey(String pLongKey, boolean pAllowEquals) {
        if (pLongKey == null) {
            throw new IllegalArgumentException("pLongKey == null");
        }
        if (_currentArg == null) {
            return false;
        }
        return _currentArg.equals(_clap.getLongOptPrefix() + pLongKey) || pAllowEquals && _currentArg
                .startsWith(_clap.getLongOptPrefix() + pLongKey + _clap.getLongOptAssignment());
    }

    public boolean hasCurrentShortKey(char pShortKey) {
        return _currentArg != null
                && _currentArg.length() >= 2
                && _currentArg.charAt(0) == _clap.getShortOptPrefix()
                && _currentArg.charAt(1) == pShortKey;
    }

    public boolean hasMoreTokens() {
        return _currentArg != null;
    }

    @Override
    public String toString() {
        return _currentArg + " " + _currentArgIndex;
    }

}
