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

public final class PatternTile extends Pattern {
	public String getName() {
		return "Tile";
	}
	
	public String getHelpText() {
		return "A simple square tile with an optional smooth border";
	}
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");
	
	FloatParam borderX;
	FloatParam borderY;
	FloatParam smooth;
	
	public PatternTile() {
		borderX = CreateLocalFloatParam("BorderX", 0.1f, 0.0f, 0.5f);
		borderX.setDefaultIncrement(0.025f);
		borderY = CreateLocalFloatParam("BorderY", 0.1f, 0.0f, 0.5f);
		borderY.setDefaultIncrement(0.025f);
		smooth = CreateLocalFloatParam("Smooth", 0.0f, 0.0f, 0.5f);
		smooth.setDefaultIncrement(0.025f);
	}

	protected Vector4 _valueRGBA(float u, float v) {
		if ((u < borderX.get()) || (u > (1.0f-borderX.get()))) return colorGradientParam.get().getColor(0.0f);
		if ((v < borderY.get()) || (v > (1.0f-borderY.get()))) return colorGradientParam.get().getColor(0.0f);
		
		float distU = Math.min(u - borderX.get(), 1.0f - u - borderX.get());
		float distV = Math.min(v - borderY.get(), 1.0f - v - borderY.get());
		float dist = Math.min(distU, distV);
		
		if (dist < smooth.get()) {
			return colorGradientParam.get().getColor(dist/smooth.get());
		}
		
		return colorGradientParam.get().getColor(1.0f);
		
		/*boolean white = (((int)((u*scaleX.get())*2.0) + ((int)((v*scaleY.get())*2.0)))&1)==0;
		if (white) return 1.0f;
		else return 0.0f;*/
	}
}
