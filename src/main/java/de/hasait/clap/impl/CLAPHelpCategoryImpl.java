/*
 * Copyright (C) 2013 by HasaIT (hasait at web dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.clap.impl;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 */
public class CLAPHelpCategoryImpl implements Comparable<CLAPHelpCategoryImpl> {

	private final int _order;

	private final String _titleNLSKey;

	public CLAPHelpCategoryImpl(final int pOrder, final String pTitleNLSKey) {
		super();

		if (pTitleNLSKey == null) {
			throw new IllegalArgumentException();
		}

		_order = pOrder;
		_titleNLSKey = pTitleNLSKey;
	}

	@Override
	public int compareTo(final CLAPHelpCategoryImpl pOther) {
		final int orderResult = Integer.valueOf(_order).compareTo(Integer.valueOf(pOther._order));
		if (orderResult != 0) {
			return orderResult;
		}

		final int titleNLSKeyResult = _titleNLSKey.compareTo(pOther._titleNLSKey);
		if (titleNLSKeyResult != 0) {
			return titleNLSKeyResult;
		}

		return 0;
	}

	@Override
	public boolean equals(final Object pOther) {
		if (pOther == this) {
			return true;
		}

		if (pOther == null) {
			return false;
		}

		if (getClass() != pOther.getClass()) {
			return false;
		}

		final CLAPHelpCategoryImpl other = (CLAPHelpCategoryImpl) pOther;

		if (_order != other._order) {
			return false;
		}

		if (!_titleNLSKey.equals(other._titleNLSKey)) {
			return false;
		}

		return true;
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
