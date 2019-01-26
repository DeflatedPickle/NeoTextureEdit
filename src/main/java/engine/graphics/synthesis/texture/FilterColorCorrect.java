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

import engine.base.Utils;
import engine.base.Vector3;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.FloatParam;

public final class FilterColorCorrect extends Channel {
	private final FloatParam brightness = CreateLocalFloatParam("Brightness", 0.0f, -1.0f, 1.0f).setDefaultIncrement(0.125f);
	private final FloatParam contrast = CreateLocalFloatParam("Contrast", 1.0f, 0.0f, 5.0f).setDefaultIncrement(0.125f);
	private final FloatParam gamma = CreateLocalFloatParam("Gamma", 1.0f, 1.0f/256.0f, 3.0f).setDefaultIncrement(0.25f);
	private final FloatParam desaturate = CreateLocalFloatParam("Desaturate", 0.0f, 0.0f, 1.0f).setDefaultIncrement(0.125f);
	
	
	public String getName() {
		return "ColorCorrect";
	}
	
	public String getHelpText() {
		return "Changes the brightness/contrast of the input by computing\n" +
				"O = ((I - 0.5) * contrast + brightness) + 0.5\n" +
				"the result is clamped to [0,1]. \n" +
				"Next desaturate is applied in HSV space\n" +
				"Finally Gamma is applied and computed\n" +
				"as O = (O)^{1/gamma}. If you need it in a different order\n" +
				"you need to use multiple of these nodes.";
	}
	
	public FilterColorCorrect() {
		super(1);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	private Vector4 _function(Vector4 c0) {
		c0.sub_ip(0.5f);
		c0.mult_ip(contrast.get());
		c0.add_ip(brightness.get());
		c0.add_ip(0.5f);
		c0.clamp(0.0f, 1.0f);
		
		float d = desaturate.get();
		if (d != 0.0f) {
			Vector3 temp = c0.getVector3();
			Utils.rgbToHSV_ip(temp);
			temp.y *= (1.0f - d);
			Utils.hsvToRGB_ip(temp);
			c0.setXYZ(temp.x, temp.y, temp.z);
		}
		
		float g = gamma.get();
		if (g != 1.0f) c0.pow_ip(1.0f/g);
		
		return c0;
	}
	
	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		out.set(_function(caches[0].sample(localX, localY)));
	}
	
	
	protected float _value1f(float u, float v) {
		Vector4 val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}
	
	protected Vector4 _valueRGBA(float u, float v) {
		return _function(inputChannels[0].valueRGBA(u, v));
	}
	
}
