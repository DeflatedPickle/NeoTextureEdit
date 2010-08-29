/**
    Copyright (C) 2010  Holger Dammertz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package neoTextureEdit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import engine.parameters.EnumParam;

public class EnumParameterEditor extends AbstractParameterEditor implements ActionListener {
	private static final long serialVersionUID = -6066554018302988954L;
	EnumParam param;
	JTextField inputField;
	JComboBox enumComboBox;
	
	public EnumParameterEditor(EnumParam p) {
		super();
		param = p;
		int x = 0;
		int y = 0;

		JLabel nameLabel = new JLabel(p.getName() + ":");
		nameLabel.setBounds(x, y, NAME_WIDTH, h);
		x += NAME_WIDTH;
		add(nameLabel);
		
		enumComboBox = new JComboBox();
		enumComboBox.setBounds(x, y, BUTTON_WIDTH*2 + TEXTFIELD_WIDTH, h); x += BUTTON_WIDTH*2 + TEXTFIELD_WIDTH;
		add(enumComboBox);
		for (int i = 0; i < param.getNumEnums(); i++) {
			enumComboBox.addItem(param.getEnumAt(i));
		}
		enumComboBox.setSelectedIndex(param.getEnumPos());
		enumComboBox.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		param.setEnumPos(enumComboBox.getSelectedIndex());
	}
}
