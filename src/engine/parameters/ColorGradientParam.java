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

import engine.base.Vector4;

public final class ColorGradientParam extends AbstractParam {
	private final ColorGradient m_Gradient;
	
	private ColorGradientParam(String name) {
		this.name = name;
		m_Gradient = new ColorGradient();
		// default grayscale gradient
		m_Gradient.addEntry(new Vector4(0.0f, 0.0f, 0.0f, 1.0f), 0.0f);
		m_Gradient.addEntry(new Vector4(1.0f, 1.0f, 1.0f, 1.0f), 1.0f);
	}
	
	// saves only the value
	public void save(Writer w) throws IOException  {
		w.write(m_Gradient.getNumEntries() + " ");
		for (int i = 0; i < m_Gradient.getNumEntries(); i++) {
			Vector4 v = m_Gradient.getEntryColor(i);
			w.write(v.x+" "+v.y+" "+v.z+" "+v.w+" ");
			w.write(m_Gradient.getEntryPosition(i) + " ");
		}
	}
	// loads only the value; uses the set method to set it (thus change listener are notified)
	public void load(Scanner s) {
		m_Gradient.clear();
		int num = s.nextInt();
		for (int i = 0; i < num; i++) {
			// !!TODO: this is a workaround for a weirded windows s.nextFloat bug;
			//m_Gradient.addEntry(new Vector4(s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat()), s.nextFloat());
			m_Gradient.addEntry(new Vector4(Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next())),Float.parseFloat(s.next()));
		}
		notifyParamChangeListener();
	}
	
	
	public ColorGradient get() {
		return m_Gradient;
	}
	

	public static ColorGradientParam Create(String name) {
		ColorGradientParam ret = new ColorGradientParam(name);
		return ret;
	}

	
	public static ColorGradientParam CreateManaged(String name) {
		ColorGradientParam ret = Create(name);
		ParameterManager.add(ret);
		return ret;
	}
}
