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

import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.BoolParam;
import org.joml.Vector4f;

/**
 * This filter computes the output image as
 * Cout = Cin + (1-Cout.alpha) + Cout*Cout.alpha;
 * @author Holger Dammertz
 *
 */
public final class FilterMask extends Channel {
	BoolParam invert = CreateLocalBoolParam("Invert", false);
	
	public String getHelpText() {
		return "Blends two images based on an grayscale mask.\n" +
				"Without invert, the node computes\n" +
				"   output = (1-input3)*input1 + input3*input2";
	}
	
	public String getName() {
		return "Mask";
	}
	
	public FilterMask() {
		super(3);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else if (idx == 1) return OutputType.RGBA;
		else if (idx == 2) return OutputType.SCALAR;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	
	private final void _function(Vector4f out, Vector4f in0, Vector4f in1, Vector4f in2) {
		out.set(in0);
		float w = (in2.x + in2.y + in2.z) * (1f / 3f);
		if (invert.get()) w = 1.0f - w;
		out.mul(1.0f - w);
		out.fma(w, in1);
	}
	
	protected void cache_function(Vector4f out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		_function(out, caches[0].sample(localX, localY), caches[1].sample(localX, localY), caches[2].sample(localX, localY));
	}
		
	
	protected float _value1f(float u, float v) {
		Vector4f val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}
	
	protected Vector4f _valueRGBA(float u, float v) {
		Vector4f c0 = inputChannels[0].valueRGBA(u, v);
		Vector4f c1 = inputChannels[1].valueRGBA(u, v);
		Vector4f c2 = inputChannels[2].valueRGBA(u, v);
		Vector4f ret = new Vector4f();

		_function(ret, c0, c1, c2);
		
		return ret;
	}
}
