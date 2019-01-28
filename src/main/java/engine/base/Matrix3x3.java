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

import org.joml.Vector3f;

/**
 * A Simple 3x3 matrix class
 * @author Holger Dammertz
 *
 */
public class Matrix3x3 {
	// 11 12 13
	// 21 22 23
	// 31 32 33
	final float[] d = new float[9];
	
	public Matrix3x3() {
		d[0] = 1.0f; d[1] = 0.0f; d[2] = 0.0f;
		d[3] = 0.0f; d[4] = 1.0f; d[5] = 0.0f;
		d[6] = 0.0f; d[7] = 0.0f; d[8] = 1.0f;	
	}
	
	public Matrix3x3(float a11, float a12, float a13, float a21, float a22, float a23, float a31, float a32, float a33) {
		d[0] = a11; d[1] = a12; d[2] = a13;
		d[3] = a21; d[4] = a22; d[5] = a23;
		d[6] = a31; d[7] = a32; d[8] = a33;
	}
	
	public void set(Matrix3x3 m) {
		for (int i = 0; i < 9; i++) d[i] = m.d[i];
	}
	
	public void set(float a11, float a12, float a13, float a21, float a22, float a23, float a31, float a32, float a33) {
		d[0] = a11; d[1] = a12; d[2] = a13;
		d[3] = a21; d[4] = a22; d[5] = a23;
		d[6] = a31; d[7] = a32; d[8] = a33;
	}
	
	public void set(int idx, float val) {
		d[idx] = val;
	}
	
	public float get(int i) {
		return d[i];
	}
	
	
	public void mult_ip(Matrix3x3 m) {
		float a11 = d[0] * m.d[0] + d[1] * m.d[3] + d[2] * m.d[6];
		float a12 = d[0] * m.d[1] + d[1] * m.d[4] + d[2] * m.d[7];
		float a13 = d[0] * m.d[2] + d[1] * m.d[5] + d[2] * m.d[8];
		float a21 = d[3] * m.d[0] + d[4] * m.d[3] + d[5] * m.d[6];
		float a22 = d[3] * m.d[1] + d[4] * m.d[4] + d[5] * m.d[7];
		float a23 = d[3] * m.d[2] + d[4] * m.d[5] + d[5] * m.d[8];
		float a31 = d[6] * m.d[0] + d[7] * m.d[3] + d[8] * m.d[6];
		float a32 = d[6] * m.d[1] + d[7] * m.d[4] + d[8] * m.d[7];
		float a33 = d[6] * m.d[2] + d[7] * m.d[5] + d[8] * m.d[8];
		d[0] = a11; d[1] = a12; d[2] = a13;
		d[3] = a21; d[4] = a22; d[5] = a23;
		d[6] = a31; d[7] = a32; d[8] = a33;
	}
	
	public Vector3f mult(Vector3f v) {
		return new Vector3f(d[0]*v.x + d[1]*v.y + d[2]*v.z,
				           d[3]*v.x + d[4]*v.y + d[5]*v.z,
				           d[6]*v.x + d[7]*v.y + d[8]*v.z);
	}
	
	
	public static Matrix3x3 CreateIdentity() {
		return new Matrix3x3(1.0f, 0.0f, 0.0f, 
				             0.0f, 1.0f, 0.0f,
				             0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix3x3 Create2DHomogenous_Scale(float sx, float sy) {
		return new Matrix3x3(sx, 0, 0, 0, sy, 0, 0, 0, 1);
	}

	public static Matrix3x3 Create2DHomogenous_Translate(float tx, float ty) {
		return new Matrix3x3(1, 0, tx, 0, 1, ty, 0, 0, 1);
	}
	
	public static Matrix3x3 Create2DHomogenous_Rotate(float rad) {
		return CreateZRotMatrix_Rad(rad);
	}
	
	
	
	
	public static Matrix3x3 CreateXRotMatrix_Rad(float rad) {
		return new Matrix3x3(1.0f, 0.0f, 0.0f, 
				             0.0f, FMath.cos(rad), -FMath.sin(rad), 
				             0.0f, FMath.sin(rad), FMath.cos(rad));
	}
	
	public static Matrix3x3 CreateYRotMatrix_Rad(float rad) {
		return new Matrix3x3(FMath.cos(rad), 0.0f, -FMath.sin(rad), 
				             0.0f, 1.0f, 0.0f, 
				             FMath.sin(rad), 0.0f, FMath.cos(rad));
	}

	public static Matrix3x3 CreateZRotMatrix_Rad(float rad) {
		return new Matrix3x3(FMath.cos(rad), FMath.sin(rad), 0.0f,
							 -FMath.sin(rad), FMath.cos(rad), 0.0f,
				             0.0f, 0.0f, 1.0f);
	}
	

	
	

}
