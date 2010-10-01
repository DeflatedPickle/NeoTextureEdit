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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

class ColorChooserDialog extends JDialog {
	private static final long serialVersionUID = -6546658810976403236L;
	JButton okButton;
	JButton cancelButton;
	Color returnColor;
	JColorChooser chooser;
	
	ColorChooserDialog(JFrame parent) {
		super(parent);
		setModal(true);
		setLayout(new BorderLayout());
		chooser = new JColorChooser();
		add(chooser, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		//setLocation(MainWindow.getIntP("colorDialogX", 200), MainWindow.getIntP("colorDialogY", 200));
		
		
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				returnColor = chooser.getColor();
				setVisible(false);
			}
		});
		
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setVisible(false);
			}
		});
		
	}
	
	public Color getColorSelection(Color currentColor) {
		chooser.setColor(currentColor);
		returnColor = currentColor;
		pack();
		setVisible(true);
		return returnColor;
	}
}
