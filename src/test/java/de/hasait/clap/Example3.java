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

import java.util.Arrays;

public class Example3 {

    public interface Command {

        void execute(String[] files);

    }

    @CLAPKeyword("rotate")
    public static class RotateCommand implements Command {

        private boolean ccw;

        public boolean isCcw() {
            return ccw;
        }

        @CLAPOption(longKey = "ccw", descriptionNLSKey = "Rotate counterclockwise")
        public void setCcw(boolean ccw) {
            this.ccw = ccw;
        }

        @Override
        public void execute(String[] files) {
            System.out.println("rotate " + (ccw ? "ccw" : "cw") + ": " + Arrays.asList(files));
        }

    }

    @CLAPKeyword("scale")
    public static class ScaleCommand implements Command {

        private int percent;

        public int getPercent() {
            return percent;
        }

        @CLAPOption(shortKey = 'p', longKey = "percent", required = true, descriptionNLSKey = "Scale percentage", argUsageNLSKey = "percent")
        public void setPercent(int percent) {
            this.percent = percent;
        }

        @Override
        public void execute(String[] files) {
            System.out.println("scale " + percent + "%: " + Arrays.asList(files));
        }

    }

    public static class Args {

        private Command command;

        private String[] files;

        public Command getCommand() {
            return command;
        }

        @CLAPDecision(branches = {
                ScaleCommand.class,
                RotateCommand.class
        })
        public void setCommand(Command command) {
            this.command = command;
        }

        public String[] getFiles() {
            return files;
        }

        @CLAPOption(required = true, descriptionNLSKey = "Files to process", argUsageNLSKey = "file")
        public void setFiles(String[] files) {
            this.files = files;
        }

    }

    public static void main(String[] rawArgs) {
        CLAP clap = new CLAP();
        CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "Increase verbosity level");
        CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "Print help", true);
        CLAPValue<Args> argsClassOption = clap.addClass(Args.class);

        CLAPResult result;
        try {
            result = clap.parse(rawArgs);
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

        Args args = result.getValue(argsClassOption);
        args.getCommand().execute(args.files);
    }

}
