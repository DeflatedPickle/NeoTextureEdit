package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;
import org.joml.Vector4f;

public class FilterBlur extends Channel {
	FloatParam radius = CreateLocalFloatParam("Radius", 1, 0, 50).setDefaultIncrement(0.25f);
	IntParam numSamples = CreateLocalIntParam("# Samples", 64, 8, 1024);
	EnumParam weightFunction = CreateLocalEnumParam("Weight", "Gaussian,Box");
	FloatParam anisotropy = CreateLocalFloatParam("Anisotropy", 0.0f, 0.0f, 1.0f).setDefaultIncrement(0.125f);
	FloatParam angle = CreateLocalFloatParam("Angle", 0.0f, 0.0f, 180.0f).setDefaultIncrement(30.0f);

	public String getName() {
		return "Blur";
	}

	public FilterBlur() {
		super(1);
		vizType = ChannelVizType.SLOW;
	}
	
	public String getHelpText() {
		return "Basic Blur Filter \n" +
		"Warning: This filter can be very SLOW. \n" +
		"In the current implementation it uses a fixed # Samples\n" +
		"independent of the filter radius or output resolution.\n\n" +
		"radius: the blur radius in % of the image width.\n" + 
		"anisotropy: 0: no anisotropy 1:full anisotropy.\n" +
		"angle: 0-180 degree angle of anisotropy.";
	}

	public OutputType getOutputType() {
		return OutputType.RGBA;
	}

	public OutputType getChannelInputType(int idx) {
		return OutputType.RGBA;
	}
	
	
	private Vector4f performFilter(Vector4f out, TileCacheEntry[] caches, float u, float v) {
		Vector4f val = new Vector4f();
		
		float r = radius.get()/100.0f;
		float weightSum = 0.0f;
		
		float rotU = FMath.cos(FMath.PI*angle.get()/180.0f);
		float rotV = FMath.sin(FMath.PI*angle.get()/180.0f);
		
		if (weightFunction.getEnumPos() == 0 && r > 0.0f) { // Gaussian
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				// apply anisotropy
				du *= (1.0f - anisotropy.get());
				float nu = du*rotU - dv*rotV;
				float nv = dv*rotU + du*rotV;
				du = nu;
				dv = nv;

				float l = FMath.sqrt(du*du + dv*dv);
				float w = FMath.exp(-(l/r));
				
				
				if (caches != null) val.add(caches[0].sample_Normalized(du+u, dv+v).mul(w));
				else val.add(inputChannels[0].valueRGBA(du+u, dv+v).mul(w));
				weightSum += w;
			}
		} else { // Box
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				// apply anisotropy
				du *= (1.0f - anisotropy.get());
				float nu = du*rotU - dv*rotV;
				float nv = dv*rotU + du*rotV;
				du = nu;
				dv = nv;

				if (caches != null) val.add(caches[0].sample_Normalized(du+u, dv+v));
				else val.add(inputChannels[0].valueRGBA(du+u, dv+v));
				weightSum += 1.0f;
			}
		
		}
		
		val.mul(1.0f/weightSum);
		if (out != null) out.set(val);
		return val;
	}
	

	protected void cache_function(Vector4f out, TileCacheEntry[] caches,	int localX, int localY, float u, float v) {
		performFilter(out, caches, u, v);
	}

	protected Vector4f _valueRGBA(float u, float v) {
		return performFilter(null, null, u, v);
	}

}
