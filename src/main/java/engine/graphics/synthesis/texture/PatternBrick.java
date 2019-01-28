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

package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.parameters.AbstractParam;
import engine.parameters.BoolParam;
import engine.parameters.ColorGradientParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;
import org.joml.Vector4f;

public final class PatternBrick extends Pattern {
	public String getName() {
		return "Brick";
	}

	float[] randomShifts;
	float[] randomColors;

	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");

	IntParam widthX;
	IntParam widthY;

	FloatParam shiftX;
	FloatParam shiftRandWeight;

	FloatParam holeX;
	FloatParam holeY;
	
	FloatParam smooth;
	
	BoolParam useRandomColor;

	public PatternBrick() {
		widthX = CreateLocalIntParam("NumX", 4, 1, Integer.MAX_VALUE);
		widthY = CreateLocalIntParam("NumY", 6, 1, Integer.MAX_VALUE);
		shiftX = CreateLocalFloatParam("Shift", 0.5f, 0.0f, 1.0f);
		shiftX.setDefaultIncrement(0.1f);
		shiftRandWeight = CreateLocalFloatParam("RandShift", 0.1f, 0.0f, 1.0f);
		shiftRandWeight.setDefaultIncrement(0.1f);
		holeX = CreateLocalFloatParam("GapX", 0.03f, 0.0f, Float.MAX_VALUE);
		holeX.setDefaultIncrement(0.01f);
		holeY = CreateLocalFloatParam("GapY", 0.03f, 0.0f, Float.MAX_VALUE);
		holeY.setDefaultIncrement(0.01f);
		smooth = CreateLocalFloatParam("Smooth", 0.05f, 0.0f, 0.5f);
		smooth.setDefaultIncrement(0.025f);
		
		useRandomColor = CreateLocalBoolParam("RandColor", false);

		createRandomShifts();
		createRandomColors();
	}

	public void parameterChanged(AbstractParam source) {
		if (source == null) {
			createRandomColors();
			createRandomShifts();
		}
		
		if ((source == widthY) || (source == widthX))
			createRandomColors();
		if (source == widthY)
			createRandomShifts();
		super.parameterChanged(source);
	}

	void createRandomColors() {
		randomColors = new float[widthY.get() * widthX.get()];
		FMath.setSeed(3123);
		for (int i = 0; i < randomColors.length; i++)
			randomColors[i] = FMath.random();
		
		
	}

	void createRandomShifts() {
		randomShifts = new float[widthY.get()];
		FMath.setSeed(31232);
		for (int i = 0; i < randomShifts.length; i++)
			randomShifts[i] = FMath.random();
	}

	protected Vector4f _valueRGBA(float u, float v) {
		// boolean white = (((int) ((u * scaleX.get()) * 2.0) + ((int) ((v *
		// scaleY.get()) * 2.0))) & 1) == 0;

		float normX = (widthX.get());
		float normY = (widthY.get());
		float uu = u * normX;
		float vv = v * normY;

		float y = vv - FMath.ffloor(vv);

		if (vv >= normY) vv -= normY;
		if (uu >= normX) uu -= normX;
		
		int by = FMath.ffloor(vv);

		if ((vv * 0.5f - FMath.ffloor(vv * 0.5f)) >= 0.5f)
			uu += shiftX.get();
		uu += randomShifts[by] * shiftRandWeight.get();
		
		if (uu >= normX) uu -= normX;

		int bx = FMath.ffloor(uu);

		float x = uu - FMath.ffloor(uu);

		float val = 1.0f;

		if (useRandomColor.get()) val = randomColors[bx + by * widthX.get()];

		boolean inside = true;
		if (holeX.get() > 0) inside &= (x > (holeX.get() * normX));
		if (holeY.get() > 0) inside &= (y > (holeY.get() * normY));

		if (inside) {
			float dist = Math.min(Math.min(x -holeX.get()*normX, 1 - x)/normX, Math.min(y - holeY.get()*normY, 1 - y)/normY);
			dist *= Math.min(normX, normY);
			
			if (dist < smooth.get()) {
				val = val * dist/smooth.get();
			}
		} else val = 0.0f;
		
		return colorGradientParam.get().getColor(val);
	}

}
