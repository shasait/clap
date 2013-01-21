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
public @interface CLAPOptionA {

	public static final int UNLIMITED_ARG_COUNT = -1;
	public static final int AUTOMATIC_ARG_COUNT = -2;

	public abstract int argCount() default AUTOMATIC_ARG_COUNT;

	public abstract String argUsageNLSKey() default "";

	public abstract String descriptionNLSKey() default "";

	public abstract String longKey() default "";

	public abstract char multiArgSplit() default ' ';

	public abstract int order() default 1000;

	public abstract boolean required() default false;

	public abstract char shortKey() default ' ';

}
