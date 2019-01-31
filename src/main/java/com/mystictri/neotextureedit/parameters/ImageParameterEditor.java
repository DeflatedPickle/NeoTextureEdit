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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import com.mystictri.neotextureedit.TextureEditor;
import engine.parameters.ImageParam;

public class ImageParameterEditor extends AbstractParameterEditor implements
        ActionListener {
    private static final long serialVersionUID = -4192786178673946778L;
    ImageParam param;
    JButton loadButton;
    JButton reloadButton;

    public ImageParameterEditor(ImageParam p) {
        super();
        param = p;
        int x = 0;
        int y = 0;

        JLabel nameLabel = new JLabel(p.getName() + ":");
        nameLabel.setBounds(x, y, NAME_WIDTH, h);
        x += NAME_WIDTH;
        add(nameLabel);

        loadButton = new JButton();
        loadButton.setBounds(x, y, BUTTON_WIDTH + TEXTFIELD_WIDTH, h);
        x += BUTTON_WIDTH + TEXTFIELD_WIDTH;
        loadButton.addActionListener(this);
        add(loadButton);

        reloadButton = new JButton("R");
        reloadButton.setBounds(x, y, BUTTON_WIDTH, h);
        x += BUTTON_WIDTH;
        reloadButton.addActionListener(this);
        reloadButton.setToolTipText("Reload Image");
        add(reloadButton);

        setFileName(p.getImageFilename());
    }

    String getOnlyFilename(String filename) {
        if (filename.lastIndexOf('/') > 0) return filename.substring(filename.lastIndexOf('/') + 1);
        else if (filename.lastIndexOf('\\') > 0) return filename.substring(filename.lastIndexOf('\\') + 1);
        else return filename;
    }

    void setFileName(String filename) {
        if (param.loadImage(filename)) {
            loadButton.setText(getOnlyFilename(filename));
            loadButton.setToolTipText(filename);
        }
    }

    /*
     * void checkAndApplyChange() { try { String txt = inputField.getText(); int
     * val = (Integer.parseInt(txt)); param.set(val); } catch
     * (NumberFormatException nfe) { } int pos = inputField.getCaretPosition();
     * inputField.setValue(param.get()); inputField.setCaretPosition(pos);
     *
     * }
     */

    public void actionPerformed(ActionEvent e) {
        // checkAndApplyChange();
        if (e.getSource() == loadButton) {
            JFileChooser c = TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage;
            c.setDialogTitle("Open image ...");
            if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String name = c.getSelectedFile().getAbsolutePath();
                setFileName(name);
            }
        }
        else if (e.getSource() == reloadButton) {
            param.reloadeImage();
        }
    }
}
