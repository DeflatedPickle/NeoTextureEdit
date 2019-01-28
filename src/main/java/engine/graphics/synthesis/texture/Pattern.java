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
import engine.parameters.Matrix3x3Param;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A Pattern is a function that lives on [0,1)x[0,1)=>[0,1]. It is a channel with 0 input parameters
 * and thus works as a generator. Usually it is scala valued but may also be RGB.
 * @author Holger Dammertz
 *
 */
public class Pattern extends Channel {
	
	Matrix3x3Param transformation = CreateLocalMatrix3x3Param("Transformation");
	
	public String getName() {
		return "Pattern";
	}

	public OutputType getChannelInputType(int idx) {
		System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	
	final protected Vector3f transform(float u, float v) {
		Vector3f p = transformation.getMatrix().mult(new Vector3f(u, v, 1.0f));
		p.x = p.x - FMath.ffloor(p.x);
		p.y = p.y - FMath.ffloor(p.y);
		return p;
	}
	
	@Override
	public Vector4f valueRGBA(float u, float v) {
		Vector3f p = transform(u, v);
		Vector4f val = _valueRGBA(p.x, p.y);
		return val;
	}
	
}
