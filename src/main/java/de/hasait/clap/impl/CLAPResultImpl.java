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

    private CLAPResultImpl(CLAPResultImpl other) {
        super();

        _valueMap = new HashMap<>(other._valueMap);
        _countMap = new HashMap<>(other._countMap);
    }

    @Override
    public CLAPResult clone() {
        return new CLAPResultImpl(this);
    }

    @Override
    public boolean contains(CLAPValue<?> node) {
        return getCount(node) > 0;
    }

    @Override
    public boolean contains(CLAPNode node) {
        final Integer count = _countMap.get(node);
        return count != null && count > 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        final CLAPResultImpl casted = (CLAPResultImpl) other;

        if (!_valueMap.equals(casted._valueMap)) {
            return false;
        }

        return _countMap.equals(casted._countMap);
    }

    @Override
    public int getCount(CLAPValue<?> node) {
        final Integer count = _countMap.get(node);
        return count == null ? 0 : count;
    }

    @Override
    public <T> T getValue(CLAPValue<T> node) {
        return (T) _valueMap.get(node);
    }

    @Override
    public int hashCode() {
        return _valueMap.hashCode();
    }

    public void setCount(Object node, int count) {
        _countMap.put(node, count);
    }

    public <T> void setValue(CLAPValue<T> node, T value) {
        _valueMap.put(node, value);
    }

}
