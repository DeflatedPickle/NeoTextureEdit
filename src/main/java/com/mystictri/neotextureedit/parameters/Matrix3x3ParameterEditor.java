/*
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

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;

import engine.base.FMath;
import engine.base.Matrix3x3;
import engine.parameters.Matrix3x3Param;

public class Matrix3x3ParameterEditor extends AbstractParameterEditor {
    private static final long serialVersionUID = -4845723469494814010L;
    private Matrix3x3Param mat;

    public Matrix3x3ParameterEditor(Matrix3x3Param matrixParam) {
        setPreferredSize(new Dimension(256 - 16, h * 4 + 8 + 32));
        setSize(getPreferredSize());
        setLayout(null);
        mat = matrixParam;

        setBorder(BorderFactory.createTitledBorder("Coordinate Transformation"));

        int x = 8;
        int y = 8 + 20;

        float rotAngle = FMath.deg2rad(45 / 4.0f);
        float translation = 0.125f / 2.0f;
        float scale = 2.0f;

        Matrix3x3 m = mat.getMatrix();

        var translationXLabel = new JLabel("Translation X:");
        translationXLabel.setBounds(x, y, 80, h);
        add(translationXLabel);

        x = 90;

        // This is needed to check if it was increased or decreased, and it's an AtomicReference as we update it in a lambda
        AtomicReference<Float> translationXValue = new AtomicReference<>(0f);
        var translationXSpinner = new JSpinner(new SpinnerNumberModel(translationXValue.get().floatValue(), -100f, 100f, 0.125f));
        translationXSpinner.setBounds(x, y, TEXTFIELD_WIDTH, h);
        translationXSpinner.addChangeListener(e -> {
            var spinner = (JSpinner) e.getSource();
            // Increases or decreases the translation along the X axis
            mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate(translation * ((Double) Math.signum((Double) spinner.getValue() - translationXValue.get())).floatValue(), 0f));
            translationXValue.set(((Double) spinner.getValue()).floatValue());

            mat.notifyParamChangeListener();
        });
        add(translationXSpinner);

        x = 8;
        y += h;

        var translationYLabel = new JLabel("Translation Y:");
        translationYLabel.setBounds(x, y, 80, h);
        add(translationYLabel);

        x = 90;

        // This is needed to check if it was increased or decreased, and it's an AtomicReference as we update it in a lambda
        AtomicReference<Float> translationYValue = new AtomicReference<>(0f);
        var translationYSpinner = new JSpinner(new SpinnerNumberModel(translationYValue.get().floatValue(), -100f, 100f, 0.125f));
        translationYSpinner.setBounds(x, y, TEXTFIELD_WIDTH, h);
        translationYSpinner.addChangeListener(e -> {
            var spinner = (JSpinner) e.getSource();
            // Increases or decreases the translation along the Y axis
            mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Translate(0f, translation * ((Double) Math.signum((Double) spinner.getValue() - translationYValue.get())).floatValue()));
            translationYValue.set(((Double) spinner.getValue()).floatValue());

            mat.notifyParamChangeListener();
        });
        add(translationYSpinner);

        x = 8;
        y += h;

        var rotationLabel = new JLabel("Rotation:");
        rotationLabel.setBounds(x, y, 80, h);
        add(rotationLabel);

        x = 90;

        // This is needed to check if it was increased or decreased, and it's an AtomicReference as we update it in a lambda
        AtomicReference<Float> rotationValue = new AtomicReference<>(0f);
        var rotationSpinner = new JSpinner(new SpinnerNumberModel(rotationValue.get().floatValue(), -100f, 100f, 0.125f));
        rotationSpinner.setBounds(x, y, TEXTFIELD_WIDTH, h);
        rotationSpinner.addChangeListener(e -> {
            var spinner = (JSpinner) e.getSource();
            // Increases or decreases the rotation
            mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Rotate(rotAngle * ((Double) Math.signum((Double) spinner.getValue() - rotationValue.get())).floatValue()));
            rotationValue.set(((Double) spinner.getValue()).floatValue());

            mat.notifyParamChangeListener();
        });
        add(rotationSpinner);

        x = 8;
        y += h;

        var zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(x, y, 80, h);
        add(zoomLabel);

        x = 90;

        // This is needed to check if it was increased or decreased, and it's an AtomicReference as we update it in a lambda
        AtomicReference<Float> zoomValue = new AtomicReference<>(0f);
        var zoomSpinner = new JSpinner(new SpinnerNumberModel(zoomValue.get().floatValue(), -100f, 100f, 0.125f));
        zoomSpinner.setBounds(x, y, TEXTFIELD_WIDTH, h);
        zoomSpinner.addChangeListener(e -> {
            var spinner = (JSpinner) e.getSource();
            // Increases or decreases the zoom
            if ((Double) spinner.getValue() > zoomValue.get()) {
                mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Scale(1f / scale, 1f / scale));
            }
            else {
                mat.getMatrix().mult_ip(Matrix3x3.Create2DHomogenous_Scale(scale, scale));
            }
            zoomValue.set(((Double) spinner.getValue()).floatValue());

            mat.notifyParamChangeListener();
        });
        add(zoomSpinner);
    }
}
