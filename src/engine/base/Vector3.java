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
 * A Utility class.
 * @author Holger Dammertz
 *
 */
public class Vector3 implements Serializable {
	private static final long serialVersionUID = 7711363190023865751L;
	
	public static final Vector3 ZERO = new Vector3(0,0,0);
	public static final Vector3 ONE = new Vector3(1,1,1);
	
	public static final Vector3[] COLORS = {new Vector3(1,0,0), new Vector3(0,1,0), new Vector3(0,0,1), new Vector3(1,1,0), new Vector3(1,0,1), new Vector3(0,1,1)};
	
	public static Vector3 simpleColor(int idx) {
		return COLORS[idx%COLORS.length];
	}

	public float x, y, z;

	public Vector3() {
		x = y = z = 0.0f;
	}

	public Vector3(Vector3 v) {
		set(v.x, v.y, v.z);
	}

	public Vector3(float v) {
		x = y = z = v;
	}

	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	public static Vector3 createHemisphereSample_cos(float u, float v) {
		float r1 = v * 2.0f*FMath.PI;
		float r2 = FMath.sqrt(1.0f - u);
		float sp = FMath.sqrt(1.0f - r2*r2); // !! == sqrtf(u) !!
		return new Vector3(FMath.cos(r1) * sp, FMath.sin(r1) * sp, r2);
	}

	// returns the distance between the two points
	public float distance(Vector3 v) {
		float dx = x - v.x;
		float dy = y - v.y;
		float dz = z - v.z;
		return FMath.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public float distance2(Vector3 v) {
		float dx = x - v.x;
		float dy = y - v.y;
		float dz = z - v.z;
		return (dx * dx + dy * dy + dz * dz);
	}

	// ??Optimize??
	public float get(int i) {
		if (i == 0) return x;
		else if (i == 1) return y;
		return z;
	}
	
	public float sumComponents() {
		return x + y + z;
	}
	
	
	public Vector3 getSignVector() {
		return new Vector3((x<0)?-1:1, (y<0)?-1:1, (z<0)?-1:1);
	}
	
	public float sumAbsComponents() {
		return (FMath.abs(x) + FMath.abs(y) + FMath.abs(z));
	}

	public void set(Vector3 v) {
		set(v.x, v.y, v.z);
	}

	public void set(float f) {
		x = y = z = f;
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void add_ip(Vector3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public void sub_ip(Vector3 v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}
	
	public void sub_ip(Vector3 v, Vector3 w) {
		x = v.x - w.x;
		y = v.y - w.y;
		z = v.z - w.z;
	}
	
	
	public static Vector3 sub(Vector3 v, Vector3 w) {
		return new Vector3(v.x - w.x, v.y - w.y, v.z - w.z);
	}
	
	public static Vector3 add(Vector3 v, Vector3 w) {
		return new Vector3(v.x + w.x, v.y + w.y, v.z + w.z);
	}
	
	
	public Vector3 reflect(Vector3 n) {
		float dot2 = -this.dot(n)*2.0f;
		Vector3 reflect = new Vector3(this);
		reflect.mult_add_ip(dot2, n);
		reflect.normalize();
		return reflect;
	}
	

	/** this += a*v */
	public void mult_add_ip(float a, Vector3 v) {
		x += a * v.x;
		y += a * v.y;
		z += a * v.z;
	}
	
	public void multComp_add_ip(Vector3 a, Vector3 v) {
		x += a.x * v.x;
		y += a.y * v.y;
		z += a.z * v.z;
	}

	public Vector3 mult_sub_ip(float a, Vector3 v) {
		x -= a * v.x;
		y -= a * v.y;
		z -= a * v.z;
		return this;
	}
	
	public void mult_ip(float f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public void multComp_ip(Vector3 v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
	}

	public float dot(Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public void cross_ip(Vector3 a, Vector3 b) {
		float tx = a.y * b.z - a.z * b.y;
		float ty = a.z * b.x - a.x * b.z;
		float tz = a.x * b.y - a.y * b.x;
		x = tx;
		y = ty;
		z = tz;
	}
	
	/**
	 * Computes this = (1-f)*this + f*v
	 * @param f the weight 
	 * @param v the second vector
	 */
	public void linearInterp_ip(float f, Vector3 v) {
		x = (1-f)*x + f*v.x;
		y = (1-f)*y + f*v.y;
		z = (1-f)*z + f*v.z;
	}


	public float length2() {
		return x * x + y * y + z * z;
	}

	public float length() {
		return (float) FMath.sqrt(length2());
	}

	public void setIfLess(Vector3 v) {
		x = (v.x < x) ? v.x : x;
		y = (v.y < y) ? v.y : y;
		z = (v.z < z) ? v.z : z;
	}

	public void setIfGreater(Vector3 v) {
		x = (v.x > x) ? v.x : x;
		y = (v.y > y) ? v.y : y;
		z = (v.z > z) ? v.z : z;
	}

	public void setIfLess(float vx, float vy, float vz) {
		x = (vx < x) ? vx : x;
		y = (vy < y) ? vy : y;
		z = (vz < z) ? vz : z;
	}

	public void setIfGreater(float vx, float vy, float vz) {
		x = (vx > x) ? vx : x;
		y = (vy > y) ? vy : y;
		z = (vz > z) ? vz : z;
	}

	public float normalize() {
		float l = length();
		float il = 1.0f / l;
		x *= il;
		y *= il;
		z *= il;
		return l;
	}

	public int maxIdx() {
		if (x > y) {
			if (x > z) return 0;
			else return 2;
		} else {
			if (y > z) return 1;
			else return 2;
		}
	}

	public int minIdx() {
		if (x < y) {
			if (x < z) return 0;
			else return 2;
		} else {
			if (y < z) return 1;
			else return 2;
		}
	}
	
	public void setDir(float theta, float phi) {
		set(FMath.cos(phi)*FMath.sin(theta), FMath.sin(phi)*FMath.sin(theta), FMath.cos(theta));
	}
	
	// assumes a normalized vector
	public float computeTheta() {
		return FMath.acos(z);// Math.min(1.0f, z));
	}

	// assumes a normalized vector
	public float computePhi() {
		return FMath.atan2(y, x) + FMath.PI;
	}

	public String toString() {
		return String.format("(%.3f, %.3f, %.3f)", x, y, z);
	}

	public boolean isNaN() {
		return Float.isNaN(x) | Float.isNaN(y) & Float.isNaN(z);
	}
	
	public boolean isAllZero() {
		return ((x==0)&&(y==0)&&(z==0));
	}

	public void round(int v) {
		float iv = 1.0f / v;
		x = Math.round(x * v) * iv;
		y = Math.round(y * v) * iv;
		z = Math.round(z * v) * iv;
	}

	public void clamp(float min, float max) {
		if (x < min) x = min;
		if (x > max) x = max;
		if (y < min) y = min;
		if (y > max) y = max;
		if (z < min) z = min;
		if (z > max) z = max;
	}

	public int hashCode() {
		return Float.floatToIntBits(x) ^ Float.floatToIntBits(y) ^ Float.floatToIntBits(z);
	}

	public boolean equals(Object o) {
		Vector3 v = (Vector3) o;
		return (x == v.x) & (y == v.y) & (z == v.z);
	}
}
