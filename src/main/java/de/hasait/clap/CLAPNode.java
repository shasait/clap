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

package de.hasait.clap;

/**
 * Node of the parse tree used for parsing arguments in {@link CLAP#parse(String...)}.
 */
public interface CLAPNode {

    /**
     * <p>Add annotated class.</p>
     * Annotations: <ul>
     * <li>{@link CLAPOption}</li>
     * <li>{@link CLAPKeyword}</li>
     * <li>{@link CLAPKeywords}</li>
     * <li>{@link CLAPDecision}</li>
     * <li>{@link CLAPHelpCategory}</li>
     * <li>{@link CLAPUsageCategory}</li>
     * <li>{@link CLAPDelegate}</li>
     * </ul>
     *
     * @param clazz The class.
     * @return The handle for getting the actual value after parsing.
     */
    <V> CLAPValue<V> addClass(Class<V> clazz);

    /**
     * Add decision node. Only one of the direct children can match, e.g.:
     *
     * <pre>
     * --client --connect &lt;address&gt; | --server --port &lt;port&gt;
     * </pre>
     */
    CLAPNode addDecision();

    /**
     * Build decision by using annotated classes: You have one base class or interface and for each decision branch a
     * class extending the base class or implementing the interface. The base class can contain common options. Complex
     * type hierarchies are supported.
     *
     * @param resultClass   The base class or interface
     * @param branchClasses The branch classes
     * @return The handle for getting the actual value after parsing.
     */
    <V> CLAPValue<V> addDecision(Class<V> resultClass, Class<? extends V>... branchClasses);

    /**
     * Add flag node.
     *
     * @param shortKey          Short key or <code>null</code>.
     * @param longKey           Long key or <code>null</code>.
     * @param required          Typically flags are not required (<code>false</code>), but for decision branches they can be
     *                          required, e.g. one branch with an option and another branch with a required flag and an option, so the
     *                          existence of the flag decides which branch is active.
     * @param descriptionNLSKey NLSkey for the description
     * @return The node.
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default CLAPValue<Boolean> addFlag(Character shortKey, String longKey, boolean required, String descriptionNLSKey, boolean immediateReturn) {
        return addOption(Boolean.class, shortKey, longKey, required, 0, null, descriptionNLSKey, null, immediateReturn);
    }

    /**
     * Add flag node.
     *
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default CLAPValue<Boolean> addFlag(Character shortKey, String longKey, boolean required, String descriptionNLSKey) {
        return addOption(Boolean.class, shortKey, longKey, required, 0, null, descriptionNLSKey, null);
    }

    /**
     * Add a keyword.
     */
    void addKeyword(String keyword);

    CLAPNode addNodeList();

    /**
     * <p>Add option.</p>
     * For most use cases the following methods can be used: <ul>
     * <li>Option without arguments: {@link #addFlag(Character, String, boolean, String)}</li>
     * <li>Option with single argument: {@link #addOption1(Class, Character, String, boolean, String, String)}</li>
     * <li>Option with unlimited arguments:
     * {@link #addOptionU(Class, Character, String, boolean, Character, String, String)}</li>
     * <li>Nameless options: {@link #addNameless1(Class, boolean, String, String)} and {@link #addNamelessU(Class, boolean, String, String)}</li>
     * </ul>
     *
     * @param resultClass       The value type, for custom types use {@link CLAP#addConverter(Class, CLAPConverter)}.
     * @param shortKey          Short key or <code>null</code>.
     * @param longKey           Long key or <code>null</code>.
     * @param required          Required (<code>true</code>) or optional (<code>false</code>)
     * @param argCount          Arguments to the option: <code>null</code> for autodetect (i.e. arrays and collections become
     *                          unlimited); {@link CLAP#UNLIMITED_ARG_COUNT} for unlimited; <code>0</code> for no arg;
     *                          <code>&gt; 0</code> for exact arg count.
     * @param multiArgSplit     Character for splitting multiple arguments
     * @param descriptionNLSKey NLSkey for the description
     * @param argUsageNLSKey    NLSkey for the usage
     * @param immediateReturn   If found while parsing, return immediately the current result - typically used for help flags to skip validation.
     * @return The handle for getting the actual value after parsing.
     */
    <V> CLAPValue<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey, boolean immediateReturn);

    default <V> CLAPValue<V> addOption(Class<V> resultClass, Character shortKey, String longKey, boolean required, Integer argCount, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey) {
        return addOption(resultClass, shortKey, longKey, required, argCount, multiArgSplit, descriptionNLSKey, argUsageNLSKey, false);
    }

    /**
     * Add option with single argument.
     *
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default <V> CLAPValue<V> addOption1(Class<V> resultClass, Character shortKey, String longKey, boolean required, String descriptionNLSKey, String argUsageNLSKey) {
        return addOption(resultClass, shortKey, longKey, required, 1, null, descriptionNLSKey, argUsageNLSKey);
    }

    /**
     * Add option with unlimited arguments.
     *
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default <V> CLAPValue<V[]> addOptionU(Class<V> resultClass, Character shortKey, String longKey, boolean required, Character multiArgSplit, String descriptionNLSKey, String argUsageNLSKey) {
        return addOption(CLAP.asArrayClass(resultClass), shortKey, longKey, required, CLAP.UNLIMITED_ARG_COUNT, multiArgSplit,
                         descriptionNLSKey, argUsageNLSKey
        );
    }

    /**
     * Add nameless option with 1 arguments, e.g. a filename as last argument.
     *
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default <V> CLAPValue<V> addNameless1(Class<V> resultClass, boolean required, String descriptionNLSKey, String argUsageNLSKey) {
        return addOption1(resultClass, null, null, required, descriptionNLSKey, argUsageNLSKey);
    }

    /**
     * Add nameless option with unlimited arguments, e.g. a list of files as last arguments.
     *
     * @see #addOption(Class, Character, String, boolean, Integer, Character, String, String, boolean)
     */
    default <V> CLAPValue<V[]> addNamelessU(Class<V> resultClass, boolean required, String descriptionNLSKey, String argUsageNLSKey) {
        return addOptionU(resultClass, null, null, required, null, descriptionNLSKey, argUsageNLSKey);
    }

    /**
     * <p>Can be used to group options in the help screen.</p>
     * For example &quot;Server Options&quot; is a separate help category with one option:
     * <pre>
     * Common Options
     *
     *   -h, --help         Display this message
     *
     *   -v, --verbose      Verbosity level, use multiple times to increase
     *
     * Server Options
     *
     *   -i, --interface    The interface, where the server is listening
     *
     *   -p, --port         The port, where the server is listening
     * </pre>
     *
     * @param order Initially set to <code>1000</code>.
     */
    void setHelpCategory(int order, String titleNLSKey);

    /**
     * @param order Initially set to <code>1000</code>.
     */
    void setUsageCategory(int order, String titleNLSKey);

}
