package engine.parameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * The InfoParam is no real parameter but contains a string that can be displayed. This param
 * as all other params notifies the change listener.
 * @author Holger Dammertz
 *
 */
public class InfoParam extends AbstractParam {
	String message;
	
	private InfoParam(String name, String message) {
		this.name = name;
		set(message);
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
	}

	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
	}
	
	
	public void set(String message) {
		if (message.equals(this.message)) return;
		this.message = message; 
		notifyParamChangeListener();
	}
	
	
	public String get() {
		return message;
	}
	
	public static InfoParam create(String name, String message) {
		InfoParam ret = new InfoParam(name, message);
		return ret;
	}
	
	public static InfoParam createManaged(String name, String message) {
		InfoParam ret = create(name, message);
		ParameterManager.add(ret);
		return ret;
	}
}
