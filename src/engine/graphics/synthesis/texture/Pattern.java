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

package engine.graphics.synthesis.texture;

/**
 * A Pattern is a function that lives on [0,1)x[0,1)=>[0,1]. It is a channel with 0 input parameters
 * and thus works as a generator. Usually it is scala valued but may also be RGB.
 * @author Holger Dammertz
 *
 */
public class Pattern extends Channel {
	
	public String getName() {
		return "Pattern";
	}

	public OutputType getChannelInputType(int idx) {
		System.err.println("Invalid channel access in " + this);
		return OutputType.SCALAR;
	}
	
}
