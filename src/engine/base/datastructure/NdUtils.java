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

package engine.base.datastructure;

import engine.base.FMath;

public class NdUtils {

	public static float distance2(NdPositionable p0, NdPositionable p1) {
		float d2 = 0.0f;
		for (int i = 0; i < p0.getDIM(); i++) {
			float v = p1.getPos(i) - p0.getPos(i);
			d2 += v*v;
		}
		return d2;
	}

	public static float distance(NdPositionable p0, NdPositionable p1) {
		float d2 = 0.0f;
		for (int i = 0; i < p0.getDIM(); i++) {
			float v = p1.getPos(i) - p0.getPos(i);
			d2 += v*v;
		}
		return FMath.sqrt(d2);
	}
}
