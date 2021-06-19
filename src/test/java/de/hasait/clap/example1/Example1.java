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

package de.hasait.clap.example1;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.hasait.clap.CLAP;
import de.hasait.clap.CLAPException;
import de.hasait.clap.CLAPNode;
import de.hasait.clap.CLAPResult;
import de.hasait.clap.CLAPValue;

/**
 * Imperative style example.
 */
public class Example1 {

    private static final int DEFAULT_PORT = 1234;
    private static final ResourceBundle NLS = ResourceBundle.getBundle("example1-msg");

    public static void main(String[] pArgs) {
        final CLAP clap = new CLAP(NLS);

        final CLAPValue<Boolean> verboseFlag = clap.addFlag('v', "verbose", false, "option.verbose.description");
        final CLAPValue<Boolean> helpFlag = clap.addFlag('h', "help", false, "option.help.description");
        final CLAPNode listenOptionsNode = clap.addNodeList();
        listenOptionsNode.setUsageCategory(1001, "usage_category.listen");
        listenOptionsNode.setHelpCategory(1001, "help_category.listen");
        final CLAPValue<Integer> portOption = listenOptionsNode
                .addOption1(Integer.class, 'p', "port", true, "option.port.description", "option.port.usage");
        final CLAPValue<String> interfaceOption = listenOptionsNode
                .addOption1(String.class, 'i', "interface", false, "option.interface.description", "option.interface.usage");

        final CLAPResult clapResult;
        try {
            clapResult = clap.parse(pArgs);
        } catch (CLAPException e) {
            printUsageAndHelp(clap);
            throw e;
        }

        if (clapResult.contains(helpFlag)) {
            printUsageAndHelp(clap);
            return;
        }

        final int verbosityLevel = clapResult.getCount(verboseFlag);

        final int port;
        if (clapResult.contains(portOption)) {
            port = clapResult.getValue(portOption);
        } else {
            port = DEFAULT_PORT;
        }
        final String interfaceName = clapResult.getValue(interfaceOption);

        final Example1 example1 = new Example1(verbosityLevel, port, interfaceName);
        example1.run();
    }

    private static void printUsageAndHelp(CLAP clap) {
        clap.printUsage(System.out);
        clap.printHelp(System.out);
    }

    private final int _verboseLevel;
    private final int _port;
    private final String _interfaceName;

    public Example1(int pVerboseLevel, int pPort, String pInterfaceName) {
        super();

        _verboseLevel = pVerboseLevel;
        _port = pPort;
        _interfaceName = pInterfaceName;
    }

    private void run() {
        System.out.println(NLS.getString("msg.starting"));
        if (_verboseLevel > 0) {
            System.out.println(MessageFormat.format(NLS.getString("msg.listening_on"), _interfaceName, _port));
        }
        // your logic here
        System.out.println(NLS.getString("msg.started"));
    }

}
