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
import engine.parameters.BoolParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;

/**
 * This filter computes the output image as
 * Cout = Cin + (1-Cout.alpha) + Cout*Cout.alpha;
 * @author Holger Dammertz
 *
 */
public class FilterBlend extends Channel {
	
	EnumParam blendFunction = CreateLocalEnumParam("Layer Func.", "Normal,Multiply,Divide,Screen,Overlay,Dodge,Burn,Difference,Addition,Subtract");
	FloatParam opacity = CreateLocalFloatParam("Opacity", 1.0f, 0.0f, 1.0f).setDefaultIncrement(0.125f);
	BoolParam invertAlpha = CreateLocalBoolParam("Inv. Alpha", false);
	
	
	public String getName() {
		return "Blend";
	}
	
	public String getHelpText() {
		return "Gets two inputs and blends the second one with the first \n" +
				"similar to the Layer Operations in PhotoShop or Gimp.\n" +
				"Opacity controls how much of the first image stays visible.\n" +
				"If I1 has an alpha channel it is used as\n" +
				"additional blending weight (multiplied with opacity).\n" +
				"Output Alpha is set to the alpha value of I0.\n" +
				"\n" +
				"Functions (operate only on RGB); I0 = first input; I1 = second input; O = Output:\n" +
				"   Normal:     O = I1 \n" +
				"   Mutliply:   O = I0*I1 \n" +
				"   Divide:     O = I1/(I0+1) \n" +
				"   Screen:     O = 1-(1-I0)*(1-I1) \n" +
				"   Overlay:    O = I0*(I0 + 2*I1*(1-I0)) \n" +
				"   Dodge:      O = I0/((1-I1)+1) \n" +
				"   Burn:       O = 1-((1-I0)/(I1+1)) \n" +
				"   Difference: O = |I0-I1| \n" +
				"   Addition:   O = min(1, I0+I1) \n" +
				"   Subtract:   O = max(0, I0-I1) \n" +
				"";
	}
	

	public FilterBlend() {
		super(2);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else if (idx == 1) return OutputType.RGBA;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	private final Vector4 _function(Vector4 c0, Vector4 c1) {
		float alpha = c1.w;
		if (invertAlpha.get()) alpha = 1.0f - alpha;
		
		final Vector4 color = new Vector4(c1);
		final int func = blendFunction.getEnumPos();
		
		alpha *= (opacity.get()); 
		
		// apply the blending function without alpha:
		if (func == 0) { // normal
			// do nothing
		} else if (func == 1) { // Multiply
			color.multComp_ip(c0);
		} else if (func == 2) { // Divide 
			color.multComp_ip(1/(c0.x + 1), 1/(c0.y + 1), 1/(c0.z + 1), 1);
		} else if (func == 3) { // Screen 
			color.set(1-(1-c0.x)*(1-c1.x), 1-(1-c0.y)*(1-c1.y), 1-(1-c0.z)*(1-c1.z), 1);
		} else if (func == 4) { // Overlay
			color.set(c0.x*(c0.x + 2*(c1.x)*(1-c0.x)), c0.y*(c0.y + 2*(c1.y)*(1-c0.y)), c0.z*(c0.z + 2*(c1.z)*(1-c0.z)), 1);
		} else if (func == 5) { // Dodge
			color.set(c0.x/((1-c1.x)+1), c0.y/((1-c1.y)+1), c0.z/((1-c1.z)+1),1); 
		} else if (func == 6) { // Burn
			color.set(1-((1-c0.x)/(c1.x+1)), 1-((1-c0.y)/(c1.y+1)), 1-((1-c0.z)/(c1.z+1)), 1);
		} else if (func == 7) { // Difference
			color.set(c0).sub_ip(c1).abs_ip();
		} else if (func == 8) { // Addition
			color.add_ip(c0);
		} else if (func == 9) { // Subtract
			color.set(c0).sub_ip(c1);
		} 
		
		color.clamp(0.0f, 1.0f);
		
		float origW = c0.w; // keep alpha ???
		c0.mult_ip(1.0f - alpha);
		c0.mult_add_ip(alpha, color);
		c0.w = origW;
		return c0;
	}
	
	
	/*protected void cache_function(Vector4 out, CacheEntry[] ce, float u, float v) {
		out.set(_function(ce[0].sample(u, v), ce[1].sample(u, v)));
	}*/
	
	
	
	protected float _value1f(float u, float v) {
		Vector4 val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}
	
	protected Vector4 _valueRGBA(float u, float v) {
		return _function(inputChannels[0].valueRGBA(u, v), inputChannels[1].valueRGBA(u, v));
	}
}
