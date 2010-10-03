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

import java.util.Random;

/**
 * Float Math. Currently a wrapper around Math to return float values
 * @author Holger Dammertz
 *
 */
public final class FMath {
	public static final float PI = 3.1415926535897932384626f;
	static Random rnd = new Random();

	public static final float sqrt(float a) {
		return (float)Math.sqrt(a);
	}
	
	public static final float sin(float a) {
		return (float)Math.sin(a);
	}
	
	public static final float exp(float a) {
		return (float)Math.exp(a);
	}

	public static final float cos(float a) {
		return (float)Math.cos(a);
	}
	
	public static final float acos(float a) {
		return (float)Math.acos(a);
	}
	
	public static final float atan2(float a, float b) {
		return (float)Math.atan2(a, b);
	}
	
	public static final float abs(float a) {
		return (a>0)?a:-a;
	}
	
	public static final float pow(float a, float b) {
		return (float)Math.pow(a, b);
	}
	
	public static float sgn(float a) {
		return (a>0)?1.0f:-1.0f;
	}
	
	public static final int ffloor(float a) {
		return (a>=0)?(int)a:(int)a-1;
	}
	
	public static final float tan(float a) {
		return (float)Math.tan((float)a);
	}
	
	public static final float deg2rad(float a) {
		return (a*PI)/180.0f;
	}
	
	public static final void setSeed(long seed) {
		rnd.setSeed(seed);
	}
	
	public static final synchronized float random() {
		return rnd.nextFloat();
		//return (float)Math.random();
	}
	
	public static final synchronized int randomInt() {
		return rnd.nextInt();
		//return (float)Math.random();
	}

	public static final float random(float min, float max) {
		return min + rnd.nextFloat()*(max - min);
	}
	
	// vanDerCorput Radical Inverse
	public static float radicalInverse_vdC(final int Base, int i) {
		double digit, radical, inverse;
		digit = radical = 1.0 / (double)Base;
		inverse = 0.0;
		while(i != 0) {
			inverse += digit * (double)(i % Base);
			digit *= radical;
			i /= Base;
		}
		return (float)inverse;
	}
	
	
	public static int getExp(float v) {
		if (v == 0) return 0;
		return ((0x7F800000 & Float.floatToIntBits(v))>>23) - 126;
	}
	
	public static float getManissa(float v) {
		return Float.intBitsToFloat((0x807FFFFF & Float.floatToIntBits(v)) | Float.floatToIntBits(0.5f));
	}
	
	// testing code
	public static void main(String[] args) {
		//System.out.println(getExp(1.0f));
		//System.out.println(getExp(2.3f));
		//System.out.println(getExp(8.1f));

		System.out.println(getManissa(11.0f));
		
		System.out.println((int)((char)0xFFFF));
	}

}
