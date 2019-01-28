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

import org.joml.Vector4f;

import java.util.Vector;

//!!TODO: this can be optimized easily
public final class ColorGradient {

	private static final class Entry {
		public Vector4f color;
		public float position;

		public Entry(Vector4f col, float pos) {
			color = col;
			position = pos;
		}
	}
	final Vector<Entry> entries = new Vector<Entry>();
	
/*	
	public ColorGradient getCopy() {
		ColorGradient ret = new ColorGradient();
		for (int i = 0; i < entries.size(); i++) {
			ret.entries.add(new Entry(new Vector4(entries.get(i).color), entries.get(i).position));
		}
		return ret;
	}
	*/

	
	public int getNumEntries() {
		return entries.size();
	}
	
	public void deleteEntry(int idx) {
		entries.remove(idx);
	}
	
	public Vector4f getEntryColor(int idx) {
		return entries.get(idx).color;
	}

	public float getEntryPosition(int idx) {
		return entries.get(idx).position;
	}
	
	public void updatePosition(int idx, float val) {
		entries.get(idx).position = val;
	}
	
	public void updateColorRGB(int idx, float r, float g, float b) {
		entries.get(idx).color.set(new Vector4f(r, g, b, entries.get(idx).color.w));
	}
	
	public void updateAlpha(int idx, float a) {
		entries.get(idx).color.w = a;
	}
	
	
	// makes sure that all entrys are in [0, 1]
	public ColorGradient renormalize() {
		if (getNumEntries() < 2) return this;
		float start = entries.firstElement().position;
		float norm = 1.0f/entries.lastElement().position;
		for (int i = 0 ; i < getNumEntries(); i++) {
			entries.get(i).position = (entries.get(i).position - start) * norm;
		}
		return this;
	}
	
	
	public ColorGradient addEntryRGB(float r, float g, float b, float pos) {
		return addEntry(new Vector4f(r, g, b, 1.0f), pos);
	}

	public ColorGradient addEntry(Vector4f color, float pos) {
		if (entries.size() == 0)
			entries.add(new Entry(color, pos));
		else if (entries.firstElement().position > pos)
			entries.add(0, new Entry(color, pos));
		else if (entries.lastElement().position < pos)
			entries.add(new Entry(color, pos));
		else {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).position > pos) {
					entries.add(i, new Entry(color, pos));
					break;
				}
			}
		}
		
		return this;
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void setFrom(ColorGradient g) {
		if (g == null) return;
		if (g.getNumEntries() <= 1) return;
		clear();
		for (int i = 0; i < g.getNumEntries(); i++) {
			addEntry(new Vector4f(g.getEntryColor(i)), g.getEntryPosition(i));
		}
	}
	
	
	public Vector4f getColor(float pos) {
		Vector4f ret = new Vector4f();
		
		if (pos <= entries.firstElement().position) ret.set(entries.firstElement().color);
		else if (pos >= entries.lastElement().position) ret.set(entries.lastElement().color);
		else {
			for (int i = 0; i < entries.size()-1; i++) {
				if (entries.get(i+1).position > pos) {
					Entry a = entries.get(i);
					Entry b = entries.get(i+1);
					
					float interp = (pos-a.position)/(b.position-a.position);
					//ret.set(interp);
					ret.set(a.color);
					ret.mul(1.0f - interp);
					ret.fma(interp, b.color);

					break;
				}
			}
		}
		
		return ret;
	}
	
}
