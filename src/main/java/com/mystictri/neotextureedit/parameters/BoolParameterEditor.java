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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import engine.parameters.BoolParam;

public class BoolParameterEditor extends AbstractParameterEditor implements ActionListener {
    private static final long serialVersionUID = -6066554018301988954L;
    BoolParam param;
    JTextField inputField;
    JCheckBox checkBox;

    public BoolParameterEditor(BoolParam p) {
        super();
        param = p;
        int x = 0;
        int y = 0;

        JLabel nameLabel = new JLabel(p.getName() + ":");
        nameLabel.setBounds(x, y, NAME_WIDTH, h);
        x += NAME_WIDTH;
        add(nameLabel);

        checkBox = new JCheckBox();
        checkBox.setBounds(x, y, h, h);
        x += h;
        add(checkBox);
        checkBox.setSelected(param.get());
        checkBox.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        param.set(checkBox.isSelected());
    }
}
