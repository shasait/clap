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
import java.util.Arrays;

import org.junit.Test;

public class Example6 {

    @CLAPUsageCategory(value = "GPU", categoryOrder = 10)
    public static class Gpu {

        private String gpu;

        public String getGpu() {
            return gpu;
        }

        @CLAPOption(longKey = "gpu", descriptionNLSKey = "GPU device to use")
        public void setGpu(String gpu) {
            this.gpu = gpu;
        }

    }

    public static abstract class Command {

        private boolean debug;

        private Gpu gpu;

        public boolean isDebug() {
            return debug;
        }

        @CLAPOption(longKey = "debug", descriptionNLSKey = "Debug output")
        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public Gpu getGpu() {
            return gpu;
        }

        @CLAPDelegate(order = 10)
        public void setGpu(Gpu gpu) {
            this.gpu = gpu;
        }

        public abstract void execute(PrintStream printStream, String[] files);

    }

    @CLAPUsageCategory(value = "Rotate", categoryOrder = 2)
    @CLAPKeyword("rotate")
    public static class RotateCommand extends Command {

        private boolean ccw;

        public boolean isCcw() {
            return ccw;
        }

        @CLAPOption(longKey = "ccw", descriptionNLSKey = "Rotate counterclockwise", order = 1)
        public void setCcw(boolean ccw) {
            this.ccw = ccw;
        }

        @Override
        public void execute(PrintStream printStream, String[] files) {
            printStream.println("rotate " + (ccw ? "ccw" : "cw") + ": " + Arrays.asList(files));
        }

    }

    @CLAPUsageCategory(value = "Scale", categoryOrder = 1)
    @CLAPKeyword("scale")
    public static class ScaleCommand extends Command {

        private int percent;

        public int getPercent() {
            return percent;
        }

        @CLAPOption(shortKey = 'p', longKey = "percent", required = true, descriptionNLSKey = "Scale percentage", argUsageNLSKey = "percent", order = 1)
        public void setPercent(int percent) {
            this.percent = percent;
        }

        @Override
        public void execute(PrintStream printStream, String[] files) {
            printStream.println("scale " + percent + "%: " + Arrays.asList(files));
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

        @CLAPOption(required = true, descriptionNLSKey = "Files to process", argUsageNLSKey = "file", order = 1000)
        public void setFiles(String[] files) {
            this.files = files;
        }

    }

    public static void main(String[] rawArgs) {
        main(rawArgs, System.out);
    }

    public static void main(String[] rawArgs, PrintStream printStream) {
        CLAP clap = new CLAP();
        CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "Increase verbosity level");
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

        int verbosityLevel = result.getCount(verboseOption);
        printStream.println("verbosityLevel=" + verbosityLevel);

        Args args = result.getValue(argsClassOption);
        args.getCommand().execute(printStream, args.files);
    }

}
