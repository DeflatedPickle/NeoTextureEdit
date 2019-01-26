package engine.parameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A simple text parameter storing a string. It should not contain ""
 * @author Holger Dammertz
 *
 */
public class TextParam extends AbstractParam {
	String value;
	
	private TextParam(String name, String value) {
		this.name = name;
		this.value = value;
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write("\""+value+"\" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		String text = s.next(Pattern.compile("\".*\""));
		set(text.substring(1, text.length()-1));
	}
	
	
	public void set(String v) {
		if (value.equals(v)) return;
		value = v;
		notifyParamChangeListener();
	}
	
	public String get() {
		return value;
	}
	
	public static TextParam create(String name, String value) {
		TextParam ret = new TextParam(name, value);
		return ret;
	}
	
	public static TextParam createManaged(String name, String value) {
		TextParam ret = create(name, value);
		ParameterManager.add(ret);
		return ret;
	}
	

}
