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

import java.util.Objects;

import de.hasait.clap.CLAP;

/**
 * Leaf or Node of the option tree.
 */
public abstract class AbstractCLAPRelated {

    protected final CLAP clap;

    protected AbstractCLAPRelated(CLAP clap) {
        super();

        this.clap = Objects.requireNonNull(clap, "clap must not be null");
    }

    protected final String nls(String key, Object... arguments) {
        return clap.nls(key, arguments);
    }

}
