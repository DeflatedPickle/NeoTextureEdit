package engine.parameters;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import org.lwjgl.util.vector.Vector2f;

/**
 * This is a simple 1d curve implementation.
 * 
 * !!TODO: this class should be generalized to provide a parameterized
 *         curve with an Interpolator object and a general element that
 *         can be contained
 * @author Holger Dammertz
 *
 */
public final class Curve {
	/** Contains all the control points. adding new points has to make
	 * sure that the points stay correctly ordered. The same holds true
	 * for changing the t position of the points.
	 */
	private final List<Vector2f> controlPoints = new Vector<Vector2f>();
	
	public Curve() {
	}
	
	/**
	 * Adds the control point to the list of control points
	 * @param p
	 */
	private void _addControlPoint(Vector2f p) {
		for (int i = 0; i < controlPoints.size(); i++) {
			if (controlPoints.get(i).x > p.x) {
				controlPoints.add(i, p);
				return;
			}
		}
		controlPoints.add(p);
	}

	/**
	 * Adds a copy of the given control point 
	 * into the curve using it's x coordinate
	 * for sorting.
	 * 
	 * @param p The control point
	 */
	public void addControlPoint(Vector2f p) {
		_addControlPoint(new Vector2f(p));
	}
	
	
	
	/**
	 * Returns a copy of the indexed control point. This does not allow
	 * to edit the curve
	 * @param idx if idx >= getNumControlPoints() an InvalidParameterException is thrown
	 * @return
	 */
	public Vector2f getControlPoint(int idx) {
		if (idx < 0 || idx >= controlPoints.size())
			throw new InvalidParameterException("Curve index should be in a valid range. Got " + idx + " expected it in [0," + controlPoints.size() + "]");
		return new Vector2f(controlPoints.get(idx));
	}
	
	/**
	 * Removes all control points. 
	 */
	public void clear() {
		controlPoints.clear();
	}
	
	
	/**
	 * Returns the minimum x value of this curve. If the curve does not
	 * contain any control points 0.0f is returned
	 * @return the starting range value
	 */
	public float getRangeMin() {
		if (controlPoints.size() == 0) return 0.0f;
		return controlPoints.get(0).x;
	}
	
	/**
	 * Returns the maximum x value of this curve. If the curve does not
	 * contain any control points 0.0f is returned
	 * @return the end range value
	 */
	public float getRangeMax() {
		if (controlPoints.size() == 0) return 0.0f;
		return controlPoints.get(controlPoints.size()-1).x;
	}
	
	public float getControlPointX(int idx) {
		return controlPoints.get(idx).x;
	}
	
	public float getControlPointY(int idx) {
		return controlPoints.get(idx).y;
	}
	
	public void setControlPointY(int idx, float y) {
		controlPoints.get(idx).y = y;
	}

	public void setControlPointX(int idx, float x) {
		//!!TODO: optimize this
		Vector2f p = controlPoints.remove(idx);
		p.x = x;
		_addControlPoint(p); 
	}
	
	/**
	 * Uses the current interpolation settings to interpolate the control
	 * points at the given position. If the number of control points is 0
	 * 0.0f is returned
	 *
	 * !!TODO: currently it is only a simple linear interpolation; some higher
	 * order interpolation will be needed.
	 * 
	 * @param t
	 * @return
	 */
	public float interpolate(float t) {
		if (controlPoints.size() == 0) return 0.0f;
		//!!TODO: optimize

		// we are left from the smallest control point
		if (t < controlPoints.get(0).x) 
			return controlPoints.get(0).y;
		
		for (int i = 0; i < controlPoints.size()-1; i++) {
			if (controlPoints.get(i+1).x > t) {
				float x0 = controlPoints.get(i).x;
				float x1 = controlPoints.get(i+1).x;
				float v = (t-x0)/(x1-x0);
				
				return (1.0f-v)*controlPoints.get(i).y + (v)*controlPoints.get(i+1).y;
			}
		}
		
		// we are right from the rightmost control point
		return controlPoints.get(controlPoints.size()-1).y;
	}
	
	
	
	public static void main(String[] args) {
		Curve c = new Curve();
		
		c.addControlPoint(new Vector2f(0.5f, 0.0f));
		c.addControlPoint(new Vector2f(1.0f, 1.0f));
		c.addControlPoint(new Vector2f(0.0f, 1.0f));
		
		System.out.println(c.interpolate(-1.0f));
		System.out.println(c.interpolate(0.0f));
		System.out.println(c.interpolate(0.1f));
		System.out.println(c.interpolate(0.5f));
		System.out.println(c.interpolate(0.75f));
		
		
	}
	
	
}
