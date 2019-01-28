package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.FloatParam;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

	private final Vector4f _function(Vector4f in0, Vector4f normalmap) {
		normalmap.add(new Vector4f(-0.5f));
		normalmap.mul(2.0f);
		Vector3f lightDir = new Vector3f();
		// lightDir.setDir(theta.get() * (FMath.PI / 180.0f), phi.get() * (FMath.PI / 180.0f));
		lightDir.set(FMath.cos(phi.get() * (FMath.PI / 180.0f))*FMath.sin(theta.get() * (FMath.PI / 180.0f)), FMath.sin(phi.get() * (FMath.PI / 180.0f))*FMath.sin(theta.get() * (FMath.PI / 180.0f)), FMath.cos(theta.get() * (FMath.PI / 180.0f)));

		Vector3f reflect = (new Vector3f(0, 0, -1)).reflect(normalmap.x, normalmap.y, normalmap.z);

		float ar = reflect.dot(lightDir);
		if (ar < 0)
			ar = 0;
		ar = FMath.pow(ar, shininess.get());

		in0.add(new Vector4f(ar));
		in0.min(new Vector4f(0.0f), new Vector4f(1.0f));

		return in0;
	}

	protected void cache_function(Vector4f out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		out.set(_function(caches[0].sample(localX, localY), caches[1].sample(localX, localY)));
	}

	protected Vector4f _valueRGBA(float u, float v) {
		return _function(inputChannels[0].valueRGBA(u, v), inputChannels[1].valueRGBA(u, v));
	}
}
