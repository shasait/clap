/*
 * Copyright (C) 2013 by Sebastian Hasait (sebastian at hasait dot de)
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

package de.hasait.clap;

import de.hasait.clap.impl.CLAPOptionNode;

/**
 * 
 */
public interface CLAPNode {

	<V> CLAPValue<V> addClass(Class<V> pClass);

	CLAPNode addDecision();

	<V> CLAPValue<V> addDecision(Class<V> pResultClass, Class<? extends V>... pBranchClasses);

	CLAPOptionNode<Boolean> addFlag(Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey);

	void addKeyword(String pKeyword);

	CLAPNode addNodeList();

	<V> CLAPValue<V> addOption(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Integer pArgCount, Character pMultiArgSplit,
			String pDescriptionNLSKey, String pArgUsageNLSKey);

	<V> CLAPValue<V> addOption1(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey);

	<V> CLAPValue<V> addOptionU(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Character pMultiArgSplit, String pDescriptionNLSKey,
			String pArgUsageNLSKey);

	void setHelpCategory(int pOrder, String pNLSKey);

}
