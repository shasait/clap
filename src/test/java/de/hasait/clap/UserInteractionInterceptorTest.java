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

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class UserInteractionInterceptorTest {

    public static class Args {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        @CLAPOption(shortKey = 'u', longKey = "username", descriptionNLSKey = "Username", argUsageNLSKey = "username")
        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        @CLAPOption(shortKey = 'p', longKey = "password", descriptionNLSKey = "Password", argUsageNLSKey = "password", password = true)
        public void setPassword(String password) {
            this.password = password;
        }

    }

    public static void main(String[] rawArgs) {
        main(rawArgs, System.out);
    }

    public static void main(String[] rawArgs, PrintStream printStream) {
        CLAP clap = new CLAP();
        CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "Print help", true);
        CLAPValue<Args> argsClassOption = clap.addClass(Args.class);

        CLAPResult result;
        try {
            result = clap.parse(rawArgs);
        } catch (CLAPException e) {
            clap.printUsageAndHelp(printStream);
            throw e;
        }

        if (result.contains(helpOption)) {
            clap.printUsageAndHelp(printStream);
            return;
        }

        Args args = result.getValue(argsClassOption);
        // do sth with Args
    }

    @Test
    public void testInterceptorWorks() {
        String line = "line";
        String pass = "pass";
        CLAP clap = new CLAP();
        clap.setUICallback(new CLAPUICallback() {
            @Override
            public String readLine(String prompt) {
                return line;
            }

            @Override
            public String readPassword(String prompt) {
                return pass;
            }
        });

        Args args = new Args();
        Args argsProxy = clap.userInteractionInterceptor(args, "prompt {0}", "cancelled", true);

        assertNull(args.getUsername());
        assertNull(args.getPassword());

        assertEquals(line, argsProxy.getUsername());
        assertEquals(pass, argsProxy.getPassword());

        assertEquals(line, args.getUsername());
        assertEquals(pass, args.getPassword());
    }

}
