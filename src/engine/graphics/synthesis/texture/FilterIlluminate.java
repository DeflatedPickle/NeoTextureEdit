package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.base.Vector3;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.FloatParam;

public final class FilterIlluminate extends Channel {
	FloatParam theta = CreateLocalFloatParam("Theta", 45.0f, 0.0f, 90.0f).setDefaultIncrement(11.5f);
	FloatParam phi = CreateLocalFloatParam("Phi", 45.0f, 0.0f, 360.0f).setDefaultIncrement(22.5f);

	FloatParam shininess = CreateLocalFloatParam("Shininess", 20.0f, 1.0f, 2000.0f).setDefaultIncrement(10.0f);

	public String getName() {
		return "Illuminate";
	}

	public String getHelpText() {
		return "Input 1: A Color channel\n" +
				"Input 2: A Normal-Map\n" +
				"Using a normal map this filter computes a phong illumination.";
	}

	public FilterIlluminate() {
		super(2);
	}

	public OutputType getOutputType() {
		return OutputType.RGBA;
	}

	public OutputType getChannelInputType(int idx) {
		if (idx == 0)
			return OutputType.RGBA;
		else if (idx == 1)
			return OutputType.RGBA;
		else
			System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}

	private final Vector4 _function(Vector4 in0, Vector4 normalmap) {
		normalmap.add_ip(new Vector4(-0.5f));
		normalmap.mult_ip(2.0f);
		Vector3 lightDir = new Vector3();
		lightDir.setDir(theta.get() * (FMath.PI / 180.0f), phi.get() * (FMath.PI / 180.0f));

		Vector3 reflect = (new Vector3(0, 0, -1)).reflect(normalmap.getVector3());

		float ar = reflect.dot(lightDir);
		if (ar < 0)
			ar = 0;
		ar = FMath.pow(ar, shininess.get());

		in0.add_ip(ar);
		in0.clamp(0.0f, 1.0f);

		return in0;
	}

	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		out.set(_function(caches[0].sample(localX, localY), caches[1].sample(localX, localY)));
	}

	protected Vector4 _valueRGBA(float u, float v) {
		return _function(inputChannels[0].valueRGBA(u, v), inputChannels[1].valueRGBA(u, v));
	}
}
