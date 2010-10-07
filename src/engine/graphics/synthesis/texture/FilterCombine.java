package engine.graphics.synthesis.texture;

import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.parameters.EnumParam;

/**
 * Allows to combine 4 input channels arbitrary into and output RGBA image
 * @author Holger Dammertz
 *
 */
public class FilterCombine extends Channel {
	
	EnumParam input0channel = CreateLocalEnumParam("Input 1", "R,G,B,A");
	EnumParam input1channel = CreateLocalEnumParam("Input 2", "R,G,B,A").setEnumPos(1);
	EnumParam input2channel = CreateLocalEnumParam("Input 3", "R,G,B,A").setEnumPos(2);
	EnumParam input3channel = CreateLocalEnumParam("Input 4", "R,G,B,A").setEnumPos(3);
	
	public String getHelpText() {
		return "Combines the 4 inputs into an RGBA image allowing\n" +
				"to select each channel mapping individually.";
	}
	
	public String getName() {
		return "Combine";
	}
	
	public FilterCombine() {
		super(4);
	}
	
	public OutputType getOutputType() {
		return OutputType.RGBA;
	}
	
	public OutputType getChannelInputType(int idx) {
		return OutputType.RGBA;
	}
	
	
	private final void _function(Vector4 out, Vector4 in0, Vector4 in1, Vector4 in2, Vector4 in3) {
		out.set(in0.get(input0channel.getEnumPos()), in1.get(input1channel.getEnumPos()),
				in2.get(input2channel.getEnumPos()), in3.get(input3channel.getEnumPos()));
	}
	
	protected void cache_function(Vector4 out, TileCacheEntry[] caches, int localX, int localY, float u, float v) {
		_function(out, caches[0].sample(localX, localY), caches[1].sample(localX, localY), caches[2].sample(localX, localY), caches[3].sample(localX, localY));
	}
		
	protected Vector4 _valueRGBA(float u, float v) {
		Vector4 c0 = inputChannels[0].valueRGBA(u, v);
		Vector4 c1 = inputChannels[1].valueRGBA(u, v);
		Vector4 c2 = inputChannels[2].valueRGBA(u, v);
		Vector4 c3 = inputChannels[3].valueRGBA(u, v);
		Vector4 ret = new Vector4();

		_function(ret, c0, c1, c2, c3);
		
		return ret;
	}

}
