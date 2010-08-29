package engine.graphics.synthesis.texture;

import engine.base.FMath;
import engine.base.Vector4;
import engine.parameters.ColorGradientParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;

public class PatternFunction extends Pattern {
	public String getName() {
		return "Function";
	}
	
	public String getHelpText() {
		return "Different simple functions.";
	}
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");
	EnumParam functionU = CreateLocalEnumParam("FunctionU", "Sin,Saw,Square");
	EnumParam functionV = CreateLocalEnumParam("FunctionV", "Sin,Saw,Square");
	EnumParam type = CreateLocalEnumParam("Combiner", "Multiply,Add,Max,Min,Pow");
	FloatParam scaleX = CreateLocalFloatParam("ScaleX", 1.0f, 0.0f, Float.MAX_VALUE);
	FloatParam scaleY = CreateLocalFloatParam("ScaleY", 1.0f, 0.0f, Float.MAX_VALUE);;
	
	
	public PatternFunction() {
	}
	
	
	private final float saw(float i) {
		i = i - (int)i;
		if (i > 0.5f) return 2.0f*(1-i);
		else return 2.0f*i;
	}
	
	private final float square(float i) {
		i = i - (int)i;
		if (i > 0.5f) return 0.0f;
		else return 1.0f;
	}
	
	protected Vector4 _valueRGBA(float u, float v) {
		float su = 0.0f; 
		float sv = 0.0f;
		
		if (functionU.getEnumPos() == 0) su = FMath.cos(FMath.PI*2.0f*scaleX.get()*(u+0.5f))*0.5f + 0.5f;
		else if (functionU.getEnumPos() == 1) su = saw(scaleX.get()*u);
		else if (functionU.getEnumPos() == 2) su = square(scaleX.get()*u);
		if (functionV.getEnumPos() == 0) sv = FMath.cos(FMath.PI*2.0f*scaleY.get()*(v+0.5f))*0.5f + 0.5f;
		else if (functionV.getEnumPos() == 1) sv = saw(scaleY.get()*v);
		else if (functionV.getEnumPos() == 2) sv = square(scaleY.get()*v);
		
		
		float val = 0.0f;
		if (type.getEnumPos() == 0) val = su*sv; 
		else if (type.getEnumPos() == 1) val = (su+sv)*0.5f; 
		else if (type.getEnumPos() == 2) val = Math.max(su, sv);
		else if (type.getEnumPos() == 3) val = Math.min(su, sv);
		else if (type.getEnumPos() == 4) val = FMath.pow(su, sv);

		return colorGradientParam.get().getColor(val);
	}
}
