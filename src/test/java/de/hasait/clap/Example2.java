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

public class Example2 {

    public static void main(String[] args) {
        CLAP clap = new CLAP();
        CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "Increase verbosity level");
        CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "Print help", true);
        CLAPNode decision = clap.addDecision();
        CLAPNode clientBranch = decision.addNodeList();
        clientBranch.setHelpCategory(2000, "Client");
        CLAPValue<String> clientHost = clientBranch.addOption1(String.class, 'H', "host", true, "The host to connect to", "host");
        CLAPValue<Integer> clientPort = clientBranch.addOption1(Integer.class, 'p', "port", true, "The port to connect to", "port");

        CLAPNode serverBranch = decision.addNodeList();
        serverBranch.setHelpCategory(2001, "Server");
        CLAPValue<Integer> serverPort = serverBranch.addOption1(Integer.class, 'l', "listen", true, "The port to listen on", "port");

        CLAPResult result;
        try {
            result = clap.parse(args);
        } catch (CLAPException e) {
            clap.printUsageAndHelp(System.out);
            throw e;
        }

        if (result.contains(helpOption)) {
            clap.printUsageAndHelp(System.out);
            return;
        }

        int verbosityLevel = result.getCount(verboseOption);
        System.out.println("verbosityLevel=" + verbosityLevel);

        if (result.contains(clientBranch)) {
            System.out.println("Connecting to " + result.getValue(clientHost) + ":" + result.getValue(clientPort) + "...");
        } else if (result.contains(serverBranch)) {
            System.out.println("Listening on " + result.getValue(serverPort) + "...");
        }
    }

}
