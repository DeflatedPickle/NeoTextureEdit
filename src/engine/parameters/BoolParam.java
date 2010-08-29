/**
    Copyright (C) 2010  Holger Dammertz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package engine.parameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class BoolParam extends AbstractParam {
	boolean value;
	
	private BoolParam(String name, boolean value) {
		this.name = name;
		this.value = value;
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(value+" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		set(s.nextBoolean());
	}
	
	
	public void set(boolean v) {
		if (value == v) return;
		value = v;
		notifyParamChangeListener();
	}
	
	public void invert() {
		set(!value);
	}
	
	public boolean get() {
		return value;
	}
	
	public static BoolParam Create(String name, boolean value) {
		BoolParam ret = new BoolParam(name, value);
		return ret;
	}
	
	public static BoolParam CreateManaged(String name, boolean value) {
		BoolParam ret = Create(name, value);
		ParameterManager.add(ret);
		return ret;
	}
	
}
