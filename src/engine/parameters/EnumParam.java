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

/**
 * A Param that stores several strings and can be used as an enum. It can be also seen as an int-parameter where
 * every number from 0 to n has a name. The enumList string should be a comma separated list; no spaces should be
 * between the commas.
 * @author Holger Dammertz
 *
 */
public class EnumParam extends AbstractParam {
	private final Vector<String> enums = new Vector<String>();
	
	private int valuePos;

	private EnumParam(String name, String enumList) {
		valuePos = 0;
		this.name = name;
		
		Scanner s = new Scanner(enumList).useDelimiter(",");
		
		while (s.hasNext()) {
			enums.add(s.next());
		}
		
		if (enums.size() <= 0) System.err.println("WARNING: EnumParam with zero entries!");
		
		checkBounds();
		notifyParamChangeListener();
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(valuePos+" ");
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		setEnumPos(s.nextInt());
	}
	
	public int getNumEnums() {
		return enums.size();
	}
	
	public String getEnumAt(int idx) {
		return enums.get(idx);
	}
	
	public void increment() {
		setEnumPos((valuePos + 1) % enums.size());
	}
	
	public void decrement() {
		int pos = (valuePos - 1);
		if (pos < 0) pos = enums.size()-1;
		setEnumPos(pos);
	}
	
	public boolean checkBounds() {
		if (valuePos >= enums.size()) {valuePos = enums.size()-1; return false;}
		if (valuePos < 0) {valuePos = 0; return false;}
		return true;
	}
	
	public int getIndex(String token) {
		for (int i = 0; i < enums.size(); i++) if (enums.get(i).compareTo(token) == 0) return i;
		return -1;
	}
	
	public EnumParam setEnumPos(int pos) {
		if (valuePos == pos) return this;
		valuePos = pos;
		checkBounds();
		notifyParamChangeListener();
		return this;
	}
	
	public int getEnumPos() {
		return valuePos;
	}
	
	public String getCurrentEnum() {
		return enums.get(valuePos);
	}
	
	
	public static EnumParam Create(String name, String enumList) {
		EnumParam ret = new EnumParam(name,enumList);
		return ret;
	}

	
	public static EnumParam CreateManaged(String name, String enumList) {
		EnumParam ret = Create(name, enumList);
		ParameterManager.add(ret);
		return ret;
	}
}
