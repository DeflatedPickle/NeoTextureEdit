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

public class IntParam extends AbstractParam {
	private int min;
	private int max;
	private int value;

	private IntParam(String name, int value, int min, int max) {
		this.min = min;
		this.max = max;
		this.name = name;
		this.value = value;
		checkBounds();
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(value+" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		set(s.nextInt());
	}
	
	public boolean checkBounds() {
		if (value > max) {value = max; return false;}
		if (value < min) {value = min; return false;}
		return true;
	}
	
	public void set(int v) {
		if (value == v) return;
		value = v;
		checkBounds();
		notifyParamChangeListener();
	}
	
	public int get() {
		return value;
	}
	
	public void increment() {
		set(value + 1);
	}

	public void decrement() {
		set(value - 1);
	}
	
	public static IntParam create(String name, int value, int min, int max) {
		IntParam ret = new IntParam(name, value, min, max);
		return ret;
	}

	
	public static IntParam createManaged(String name, int value, int min, int max) {
		IntParam ret = create(name, value, min, max);
		ParameterManager.add(ret);
		return ret;
	}
}
