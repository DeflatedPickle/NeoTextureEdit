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

import engine.base.Vector3;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.FloatParam;

public final class FilterNormalMap extends Channel {
	public static boolean ms_FlipX = false;
	public static boolean ms_FlipY = false;
	
	FloatParam strength = CreateLocalFloatParam("Strength", 1.0f, 0.0f, Float.MAX_VALUE).setDefaultIncrement(0.125f);
	
	public String getName() {
		return "Normal Map";
	}


	public FilterNormalMap() {
		super(1);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.SCALAR;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	private final Vector4 _function(float du, float dv) {
		Vector3 n = new Vector3(du*strength.get(), dv*strength.get(), 1.0f);
		n.normalize();
		
		Vector4 c = new Vector4(n.x * 0.5f + 0.5f, n.y * 0.5f + 0.5f, n.z * 0.5f + 0.5f, 1.0f);
		return c;
	}
	
	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		float du = caches[0].sample_du(localX, localY).XYZto1f(); //inputChannels[0].du1f(u, v).XYZto1f();
		float dv = caches[0].sample_dv(localX, localY).XYZto1f(); //inputChannels[0].dv1f(u, v).XYZto1f();
		out.set(_function(ms_FlipX?-du:du, ms_FlipY?-dv:dv));
	}
	
	
	protected float _value1f(float u, float v) {
		Vector4 val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}
	
	protected Vector4 _valueRGBA(float u, float v) {
		float du = inputChannels[0].du1f(u, v).XYZto1f();
		float dv = inputChannels[0].dv1f(u, v).XYZto1f();
		return _function(ms_FlipX?-du:du, ms_FlipY?-dv:dv);
	}
}
