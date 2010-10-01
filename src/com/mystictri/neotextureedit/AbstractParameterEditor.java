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

import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * A baseclass for simple gui JPanels that edit parameter values stored in AbstractParam subclasses
 * @author Holger Dammertz
 *
 */
class AbstractParameterEditor extends JPanel {
	private static final long serialVersionUID = -7348635754519937992L;
	static int h = 24;
	
	public static int NAME_WIDTH = 80;
	public static int BUTTON_WIDTH = 48;
	public static int TEXTFIELD_WIDTH = 64;

	public AbstractParameterEditor() {
		setPreferredSize(new Dimension(NAME_WIDTH + 2*BUTTON_WIDTH + TEXTFIELD_WIDTH, h));
		setSize(getPreferredSize());
		setLayout(null);
	}
}
