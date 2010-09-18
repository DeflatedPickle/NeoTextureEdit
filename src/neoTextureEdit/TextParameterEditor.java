package neoTextureEdit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import engine.parameters.TextParam;

public class TextParameterEditor extends AbstractParameterEditor implements ActionListener, FocusListener {
	private static final long serialVersionUID = 4440525057713494866L;
	TextParam param;
	JFormattedTextField inputField;

	public TextParameterEditor(TextParam p) {
		super();
		param = p;
		int x = 0;
		int y = 0;

		JLabel nameLabel = new JLabel(p.getName() + ":");
		nameLabel.setBounds(x, y, NAME_WIDTH, h);
		x += NAME_WIDTH;
		add(nameLabel);
	
		inputField = new JFormattedTextField(p.get());
		inputField.setValue(param.get());
		inputField.addActionListener(this);
		inputField.addFocusListener(this);
		
		inputField.setBounds(x, y, TEXTFIELD_WIDTH + 2*BUTTON_WIDTH, h);
		x += TEXTFIELD_WIDTH;
		add(inputField);
	}

	public void changedUpdate(DocumentEvent e) {
		System.out.println("Changed Update");
	}

	void checkAndApplyChange() {
		try {
			String txt = inputField.getText().trim();
			System.out.println("checkAndApplyChange: " + inputField.getValue());
			param.set(txt);
		} catch (NumberFormatException nfe) {
		}
		int pos = inputField.getCaretPosition();
		inputField.setValue(param.get());
		inputField.setCaretPosition(pos);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		checkAndApplyChange();
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		checkAndApplyChange();
	}

}
