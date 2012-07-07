package engine.graphics.synthesis.texture;

import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.BoolParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;

public class FilterModulus extends Channel {
	FloatParam modulus;
	BoolParam normalize = CreateLocalBoolParam("Normalize", true);
	IntParam xBias = CreateLocalIntParam("X Bias", 0, 0, 64);

	public String getName() {
		return "Modulus";
	}

	public String getHelpText() {
		return "Computes the modulus of the input.";
	}

	public FilterModulus() {
		super(1);
		modulus = CreateLocalFloatParam("Modulus", 0.5f, 0.0f, 1.0f);
	}

	public OutputType getOutputType() {
		return OutputType.RGBA;
	}

	public OutputType getChannelInputType(int idx) {
		if (idx == 0) return OutputType.RGBA;
		else System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}

	private final Vector4 _function(Vector4 in0, float u, float v) {
		Vector4 c = new Vector4(in0);
		float m = modulus.get();
		float s = normalize.get() ? 1f / m : 1f;
		float bias = u * m * xBias.get();
		c.x = Math.max(0.0f, Math.min(1.0f, ((c.x + bias) % m) * s));
		c.y = Math.max(0.0f, Math.min(1.0f, ((c.y + bias) % m) * s));
		c.z = Math.max(0.0f, Math.min(1.0f, ((c.z + bias) % m) * s));
		return c;
	}

	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		out.set(_function(caches[0].sample(localX, localY), u, v));
	}


	protected Vector4 _valueRGBA(float u, float v) {
		Vector4 c0 = inputChannels[0].valueRGBA(u, v);
		return _function(c0, u, v);
	}
}
