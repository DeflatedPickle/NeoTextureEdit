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

/**
 * A Utility class.
 * @author Holger Dammertz
 *
 */
public class Vector2 {
	public float x, y;
	
	public Vector2() {
		x = y = 0;
	}
	
	public Vector2(Vector2 v) {
		x = v.x;
		y = v.y;
	}
	
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public int getDIM() {
		return 2;
	}
	
	public float getPos(int i) {
		if (i == 0) return x;
		else return y;
	}

	public void set(Vector2 v) {
		x = v.x;
		y = v.y;
	}
	
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void multadd_ip(Vector2 v, float f) {
		x += v.x * f;
		y += v.y * f;
	}
	
	public void rotate_90degree_ip() {
		float t = y;
		y = -x;
		x = t;
	}
	
	public float distance2(Vector2 v) {
		return (x - v.x)*(x - v.x) + (y - v.y)*(y - v.y);
	}

	public float distance(Vector2 v) {
		return FMath.sqrt((x - v.x)*(x - v.x) + (y - v.y)*(y - v.y));
	}
	
	public float manhatten(Vector2 v) {
		return FMath.abs(x - v.x) + FMath.abs(y - v.y);
	}
	
	public void add_ip(Vector2 v) {
		x += v.x;
		y += v.y;
	}
	
	public void sub_ip(Vector2 a) {
		x -= a.x;
		y -= a.y;
	}
	
	public void mult_ip(float f) {
		x *= f;
		y *= f;
	}
	
	public float computeLength() {
		return FMath.sqrt(x*x + y*y);
	}
	
	public float computeLength2() {
		return (x*x + y*y);
	}
	
	public void setFromAngle(float a) {
		x = FMath.cos(a);
		y = FMath.sin(a);
	}
	
	
	public float normalize_ip() {
		float l = FMath.sqrt(x*x + y*y);
		x /= l;
		y /= l;
		return l;
	}
	

	
	
	public void setIfLess(float a, float b) {
		x = (a < x) ? a : x;
		y = (b < y) ? b : y;
	}
	
	public void setIfGreater(float a, float b) {
		x = (a > x) ? a : x;
		y = (b > y) ? b : y;
	}
	
	public void setIfLess(Vector2 v) {
		x = (v.x < x) ? v.x : x;
		y = (v.y < y) ? v.y : y;
	}

	public void setIfGreater(Vector2 v) {
		x = (v.x > x) ? v.x : x;
		y = (v.y > y) ? v.y : y;
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
