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

package com.mystictri.neotextureedit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import engine.parameters.ColorParam;

class ColorParameterEditor extends AbstractParameterEditor implements ActionListener {
	private static final long serialVersionUID = 2186552287032705040L;
	ColorParam param;
	JButton colorButton;
	ColorChooserDialog chooser;

	public ColorParameterEditor(ColorParam p, ColorChooserDialog colorChooser) {
		super();
		param = p;
		chooser = colorChooser;

		int x = 0;
		int y = 0;

		JLabel nameLabel = new JLabel(p.getName() + ":");
		nameLabel.setBounds(x, y, NAME_WIDTH, h);
		x += NAME_WIDTH;
		add(nameLabel);

		colorButton = new JButton();
		colorButton.setBounds(x, y, BUTTON_WIDTH, h);
		x += BUTTON_WIDTH;
		colorButton.addActionListener(this);
		add(colorButton);

		colorButton.setBackground(new Color(param.get().x, param.get().y, param.get().z));
		colorButton.setForeground(new Color(param.get().x, param.get().y, param.get().z));
	}

	public void actionPerformed(ActionEvent e) {
		Color c = chooser.getColorSelection(new Color(param.get().x, param.get().y, param.get().z));
		colorButton.setBackground(c);
		colorButton.setForeground(c);
		param.set(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
	}
}
