package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.base.Vector3;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.Matrix3x3Param;

public class FilterTransform extends Channel {
	
	Matrix3x3Param transformation = CreateLocalMatrix3x3Param("Transformation");
	
	public String getName() {
		return "Transform";
	}

	public FilterTransform() {
		super(1);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		return OutputType.RGBA;
	}
	
	
	final protected Vector3 transform(float u, float v) {
		Vector3 p = transformation.getMatrix().mult(new Vector3(u, v, 1.0f));
		p.x = p.x - FMath.ffloor(p.x);
		p.y = p.y - FMath.ffloor(p.y);
		return p;
	}
	
	//!!TODO; find an acceptable way to use the cache here (tiled cache is the problem here)
	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		Vector3 p = transform(u, v);
		out.set(inputChannels[0].valueRGBA(p.x, p.y));
	}

	
	protected Vector4 _valueRGBA(float u, float v) {
		Vector3 p = transform(u, v);
		return inputChannels[0].valueRGBA(p.x, p.y);
	}

}
