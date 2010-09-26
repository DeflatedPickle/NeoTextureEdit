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

import engine.base.Vector4;
import engine.parameters.ColorGradientParam;
import engine.parameters.FloatParam;

public final class PatternChecker extends Pattern {
	public String getName() {
		return "Checker";
	}
	
	public String getHelpText() {
		return "A checkerboard pattern.";
	}
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");

	
	FloatParam scaleX;
	FloatParam scaleY;
	
	public PatternChecker() {
		scaleX = CreateLocalFloatParam("ScaleX", 1.0f, 0.0f, Float.MAX_VALUE);
		scaleY = CreateLocalFloatParam("ScaleY", 1.0f, 0.0f, Float.MAX_VALUE);
	}

	public PatternChecker(float sx, float sy) {
		this();
		scaleX.set(sx);
		scaleY.set(sy);
	}

	protected Vector4 _valueRGBA(float u, float v) {
		boolean white = (((int)((u*scaleX.get())*2.0) + ((int)((v*scaleY.get())*2.0)))&1)==0;
		if (white) return colorGradientParam.get().getColor(1.0f);
		else return colorGradientParam.get().getColor(0.0f);
	}
}
