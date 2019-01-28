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

import com.mystictri.neotextureedit.TextureEditor;
import engine.base.FMath;
import engine.parameters.ColorGradientParam;
import engine.parameters.EnumParam;
import org.joml.Vector4f;


public final class PatternGradient extends Pattern {
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");
	EnumParam gradientType = CreateLocalEnumParam("Type", "Linear,Radial,Square");
	
	public String getName() {
		return "Gradient";
	}
	
	public String getHelpText() {
		return "Simple gradient. Linear is chosen this way to be tilable.";
	}
	
	
	public PatternGradient() {
	}

	protected Vector4f _valueRGBA(float u, float v) {
		float pos = 0.0f;
		
		if (gradientType.getEnumPos() == 0) { // Linear
			if (v <= 0.5f) pos = (v*2.0f);
			else pos = ((1.0f-v)*2.0f);
		} else if (gradientType.getEnumPos() == 1) { // Radial
			float x = 0.5f - u;
			float y = 0.5f - v;
			pos = 2.0f * (0.5f - FMath.sqrt(x*x + y*y));
		} else if (gradientType.getEnumPos() == 2) { // Square
			pos =  1.0f - 2.0f*Math.max(FMath.abs(0.5f - u), FMath.abs(0.5f - v));
		} else {
			TextureEditor.logger.error("Invalid gradientType");
		}
		
		return colorGradientParam.get().getColor(pos);
	}
}
