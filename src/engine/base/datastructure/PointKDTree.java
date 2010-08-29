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

import java.util.Collections;
import java.util.Vector;

import engine.base.FMath;

/** 
 * This is a general n-Dimensional kd tree for storing n-d points and efficiently
 * searching neighbors.
 * 
 * !!TODO: the axis distance in the traversal is currently computed using
 *         euclidean (squared) distance: this is wrong if the nd_Distance function
 *         uses some other distance measure
 * 
 * @author Holger Dammertz
 *
 * @param <Type>
 */
public class PointKDTree<Type> {
	Vector<KDPoint> tree = new Vector<KDPoint>();
	int DIM;
	
	public PointKDTree(int dimension) {
		if (dimension < 2) System.err.println("ERROR: PointKDTree only supports dimension > 1");
		DIM = dimension;
	}
	
	public void add(NdPositionable o) {
		tree.add(new KDPoint(o));
	}

	public void addWithoutDuplis(NdPositionable o, float epsilon) {
		for (KDPoint p : tree) {
			if (o.nd_distance2Func(p.point) <= epsilon) return;
		}
		tree.add(new KDPoint(o));
	}
	
	public Type get(int i) {
		return (Type)tree.get(i).point;
	}
	
	public int getDimension() {
		return DIM;
	}
	
	/**
	 * 
	 * @return The number of points in this tree
	 */
	public int size() {
		return tree.size();
	}
	
	public void clear() {
		tree.clear();
	}
	
	/** This method has to be called after adding points or changing the location of points
	 */
	public void build() {
		//System.out.print("Building kd-tree (k = "+DIM+") with " + tree.size() + " points...");
		build_recursive(0, tree.size());
		//System.out.println("done.");
	}
	
	
	int split_and_sort(int si, int ei, int axis, float plane) {
		while (si < ei) {
			while ((si < ei) && (tree.get(si).point.getPos(axis) < plane)) si++;
			while ((si < ei) && (tree.get(ei-1).point.getPos(axis) >= plane)) ei--;
			if (si < ei-1) Collections.swap(tree, si, ei-1);
		}
		return si;
	}
	
	void build_recursive(int si, int ei) {
		if (si == ei-1) {
			KDPoint kp = tree.get(si);
			kp.right = -1;
			kp.axis = -1;
			return;
		}
		
		NdAABB aabb = new NdAABB(DIM);
		
		for (int i = si; i < ei; i++) aabb.update(tree.get(i).point);
		int axis = aabb.getMaxExtendAxis();
		float plane = aabb.getCenter(axis);
		
		// find the point closest to the plane
		int pIdx = si;
		float dist = FMath.abs(plane - tree.get(pIdx).point.getPos(axis));
		for (int i = si+1; i < ei; i++) {
			float d = FMath.abs(plane - tree.get(i).point.getPos(axis));
			if (d < dist) {
				dist = d;
				pIdx = i;
			}
		}
		plane = tree.get(pIdx).point.getPos(axis);
		Collections.swap(tree, si, pIdx); // move it at the beginning
		int m = split_and_sort(si+1, ei, axis, plane);
		KDPoint kp = tree.get(si);
		kp.right = m;
		kp.axis = axis;
		
		//System.out.format("(%d %d) -> %d\n", si, ei, m); 
		
		if (si+1 < m) build_recursive(si+1, m);
		if (m < ei) build_recursive(m, ei);
		else kp.right = -1;
	}
	
	final int stack[] = new int[512];
	
	
	public Type getNearestBruteFroce(NdPositionable pos) {
		float minDist = Float.MAX_VALUE;
		KDPoint nearest = null;
		for (KDPoint p : tree) {
			float d = pos.nd_distance2Func(p.point);
			if (d < minDist) {
				minDist = d;
				nearest = p;
			}
		}
		return (Type)nearest.point;
	}
	
	
	public Type getNearest(NdPositionable pos) {
		int top = 0;
		stack[top] = 0;
		float mindist = Float.MAX_VALUE;
		KDPoint ret = null;
		while (top >= 0) {
			KDPoint kp = tree.get(stack[top]);

			float dist = pos.nd_distance2Func(kp.point);
			if (dist < mindist) {
				mindist = dist;
				ret = kp;
			}
			
			if (kp.axis == -1) {
				top--;
			} else {
				int ax = kp.axis;
				float d = pos.getPos(ax) - kp.point.getPos(ax);
				if ((d < 0)) { 
					int left = stack[top] + 1;
					if ((kp.right != -1) && (left != kp.right) && (d*d < mindist)) {
						stack[top] = kp.right; top++;
					}
					stack[top] = left;
				} else {
					int left = stack[top] + 1;
					if ((left != kp.right) && (d*d < mindist)) {
						stack[top] = left; top++;
					}
					if (kp.right != -1) {
						stack[top] = kp.right;
					} else {
						top--;
					}
				}
			}
		}
		//!!TODO;
		return (Type)ret.point;
	}
	
	// !!TODO: optimize
	float insertKNearest(NdPositionable[] knearest, NdPositionable insert, NdPositionable pos) {
		float maxdist = 0.0f; 
		int maxidx = 0;
		//TODO: !!UGUG very inefficient to always recompute this here
		for (int i = 0; i < knearest.length; i++) {
			if (knearest[i] == null) {
				knearest[i] = insert;
				if (i < knearest.length-1) return Float.MAX_VALUE;
				else return Math.max(maxdist, pos.nd_distance2Func(insert));
			}
			float dist = pos.nd_distance2Func(knearest[i]);
			if (dist > maxdist) {
				maxdist = dist;
				maxidx = i;
			}
		}
		knearest[maxidx] = insert;
		maxdist = 0.0f;
		//TODO: !!UGUG again very inefficient to always recompute this here
		for (int i = 0; i < knearest.length; i++) {
			float dist = pos.nd_distance2Func(knearest[i]);
			if (dist > maxdist) {
				maxdist = dist;
			}
		}
		
		//System.out.println(maxdist);
		return maxdist;
	}

	//!!TODO: currently this returns an unsorted list of the k-nearest
	public void getKNearest(NdPositionable pos, Type[] k_nearest) {
		NdPositionable[] knearest = (NdPositionable[])k_nearest;
		for (int i = 0; i < knearest.length; i++) knearest[i] = null;
		int top = 0;
		stack[top] = 0;
		float mindist = Float.MAX_VALUE;
		
		// TOO slow in curren timplementation DistanceHeap<NdPositionable> heap = new DistanceHeap<NdPositionable>(k_nearest.length);
		
		while (top >= 0) {
			KDPoint kp = tree.get(stack[top]);

			float dist = pos.nd_distance2Func(kp.point);
			
			// TOO slow: heap.add(kp.point, dist); if (heap.isFull()) mindist = heap.getLastDistance();
			if (dist < mindist) {
				mindist = insertKNearest(knearest, kp.point, pos);
			}
			
			if (kp.axis == -1) {
				top--;
			} else {
				int ax = kp.axis;
				float d = pos.getPos(ax) - kp.point.getPos(ax);
				if ((d < 0)) { 
					int left = stack[top] + 1;
					if ((kp.right != -1) && (left != kp.right) && (d*d < mindist)) {
						stack[top] = kp.right; top++;
					}
					stack[top] = left;
				} else {
					int left = stack[top] + 1;
					if ((left != kp.right)  && (d*d < mindist)) {
						stack[top] = left; top++;
					}
					if (kp.right != -1) {
						stack[top] = kp.right;
					} else {
						top--;
					}
				}
			}
		}
		//TOO slow: heap.getAllElements((NdPositionable[])k_nearest);
	}
	
	class KDPoint {
		NdPositionable point;
		int right;
		int axis;
		
		KDPoint(NdPositionable o) {
			point = o;
			right = -1;
			axis = -1;
		}
	}
}
