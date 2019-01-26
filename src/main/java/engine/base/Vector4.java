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

package engine.base;

import java.io.Serializable;

/**
 * A Utility class for a mathematical Vector containg 4 float values. It has also support
 * functions for interpreting the Vector4 as a color
 * @author Holger Dammertz
 *
 */
public class Vector4 implements Serializable {
	private static final long serialVersionUID = 1027934130386016305L;

	public float x, y, z, w;

	public Vector4() {
		x = y = z = w = 0.0f;
	}

	public Vector4(Vector4 v) {
		set(v.x, v.y, v.z, v.w);
	}
	
	public Vector4(Vector3 v, float w) {
		set(v.x, v.y, v.z, w);
	}

	public Vector4(float v) {
		x = y = z = w = v;
	}
	
	public Vector4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	
	public Vector4 set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vector4 set(float v) {
		x = y = z = w = v;
		return this;
	}
		
	public void setXYZ(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	public Vector3 getVector3() {
		return new Vector3(x, y, z);
	}
	
	public Vector4 set(Vector4 v) {
		return set(v.x, v.y, v.z, v.w);
	}
	
	// ??Optimize??
	public float get(int i) {
		if (i == 0) return x;
		else if (i == 1) return y;
		else if (i == 2) return z;
		return w;
	}
	
	public float XYZto1f() {
		return (x+y+z)*(1.0f/3.0f);
	}
	
	public Vector4 add_ip(Vector4 v) {
		x += v.x;
		y += v.y;
		z += v.z;
		w += v.w;
		return this;
	}
	
	public Vector4 add_ip(float f) {
		x += f;
		y += f;
		z += f;
		w += f;
		return this;
	}

	public Vector4 sub_ip(Vector4 v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		w -= v.w;
		return this;
	}
	
	public Vector4 sub_ip(float f) {
		x -= f;
		y -= f;
		z -= f;
		w -= f;
		return this;
	}
	
	public Vector4 sub_ip(Vector4 v, Vector4 u) {
		x = v.x - u.x;
		y = v.y - u.y;
		z = v.z - u.z;
		w = v.w - u.w;
		return this;
	}

	/** this += a*v */
	public Vector4 mult_add_ip(float a, Vector4 v) {
		x += a * v.x;
		y += a * v.y;
		z += a * v.z;
		w += a * v.w;
		return this;
	}
	
	public Vector4 multComp_add_ip(Vector4 a, Vector4 v) {
		x += a.x * v.x;
		y += a.y * v.y;
		z += a.z * v.z;
		w += a.w * v.w;
		return this;
	}
	
	public float length2() {
		return x*x+y*y+z*z+w*w;
	}

	public Vector4 mult_sub_ip(float a, Vector4 v) {
		x -= a * v.x;
		y -= a * v.y;
		z -= a * v.z;
		w -= a * v.w;
		return this;
	}
	
	public Vector4 abs_ip() {
		x = FMath.abs(x);
		y = FMath.abs(y);
		z = FMath.abs(z);
		w = FMath.abs(w);
		return this;
	}
	
	public Vector4 pow_ip(float e) {
		x = FMath.pow(x, e);
		y = FMath.pow(y, e);
		z = FMath.pow(z, e);
		w = FMath.pow(w, e);
		return this;
	}
	
	public Vector4 mult_ip(float f) {
		x *= f;
		y *= f;
		z *= f;
		w *= f;
		return this;
	}

	public Vector4 multComp_ip(float a, float b, float c, float d) {
		x *= a;
		y *= b;
		z *= c;
		w *= d;
		return this;
	}

	public void multComp_ip(Vector4 v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
		w *= v.w;
	}
	
	public void clamp(float min, float max) {
		if (x < min) x = min;
		if (x > max) x = max;
		if (y < min) y = min;
		if (y > max) y = max;
		if (z < min) z = min;
		if (z > max) z = max;
		if (w < min) w = min;
		if (w > max) w = max;
	}
	
	/**
	 * Computes this = (1-f)*this + f*v
	 * @param f the weight 
	 * @param v the second vector
	 */
	public void linearInterp_ip(float f, Vector4 v) {
		x = (1-f)*x + f*v.x;
		y = (1-f)*y + f*v.y;
		z = (1-f)*z + f*v.z;
		w = (1-f)*w + f*v.w;
	}
	
	public Vector4 swizzle_ip_wxyz() {
		float temp = w;
		w = z;
		z = y;
		y = x;
		x = temp;
		return this;
	}

}
