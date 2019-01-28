package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.BoolParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import org.joml.Vector4f;

public class FilterMath1 extends Channel {
	FloatParam A = CreateLocalFloatParam("A", 0.0f, -Float.MAX_VALUE, Float.MAX_VALUE).setDefaultIncrement(0.25f);
	
	EnumParam function = CreateLocalEnumParam("Function", "I + a,I * a,a - I,I ^ a");
	
	BoolParam onR = CreateLocalBoolParam("R", true);
	BoolParam onG = CreateLocalBoolParam("G", true);
	BoolParam onB = CreateLocalBoolParam("B", true);
	BoolParam onA = CreateLocalBoolParam("A", false);

	public String getName() {
		return "Math1";
	}

	public String getHelpText() {
		return "Computes some basic math on a single input.";
	}
	
	public FilterMath1() {
		super(1);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}

	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
	float apply(float I) {
		float a = A.get();
		switch (function.getEnumPos()) {
			case 0: return I + a;
			case 1: return I * a;
			case 2: return a - I;
			case 3: return FMath.pow(I, a);
			default:
				System.err.println("Invalid function selector in " + this);
				return I;
		}
	}

	private final Vector4f _function(Vector4f in0, float u, float v) {
		Vector4f c = new Vector4f(in0);
		if (onR.get()) c.x = apply(c.x);
		if (onG.get()) c.y = apply(c.y);
		if (onB.get()) c.z = apply(c.z);
		if (onA.get()) c.w = apply(c.w);
		return c;
	}

	protected void cache_function(Vector4f out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		out.set(_function(caches[0].sample(localX, localY), u, v));
	}


	protected Vector4f _valueRGBA(float u, float v) {
		Vector4f c0 = inputChannels[0].valueRGBA(u, v);
		return _function(c0, u, v);
	}	
}
