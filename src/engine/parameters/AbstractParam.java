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
import java.util.Vector;

public class AbstractParam {
	public static boolean SILENT = false;
	protected String name;
	protected Vector<ParamChangeListener> changeListener = new Vector<ParamChangeListener>();
	
	public boolean hidden = false; // it is application dependent what hidden means; usually a hidden parameter does not appear in the editor 
	
	public void addParamChangeListener(ParamChangeListener listener) {
		changeListener.add(listener);
	}

	public void removeParamChangeListener(ParamChangeListener listener) {
		changeListener.remove(listener);
	}
	
	public void save(Writer w) throws IOException {
		System.out.println("Warning: unimplemented save method in param " + name + " " + this);
	}
	
	public void load(Scanner s) {
		System.out.println("Warning: unimplemented load method in param " + name + " " + this);
	}
	
	public String getName() {
		return name;
	}
	
	
	public void notifyParamChangeListener() {
		if (SILENT) return;
		for (int i = 0; i < changeListener.size(); i++) {
			changeListener.get(i).parameterChanged(this);
		}
	}
}
