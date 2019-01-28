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
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.FloatParam;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class FilterEmboss extends Channel {
	FloatParam strength;
	FloatParam angle;
	
	public String getName() {
		return "Emboss";
	}
	
	public String getHelpText() {
		return "A simple emboss effect that adds or subtracts white based on\n" +
				"the grayscale derivative of the second input.";
	}
	
	
	public FilterEmboss() {
		super(2);
		strength = CreateLocalFloatParam("Strength", 8.0f, 0.0f, Float.MAX_VALUE);
		angle = CreateLocalFloatParam("Angle", 45.0f, 0.0f, 360.0f);;
		angle.setDefaultIncrement(22.5f);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else if (idx == 1) return OutputType.SCALAR;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	private final Vector4f _function(Vector4f in0, float du, float dv) {
		Vector4f c = new Vector4f(in0);
		Vector3f n = new Vector3f(du*strength.get(), dv*strength.get(), 0.0f);
		
		float a = FMath.deg2rad(angle.get());
		Vector3f dir = new Vector3f(FMath.cos(a),FMath.sin(a),0);
		float addValue = n.dot(dir);
		
		c.x = Math.max(0.0f, Math.min(1.0f, c.x + addValue));
		c.y = Math.max(0.0f, Math.min(1.0f, c.y + addValue));
		c.z = Math.max(0.0f, Math.min(1.0f, c.z + addValue));
		
		return c;
	}
	
	protected void cache_function(Vector4f out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		//float du = ce[1].du(u, v).XYZto1f();
		//float dv = ce[1].dv(u, v).XYZto1f();
		Vector4f du = caches[1].sample_du(localX, localY); //inputChannels[1].du1f(u, v).XYZto1f();
		Vector4f dv = caches[1].sample_dv(localX, localY); //inputChannels[1].dv1f(u, v).XYZto1f();
		out.set(_function(caches[0].sample(localX, localY), (du.x + du.y + du.z) * (1f / 3f), (dv.x + dv.y + dv.z) * (1f / 3f)));
	}
	

	protected Vector4f _valueRGBA(float u, float v) {
		Vector4f du = inputChannels[1].du1f(u, v);
		Vector4f dv = inputChannels[1].dv1f(u, v);
		return _function(inputChannels[0].valueRGBA(u, v), (du.x + du.y + du.z) * (1f / 3f), (dv.x + dv.y + dv.z) * (1f / 3f));
		
		/*Vector3f n = new Vector3f(inputChannels[1].du1f(u, v)*strength.get(), inputChannels[1].dv1f(u, v)*strength.get(), 0.0f);
		
		float a = FMath.deg2rad(angle.get());
		Vector3f dir = new Vector3f(FMath.cos(a),FMath.sin(a),0);
		float addValue = n.dot(dir);
		
		
		Vector4f c = inputChannels[0].valueRGBA(u, v);

		c.x = Math.max(0.0f, Math.min(1.0f, c.x + addValue));
		c.y = Math.max(0.0f, Math.min(1.0f, c.y + addValue));
		c.z = Math.max(0.0f, Math.min(1.0f, c.z + addValue));
		
		return c;*/
	}
	
}
