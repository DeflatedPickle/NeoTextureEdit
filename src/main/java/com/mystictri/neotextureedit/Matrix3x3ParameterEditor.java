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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.mystictri.neotextureedit.parameters.AbstractParameterEditor;
import engine.base.FMath;
import engine.base.Matrix3x3;
import engine.parameters.Matrix3x3Param;

class Matrix3x3ParameterEditor extends AbstractParameterEditor implements DocumentListener, ActionListener {
	private static final long serialVersionUID = -4845723469494814010L;
	Matrix3x3Param mat;
	final JTextField[] fields = new JTextField[9];
	
	JButton txInc, txDec, tyInc, tyDec, rotInc, rotDec, zoomInc, zoomDec, resetmat;
	
	static final int MAT_BUT_WIDTH = 32;
	
	boolean ignoreDocumentEvent = false; // some kind of hack to avoid notifying an event when updating all text fields at once after a matrix change through a button press

	public Matrix3x3ParameterEditor(Matrix3x3Param matrixParam) {
		setPreferredSize(new Dimension(256-16, h*3+8+32));
		setSize(getPreferredSize());
		setLayout(null);
		mat = matrixParam;
		
		setBorder(BorderFactory.createTitledBorder("Coordinate Transformation"));
		
		int x = 8;
		int y = 8+20;
		
		Matrix3x3 m = mat.getMatrix();
		for (int i = 0; i < 9; i++) {
			fields[i] = new JTextField(""+m.get(i));
			fields[i].setBounds(x, y, 40, h);
			add(fields[i]);
			fields[i].getDocument().addDocumentListener(this);
			if ((i%3)==2) {x = 8; y += h;}
			else x += 42;
		}
		
		y = 8+20;
		int startX = 3*42+8;
		
		x = startX;
		zoomInc = matTransformButton("z+", x, y); x += MAT_BUT_WIDTH;
		tyDec = matTransformButton("y-", x, y); x += MAT_BUT_WIDTH;
		zoomDec = matTransformButton("z-", x, y); x += MAT_BUT_WIDTH;
		x = startX; y+=h;
		txDec = matTransformButton("x-", x, y); x += MAT_BUT_WIDTH;
		resetmat = matTransformButton("I", x, y); x += MAT_BUT_WIDTH;
		txInc = matTransformButton("x+", x, y); x += MAT_BUT_WIDTH;
		x = startX; y+=h;
		rotDec = matTransformButton("r-", x, y); x += MAT_BUT_WIDTH;
		tyInc = matTransformButton("y+", x, y); x += MAT_BUT_WIDTH;
		rotInc = matTransformButton("r+", x, y); x += MAT_BUT_WIDTH;
		x = startX; y+=h;
	}
	
	private JButton matTransformButton(String name, int x, int y) {
		JButton ret = new JButton(name);
		ret.setBounds(x, y, MAT_BUT_WIDTH, h);
		ret.addActionListener(this);
		add(ret);
		return ret;
	}

	public void changedUpdate(DocumentEvent e) {
		//System.out.println(e);
	}

	public void insertUpdate(DocumentEvent e) {
		checkAndApplyChange(e.getDocument());
	}

	public void removeUpdate(DocumentEvent e) {
		checkAndApplyChange(e.getDocument());
	}
	
	void checkAndApplyChange(Document d) {
		if (ignoreDocumentEvent) return;
		float val = 1.0f;
		int idx = -1;
		for (int i = 0; i < 9; i++) if (d == fields[i].getDocument()) idx = i;
		if (idx == -1) System.err.println("ERROR in Matrix3x3ParameterEditor.checkAndApplyChange");
		try {
			String txt = d.getText(0, d.getLength());
			val = (Float.parseFloat(txt));
			mat.getMatrix().set(idx, val);
			mat.notifyParamChangeListener();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (NumberFormatException nfe) {
		}
	}
	
	void updateTextFieldEntries() {
		ignoreDocumentEvent = true;
		for (int i = 0; i < 9; i++) {
			fields[i].setText(String.format("%.4f",mat.getMatrix().get(i)));
			fields[i].setCaretPosition(0);
		}
		ignoreDocumentEvent = false;
		mat.notifyParamChangeListener();
	}
	

	/**
	 * Performs the matrix transformation when one of the transform buttons is pressed.
	 * Holding down shift reduces the amount of the transformation
	 */
	public void actionPerformed(ActionEvent e) {
		float rotAngle = FMath.deg2rad(45/4.0f);
		float translation = 0.125f/2.0f;
		float scale = 2.0f;
		
		if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			rotAngle = FMath.deg2rad(45/16.0f);
			translation = 0.125f/16.0f;
			scale = 1.125f;
		}
		
		if (e.getSource() == resetmat) mat.getMatrix().set(Matrix3x3.CreateIdentity());
		if (e.getSource() == txInc) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate( translation, 0.0f));
		if (e.getSource() == txDec) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate(-translation, 0.0f));
		if (e.getSource() == tyInc) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate(0.0f, translation));
		if (e.getSource() == tyDec) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate(0.0f, -translation));
		if (e.getSource() == zoomInc) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Scale(1.0f/scale, 1.0f/scale));
		if (e.getSource() == zoomDec) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Scale(scale, scale));
		if (e.getSource() == rotInc) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Rotate(rotAngle));
		if (e.getSource() == rotDec) mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Rotate(-rotAngle));
		
		
		updateTextFieldEntries();
	}
}
