/**
 * Copyright (C) 2010  Holger Dammertz
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mystictri.neotextureedit.parameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;

import engine.parameters.IntParam;

public class IntParameterEditor extends AbstractParameterEditor implements ActionListener, FocusListener {
    private static final long serialVersionUID = -4292786478673916788L;
    IntParam param;
    // JFormattedTextField inputField;
    JSpinner inputField;

    public IntParameterEditor(IntParam p) {
        super();
        param = p;
        int x = 0;
        int y = 0;

        JLabel nameLabel = new JLabel(p.getName() + ":");
        nameLabel.setBounds(x, y, NAME_WIDTH, h);
        x += NAME_WIDTH;
        add(nameLabel);

        // TODO: Add a JSlider too, maybe?
        inputField = new JSpinner(new SpinnerNumberModel(param.get(), -100, 100, 1));
        inputField.setBounds(x, y, TEXTFIELD_WIDTH, h);
        inputField.addFocusListener(this);
        add(inputField);

        JSpinner.NumberEditor numberEditor = (JSpinner.NumberEditor) inputField.getEditor();
        var formattedTextField = numberEditor.getTextField();
        var formatter = (DefaultFormatter) formattedTextField.getFormatter();
        formatter.setCommitsOnValidEdit(true);

        inputField.addChangeListener(e -> param.set((Integer) inputField.getValue()));
    }

    void checkAndApplyChange() {
        try {
            // String txt = inputField.getText();
            // int val = (Integer.parseInt(txt));

            try {
                inputField.commitEdit();
            }
            catch (ParseException ignored) {
            }

            param.set((Integer) inputField.getValue());
        }
        catch (NumberFormatException ignored) {
        }
        // int pos = inputField.getCaretPosition();
        // inputField.setValue(param.get());
        // inputField.setCaretPosition(pos);

    }


    public void actionPerformed(ActionEvent e) {
        checkAndApplyChange();
    }

    public void focusGained(FocusEvent arg0) {
        //inputField.setCaretPosition(lastCaretPosition);
    }

    public void focusLost(FocusEvent arg0) {
        //lastCaretPosition = inputField.getCaretPosition();
        checkAndApplyChange();
    }
}

