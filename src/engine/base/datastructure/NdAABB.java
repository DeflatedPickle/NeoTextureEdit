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

public class NdAABB {
	float data[];
	int DIM;
	
	public NdAABB(int dimension) {
		DIM = dimension;
		data = new float[DIM*2];
		
		for (int i = 0; i < DIM; i++) {
			data[i] = Float.MAX_VALUE;
			data[DIM+i] = -Float.MAX_VALUE;
		}
	}
	
	public void setMin(float v) {
		for (int i = 0; i < DIM; i++) data[i] = v;
	}

	public void setMax(float v) {
		for (int i = 0; i < DIM; i++) data[DIM+i] = v;
	}
	
	public float getMin(int i) {
		return data[i];
	}

	public float getMax(int i) {
		return data[DIM+i];
	}
	
	public void setMin(int i, float v) {
		data[i] = v;
	}

	public void setMax(int i, float v) {
		data[DIM+i] = v;
	}

	public NdAABB(NdAABB box) {
		DIM = box.DIM;
		data = new float[DIM*2];
		for (int i = 0; i < data.length; i++) data[i] = box.data[i];
	}
	
	public void update(NdPositionable o) {
		for (int i = 0; i < DIM; i++) {
			float v = o.getPos(i);
			if (v < data[i]) data[i] = v;
			if (v > data[DIM+i]) data[DIM+i] = v;
		}
	}
	
	public boolean contains(NdPositionable o) {
		for (int i = 0; i < DIM; i++) {
			if ((o.getPos(i) < data[i]) || (o.getPos(i) >= data[DIM+i])) return false;
		}
		return true;
	}
	
	public float getExtend(int axis) {
		return data[DIM+axis] - data[axis];
	}
	
	public float getCenter(int axis) {
		return (data[DIM+axis] + data[axis]) * 0.5f;
	}
	
	
	public float computeVolume() {
		float vol = (data[0+DIM] - data[0]);
		for (int i = 1; i < DIM; i++) {
			vol *= (data[i+DIM] - data[i]);
		}
		return vol;
	}
	
	
	public boolean computeIntersection(NdAABB a, NdAABB b) {
		for (int i = 0; i < 2*DIM; i++) data[i] = a.data[i];
		for (int i = 0; i < DIM; i++) {
			if (b.data[i] > data[i]) data[i] = b.data[i];
			if (b.data[i+DIM] < data[i+DIM]) data[i+DIM] = b.data[i+DIM];
			if (data[i] >= data[i+DIM]) return false;
		}
		return true;
	}
	
	
	public NdVector computeCenter() {
		NdVector v = new NdVector(DIM);
		for (int i = 0; i < DIM; i++) {
			v.set(i, (data[i]+data[i+DIM])*0.5f);
		}
		return v;
	}
	
	
	public int getMaxExtendAxis() {
		int ax = 0;
		float maxd = getExtend(0);
		for (int i = 1; i < DIM; i++) {
			float d = getExtend(i);
			if (d > maxd) {
				maxd = d;
				ax = i;
			}
		}
		return ax;
	}
	
	public String toString() {
		String ret = "( ";
		for (int i = 0; i < DIM; i++) {
			ret += getMin(i) + " ";
		}
		ret += ") ( ";
		for (int i = 0; i < DIM; i++) {
			ret += getMax(i) + " ";
		}
		ret += ")";
		return ret;
	}
	
}
