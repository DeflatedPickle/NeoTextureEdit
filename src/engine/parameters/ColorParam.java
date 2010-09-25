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

import engine.base.Vector3;

public final class ColorParam extends AbstractParam {
	final Vector3 color = new Vector3();

	private ColorParam(String name, float r, float g, float b) {
		this.name = name;
		color.set(r, g, b);
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(color.x+" "+color.y+" "+color.z+" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		//set(s.nextFloat(), s.nextFloat(), s.nextFloat());
		set(Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()));
	}
	
	public void set(float r, float g, float b) {
		color.set(r, g, b);
		notifyParamChangeListener();
	}
	
	public Vector3 get() {
		return new Vector3(color);
	}
	
	public static ColorParam Create(String name, float r, float g, float b) {
		ColorParam ret = new ColorParam(name, r, g, b);
		return ret;
	}

	
	public static ColorParam CreateManaged(String name, float r, float g, float b) {
		ColorParam ret = Create(name, r, g, b);
		ParameterManager.add(ret);
		return ret;
	}
	
}
