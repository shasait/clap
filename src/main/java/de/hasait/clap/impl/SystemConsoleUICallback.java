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

import java.io.Console;

import de.hasait.clap.CLAPUICallback;

/**
 * Implementation of {@link CLAPUICallback} using {@link System#console()}.
 */
public class SystemConsoleUICallback implements CLAPUICallback {

    @Override
    public String readLine(String prompt) {
        final Console console = System.console();
        final String line = console.readLine(prompt + ": ");
        return line;
    }

    @Override
    public String readPassword(String prompt) {
        final Console console = System.console();
        final char[] passwordRaw = console.readPassword(prompt + ": ");
        return passwordRaw == null ? null : new String(passwordRaw);
    }

}
