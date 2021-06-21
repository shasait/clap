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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a setter as target for an CLAP option.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD
})
public @interface CLAPOption {

    int UNLIMITED_ARG_COUNT = -1;
    int AUTOMATIC_ARG_COUNT = -2;

    char shortKey() default ' ';

    /**
     * shortKey as String, useful for Groovy, where char in annotation is not easy.
     */
    String sshortKey() default "";

    String longKey() default "";

    boolean required() default false;

    /**
     * Arguments to the option: not specified for autodetect (i.e. arrays and collections become unlimited);
     * {@link #UNLIMITED_ARG_COUNT} for unlimited;
     * <code>0</code> for no arg;
     * <code>&gt; 0</code> for exact arg count.
     */
    int argCount() default AUTOMATIC_ARG_COUNT;

    char multiArgSplit() default ' ';

    /**
     * multiArgSplit as String, useful for Groovy, where char in annotation is not easy.
     */
    String smultiArgSplit() default "";

    String descriptionNLSKey() default "";

    String argUsageNLSKey() default "";

    /**
     * The order within the class; for help and usage ordering see {@link CLAPHelpCategory} and {@link CLAPUsageCategory}.
     */
    int order() default 1000;

}
