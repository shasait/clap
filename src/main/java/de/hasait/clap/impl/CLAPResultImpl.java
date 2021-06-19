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

import java.util.HashMap;
import java.util.Map;

import de.hasait.clap.CLAPNode;
import de.hasait.clap.CLAPResult;
import de.hasait.clap.CLAPValue;

/**
 * Result.
 */
public class CLAPResultImpl implements Cloneable, CLAPResult {

    private final Map<CLAPValue<?>, Object> _valueMap;
    private final Map<Object, Integer> _countMap;

    public CLAPResultImpl() {
        super();

        _valueMap = new HashMap<>();
        _countMap = new HashMap<>();
    }

    private CLAPResultImpl(CLAPResultImpl pOther) {
        super();

        _valueMap = new HashMap<>(pOther._valueMap);
        _countMap = new HashMap<>(pOther._countMap);
    }

    @Override
    public CLAPResult clone() {
        return new CLAPResultImpl(this);
    }

    @Override
    public boolean contains(CLAPValue<?> pNode) {
        return getCount(pNode) > 0;
    }

    @Override
    public boolean contains(CLAPNode pNode) {
        final Integer count = _countMap.get(pNode);
        return count != null && count > 0;
    }

    @Override
    public boolean equals(Object pOther) {
        if (pOther == this) {
            return true;
        }

        if (pOther == null) {
            return false;
        }

        if (getClass() != pOther.getClass()) {
            return false;
        }

        final CLAPResultImpl other = (CLAPResultImpl) pOther;

        if (!_valueMap.equals(other._valueMap)) {
            return false;
        }

        return _countMap.equals(other._countMap);
    }

    @Override
    public int getCount(CLAPValue<?> pNode) {
        final Integer count = _countMap.get(pNode);
        return count == null ? 0 : count;
    }

    @Override
    public <T> T getValue(CLAPValue<T> pNode) {
        return (T) _valueMap.get(pNode);
    }

    @Override
    public int hashCode() {
        return _valueMap.hashCode();
    }

    public void setCount(Object pNode, int pCount) {
        _countMap.put(pNode, pCount);
    }

    public <T> void setValue(CLAPValue<T> pNode, T pValue) {
        _valueMap.put(pNode, pValue);
    }

}
