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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import engine.base.FMath;
import engine.base.Utils;
import engine.parameters.ColorGradient;
import engine.parameters.ColorGradientParam;
import org.joml.Vector4f;

public class GradientEditorPanel extends JPanel implements MouseMotionListener, MouseListener, ActionListener {
	private static final long serialVersionUID = 5037538191801103584L;
	ColorGradient m_ActiveGradient;

	public static ColorChooserDialog colorChooser;

	BufferedImage gradientImage;
	
	static final ColorGradient ms_CopiedColorGradient = new ColorGradient();

	int dragDX = 0;
	int dragDY = 0;
	Point mousePosition;
	ColorMarker dragMarker = null;
	boolean alphaDrag = false;
	boolean allDrag = false;
	boolean actuallyDragged = false; // used to avoid notifying the channel of
	// changes when non happened
	float alphaDragStartValue = 0.0f;
	int dragStopUpX = -1;
	int dragStopDownX = -1;

	JPopupMenu m_PopUp;

	static final int gradientImagePosX = 16;
	static final int gradientImagePosY = 32;

	ColorGradientParam m_ColorGradientParam;

	static class GradientPreset {
		ColorGradient gradient;
		BufferedImage img;
		ImageIcon preview;
		int previewX = 128;
		int previewY = 16;

		public GradientPreset(ColorGradient g) {
			gradient = g;
			img = createGradientImage(previewX, previewY, g, null);
			preview = new ImageIcon(img);
		}

		public void updateGradient(ColorGradient g) {
			gradient.setFrom(g);
			preview.setImage(createGradientImage(previewX, previewY, gradient, img));
		}
	}

	static Vector<GradientPreset> ms_Presets = new Vector<GradientPreset>();

	static BufferedImage createGradientImage(int xres, int yres, ColorGradient grad, BufferedImage img) {
		if (img == null)
			img = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				float pos = (float) x / (float) img.getWidth();
				Vector4f col = grad.getColor(pos);
				float bg = ((((x + y) / 8) % 2) != 0) ? 1.0f : 0.75f;
				col.x = col.x * col.w + bg * (1.0f - col.w);
				col.y = col.y * col.w + bg * (1.0f - col.w);
				col.z = col.z * col.w + bg * (1.0f - col.w);
				img.setRGB(x, y, Utils.floatRGBToINTColor(col.x, col.y, col.z));
			}
		}
		return img;
	}

	static {
		TextureEditor.logger.info("Creating Gradient Presets");
		// the first entry gets overwritten with the old gradient in case
		// someone wants to use it;
		ms_Presets.add(new GradientPreset(new ColorGradient().addEntryRGB(0f, 0f, 0f, 0f).addEntryRGB(0f, 0f, 0f, 0f)));
		//ms_Presets.add(new GradientPreset(new ColorGradient().addEntryRGB(0f, 0f, 0f, 0f).addEntryRGB(1f, 1f, 1f, 1f)));
		//ms_Presets.add(new GradientPreset(new ColorGradient().addEntryRGB(1f, 0f, 0f, 0f).addEntryRGB(0f, 1f, 1f, 1f)));
		
		ms_Presets.add(new GradientPreset(new ColorGradient().addEntryRGB(0f, 0f, 0f, 0f).addEntryRGB(1f, 1f, 1f, 1f)));
		// loosely based on the GIMP Presets
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.215635f,0.407414f,0.984953f,1.000000f),0.000000f) .addEntry(new Vector4f(0.040368f,0.833333f,0.619375f,1.000000f),0.572621f) .addEntry(new Vector4f(0.680490f,0.355264f,0.977430f,1.000000f),0.716194f) .addEntry(new Vector4f(0.553909f,0.351853f,0.977430f,1.000000f),0.749583f) .addEntry(new Vector4f(1.000000f,0.000000f,1.000000f,1.000000f),0.824708f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.876461f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.922731f,0.452483f,0.984953f,1.000000f),0.000000f) .addEntry(new Vector4f(0.122236f,0.319840f,0.583333f,1.000000f),0.570952f) .addEntry(new Vector4f(0.059646f,1.000000f,0.558369f,1.000000f),0.664441f) .addEntry(new Vector4f(0.969697f,0.948568f,0.533333f,1.000000f),0.756260f) .addEntry(new Vector4f(1.000000f,0.490000f,1.000000f,1.000000f),0.843072f) .addEntry(new Vector4f(0.238108f,0.191841f,1.000000f,1.000000f),0.949917f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.725647f,0.428066f,1.000000f),0.000000f) .addEntry(new Vector4f(0.115248f,0.249315f,0.651515f,1.000000f),0.435726f) .addEntry(new Vector4f(0.552948f,0.624658f,0.550758f,1.000000f),0.590985f) .addEntry(new Vector4f(0.990647f,1.000000f,0.450000f,1.000000f),0.799666f) .addEntry(new Vector4f(0.317635f,0.843781f,1.000000f,1.000000f),0.943239f) .addEntry(new Vector4f(0.000000f,1.000000f,0.000000f,1.000000f),0.979967f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.202999f,0.003788f,0.265152f,1.000000f),0.000000f) .addEntry(new Vector4f(0.300711f,0.001894f,0.393939f,1.000000f),0.235803f) .addEntry(new Vector4f(0.388992f,0.000947f,0.206459f,1.000000f),0.377501f) .addEntry(new Vector4f(0.689394f,0.000000f,0.027414f,1.000000f),0.437396f) .addEntry(new Vector4f(0.388992f,0.000947f,0.206459f,1.000000f),0.474124f) .addEntry(new Vector4f(0.300711f,0.001894f,0.393939f,1.000000f),0.534224f) .addEntry(new Vector4f(0.202999f,0.003788f,0.265152f,1.000000f),0.609349f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,0.000000f),0.770562f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.500000f,0.500000f,0.500000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.166667f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.250000f) .addEntry(new Vector4f(0.500000f,0.500000f,0.500000f,1.000000f),0.346689f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.425710f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.583333f) .addEntry(new Vector4f(0.416667f,0.416667f,0.416667f,1.000000f),0.671119f) .addEntry(new Vector4f(0.833333f,0.833333f,0.833333f,1.000000f),0.835003f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.916667f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.283820f,0.887055f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,1.000000f,0.631509f,1.000000f),0.565943f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.502504f) .addEntry(new Vector4f(0.890000f,0.329199f,0.177758f,1.000000f),0.637730f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.744574f) .addEntry(new Vector4f(0.910000f,0.574437f,0.000000f,1.000000f),0.786311f) .addEntry(new Vector4f(0.727273f,0.127938f,0.148370f,1.000000f),0.891486f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,0.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.502504f) .addEntry(new Vector4f(0.890000f,0.329199f,0.177758f,1.000000f),0.637730f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.744574f) .addEntry(new Vector4f(0.910000f,0.574437f,0.000000f,1.000000f),0.786311f) .addEntry(new Vector4f(0.727273f,0.127938f,0.148370f,0.000000f),0.891486f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.879999f,0.880000f,0.880000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.999999f,1.000000f,1.000000f,1.000000f),0.046745f) .addEntry(new Vector4f(0.909999f,0.910000f,0.910000f,1.000000f),0.126878f) .addEntry(new Vector4f(0.819999f,0.820000f,0.820000f,1.000000f),0.353923f) .addEntry(new Vector4f(0.903167f,1.000000f,0.000000f,1.000000f),0.472454f) .addEntry(new Vector4f(0.000000f,0.877893f,1.000000f,1.000000f),0.562604f) .addEntry(new Vector4f(0.384390f,1.000000f,0.900682f,1.000000f),0.621035f) .addEntry(new Vector4f(0.819999f,0.820000f,0.820000f,1.000000f),0.681135f) .addEntry(new Vector4f(0.879999f,0.880000f,0.880000f,1.000000f),0.747913f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.200000f,0.833333f,0.726927f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.988352f,0.860000f,1.000000f),0.567613f) .addEntry(new Vector4f(0.000000f,0.431818f,0.000000f,1.000000f),0.681135f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.047059f,0.023529f,0.137255f,1.000000f),0.000000f) .addEntry(new Vector4f(0.364706f,0.733333f,0.756863f,1.000000f),0.647746f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.088684f,0.075143f,0.174242f,1.000000f),0.101836f) .addEntry(new Vector4f(0.047059f,0.023529f,0.137255f,1.000000f),0.901503f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.840000f,0.840000f,0.840000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.333333f,0.333333f,0.333333f,1.000000f),0.238453f) .addEntry(new Vector4f(0.900000f,0.900000f,0.900000f,1.000000f),0.288815f) .addEntry(new Vector4f(0.333333f,0.333333f,0.333333f,1.000000f),0.388982f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.713411f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.878408f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.636364f,0.280000f,0.280000f,1.000000f),0.560935f) .addEntry(new Vector4f(1.000000f,0.895652f,0.840000f,1.000000f),0.774624f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.179032f,0.390004f,0.621212f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.969697f,0.969697f,1.000000f),0.764608f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.333333f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.666667f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.500000f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.333333f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.666667f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.500000f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.198883f,0.300000f,0.135000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.406805f,0.613636f,0.276136f,1.000000f),0.086811f) .addEntry(new Vector4f(0.324169f,0.454545f,0.114714f,1.000000f),0.250000f) .addEntry(new Vector4f(0.098346f,0.136364f,0.085909f,1.000000f),0.375000f) .addEntry(new Vector4f(0.196692f,0.272727f,0.171818f,1.000000f),0.500835f) .addEntry(new Vector4f(0.272727f,0.214990f,0.203431f,1.000000f),0.577629f) .addEntry(new Vector4f(0.430776f,0.666667f,0.326636f,1.000000f),0.657763f) .addEntry(new Vector4f(0.369390f,0.420000f,0.344400f,1.000000f),0.757930f) .addEntry(new Vector4f(0.091517f,0.260000f,0.000000f,1.000000f),0.839733f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.984314f,0.984314f,1.000000f),0.000000f) .addEntry(new Vector4f(0.258824f,0.121569f,0.035294f,1.000000f),0.532554f) .addEntry(new Vector4f(1.000000f,0.811765f,0.549020f,1.000000f),0.555927f) .addEntry(new Vector4f(0.349020f,0.160784f,0.058824f,1.000000f),0.612688f) .addEntry(new Vector4f(1.000000f,0.556863f,0.219608f,1.000000f),0.948247f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.370303f,0.628966f,0.787879f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.984314f,0.984314f,1.000000f),0.348915f) .addEntry(new Vector4f(0.039048f,0.132980f,0.265152f,1.000000f),0.532554f) .addEntry(new Vector4f(0.522500f,0.823569f,0.950000f,1.000000f),0.555927f) .addEntry(new Vector4f(0.087500f,0.131053f,0.250000f,1.000000f),0.612688f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.729412f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.545098f,0.196078f,1.000000f),0.594324f) .addEntry(new Vector4f(0.972549f,0.937255f,0.074510f,1.000000f),0.809683f) .addEntry(new Vector4f(0.976471f,0.968627f,0.831373f,1.000000f),0.899833f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.984314f,0.984314f,1.000000f),0.000000f) .addEntry(new Vector4f(0.258824f,0.121569f,0.035294f,1.000000f),0.532554f) .addEntry(new Vector4f(0.378491f,0.689394f,0.398544f,1.000000f),0.555927f) .addEntry(new Vector4f(0.058824f,0.349020f,0.114977f,1.000000f),0.612688f) .addEntry(new Vector4f(0.111468f,0.507576f,0.162566f,1.000000f),0.948247f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.560606f,0.560606f,0.560606f,1.000000f),0.000000f) .addEntry(new Vector4f(0.083243f,0.462121f,0.112054f,1.000000f),0.166945f) .addEntry(new Vector4f(1.000000f,0.988739f,0.298904f,1.000000f),0.265442f) .addEntry(new Vector4f(0.529502f,0.586235f,1.000000f,1.000000f),0.390651f) .addEntry(new Vector4f(0.019021f,0.108157f,0.590909f,1.000000f),0.459098f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.142500f,0.168750f,0.195000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.317326f,0.344269f,0.371212f,1.000000f),0.096828f) .addEntry(new Vector4f(0.425000f,0.357284f,0.327250f,1.000000f),0.220367f) .addEntry(new Vector4f(0.271322f,0.389264f,0.500000f,1.000000f),0.333890f) .addEntry(new Vector4f(0.592000f,0.632258f,0.800000f,1.000000f),0.423205f) .addEntry(new Vector4f(0.931818f,0.906382f,0.782727f,1.000000f),0.574290f) .addEntry(new Vector4f(0.841294f,0.844890f,0.848485f,1.000000f),0.742905f) .addEntry(new Vector4f(0.581685f,0.611610f,0.674242f,1.000000f),0.859766f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,1.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.333333f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.666667f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.500000f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,1.000000f,0.933333f,0.901961f),0.000000f) .addEntry(new Vector4f(0.827451f,1.000000f,0.988235f,1.000000f),0.699499f) .addEntry(new Vector4f(0.000000f,1.000000f,0.913725f,0.901961f),0.774624f) .addEntry(new Vector4f(0.000000f,1.000000f,0.933333f,0.000000f),0.849750f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.129412f,1.000000f,0.000000f,0.901961f),0.000000f) .addEntry(new Vector4f(0.823529f,1.000000f,0.807843f,1.000000f),0.699499f) .addEntry(new Vector4f(0.196078f,1.000000f,0.000000f,0.901961f),0.774624f) .addEntry(new Vector4f(0.031373f,1.000000f,0.000000f,0.000000f),0.849750f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.933333f,0.000000f,0.901961f),0.000000f) .addEntry(new Vector4f(0.996078f,1.000000f,0.819608f,1.000000f),0.699499f) .addEntry(new Vector4f(1.000000f,0.949020f,0.000000f,0.901961f),0.774624f) .addEntry(new Vector4f(1.000000f,0.949020f,0.000000f,0.000000f),0.849750f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.514411f,0.279242f,0.734848f,1.000000f),0.000000f) .addEntry(new Vector4f(0.604602f,0.331500f,0.650000f,1.000000f),0.098497f) .addEntry(new Vector4f(0.200503f,0.169888f,0.393939f,1.000000f),0.228715f) .addEntry(new Vector4f(0.500537f,0.323300f,0.530000f,1.000000f),0.404006f) .addEntry(new Vector4f(0.600648f,0.445741f,0.681667f,1.000000f),0.544241f) .addEntry(new Vector4f(0.700758f,0.568182f,0.833333f,1.000000f),0.713283f) .addEntry(new Vector4f(0.184745f,0.149793f,0.219697f,1.000000f),0.819699f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.015686f,0.376471f,1.000000f),0.198664f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.492487f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,1.000000f),0.787980f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,0.000000f),0.996661f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.298039f,0.227451f,0.000000f,1.000000f),0.198664f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.492487f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,1.000000f),0.787980f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,0.000000f),0.996661f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.356863f,0.094118f,1.000000f),0.198664f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.492487f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,1.000000f),0.787980f) .addEntry(new Vector4f(1.000000f,0.619608f,0.619608f,0.000000f),0.996661f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.737255f,0.737255f,0.306122f),0.000000f) .addEntry(new Vector4f(1.000000f,0.636060f,0.636060f,0.306122f),0.350584f) .addEntry(new Vector4f(1.000000f,0.517647f,0.517647f,0.605442f),0.400668f) .addEntry(new Vector4f(0.988235f,0.501961f,0.501961f,0.306122f),0.421223f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,0.000000f),0.440735f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.113725f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.000000f,0.047059f,1.000000f),0.699499f) .addEntry(new Vector4f(1.000000f,0.431373f,0.000000f,0.000000f),0.849750f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.333333f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.666667f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.000000f,0.000000f,1.000000f),0.500000f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.833333f,0.833333f,0.833333f,1.000000f),0.440678f) .addEntry(new Vector4f(0.666667f,0.666667f,0.666667f,1.000000f),0.533898f) .addEntry(new Vector4f(0.500000f,0.500000f,0.500000f,1.000000f),0.627119f) .addEntry(new Vector4f(0.333333f,0.333333f,0.333333f,1.000000f),0.720339f) .addEntry(new Vector4f(0.166667f,0.166667f,0.166667f,1.000000f),0.813559f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.906780f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.643939f,0.311237f,0.107323f,1.000000f),0.000000f) .addEntry(new Vector4f(0.871212f,0.820000f,0.820000f,1.000000f),0.346912f) .addEntry(new Vector4f(0.340000f,0.128091f,0.107291f,1.000000f),0.538564f) .addEntry(new Vector4f(1.000000f,0.521990f,0.220000f,1.000000f),0.652755f) .addEntry(new Vector4f(0.204545f,0.121376f,0.046011f,1.000000f),0.777963f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.940000f,0.865112f,0.817800f,1.000000f),0.000000f) .addEntry(new Vector4f(0.380000f,0.328734f,0.231331f,1.000000f),0.126878f) .addEntry(new Vector4f(0.553030f,0.482304f,0.336911f,1.000000f),0.499165f) .addEntry(new Vector4f(0.007576f,0.007576f,0.007576f,1.000000f),0.739566f) .addEntry(new Vector4f(0.659091f,0.659091f,0.659091f,1.000000f),0.854758f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.492424f,0.303700f,0.136994f,1.000000f),0.000000f) .addEntry(new Vector4f(0.880909f,0.584348f,0.509475f,1.000000f),0.297162f) .addEntry(new Vector4f(0.969697f,0.673909f,0.000000f,1.000000f),0.365609f) .addEntry(new Vector4f(0.857935f,0.931818f,0.026245f,1.000000f),0.549040f) .addEntry(new Vector4f(0.590000f,0.660000f,0.020000f,1.000000f),0.611540f) .addEntry(new Vector4f(0.030000f,0.050000f,0.220000f,1.000000f),0.699499f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.109804f,0.066667f,0.568627f,1.000000f),0.000000f) .addEntry(new Vector4f(0.917647f,0.043137f,0.043137f,1.000000f),0.365609f) .addEntry(new Vector4f(1.000000f,0.533333f,0.000000f,1.000000f),0.749583f) .addEntry(new Vector4f(0.937255f,0.925490f,0.215686f,1.000000f),0.864775f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.934891f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.227451f,0.207843f,0.486275f,1.000000f),0.000000f) .addEntry(new Vector4f(0.709804f,0.098039f,0.098039f,1.000000f),0.365609f) .addEntry(new Vector4f(0.819608f,0.505882f,0.270588f,1.000000f),0.749583f) .addEntry(new Vector4f(0.800000f,0.784314f,0.564706f,1.000000f),0.864775f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.934891f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.348485f,0.187023f,0.027070f,0.000000f),0.000000f) .addEntry(new Vector4f(0.598485f,0.321191f,0.046490f,1.000000f),0.797162f) .addEntry(new Vector4f(0.757576f,0.430331f,0.081217f,1.000000f),0.830551f) .addEntry(new Vector4f(1.000000f,0.681967f,0.420000f,1.000000f),0.929883f) .addEntry(new Vector4f(0.757576f,0.424126f,0.068392f,1.000000f),0.949647f) .addEntry(new Vector4f(0.481061f,0.266950f,0.042141f,1.000000f),0.976169f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.948165f,0.969697f,0.812122f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.552632f,0.270000f,1.000000f),0.203595f) .addEntry(new Vector4f(0.581721f,0.096155f,0.170043f,1.000000f),0.487479f) .addEntry(new Vector4f(0.287879f,0.155229f,0.049835f,1.000000f),0.529137f) .addEntry(new Vector4f(0.336000f,0.425966f,0.800000f,1.000000f),0.562604f) .addEntry(new Vector4f(0.852165f,0.985930f,1.000000f,1.000000f),0.697830f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.166667f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.333333f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.500000f) .addEntry(new Vector4f(0.833333f,0.833333f,0.833333f,1.000000f),0.666667f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.833333f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.007899f,0.310606f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.195893f,0.575758f,0.085655f,1.000000f),0.085142f) .addEntry(new Vector4f(0.924242f,0.750598f,0.192395f,1.000000f),0.193656f) .addEntry(new Vector4f(0.954545f,0.239854f,0.132221f,1.000000f),0.276572f) .addEntry(new Vector4f(0.530303f,0.319349f,0.236012f,1.000000f),0.387683f) .addEntry(new Vector4f(0.472649f,0.295792f,1.000000f,1.000000f),0.555556f) .addEntry(new Vector4f(0.644153f,1.000000f,0.957743f,1.000000f),0.666667f) .addEntry(new Vector4f(0.408723f,0.870000f,0.278400f,1.000000f),0.826377f) .addEntry(new Vector4f(0.363558f,0.500000f,0.000000f,1.000000f),0.884808f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,0.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.378965f) .addEntry(new Vector4f(0.204545f,0.047339f,0.018466f,1.000000f),0.721202f) .addEntry(new Vector4f(0.757576f,0.175329f,0.068392f,1.000000f),0.776294f) .addEntry(new Vector4f(1.000000f,0.501132f,0.420000f,1.000000f),0.859766f) .addEntry(new Vector4f(0.757576f,0.175329f,0.068392f,1.000000f),0.893155f) .addEntry(new Vector4f(0.757576f,0.175329f,0.068392f,0.000000f),0.928214f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.920000f,0.579600f,0.239200f,1.000000f),0.000000f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.555927f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,0.000000f),0.981636f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.944844f,0.616991f,0.289137f,1.000000f),0.000000f) .addEntry(new Vector4f(0.928860f,0.592934f,0.257008f,1.000000f),0.138982f) .addEntry(new Vector4f(0.922120f,0.582791f,0.243462f,1.000000f),0.277963f) .addEntry(new Vector4f(0.920000f,0.579600f,0.239200f,1.000000f),0.416945f) .addEntry(new Vector4f(0.903086f,0.568944f,0.234802f,1.000000f),0.555927f) .addEntry(new Vector4f(0.850329f,0.535708f,0.221086f,1.000000f),0.662354f) .addEntry(new Vector4f(0.708598f,0.446417f,0.184235f,1.000000f),0.768781f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,1.000000f),0.875209f) .addEntry(new Vector4f(0.000000f,0.000000f,0.000000f,0.000000f),0.981636f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(0.969697f,0.639354f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(1.000000f,0.988712f,0.000000f,1.000000f),0.161937f) .addEntry(new Vector4f(0.166667f,0.157690f,0.000000f,1.000000f),0.247078f) .addEntry(new Vector4f(0.007576f,0.007576f,0.007576f,1.000000f),0.287145f) .addEntry(new Vector4f(1.000000f,1.000000f,1.000000f,1.000000f),0.363940f) .addEntry(new Vector4f(0.857143f,0.843870f,0.030851f,1.000000f),0.592654f) .addEntry(new Vector4f(0.333333f,0.194690f,0.180622f,1.000000f),0.803005f) .renormalize()));
		ms_Presets.add(new GradientPreset(new ColorGradient() .addEntry(new Vector4f(1.000000f,0.534703f,0.000000f,1.000000f),0.000000f) .addEntry(new Vector4f(0.901515f,0.428701f,0.114311f,1.000000f),0.565943f) .renormalize()));

}

	static class ColorMarker {
		public Color color;
		public float pos;
		int x, y, width2, height2;
		int index;

		float alpha;

		public ColorMarker(Color c, float p, int x, int y, int width2, int height2, int gradIndex, float alpha) {
			pos = p;
			color = c;
			this.x = x;
			this.y = y;
			this.width2 = width2;
			this.height2 = height2;
			index = gradIndex;
			this.alpha = alpha;
		}

		public boolean inside(int px, int py) {
			// System.out.println(px + " " + py);
			return ((px >= (x - width2)) && (px <= (x + width2)) && (py >= (y - height2)) && (py <= (y + height2)));
		}

	}

	Vector<ColorMarker> markers = new Vector<ColorMarker>();

	public GradientEditorPanel(String name) {
		setPreferredSize(new Dimension(256-16, 64+16));
		setSize(getPreferredSize());
		setLayout(null);
		setBorder(BorderFactory.createTitledBorder(name));
		addMouseListener(this);
		addMouseMotionListener(this);

		gradientImage = new BufferedImage(256 - 16 - 32, 24, BufferedImage.TYPE_INT_RGB);

		createPopup();
	}

	public void createPopup() {
		m_PopUp = new JPopupMenu();

		createPopupMenuItem("Copy").setEnabled(true);
		createPopupMenuItem("Paste").setEnabled(true);
		
		// Adding the presets popup menus
		for (int k = 0; k < ms_Presets.size()/16; k++) {
			JMenu presets = new JMenu("Presets "+k);
			for (int i = k*16; i < Math.min(ms_Presets.size(), (k+1)*16); i++) {
				JMenuItem itm = new JMenuItem(ms_Presets.get(i).preview);
				itm.addActionListener(this);
				presets.add(itm);
				itm.setActionCommand("Preset "+i);
				if (i == 0) presets.addSeparator();
			}
			m_PopUp.add(presets);
		}		

		createPopupMenuItem("Add to Presets").setEnabled(false);
		createPopupMenuItem("Reverse");
		createPopupMenuItem("Darken All");
		createPopupMenuItem("Brighten All");
		createPopupMenuItem("Random");
	}
	
	JMenuItem createPopupMenuItem(String name) {
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(this);
		m_PopUp.add(item);
		return item;
	}
	
	

	public GradientEditorPanel(ColorGradientParam param) {
		this(param.getName());
		m_ColorGradientParam = param;
		if (m_ColorGradientParam != null)
			setColorGradient(m_ColorGradientParam.get());
	}

	public void refreshGradientImage() {
		for (int y = 0; y < gradientImage.getHeight(); y++) {
			for (int x = 0; x < gradientImage.getWidth(); x++) {
				float pos = (float) x / (float) gradientImage.getWidth();

				Vector4f col = m_ActiveGradient.getColor(pos);

				if (y < 8) { // convert to grayscale preview
					float c = (col.x + col.y + col.z) * (1.0f / 3.0f);
					col.x = col.y = col.z = c;
				} else { // if (y < 16) { // color with alpha preview
					float bg = ((((x + y) / 8) % 2) != 0) ? 1.0f : 0.75f;
					col.x = col.x * col.w + bg * (1.0f - col.w);
					col.y = col.y * col.w + bg * (1.0f - col.w);
					col.z = col.z * col.w + bg * (1.0f - col.w);
				}

				gradientImage.setRGB(x, y, Utils.floatRGBToINTColor(col.x, col.y, col.z));
			}
		}
	}

	private final Color vec4toColor(Vector4f vec) {
		return new Color(vec.x, vec.y, vec.z);
	}

	public void paint(Graphics g) {
		super.paint(g);

		g.drawImage(gradientImage, gradientImagePosX, gradientImagePosY, this);

		for (int i = 0; i < markers.size(); i++) {
			ColorMarker m = markers.get(i);
			g.setColor(m.color);
			g.fillRect(m.x - m.width2, m.y - m.height2, m.width2 * 2, m.height2 * 2);
		}
		if (dragMarker != null) {
			if (alphaDrag) {
				g.setColor(Color.white);
				g.drawString("" + dragMarker.alpha, dragMarker.x - 8, dragMarker.y - 8);
				g.setColor(Color.black);
				g.drawString("" + dragMarker.alpha, dragMarker.x - 9, dragMarker.y - 9);
			}
		}
	}

	private void addMarker(float pos, Color c, int idx, float alpha) {
		ColorMarker m = new ColorMarker(c, pos, gradientImagePosX + (int) (pos * (gradientImage.getWidth() - 1)), gradientImagePosY
				+ gradientImage.getHeight() + 4, 4, 8, idx, alpha);
		markers.add(m);
		/*
		 * JLabel mark = new JLabel(); mark.setBackground(c); mark.setBounds(x,
		 * y, 8, 8); add(mark);
		 */
	}

	public void setColorGradient(ColorGradient g) {
		m_ActiveGradient = g;
		markers.clear();
		for (int i = 0; i < g.getNumEntries(); i++) {
			addMarker(g.getEntryPosition(i), vec4toColor(g.getEntryColor(i)), i, g.getEntryColor(i).w);
		}
		refreshGradientImage();
		repaint();
	}

	float getGradPosFromDrawingPosX(int x) {
		return (float) (x - gradientImagePosX) / (float) (gradientImage.getWidth() - 1);
	}

	public void mouseDragged(MouseEvent e) {
		if (dragMarker != null) {
			actuallyDragged = true;
			if (alphaDrag) {
				float valChange = -(dragMarker.x - (e.getX() + dragDX)) / 128.0f;
				// float valChange = (dragMarker.y - (e.getY() +
				// dragDY))/128.0f;
				dragMarker.alpha = alphaDragStartValue + valChange;
				if (dragMarker.alpha > 1.0f)
					dragMarker.alpha = 1.0f;
				if (dragMarker.alpha < 0.0f)
					dragMarker.alpha = 0.0f;
				m_ActiveGradient.updateAlpha(dragMarker.index, dragMarker.alpha);
				refreshGradientImage();
			} else if (allDrag) {
				float dx = -(dragMarker.x - (e.getX() + dragDX)) / (float)(2*(gradientImage.getWidth() - 1));
				//System.out.println(dx);
				for (int i = 0; i < m_ActiveGradient.getNumEntries(); i++) {
					float pos = m_ActiveGradient.getEntryPosition(i);
					if (pos > 0 && pos < 1) {
						pos = pos + dx;
						if (pos < 0) pos = 0.0f;
						if (pos > 1) pos = 1.0f;
						markers.get(i).x = (int)(gradientImagePosX + pos * (gradientImage.getWidth() - 1));
						m_ActiveGradient.updatePosition(i, pos);
					}
				}
				refreshGradientImage();
			} else {
				dragMarker.x = e.getX() + dragDX;
				if (dragMarker.x < dragStopUpX)
					dragMarker.x = dragStopUpX;
				if (dragMarker.x > dragStopDownX)
					dragMarker.x = dragStopDownX;

				float pos = getGradPosFromDrawingPosX(dragMarker.x);
				m_ActiveGradient.updatePosition(dragMarker.index, pos);
				refreshGradientImage();
			}
		}
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
		mousePosition = e.getPoint();

		for (int i = markers.size()-1; i >= 0; i--) {
			ColorMarker m = markers.get(i);
			if (m.inside(mousePosition.x, mousePosition.y)) {
				if (e.getButton() == 3) { // right click => delete marker
					if (m_ActiveGradient.getNumEntries() > 2) {
						m_ActiveGradient.deleteEntry(m.index);
						markers.remove(m);
						setColorGradient(m_ActiveGradient);
						m_ColorGradientParam.notifyParamChangeListener();
						return;
					}
				} else if (e.getClickCount() == 2) { // double click is color
					// chooser:
					Color c = colorChooser.getColorSelection(m.color);
					m_ActiveGradient.updateColorRGB(m.index, c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
					if ((c.getRed() != m.color.getRed()) || (c.getGreen() != m.color.getGreen()) || (c.getBlue() != m.color.getBlue()))
						m_ColorGradientParam.notifyParamChangeListener();
					m.color = c;
					refreshGradientImage();
					repaint();
					return;
				} else {
					dragMarker = m;
					dragDX = m.x - mousePosition.x;
					dragDY = m.y - mousePosition.y;

					// set the bounds for dragging// assumes that there are
					// always >= 2 entries in the gradient
					if (m.index == 0) {
						dragStopUpX = gradientImagePosX;
						dragStopDownX = (gradientImagePosX + (int) (m_ActiveGradient.getEntryPosition(m.index + 1) * (gradientImage
								.getWidth() - 1))) - 1;
					} else if (m.index == m_ActiveGradient.getNumEntries() - 1) {
						dragStopUpX = (gradientImagePosX + (int) (m_ActiveGradient.getEntryPosition(m.index - 1) * (gradientImage.getWidth() - 1))) + 1;
						dragStopDownX = (gradientImagePosX + gradientImage.getWidth()) - 1;
					} else {
						dragStopUpX = (gradientImagePosX + (int) (m_ActiveGradient.getEntryPosition(m.index - 1) * (gradientImage.getWidth() - 1))) + 1;
						dragStopDownX = (gradientImagePosX + (int) (m_ActiveGradient.getEntryPosition(m.index + 1) * (gradientImage
								.getWidth() - 1))) - 1;
					}

					if (e.isShiftDown()) {
						alphaDrag = true;
						alphaDragStartValue = m.alpha;
					} else if (e.isControlDown()){
						allDrag = true;
					}
					repaint();
					return;
				}
			}
		}
		// no marker clicked:

		if (e.getButton() == 3) { // right click
			// if (e.isPopupTrigger()) {
			m_PopUp.show(e.getComponent(), e.getX(), e.getY());
			// }
		} else if (e.getClickCount() == 2) { // double click => insert
			if ((e.getX() >= gradientImagePosX) && (e.getX() < (gradientImagePosX + gradientImage.getWidth()))) { // inside the gradient image
				// (y-wise)
				float pos = getGradPosFromDrawingPosX(e.getX());
				m_ActiveGradient.addEntry(m_ActiveGradient.getColor(pos), pos);
				setColorGradient(m_ActiveGradient);
				return;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (actuallyDragged) {
			m_ColorGradientParam.notifyParamChangeListener();
		}
		dragMarker = null;
		alphaDrag = false;
		allDrag = false;
		actuallyDragged = false;
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if (m_ActiveGradient == null) {
			TextureEditor.logger.warn("Popup Action without an active gradient");
			return;
		}
		
		if (command.equals("Reverse")) {
			ColorGradient temp = new ColorGradient();
			int num = m_ActiveGradient.getNumEntries();
			for (int i = 0; i < num; i++)
				temp.addEntry(m_ActiveGradient.getEntryColor(i), 1.0f - m_ActiveGradient.getEntryPosition(i));
			m_ActiveGradient.setFrom(temp);
			setColorGradient(m_ActiveGradient);
		} else if (command.equals("Random")) {
			ms_Presets.get(0).updateGradient(m_ActiveGradient); // remember the old one at the first position
			m_ActiveGradient.clear();
			int num = 4;
			for (int i = 0; i < num; i++) {
				m_ActiveGradient.addEntry(new Vector4f(FMath.random(), FMath.random(), FMath.random(), 1.0f), i/(float)(num-1));
			}
			setColorGradient(m_ActiveGradient);
		} else if (command.equals("Darken All")) {
			for (int i = 0; i < m_ActiveGradient.getNumEntries(); i++) {
				m_ActiveGradient.getEntryColor(i).mul(0.8f,0.8f,0.8f,1.0f);
				m_ActiveGradient.getEntryColor(i).min(new Vector4f(0.0f), new Vector4f(1.0f));
			}
		} else if (command.equals("Brighten All")) {
			for (int i = 0; i < m_ActiveGradient.getNumEntries(); i++) {
				m_ActiveGradient.getEntryColor(i).mul(1.25f,1.25f,1.25f,1.0f);
				m_ActiveGradient.getEntryColor(i).min(new Vector4f(0.0f), new Vector4f(1.0f));
			}
		} else if (command.startsWith("Preset ")) {
			int presetIndex = Integer.parseInt(command.substring(7));
			if (presetIndex >= 0) {
				if (presetIndex > 0) ms_Presets.get(0).updateGradient(m_ActiveGradient); // remember the old one at the first position
				m_ActiveGradient.setFrom(ms_Presets.get(presetIndex).gradient);
				setColorGradient(m_ActiveGradient);
			}
		} else if (command.equals("Copy")) {
			ms_CopiedColorGradient.setFrom(m_ActiveGradient);
			return; // no notify needed
		} else if (command.equals("Paste")) {
			m_ActiveGradient.setFrom(ms_CopiedColorGradient);
			setColorGradient(m_ActiveGradient);
		} else {
			TextureEditor.logger.warn("Got unknown action: " + command);
			return;
		}

		m_ColorGradientParam.notifyParamChangeListener();
	}

}
