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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.hasait.clap.CLAP;

/**
 *
 */
public class CLAPHelpCategoryImpl implements Comparable<CLAPHelpCategoryImpl> {

    private final int _order;

    private final String _titleNLSKey;

    public CLAPHelpCategoryImpl(int order, String titleNLSKey) {
        super();

        _order = order;
        _titleNLSKey = titleNLSKey != null ? titleNLSKey : CLAP.NLSKEY_DEFAULT_HELP_CATEGORY;
    }

    @Override
    public int compareTo(CLAPHelpCategoryImpl other) {
        final int orderResult = Integer.compare(_order, other._order);
        if (orderResult != 0) {
            return orderResult;
        }

        final int titleNLSKeyResult = _titleNLSKey.compareTo(other._titleNLSKey);
        return titleNLSKeyResult;
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

        final CLAPHelpCategoryImpl casted = (CLAPHelpCategoryImpl) other;

        if (_order != casted._order) {
            return false;
        }

        return _titleNLSKey.equals(casted._titleNLSKey);
    }

    public int getOrder() {
        return _order;
    }

    public String getTitleNLSKey() {
        return _titleNLSKey;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_order).append(_titleNLSKey).toHashCode();
    }

}
