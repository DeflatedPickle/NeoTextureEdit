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

import java.util.Vector;

import javax.swing.event.ChangeListener;


/**
 * This class manages all adjustable parameters. It contains only static members.
 * @author Holger Dammertz
 *
 */
public class ParameterManager {
	
	public static Vector<AbstractParam> parameters = new Vector<AbstractParam>();
	static Vector<ChangeListener> changeListeners = new Vector<ChangeListener>();
	
	public static synchronized void add(AbstractParam p) {
		parameters.add(p);
		notifyChangeListener();
	}
	
	static synchronized void notifyChangeListener() {
		//ChangeEvent e = new ChangeEvent(null);
		for (int i = 0; i < changeListeners.size(); i++) {
			changeListeners.get(i).stateChanged(null);
		}
	}
	
	public static void addChangeListener(ChangeListener c) {
		changeListeners.add(c);
	}
	
	
}
