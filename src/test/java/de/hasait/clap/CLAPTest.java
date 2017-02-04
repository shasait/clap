/*
 * Copyright (C) 2013 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.clap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
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
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(String.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-vh", "-p25"); //$NON-NLS-1$ //$NON-NLS-2$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.ambiguousResult", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testAnnotationWorks01() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--astring=Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeA typeA = result.getValue(typeAClass);
		assertNotNull(typeA);
		assertEquals("Hallo", typeA.getString()); //$NON-NLS-1$
		assertNull(typeA.getBoolean());
	}

	@Test
	public void testAnnotationWorks02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--aboolean"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeA typeA = result.getValue(typeAClass);
		assertNotNull(typeA);
		assertNull(typeA.getString());
		assertTrue(typeA.getBoolean());
	}

	@Test
	public void testAnnotationWorks03() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeA> typeAClass = clap.addClass(CLAPTypeA.class);

		final CLAPResult result = clap.parse("-v", "-vh", "-a", "--astring=Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeA typeA = result.getValue(typeAClass);
		assertNotNull(typeA);
		assertEquals("Hallo", typeA.getString()); //$NON-NLS-1$
		assertTrue(typeA.getBoolean());
	}

	@Test
	public void testAnnotationWorks04() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeC> typeCClass = clap.addClass(CLAPTypeC.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--bstring=Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeC typeC = result.getValue(typeCClass);
		assertNotNull(typeC);
		final Object object = typeC.getObject();
		assertTrue(object instanceof CLAPTypeB);
		final CLAPTypeB typeB = (CLAPTypeB) object;
		assertEquals("Hallo", typeB.getString()); //$NON-NLS-1$
		assertNull(typeB.getBoolean());
	}

	@Test
	public void testAnnotationWorks05() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertEquals("Hallo", typeD.getString()); //$NON-NLS-1$
		assertNull(typeD.getBoolean());
		assertEquals(1000, typeD.getInt());
	}

	@Test
	public void testAnnotationWorks06() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addClass(CLAPTypeD.class);

		try {
			clap.parse("-v", "-vh", "--dstring=Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.validationFailed clap.error.keywordIsMissing Hallo", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testAnnotationWorks07() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Object> typeABClass = clap.addDecision(Object.class, CLAPTypeA.class, CLAPTypeB.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--bstring=Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final Object object = result.getValue(typeABClass);
		assertTrue(object instanceof CLAPTypeB);
		final CLAPTypeB typeB = (CLAPTypeB) object;
		assertEquals("Hallo", typeB.getString()); //$NON-NLS-1$
		assertNull(typeB.getBoolean());
	}

	@Test
	public void testAnnotationWorks08() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=245", "Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertEquals("Hallo", typeD.getString()); //$NON-NLS-1$
		assertNull(typeD.getBoolean());
		assertEquals(245, typeD.getInt());
	}

	@Test
	public void testAnnotationWorks09() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=-123", "Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertEquals("Hallo", typeD.getString()); //$NON-NLS-1$
		assertNull(typeD.getBoolean());
		assertEquals(-123, typeD.getInt());
	}

	@Test(expected = NumberFormatException.class)
	public void testAnnotationWorks10() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("-v", "-vh", "--dstring=Hallo", "--dint=-1f23", "Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertEquals("Hallo", typeD.getString()); //$NON-NLS-1$
		assertNull(typeD.getBoolean());
		assertEquals(-123, typeD.getInt());
	}

	@Test
	public void testDecision01() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-vh"); //$NON-NLS-1$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.validationFailed clap.error.optionIsMissing --ftp-serverclap.error.errorMessageSplitclap.error.optionIsMissing --http-server", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testDecision02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vh", "-v", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(2, result.getCount(helpOption));
	}

	@Test
	public void testDecision03() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		final CLAPValue<String> ftpServerOption = ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		final CLAPValue<String> httpServerOption = httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vh", "--ftp-server", "ftp.example.org"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(ftpServerOption));
		assertEquals("ftp.example.org", result.getValue(ftpServerOption)); //$NON-NLS-1$
		assertEquals(0, result.getCount(httpServerOption));
	}

	@Test
	public void testDecision04() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();

		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", false, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", false, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode fromToDecision = clap.addDecision();
		fromToDecision.addOption1(String.class, null, "from", true, "fromdkey", "fromukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fromToDecision.addOption1(String.class, null, "to", true, "todkey", "toukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vh", "-v", "-h", "--to", "target"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(2, result.getCount(helpOption));
	}

	@Test
	public void testDecision05() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-x"); //$NON-NLS-1$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.invalidTokenList -x", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testDecision06() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-p", "10", "-x"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.invalidTokenList -x", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testDecision07() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPNode ftpHttpDecision = clap.addDecision();
		final CLAPNode ftpDecisionBranch = ftpHttpDecision.addNodeList();
		ftpDecisionBranch.addOption1(String.class, null, "ftp-server", true, "ftpsdkey", "ftpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ftpDecisionBranch.addOption1(Integer.class, 's', "size", true, "sdkey", "sukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPNode httpDecisionBranch = ftpHttpDecision.addNodeList();
		httpDecisionBranch.addOption1(String.class, null, "http-server", true, "httpsdkey", "httpsukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		httpDecisionBranch.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-p25"); //$NON-NLS-1$ 
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.validationFailed clap.error.optionIsMissing --http-server", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testGetPasswordOrReadInteractivlyWorks01() {
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("Hallo"); //$NON-NLS-1$ 
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertNull(typeD.getString());
		assertNull(typeD.getBoolean());

		clap.setUICallback(new CLAPUICallback() {

			@Override
			public String readLine(final String pPrompt) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String readPassword(final String pPrompt) {
				return "secret"; //$NON-NLS-1$
			}

		});

		assertEquals("secret", clap.getPasswordOrReadInteractivly(typeD, "cancelkey", false).getString()); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull(typeD.getString());
	}

	@Test
	public void testGetPasswordOrReadInteractivlyWorks02() {
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("Hallo", "--dstring", "foobar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertEquals("foobar", typeD.getString()); //$NON-NLS-1$
		assertNull(typeD.getBoolean());

		clap.setUICallback(new CLAPUICallback() {

			@Override
			public String readLine(final String pPrompt) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String readPassword(final String pPrompt) {
				return "secret"; //$NON-NLS-1$
			}

		});

		assertEquals("foobar", clap.getPasswordOrReadInteractivly(typeD, "cancelkey", false).getString()); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("foobar", typeD.getString()); //$NON-NLS-1$
	}

	@Test
	public void testGetPasswordOrReadInteractivlyWorks03() {
		final CLAPValue<CLAPTypeD> typeDClass = clap.addClass(CLAPTypeD.class);

		final CLAPResult result = clap.parse("Hallo"); //$NON-NLS-1$ 
		final CLAPTypeD typeD = result.getValue(typeDClass);
		assertNotNull(typeD);
		assertNull(typeD.getString());
		assertNull(typeD.getBoolean());

		clap.setUICallback(new CLAPUICallback() {

			@Override
			public String readLine(final String pPrompt) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String readPassword(final String pPrompt) {
				return "secret"; //$NON-NLS-1$
			}

		});

		assertEquals("secret", clap.getPasswordOrReadInteractivly(typeD, "cancelkey", true).getString()); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("secret", typeD.getString()); //$NON-NLS-1$
	}

	@Test
	public void testKeywordWorks01() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addKeyword("Hallo"); //$NON-NLS-1$

		final CLAPResult result = clap.parse("-vv", "-v", "Hallo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		assertEquals(3, result.getCount(verboseOption));
	}

	@Test
	public void testKeywordWorks02() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addKeyword("Hallo"); //$NON-NLS-1$

		try {
			clap.parse("-vv", "-v"); //$NON-NLS-1$ //$NON-NLS-2$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.validationFailed clap.error.keywordIsMissing Hallo", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testLongKeyWithArg01() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port", "22", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
		assertEquals(3, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testLongKeyWithArg02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port=22", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		assertEquals(3, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testLongKeyWithArg03() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-vv", "--port=22", "-vh", "--port", "22"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.validationFailed clap.error.incorrectNumberOfArguments -p, --port 1 2", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testLongKeyWithArg04() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final CLAPValue<String[]> usersOption = clap.addOptionU(String[].class, 'u', "users", false, ';', "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port=22", "--users", "user1", "user2", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ 
		assertEquals(2, result.getCount(verboseOption));
		assertEquals(0, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(1, result.getCount(usersOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
		assertArrayEquals(new String[] {
				"user1", "user2", "-vh"}, result.getValue(usersOption)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void testLongKeyWithArg05() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final CLAPValue<String[]> usersOption = clap.addOptionU(String[].class, 'u', "users", false, ';', "udkey", "uukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
		assertEquals(3, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(1, result.getCount(usersOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
		assertArrayEquals(new String[] {
				"user1", "user2"}, result.getValue(usersOption)); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	@Test
	public void testLongKeyWithArg06() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final CLAPValue<String[]> usersOption = clap.addOption(String[].class, 'u', "users", false, 2, ';', "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port=22", "--users", "user1", "user2", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ 
		assertEquals(3, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(1, result.getCount(usersOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
		assertArrayEquals(new String[] {
				"user1", "user2"}, result.getValue(usersOption)); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	@Test
	public void testLongKeyWithArg07() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final CLAPValue<String[]> usersOption = clap.addOption(String[].class, 'u', "users", false, 2, ';', "udkey", "uukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
		assertEquals(3, result.getCount(verboseOption));
		assertTrue(result.contains(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertTrue(result.contains(portOption));
		assertEquals(1, result.getCount(usersOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
		assertArrayEquals(new String[] {
				"user1", "user2"}, result.getValue(usersOption)); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	@Test
	public void testLongKeyWithArg08() {
		clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		clap.addOption(String[].class, 'u', "users", false, 2, null, "udkey", "uukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			clap.parse("-vv", "--port=22", "--users=user1;user2", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			fail(CLAPException.class + " expected"); //$NON-NLS-1$
		} catch (final CLAPException e) {
			assertEquals("clap.error.invalidTokenList --users=user1;user2", e.getMessage()); //$NON-NLS-1$
		}
	}

	@Test
	public void testLongKeyWorks01() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("--verbose"); //$NON-NLS-1$
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(0, result.getCount(helpOption));
	}

	@Test
	public void testLongKeyWorks02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("--verbose", "--help"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
	}

	@Test
	public void testShortKeyWithArg01() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-p", "22"); //$NON-NLS-1$ //$NON-NLS-2$ 
		assertEquals(0, result.getCount(verboseOption));
		assertEquals(0, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testShortKeyWithArg02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-p22"); //$NON-NLS-1$ 
		assertEquals(0, result.getCount(verboseOption));
		assertEquals(0, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testShortKeyWithArg03() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vp22", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ 
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testShortKeyWithArg04() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vp", "22", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		assertEquals(1, result.getCount(verboseOption));
		assertTrue(result.contains(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertTrue(result.contains(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testShortKeyWithArg05() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer> portOption = clap.addOption1(Integer.class, 'p', "port", false, "pdkey", "pukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "-p", "22", "-vh"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
		assertEquals(3, result.getCount(verboseOption));
		assertTrue(result.contains(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(1, result.getCount(portOption));
		assertTrue(result.contains(portOption));
		assertEquals(Integer.valueOf(22), result.getValue(portOption));
	}

	@Test
	public void testShortKeyWithArg06() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Integer[]> numbersOption = clap.addOptionU(Integer[].class, 'n', "numbers", false, ';', "ndkey", "nukey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final CLAPResult result = clap.parse("-vv", "-n1;2;3;4", "-vh", "-n", "5", "6"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ 
		assertEquals(3, result.getCount(verboseOption));
		assertTrue(result.contains(verboseOption));
		assertEquals(1, result.getCount(helpOption));
		assertEquals(2, result.getCount(numbersOption));
		assertArrayEquals(new Integer[] {
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
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("-v"); //$NON-NLS-1$
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(0, result.getCount(helpOption));
	}

	@Test
	public void testShortKeyWorks02() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("-v", "-h"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
	}

	@Test
	public void testShortKeyWorks03() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("-vh"); //$NON-NLS-1$
		assertEquals(1, result.getCount(verboseOption));
		assertEquals(1, result.getCount(helpOption));
	}

	@Test
	public void testShortKeyWorks04() {
		final CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "vdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 
		final CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "hdkey"); //$NON-NLS-1$ //$NON-NLS-2$ 

		final CLAPResult result = clap.parse("-vvvhvvh", "-vh", "-v", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals(7, result.getCount(verboseOption));
		assertEquals(4, result.getCount(helpOption));
	}

}
