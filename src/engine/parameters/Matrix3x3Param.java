package engine.parameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import engine.base.Matrix3x3;

public class Matrix3x3Param extends AbstractParam {
	private final Matrix3x3 mat = new Matrix3x3();
	
	private Matrix3x3Param(String name) {
		this.name = name;
		notifyParamChangeListener();
	}
	
	
	public Matrix3x3 getMatrix() {
		return mat;
	}
	
	
	public void save(Writer w) throws IOException  {
		for (int i = 0; i < 9; i++)
			w.write(mat.get(i) + " ");
	}

	public void load(Scanner s) {
		mat.set(Float.parseFloat(s.next()), Float.parseFloat(s.next()), Float.parseFloat(s.next()), Float.parseFloat(s.next()),
				Float.parseFloat(s.next()), Float.parseFloat(s.next()), Float.parseFloat(s.next()), Float.parseFloat(s.next()), Float.parseFloat(s.next()));
		notifyParamChangeListener();
	}
	
	
	public static Matrix3x3Param create(String name) {
		Matrix3x3Param ret = new Matrix3x3Param(name);
		return ret;
	}
	
	public static Matrix3x3Param createManaged(String name) {
		Matrix3x3Param ret = create(name);
		ParameterManager.add(ret);
		return ret;
	}
	
	
}
