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

import org.junit.After;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Various tests for {@link CLAP}.
 */
public class CLAPTest {

    private CLAP clap;

    @Before
    public void setUp() {
        clap = new CLAP(null);
    }

    @After
    public void tearDown() {
        clap.printUsage(System.out);
        clap.printHelp(System.out);
    }

    @Test
    public void testAmbiguousResult01() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(String.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-vh", "-p25");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.ambiguousResult", e.getMessage());
        }
    }

    @Test
    public void testAnnotationWorks01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--astring=Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeA typeA = result.getValue(typeAClass);
        assertNotNull(typeA);
        assertEquals("Hallo", typeA.getString());
        assertNull(typeA.getBoolean());
    }

    @Test
    public void testAnnotationWorks02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--aboolean");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeA typeA = result.getValue(typeAClass);
        assertNotNull(typeA);
        assertNull(typeA.getString());
        assertTrue(typeA.getBoolean());
    }

    @Test
    public void testAnnotationWorks03() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

        final CLAPResult result = clap.parse("-v", "-vh", "-a", "--astring=Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeA typeA = result.getValue(typeAClass);
        assertNotNull(typeA);
        assertEquals("Hallo", typeA.getString());
        assertTrue(typeA.getBoolean());
    }

    @Test
    public void testAnnotationWorks04() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeC> typeCClass = clap.addClass(CLAPTypeC.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--bstring=Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeC typeC = result.getValue(typeCClass);
        assertNotNull(typeC);
        final Object object = typeC.getObject();
        assertTrue(object instanceof CLAPTypeB);
        final CLAPTypeB typeB = (CLAPTypeB) object;
        assertEquals("Hallo", typeB.getString());
        assertNull(typeB.getBoolean());
    }

    @Test
    public void testAnnotationWorks05() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeD typeD = result.getValue(typeDClass);
        assertNotNull(typeD);
        assertEquals("Hallo", typeD.getString());
        assertNull(typeD.getBoolean());
        assertEquals(1000, typeD.getInt());
    }

    @Test
    public void testAnnotationWorks06() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        clap.addClass(CLAPTypeD.class);

        try {
            clap.parse("-v", "-vh", "--dstring=Hallo");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.validationFailed clap.error.keywordIsMissing Hallo", e.getMessage());
        }
    }

    @Test
    public void testAnnotationWorks07() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Object> typeABClass = clap.addDecision(Object.class, CLAPTypeA.class, CLAPTypeB.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--bstring=Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final Object object = result.getValue(typeABClass);
        assertTrue(object instanceof CLAPTypeB);
        final CLAPTypeB typeB = (CLAPTypeB) object;
        assertEquals("Hallo", typeB.getString());
        assertNull(typeB.getBoolean());
    }

    @Test
    public void testAnnotationWorks08() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=245", "Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeD typeD = result.getValue(typeDClass);
        assertNotNull(typeD);
        assertEquals("Hallo", typeD.getString());
        assertNull(typeD.getBoolean());
        assertEquals(245, typeD.getInt());
    }

    @Test
    public void testAnnotationWorks09() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=-123", "Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeD typeD = result.getValue(typeDClass);
        assertNotNull(typeD);
        assertEquals("Hallo", typeD.getString());
        assertNull(typeD.getBoolean());
        assertEquals(-123, typeD.getInt());
    }

    @Test(expected = NumberFormatException.class)
    public void testAnnotationWorks10() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

        final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=-1f23", "Hallo");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        final CLAPTypeD typeD = result.getValue(typeDClass);
        assertNotNull(typeD);
        assertEquals("Hallo", typeD.getString());
        assertNull(typeD.getBoolean());
        assertEquals(-123, typeD.getInt());
    }

    @Test
    public void testDecision01() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-vh");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals(
                    "clap.error.validationFailed clap.error.optionIsMissing --ftp-serverclap.error.errorMessageSplitclap.error.optionIsMissing --http-server",
                    e.getMessage()
            );
        }
    }

    @Test
    public void testDecision02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vh", "-v", "-h");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(2, result.getCount(helpOption));
    }

    @Test
    public void testDecision03() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        final CLAPValue<String> ftpServerOption = ftpDecisionBranch
                .addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        final CLAPValue<String> httpServerOption = httpDecisionBranch
                .addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vh", "--ftp-server", "ftp.example.org");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(ftpServerOption));
        assertEquals("ftp.example.org", result.getValue(ftpServerOption));
        assertEquals(0, result.getCount(httpServerOption));
    }

    @Test
    public void testDecision04() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();

        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode fromToDecision = clap.addDecision();
        fromToDecision.addOption1(String.class, null, "from", true, "fromdkey", "fromukey");
        fromToDecision.addOption1(String.class, null, "to", true, "todkey", "toukey");

        final CLAPResult result = clap.parse("-vh", "-v", "-h", "--to", "target");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(2, result.getCount(helpOption));
    }

    @Test
    public void testDecision05() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-x");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.invalidTokenList -x", e.getMessage());
        }
    }

    @Test
    public void testDecision06() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-p", "10", "-x");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.invalidTokenList -x", e.getMessage());
        }
    }

    @Test
    public void testDecision07() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        final CLAPNode ftpHttpDecision = clap.addDecision();
        final CLAPNode ftpDecisionBranch = ftpHttpDecision.addGroup();
        ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey");
        ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        ftpDecisionBranch.addOption1(Integer.class, 's', "size", true, "sdkey", "sukey");

        final CLAPNode httpDecisionBranch = ftpHttpDecision.addGroup();
        httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey");
        httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-p25");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.validationFailed clap.error.optionIsMissing --http-server", e.getMessage());
        }
    }

    @Test
    public void testKeywordWorks01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        clap.addKeyword("Hallo");

        final CLAPResult result = clap.parse("-vv", "-v", "Hallo");
        assertEquals(3, result.getCount(verboseOption));
    }

    @Test
    public void testKeywordWorks02() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addKeyword("Hallo");

        try {
            clap.parse("-vv", "-v");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.validationFailed clap.error.keywordIsMissing Hallo", e.getMessage());
        }
    }

    @Test
    public void testLongKeyWithArg01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vv", "--port", "22", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testLongKeyWithArg02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vv", "--port=22", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testLongKeyWithArg03() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        try {
            clap.parse("-vv", "--port=22", "-vh", "--port", "22");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.validationFailed clap.error.incorrectNumberOfArguments -p, --port 1 2", e.getMessage());
        }
    }

    @Test
    public void testLongKeyWithArg04() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        final CLAPValue<String[]> usersOption = clap.addOptionU(String.class, 'u', "users", false, ';', "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vv", "--port=22", "--users", "user1", "user2", "-vh");
        assertEquals(2, result.getCount(verboseOption));
        assertEquals(0, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(1, result.getCount(usersOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
        assertArrayEquals(new String[]{
                "user1",
                "user2",
                "-vh"
        }, result.getValue(usersOption));
    }

    @Test
    public void testLongKeyWithArg05() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        final CLAPValue<String[]> usersOption = clap.addOptionU(String.class, 'u', "users", false, ';', "udkey", "uukey");

        final CLAPResult result = clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(1, result.getCount(usersOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
        assertArrayEquals(new String[]{
                "user1",
                "user2"
        }, result.getValue(usersOption));
    }

    @Test
    public void testLongKeyWithArg06() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        final CLAPValue<String[]> usersOption = clap.addOption(String[].class, 'u', "users", false, 2, ';', "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vv", "--port=22", "--users", "user1", "user2", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(1, result.getCount(usersOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
        assertArrayEquals(new String[]{
                "user1",
                "user2"
        }, result.getValue(usersOption));
    }

    @Test
    public void testLongKeyWithArg07() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        final CLAPValue<String[]> usersOption = clap.addOption(String[].class, 'u', "users", false, 2, ';', "udkey", "uukey");

        final CLAPResult result = clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertTrue(result.contains(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertTrue(result.contains(portOption));
        assertEquals(1, result.getCount(usersOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
        assertArrayEquals(new String[]{
                "user1",
                "user2"
        }, result.getValue(usersOption));
    }

    @Test
    public void testLongKeyWithArg08() {
        clap.addFlag('v', "verbose", false, "vdkey");
        clap.addFlag('h', "help", false, "hdkey");
        clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");
        clap.addOption(String[].class, 'u', "users", false, 2, null, "udkey", "uukey");

        try {
            clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.invalidTokenList --users=user1;user2", e.getMessage());
        }
    }

    @Test
    public void testLongKeyWorks01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("--verbose");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(0, result.getCount(helpOption));
    }

    @Test
    public void testLongKeyWorks02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("--verbose", "--help");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
    }

    @Test
    public void testShortKeyWithArg01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-p", "22");
        assertEquals(0, result.getCount(verboseOption));
        assertEquals(0, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testShortKeyWithArg02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-p22");
        assertEquals(0, result.getCount(verboseOption));
        assertEquals(0, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testShortKeyWithArg03() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vp22", "-h");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testShortKeyWithArg04() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vp", "22", "-h");
        assertEquals(1, result.getCount(verboseOption));
        assertTrue(result.contains(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertTrue(result.contains(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testShortKeyWithArg05() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey");

        final CLAPResult result = clap.parse("-vv", "-p", "22", "-vh");
        assertEquals(3, result.getCount(verboseOption));
        assertTrue(result.contains(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(1, result.getCount(portOption));
        assertTrue(result.contains(portOption));
        assertEquals(Integer.valueOf(22), result.getValue(portOption));
    }

    @Test
    public void testShortKeyWithArg06() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer[]> numbersOption = clap.addOptionU(Integer.class, 'n', "numbers", false, ';', "ndkey", "nukey");

        final CLAPResult result = clap.parse("-vv", "-n1;2;3;4", "-vh", "-n", "5", "6");
        assertEquals(3, result.getCount(verboseOption));
        assertTrue(result.contains(verboseOption));
        assertEquals(1, result.getCount(helpOption));
        assertEquals(2, result.getCount(numbersOption));
        assertArrayEquals(new Integer[]{
                1,
                2,
                3,
                4,
                5,
                6
        }, result.getValue(numbersOption));
    }

    @Test
    public void testShortKeyWorks01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("-v");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(0, result.getCount(helpOption));
    }

    @Test
    public void testShortKeyWorks02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("-v", "-h");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
    }

    @Test
    public void testShortKeyWorks03() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("-vh");
        assertEquals(1, result.getCount(verboseOption));
        assertEquals(1, result.getCount(helpOption));
    }

    @Test
    public void testShortKeyWorks04() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");

        final CLAPResult result = clap.parse("-vvvhvvh", "-vh", "-v", "-h");
        assertEquals(7, result.getCount(verboseOption));
        assertEquals(4, result.getCount(helpOption));
    }

    @Test(expected = CLAPException.class)
    public void testImmediateReturnWorks1a() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey");
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", true, "pdkey", "pukey");

        // fails because port is required and help is not immediateReturn
        final CLAPResult result = clap.parse("-h");
        assertTrue(result.contains(helpOption));
    }

    @Test
    public void testImmediateReturnWorks1b() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey", true);
        final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", true, "pdkey", "pukey");

        // succeeds because help is immediateReturn and therefore port is not validated
        final CLAPResult result = clap.parse("-h");
        assertTrue(result.contains(helpOption));
    }

    @Test
    public void testNamelessWithArgCount01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey", true);
        final CLAPValue<String[]> filesOption = clap.addOption(String[].class, null, null, true, 2, null, "fdkey", "fukey");

        final CLAPResult result = clap.parse("-v", "src.txt", "tgt.txt");
        assertEquals(1, result.getCount(verboseOption));
        assertArrayEquals(new String[]{
                "src.txt",
                "tgt.txt"
        }, result.getValue(filesOption));
    }

    @Test
    public void testNamelessWithArgCount02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey", true);
        final CLAPValue<String[]> filesOption = clap.addOption(String[].class, null, null, true, 2, null, "fdkey", "fukey");

        try {
            clap.parse("-v", "src.txt");
            fail(CLAPException.class + " expected");
        } catch (CLAPException e) {
            assertEquals("clap.error.validationFailed clap.error.incorrectNumberOfArguments <fukey> 2 1", e.getMessage());
        }
    }

    @Test
    public void testMultipleNamelessWithArgCount01() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey", true);
        final CLAPValue<String[]> filesOption = clap.addOption(String[].class, null, null, true, 2, null, "fdkey", "fukey");
        final CLAPValue<String[]> usersOption = clap.addOption(String[].class, null, null, true, 2, null, "udkey", "uukey");

        final CLAPResult result = clap.parse("-v", "src.txt", "tgt.txt", "foo", "bar");
        assertEquals(1, result.getCount(verboseOption));
        assertArrayEquals(new String[]{
                "src.txt",
                "tgt.txt"
        }, result.getValue(filesOption));
        assertArrayEquals(new String[]{
                "foo",
                "bar"
        }, result.getValue(usersOption));
    }

    @Test
    public void testMultipleNamelessWithArgCount02() {
        final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey");
        final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey", true);
        final CLAPValue<String> fileOption = clap.addOption1(String.class, null, null, true, "fdkey", "fukey");
        final CLAPValue<String[]> usersOption = clap.addOption(String[].class, null, null, true, 2, null, "udkey", "uukey");

        final CLAPResult result = clap.parse("-v", "src.txt", "foo", "bar");

        assertEquals(1, result.getCount(verboseOption));
        assertEquals("src.txt", result.getValue(fileOption));
        assertArrayEquals(new String[]{
                "foo",
                "bar"
        }, result.getValue(usersOption));
    }

}
