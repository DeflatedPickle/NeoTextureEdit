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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import engine.parameters.FloatParam;

/**
 * This class is a (indirect) subclass of JPanel and creates a simple editor with a text field and
 * increment/decrement buttons to edit a FloatParam object. It uses the params set method to change
 * values.
 * @author Holger Dammertz
 *
 */
class FloatParameterEditor extends AbstractParameterEditor implements ActionListener, FocusListener {
	private static final long serialVersionUID = -7712009260279412308L;
	FloatParam param;
	JFormattedTextField inputField;

	public FloatParameterEditor(FloatParam p) {
		super();
		param = p;
		int x = 0;
		int y = 0;

		JLabel nameLabel = new JLabel(p.getName() + ":");
		nameLabel.setBounds(x, y, NAME_WIDTH, h);
		x += NAME_WIDTH;
		add(nameLabel);

		JButton decrement = new JButton("-");
		decrement.setBounds(x, y, BUTTON_WIDTH, h);
		x += BUTTON_WIDTH;
		decrement.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) param.increment(-param.getDefaultIncrement()*0.125f);
				else param.decrement();
				inputField.setValue(param.get());
			}
		});
		add(decrement);

		inputField = new JFormattedTextField(p.getAsString());
		inputField.setValue(param.get());
		inputField.addActionListener(this);
		inputField.addFocusListener(this);
		
		inputField.setBounds(x, y, TEXTFIELD_WIDTH, h);
		x += TEXTFIELD_WIDTH;
		add(inputField);

		JButton increment = new JButton("+");
		increment.setBounds(x, y, BUTTON_WIDTH, h);
		x += BUTTON_WIDTH;
		increment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) param.increment(param.getDefaultIncrement()*0.125f);
				else param.increment();
				inputField.setValue(param.get());
			}
		});
		add(increment);

	}

	public void changedUpdate(DocumentEvent e) {
		System.out.println("Changed Update");
	}

	void checkAndApplyChange() {
		try {
			String txt = inputField.getText();
			System.out.println("checkAndApplyChange: " + inputField.getValue());
			float val = (Float.parseFloat(txt));
			param.set(val);
		} catch (NumberFormatException nfe) {
		}
		int pos = inputField.getCaretPosition();
		inputField.setValue(param.get());
		inputField.setCaretPosition(pos);
	}

	public void actionPerformed(ActionEvent e) {
		checkAndApplyChange();
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		checkAndApplyChange();
	}
}
