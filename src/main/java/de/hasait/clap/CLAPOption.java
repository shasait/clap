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

	int argCount() default AUTOMATIC_ARG_COUNT;

	String argUsageNLSKey() default "";

	String descriptionNLSKey() default "";

	String longKey() default "";

	char multiArgSplit() default ' ';

	int order() default 1000;

	boolean required() default false;

	char shortKey() default ' ';

	/**
	 * Short key as String, especially for Groovy.
	 */
	String sshortKey() default "";

}
