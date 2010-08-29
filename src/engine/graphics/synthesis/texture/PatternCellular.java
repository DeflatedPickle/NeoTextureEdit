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
import engine.base.Vector2;
import engine.base.Vector4;
import engine.base.datastructure.NdPositionable;
import engine.base.datastructure.PointKDTree;
import engine.parameters.AbstractParam;
import engine.parameters.BoolParam;
import engine.parameters.ColorGradientParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;

public class PatternCellular extends Pattern {
	public String getName() {
		return "Cellular";
	}
	
	
	public String getHelpText() {
		return "Worley Cellular texture basis function. \n" +
				"See http://portal.acm.org/citation.cfm?id=237267\n" +
				"Uses a kd tree for point lookup. Non-euclidean distance metrics \n" +
				"are not yet fully correct.";
	}
	
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");

	EnumParam cellFunction;
	EnumParam distanceFunction;
	EnumParam randomFunction;
	IntParam randomSeedParam;
	FloatParam valueScale;
	IntParam numPoints;
	FloatParam jitter;
	BoolParam periodic;

	float randomColors[];

	//boolean useManhattenDist = false;
	int distanceFunctionType = 0; // 0 euclid, 1 manhatten, 2 max

	final PatternPoint[] p2 = new PatternPoint[2];
	final PatternPoint[] p3 = new PatternPoint[3];

	final class PatternPoint extends Vector2 implements NdPositionable {
		public int index;

		public PatternPoint(float x, float y, int i) {
			super(x, y);
			index = i;
		}

		public final float nd_distance2Func(NdPositionable p) {
			if (distanceFunctionType == 3) { // Minkowski 0.5
				float dX = FMath.sqrt(Math.abs(x - p.getPos(0)));
				float dY = FMath.sqrt(Math.abs(y - p.getPos(1)));
				return (dX+dY)*(dX+dY);
			}
			if (distanceFunctionType == 2) { // max
				float dX = Math.abs(x - p.getPos(0));
				float dY = Math.abs(y - p.getPos(1));
				return Math.max(dX, dY);
			}else if (distanceFunctionType == 1) { // manhaten
				float dX = Math.abs(x - p.getPos(0));
				float dY = Math.abs(y - p.getPos(1));
				return dX + dY;
			} else {
				float dX = (x - p.getPos(0));
				float dY = (y - p.getPos(1));
				return (dX * dX + dY * dY);
			}
		}
	}

	final PointKDTree<PatternPoint> points = new PointKDTree<PatternPoint>(2);

	BoolParam useRandomColor;

	public PatternCellular() {
		cellFunction = CreateLocalEnumParam("Cell Type", "F1,F2,F3,Constant,F2-F1");
		distanceFunction = CreateLocalEnumParam("Distance ", "Euclid,Manhatten,Max,Minkowski0.5");
		randomFunction = CreateLocalEnumParam("PointGen", "Random,Regular,Halton23,Hammersley");
		randomSeedParam = CreateLocalIntParam("Seed", 0, 0, Integer.MAX_VALUE);
		valueScale = CreateLocalFloatParam("Intensity", 1.0f, 0.0f, Float.MAX_VALUE);
		valueScale.setDefaultIncrement(0.125f);
		jitter = CreateLocalFloatParam("Jitter", 0.0f, 0.0f, 2.0f);
		jitter.setDefaultIncrement(0.125f);
		numPoints = CreateLocalIntParam("NumPoints", 16, 2, Integer.MAX_VALUE);
		useRandomColor = CreateLocalBoolParam("RandColor", false);
		periodic = CreateLocalBoolParam("Periodic", true);

		regeneratePoints();
		createRandomColors();
		parameterChanged(null);
	}

	void createRandomColors() {
		if (randomColors == null || numPoints.get() != randomColors.length)
			randomColors = new float[numPoints.get()];
		FMath.setSeed(randomSeedParam.get());
		for (int i = 0; i < randomColors.length; i++)
			randomColors[i] = FMath.random();

	}
	
	public float distance(PatternPoint p0, PatternPoint p1) {
		if (distanceFunction.getEnumPos() == 0)
			return p0.distance(p1);
		else if (distanceFunction.getEnumPos() == 1)
			return p0.manhatten(p1);
		else if (distanceFunction.getEnumPos() == 2) 
			return Math.max(FMath.abs(p0.x - p1.x), FMath.abs(p0.y - p1.y));
		else if (distanceFunction.getEnumPos() == 3) { 
			float x = FMath.sqrt(FMath.abs(p0.x - p1.x));
			float y = FMath.sqrt(FMath.abs(p0.y - p1.y));
			return (x+y)*(x+y);
		}
		else System.err.println("PatternCellular.distance wrong");
		return 0.0f;
	}
	
	
	// !!TODO: still can be optimized a lot
	protected Vector4 _valueRGBA(float u, float v) {
		float minDistA = Float.MAX_VALUE;
		PatternPoint lookup = new PatternPoint(u, v, -1);
		PatternPoint nearest = null;
		
		if (cellFunction.getEnumPos() == 0) { // F1
			nearest = points.getNearest(lookup);
			minDistA = distance(lookup, nearest);
		} else if (cellFunction.getEnumPos() == 1) { // F2
			// !!TODO: optimize
			points.getKNearest(lookup, p2);
			nearest = (lookup.nd_distance2Func(p2[0]) < lookup.nd_distance2Func(p2[1])) ? p2[1] : p2[0];
			minDistA = distance(lookup, nearest);
		} else if (cellFunction.getEnumPos() == 2) { // F3
			// !!TODO: optimize
			points.getKNearest(lookup, p3);
			nearest = (lookup.nd_distance2Func(p3[0]) < lookup.nd_distance2Func(p3[1])) ? p3[1] : p3[0];
			nearest = (lookup.nd_distance2Func(nearest) < lookup.nd_distance2Func(p3[2])) ? p3[2] : nearest;
			minDistA = distance(lookup, nearest);
		} else if (cellFunction.getEnumPos() == 3) { // Constant
			nearest = points.getNearest(lookup);
			float ret = valueScale.get();
			if (useRandomColor.get())
				ret *= randomColors[nearest.index];
			return colorGradientParam.get().getColor(ret);
		} else if (cellFunction.getEnumPos() == 4) { // F2-F1
			// !!TODO: optimize
			points.getKNearest(lookup, p2);
			nearest = (lookup.nd_distance2Func(p2[0]) < lookup.nd_distance2Func(p2[1])) ? p2[0] : p2[1];
			minDistA = FMath.abs(distance(lookup, p2[0]) - distance(lookup, p2[1]));
		}


		float ret = minDistA;
		if (cellFunction.getEnumPos() == 0)
			ret = minDistA;
		else if (cellFunction.getEnumPos() == 1)
			ret = minDistA / 1.4142f;
		else if (cellFunction.getEnumPos() == 2)
			ret = minDistA / 2.0f;
		else if (cellFunction.getEnumPos() == 4)
			ret = minDistA * 1.4142f;
		else {
			System.err.println("DBG error: sth wrong in PatternCellular.");
		}

		ret *= valueScale.get();

		// normalization of the value depends on the number of points and the
		// distance measure
		int numPoints = points.size();
		if (periodic.get())
			numPoints /= 4;

		ret *= FMath.sqrt(numPoints);

		/*
		 * if (distanceFunction.getEnumPos() == 0) ret *= FMath.sqrt(numPoints);
		 * else if (distanceFunction.getEnumPos() == 1) ret *= numPoints * 0.5f;
		 * else if (distanceFunction.getEnumPos() == 2) ret *=
		 * FMath.sqrt(numPoints)*0.5f;
		 */

		// clamping
		if (ret < 0.0f)
			ret = 0.0f;
		if (ret > 1.0f)
			ret = 1.0f;

		if (useRandomColor.get())
			ret *= randomColors[nearest.index];

		return colorGradientParam.get().getColor(ret);
	}

	public void regeneratePoints() {
		points.clear();

		FMath.setSeed(randomSeedParam.get());

		for (int i = 0; i < numPoints.get(); i++) {
			float jitterX = FMath.random(-0.5f, 0.5f) * jitter.get() / FMath.sqrt(numPoints.get());
			float jitterY = FMath.random(-0.5f, 0.5f) * jitter.get() / FMath.sqrt(numPoints.get());
			if (randomFunction.getEnumPos() == 0) { // Random
				points.add(new PatternPoint(FMath.random() + jitterX, FMath.random() + jitterY, i));
			} else if (randomFunction.getEnumPos() == 1) { // Regular
				int mod = (int) (FMath.sqrt(numPoints.get()) + 0.999f);
				float x = (i % mod) / (float) mod;
				float y = (i / mod) / (float) mod;
				points.add(new PatternPoint(x + jitterX, y + jitterY, i));
			} else if (randomFunction.getEnumPos() == 2) { // Halton 2 3 //
															// !!TODO: check
				points.add(new PatternPoint(FMath.radicalInverse_vdC(2, i) + jitterX, FMath.radicalInverse_vdC(3, i) + jitterY, i));
			} else if (randomFunction.getEnumPos() == 3) { // Hammersley //
															// !!TODO: check
				float div = 1.0f / ((float) numPoints.get());
				points.add(new PatternPoint(i * div + jitterX, FMath.radicalInverse_vdC(2, i) + jitterY, i));
			}
		}

		// for kd tree lookup we replicate the points to get a periodic pattern
		if (periodic.get())
			replicatePointsOnTorus();

		points.build();
	}

	private void replicatePointsOnTorus() {
		//System.out.println("Replicating points");
		int num = points.size();
		for (int i = 0; i < num; i++) {
			PatternPoint p = points.get(i);
			float nx, ny;
			if (p.x < 0.5f)
				nx = p.x + 1.0f;
			else
				nx = p.x - 1.0f;
			if (p.y < 0.5f)
				ny = p.y + 1.0f;
			else
				ny = p.y - 1.0f;
			points.add(new PatternPoint(nx, p.y, p.index));
			points.add(new PatternPoint(p.x, ny, p.index));
			points.add(new PatternPoint(nx, ny, p.index));
		}
	}

	public void parameterChanged(AbstractParam source) {
		distanceFunctionType = distanceFunction.getEnumPos();

		if (source == null) {
			regeneratePoints();
			createRandomColors();
		} else if (source == randomFunction || source == periodic || source == jitter) {
			regeneratePoints();
		} else if (source == randomSeedParam) {
			regeneratePoints();
			createRandomColors();
		} else if (source == numPoints) {
			regeneratePoints();
			createRandomColors();
		}
		super.parameterChanged(source);
	}
}
