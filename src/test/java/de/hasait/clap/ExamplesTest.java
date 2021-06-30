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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExamplesTest {

    @Test
    public void testExample1UsageAndHelp() throws Exception {
        testUsageAndHelp(Example1.class);
    }

    @Test
    public void testExample2UsageAndHelp() throws Exception {
        testUsageAndHelp(Example2.class);
    }

    @Test
    public void testExample3UsageAndHelp() throws Exception {
        testUsageAndHelp(Example3.class);
    }

    @Test
    public void testExample4UsageAndHelp() throws Exception {
        testUsageAndHelp(Example4.class);
    }

    @Test
    public void testExample5UsageAndHelp() throws Exception {
        testUsageAndHelp(Example5.class);
    }

    @Test
    public void testExample6UsageAndHelp() throws Exception {
        testUsageAndHelp(Example6.class);
    }

    @Test
    public void testExample7UsageAndHelp() throws Exception {
        testUsageAndHelp(Example7.class);
    }

    @Test
    public void testUserInteractionInterceptorTestUsageAndHelp() throws Exception {
        testUsageAndHelp(UserInteractionInterceptorTest.class);
    }

    private static void testUsageAndHelp(Class<?> exampleClass) throws Exception {
        ByteArrayOutputStream actualBaos = new ByteArrayOutputStream();
        PrintStream actualPs = new PrintStream(actualBaos);
        exampleClass.getMethod("main", new Class[]{
                String[].class,
                PrintStream.class
        }).invoke(null, new String[]{"-h"}, actualPs);
        actualPs.close();
        String actualOutput = actualBaos.toString();
        ByteArrayOutputStream expectedBaos = new ByteArrayOutputStream();
        String resourcePath = "/" + exampleClass.getName().replace('.', '/') + "UsageAndHelp.txt";
        InputStream expectedIn = exampleClass.getResourceAsStream(resourcePath);
        if (expectedIn != null) {
            byte[] buffer = new byte[1024];
            int n;
            while (-1 != (n = expectedIn.read(buffer))) {
                expectedBaos.write(buffer, 0, n);
            }
            expectedIn.close();
        }
        String expectedOutput = expectedBaos.toString();
        assertEquals(expectedOutput, actualOutput);
    }


}
