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

package engine.base.datastructure;

import engine.base.FMath;

public class NdVector implements NdPositionable {
	int s;
	float[] v;
	
	public NdVector(int dimension) {
		s = dimension;
		v = new float[s];
	}
	
	public NdVector(int dimension, float val) {
		s = dimension;
		v = new float[s];
		for (int i = 0; i < s; i++) v[i] = val;
	}
	
	public float sumComponents() {
		float ret = 0.0f;
		for (int i = 0; i < s; i++) ret += v[i];
		return ret;
	}
	
	public float sumAbsComponents() {
		float ret = 0.0f;
		for (int i = 0; i < s; i++) ret += FMath.abs(v[i]);
		return ret;
	}
	
	public NdVector(NdVector vec) {
		s = vec.s;
		v = new float[s];
		for (int i = 0; i < s; i++) v[i] = vec.v[i];
	}
	
	public void set(int dim, float value) {
		v[dim] = value;
	}
	
	public void add_ip(NdVector vec) {
		for (int i = 0; i < s; i++) v[i] += vec.v[i];
	}

	public void mult_ip(float val) {
		for (int i = 0; i < s; i++) v[i] *= val;
	}

	public int getDIM() {
		return s;
	}

	public float getPos(int dim) {
		return v[dim];
	}
	
	public float nd_distance2Func(NdPositionable p) {
		return NdUtils.distance2(this, p);
	}
	
	
	public String toString() {
		String ret = "( ";
		for (int i = 0; i < s; i++) ret += v[i]+ " ";
		ret += ")";
		return ret;
	}

}
