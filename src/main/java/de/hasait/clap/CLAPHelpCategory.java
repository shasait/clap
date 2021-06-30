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
 * <p>Annotation to set help category. Can be used to group options in the help screen.</p>
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
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
public @interface CLAPHelpCategory {

    /**
     * The title of the category (plain or nls key).
     */
    String value() default "";

    /**
     * The order of the help category compared to other categories; if not specified order by name.
     */
    int categoryOrder() default 0;

    /**
     * The order within the help category; if not specified order by name.
     */
    int entryOrder() default 0;

}
