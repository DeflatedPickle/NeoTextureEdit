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

/**
 * Interface for an N-dimensional object that has a locatio like a point.
 * @author Holger Dammertz
 *
 */
public interface NdPositionable {
	//
	public float getPos(int dim);
	
	/**
	 * This function is here as an experiment to try for example in the
	 * KDTree to overwrite the distance measure
	 * @param p
	 * @return
	 */
	public float nd_distance2Func(NdPositionable p);
	
	// returns the dimension of the object
	public int getDIM();
}
