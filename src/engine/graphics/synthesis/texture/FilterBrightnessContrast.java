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
import engine.parameters.FloatParam;

public class FilterBrightnessContrast extends Channel {
	FloatParam brightness;
	FloatParam contrast;
	
	public String getName() {
		return "Bright/Cont";
	}
	
	public String getHelpText() {
		return "Changes the brightness/contrast of the input by computing\n" +
				"O = ((I - 0.5) * contrast + brightness) + 0.5\n" +
				"the result is clamped to [0,1].";
	}
	
	
	public FilterBrightnessContrast() {
		super(1);
		brightness = CreateLocalFloatParam("Brightness", 0.0f, -1.0f, 1.0f);
		brightness.setDefaultIncrement(0.125f);
		contrast = CreateLocalFloatParam("Contrast", 1.0f, 0.0f, 5.0f);
		contrast.setDefaultIncrement(0.125f);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	private final Vector4 _function(Vector4 c0) {
		c0.sub_ip(0.5f);
		c0.mult_ip(contrast.get());
		c0.add_ip(brightness.get());
		c0.add_ip(0.5f);
		c0.clamp(0.0f, 1.0f);
		return c0;
	}
	
	protected void cache_function(Vector4 out, CacheEntry[] ce, float u, float v) {
		out.set(_function(ce[0].sample(u, v)));
	}
	
	
	protected float _value1f(float u, float v) {
		Vector4 val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}
	
	protected Vector4 _valueRGBA(float u, float v) {
		return _function(inputChannels[0].valueRGBA(u, v));
	}
	
}
