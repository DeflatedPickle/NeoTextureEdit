package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;

public class FilterBlur extends Channel {
	FloatParam radius = CreateLocalFloatParam("Radius", 1, 0, 50).setDefaultIncrement(0.25f);
	IntParam numSamples = CreateLocalIntParam("# Samples", 64, 8, 1024);
	EnumParam weightFunction = CreateLocalEnumParam("Weight", "Gaussian,Box");

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
		"radius: the blur radius in % of the image width.";
	}

	public OutputType getOutputType() {
		return OutputType.RGBA;
	}

	public OutputType getChannelInputType(int idx) {
		return OutputType.RGBA;
	}

	//!!TODO: find a way do avoid duplicating the filter code for the cached and for the uncached _valueRGBA function
	protected void cache_function(Vector4 out, TileCacheEntry[] caches,	int localX, int localY, float u, float v) {
		Vector4 val = new Vector4();
		
		float r = radius.get()/100.0f;
		float weightSum = 0.0f;
		
		if (weightFunction.getEnumPos() == 0 && r > 0.0f) { // Gaussian
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				float l = FMath.sqrt(du*du + dv*dv);
				float w = FMath.exp(-(l/r));
				val.add_ip(caches[0].sample_Normalized(du+u, dv+v).mult_ip(w));
				weightSum += w;
			}
		} else { // Box
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				val.add_ip(caches[0].sample_Normalized(du+u, dv+v));
				weightSum += 1.0f;
			}
		
		}
		
		val.mult_ip(1.0f/weightSum);
		out.set(val);
	}

	protected Vector4 _valueRGBA(float u, float v) {
		Vector4 val = new Vector4();
		
		float r = radius.get()/100.0f;
		float weightSum = 0.0f;
		
		if (weightFunction.getEnumPos() == 0 && r > 0.0f) { // Gaussian
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				float l = FMath.sqrt(du*du + dv*dv);
				float w = FMath.exp(-(l/r));
				val.add_ip(inputChannels[0].valueRGBA(du+u, dv+v).mult_ip(w));
				weightSum += w;
			}
		} else { // Box
			for (int i = 0; i < numSamples.get(); i++) {
				float du = (FMath.radicalInverse_vdC(2, i)*2.0f - 1.0f)*r;
				float dv = (FMath.radicalInverse_vdC(3, i)*2.0f - 1.0f)*r;
				val.add_ip(inputChannels[0].valueRGBA(du+u, dv+v));
				weightSum += 1.0f;
			}
		
		}
		
		val.mult_ip(1.0f/weightSum);
		return val;
	}

}
