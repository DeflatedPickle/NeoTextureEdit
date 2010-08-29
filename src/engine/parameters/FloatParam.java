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

public class FloatParam extends AbstractParam {
	private float min;
	private float max;
	private float value;
	
	private float defaultIncrement;

	private FloatParam(String name, float value, float min, float max) {
		this.min = min;
		this.max = max;
		this.name = name;
		this.value = value;
		defaultIncrement = 1.0f;
		checkBounds();
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(value+" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		//set(s.nextFloat());
		// !!TODO: this is a workaround for a weirded windows s.nextFloat bug;
		set(Float.parseFloat(s.next()));
	}
	
	public void increment() {
		set(value + defaultIncrement);
	}

	public void increment(float inc) {
		set(value + inc);
	}

	public void decrement(float dec) {
		set(value - dec);
	}

	public void decrement() {
		value -= defaultIncrement;
		checkBounds();
		notifyParamChangeListener();
	}
	
	public FloatParam setDefaultIncrement(float inc) {
		defaultIncrement = inc;
		return this;
	}
	
	public float getDefaultIncrement() {
		return defaultIncrement;
	}
	
	public boolean checkBounds() {
		if (value > max) {value = max; return false;}
		if (value < min) {value = min; return false;}
		return true;
	}
	
	public void set(float v) {
		if (value == v) return;
		value = v;
		checkBounds();
		notifyParamChangeListener();
	}
	
	public float get() {
		return value;
	}
	
	public String getAsString() {
		return String.format("%.3f", value);
	}
	
	public static FloatParam Create(String name, float value, float min, float max) {
		FloatParam ret = new FloatParam(name, value, min, max);
		return ret;
	}

	
	public static FloatParam CreateManaged(String name, float value, float min, float max) {
		FloatParam ret = Create(name, value, min, max);
		ParameterManager.add(ret);
		return ret;
	}

}
