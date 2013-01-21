/*
 * Copyright (C) 2013 by HasaIT (hasait at web dot de)
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

package de.hasait.clap.impl;

import java.util.List;

import de.hasait.clap.CLAP;

/**
 * Base class of all nodes forming the option tree.
 */
public abstract class AbstractCLAPNode {

	private final CLAP _clap;

	protected AbstractCLAPNode(final CLAP pCLAP) {
		super();

		_clap = pCLAP;
	}

	public abstract void collectOptionNodes(List<CLAPOptionNode<?>> pOptions);

	public abstract void fillResult(CLAPParseContext pContext, CLAPResultImpl pResult);

	public CLAP getCLAP() {
		return _clap;
	}

	public final String nls(final String pKey) {
		return _clap.nls(pKey);
	}

	public abstract CLAPParseContext[] parse(CLAPParseContext pContext);

	public abstract void printUsage(StringBuilder pResult);

	public abstract void validate(CLAPParseContext pContext, List<String> pErrorMessages);

}
