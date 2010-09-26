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
import engine.base.Vector3;
import engine.base.Vector4;
import engine.parameters.FloatParam;

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
	
	private final Vector4 _function(Vector4 in0, float du, float dv) {
		Vector4 c = new Vector4(in0);
		Vector3 n = new Vector3(du*strength.get(), dv*strength.get(), 0.0f);
		
		float a = FMath.deg2rad(angle.get());
		Vector3 dir = new Vector3(FMath.cos(a),FMath.sin(a),0);
		float addValue = n.dot(dir);
		
		c.x = Math.max(0.0f, Math.min(1.0f, c.x + addValue));
		c.y = Math.max(0.0f, Math.min(1.0f, c.y + addValue));
		c.z = Math.max(0.0f, Math.min(1.0f, c.z + addValue));
		
		return c;
	}
	
	protected void cache_function(Vector4 out, CacheEntry[] ce, float u, float v) {
		out.set(_function(ce[0].sample(u, v), ce[1].du(u, v).XYZto1f(), ce[1].dv(u, v).XYZto1f()));
	}
	

	protected Vector4 _valueRGBA(float u, float v) {
		
		return _function(inputChannels[0].valueRGBA(u, v), inputChannels[1].du1f(u, v).XYZto1f(), inputChannels[1].dv1f(u, v).XYZto1f());
		
		/*Vector3 n = new Vector3(inputChannels[1].du1f(u, v)*strength.get(), inputChannels[1].dv1f(u, v)*strength.get(), 0.0f);
		
		float a = FMath.deg2rad(angle.get());
		Vector3 dir = new Vector3(FMath.cos(a),FMath.sin(a),0);
		float addValue = n.dot(dir);
		
		
		Vector4 c = inputChannels[0].valueRGBA(u, v);

		c.x = Math.max(0.0f, Math.min(1.0f, c.x + addValue));
		c.y = Math.max(0.0f, Math.min(1.0f, c.y + addValue));
		c.z = Math.max(0.0f, Math.min(1.0f, c.z + addValue));
		
		return c;*/
	}
	
}
