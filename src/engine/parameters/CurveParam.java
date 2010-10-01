package engine.parameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public final class CurveParam  extends AbstractParam {
	private final Curve curve;
	
	private CurveParam(String name, int numCP, float min, float max) {
		this.name = name;
		curve = new Curve();
	}
	
	public Curve getCurve() {
		return curve;
	}
	
	public void save(Writer w) throws IOException  {
		//!!TODO
		//w.write(valuePos+" ");
	}

	public void load(Scanner s) {
		//!!TODO
		//setEnumPos(s.nextInt());
	}
	
	public static CurveParam create(String name, int numCP, float min, float max) {
		CurveParam ret = new CurveParam(name, numCP, min, max);
		return ret;
	}

	
	public static CurveParam createManaged(String name, int numCP, float min, float max) {
		CurveParam ret = create(name, numCP, min, max);
		ParameterManager.add(ret);
		return ret;
	}
}
