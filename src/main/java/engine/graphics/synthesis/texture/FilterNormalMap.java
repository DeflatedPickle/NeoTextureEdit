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
import engine.parameters.FloatParam;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class FilterNormalMap extends Channel {
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
	
	private final Vector4f _function(float du, float dv) {
		Vector3f n = new Vector3f(du*strength.get(), dv*strength.get(), 1.0f);
		n.normalize();

		Vector4f c = new Vector4f(n.x * 0.5f + 0.5f, n.y * 0.5f + 0.5f, n.z * 0.5f + 0.5f, 1.0f);
		return c;
	}

	protected void cache_function(Vector4f out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		Vector4f du = caches[0].sample_du(localX, localY); //inputChannels[0].du1f(u, v).XYZto1f();
		Vector4f dv = caches[0].sample_dv(localX, localY); //inputChannels[0].dv1f(u, v).XYZto1f();
		out.set(_function((du.x + du.y + du.z) * (1f / 3f), (dv.x + dv.y + dv.z) * (1f / 3f)));
	}


	protected float _value1f(float u, float v) {
		Vector4f val = valueRGBA(u, v);
		return (val.x+val.y+val.z)*(1.0f/3.0f);
	}

	protected Vector4f _valueRGBA(float u, float v) {
		Vector4f du = inputChannels[0].du1f(u, v);
		Vector4f dv = inputChannels[0].dv1f(u, v);
		return _function((du.x + du.y + du.z) * (1f / 3f), (dv.x + dv.y + dv.z) * (1f / 3f));
	}
}
