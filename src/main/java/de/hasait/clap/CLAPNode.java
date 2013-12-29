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
 * Node of the parse tree used for parsing arguments in {@link CLAP#parse(String...)}.
 */
public interface CLAPNode {

	/**
	 * <p>
	 * Add annotated class.
	 * </p>
	 * <p>
	 * Annotations:
	 * <ul>
	 * <li>{@link CLAPOption}</li>
	 * <li>{@link CLAPKeyword}</li>
	 * <li>{@link CLAPKeywords}</li>
	 * <li>{@link CLAPDecision}</li>
	 * <li>{@link CLAPHelpCategory}</li>
	 * <li>{@link CLAPUsageCategory}</li>
	 * <li>{@link CLAPDelegate}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param pClass The class.
	 * @return The handle for getting the actual value after parsing.
	 */
	<V> CLAPValue<V> addClass(Class<V> pClass);

	/**
	 * Add decision node. Only one of the direct children can match, e.g.:
	 * 
	 * <pre>
	 * --client --connect &lt;address&gt; | --server --port &lt;port&gt;
	 * </pre>
	 * 
	 * @return
	 */
	CLAPNode addDecision();

	/**
	 * Build decision by using annotated classes: You have one base class or interface and for each decision branch a
	 * class extending the base class or implementing the interface. The base class can contain common options. Complex
	 * type hierarchies are supported.
	 * 
	 * @param pResultClass The base class or interface
	 * @param pBranchClasses The branch classes
	 * @return The handle for getting the actual value after parsing.
	 */
	<V> CLAPValue<V> addDecision(Class<V> pResultClass, Class<? extends V>... pBranchClasses);

	/**
	 * Add flag node.
	 * 
	 * @param pShortKey Short key or <code>null</code>.
	 * @param pLongKey Long key or <code>null</code>.
	 * @param pRequired Typically flags are not required (<code>false</code>), but for decision branches they can be
	 *            required, e.g. one branch with an option and another branch with a required flag and an option, so the
	 *            existence of the flag decides which branch is active.
	 * @param pDescriptionNLSKey NLSkey for the description
	 * @param pArgUsageNLSKey NLSkey for the usage
	 * @return The node.
	 */
	CLAPOptionNode<Boolean> addFlag(Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey);

	/**
	 * Add a keyword.
	 * 
	 * @param pKeyword
	 */
	void addKeyword(String pKeyword);

	CLAPNode addNodeList();

	/**
	 * <p>
	 * Add option. For most use cases the following methods can be used:
	 * <ul>
	 * <li>Option without arguments: {@link #addFlag(Character, String, boolean, String, String)}</li>
	 * <li>Option with single argument: {@link #addOption1(Class, Character, String, boolean, String, String)}</li>
	 * <li>Option with unlimited arguments:
	 * {@link #addOptionU(Class, Character, String, boolean, Character, String, String)}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param pResultClass The value type, for custom types use {@link CLAP#addConverter(Class, CLAPConverter)}.
	 * @param pShortKey Short key or <code>null</code>.
	 * @param pLongKey Long key or <code>null</code>.
	 * @param pRequired Required (<code>true</code>) or optional (<code>false</code>)
	 * @param pArgCount Arguments to the option: <code>null</code> for autodetect (i.e. arrays and collections become
	 *            unlimited); {@link CLAP#UNLIMITED_ARG_COUNT} for unlimited; <code>0</code> for no arg;
	 *            <code>&gt; 0</code> for exact arg count.
	 * @param pMultiArgSplit Character for splitting multiple arguments
	 * @param pDescriptionNLSKey NLSkey for the description
	 * @param pArgUsageNLSKey NLSkey for the usage
	 * @return The handle for getting the actual value after parsing.
	 */
	<V> CLAPValue<V> addOption(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Integer pArgCount, Character pMultiArgSplit,
			String pDescriptionNLSKey, String pArgUsageNLSKey);

	<V> CLAPValue<V> addOption1(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, String pDescriptionNLSKey, String pArgUsageNLSKey);

	<V> CLAPValue<V> addOptionU(Class<V> pResultClass, Character pShortKey, String pLongKey, boolean pRequired, Character pMultiArgSplit, String pDescriptionNLSKey,
			String pArgUsageNLSKey);

	void setHelpCategory(int pOrder, String pTitleNLSKey);

	void setUsageCategory(int pOrder, String pTitleNLSKey);

}
