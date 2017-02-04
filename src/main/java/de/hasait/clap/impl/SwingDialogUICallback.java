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

package de.hasait.clap.impl;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.hasait.clap.CLAPUICallback;

/**
 * Implementation of {@link CLAPUICallback} using Swing.
 */
public class SwingDialogUICallback implements CLAPUICallback {

	private final JComponent _dialogParent;

	/**
	 * @param pDialogParent The parent for dialogs.
	 */
	public SwingDialogUICallback(final JComponent pDialogParent) {
		super();
		_dialogParent = pDialogParent;
	}

	@Override
	public String readLine(final String pPrompt) {
		final JTextField field = new JTextField(40);
		final int result = JOptionPane.showConfirmDialog(_dialogParent, field, pPrompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}
		final String line = field.getText();
		return line;
	}

	@Override
	public String readPassword(final String pPrompt) {
		final JPasswordField field = new JPasswordField(40);
		final int result = JOptionPane.showConfirmDialog(_dialogParent, field, pPrompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}
		final char[] passwordRaw = field.getPassword();
		return passwordRaw == null ? null : new String(passwordRaw);
	}

}
