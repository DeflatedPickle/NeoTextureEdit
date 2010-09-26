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
import engine.parameters.ColorParam;

public final class PatternConstantColor extends Pattern {
	public String getName() {
		return "Constant Color";
	}
	
	public String getHelpText() {
		return "Provides a constant color output.";
	}
	
	
	ColorParam color;
	
	public PatternConstantColor() {
		color = CreateLocalColorParam("Color", 0.8f, 0.4f, 0.7f);
	}

	public PatternConstantColor(float r, float g, float b) {
		color = CreateLocalColorParam("Color", r, g, b);
	}

	protected Vector4 _valueRGBA(float u, float v) {
		return new Vector4(color.get(), 1.0f);
	}
	
}
