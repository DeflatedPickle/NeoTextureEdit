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

/**
 * This class can be extended to have a parameter manager that locally
 * manages all created parameters and is called when a parameter changes.
 * @author Holger Dammertz
 *
 */
public class LocalParameterManager  implements ParamChangeListener {
	public Vector<AbstractParam> m_LocalParameters = new Vector<AbstractParam>();
	
	/**
	 * You need to override this method if you want to get notified
	 * of a parameter change.
	 */
	public void parameterChanged(AbstractParam source) {
		
	}
	
	protected TextParam CreateLocalTextParam(String name, String value) {
		TextParam p = TextParam.Create(name, value);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected FloatParam CreateLocalFloatParam(String name, float value, float min, float max) {
		FloatParam p = FloatParam.Create(name, value, min, max);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected IntParam CreateLocalIntParam(String name, int value, int min, int max) {
		IntParam p = IntParam.Create(name, value, min, max);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected ColorParam CreateLocalColorParam(String name, float r, float g, float b) {
		ColorParam p = ColorParam.Create(name, r, g, b);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected ColorGradientParam CreateLocalColorGradientParam(String name) {
		ColorGradientParam p = ColorGradientParam.Create(name);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	

	protected EnumParam CreateLocalEnumParam(String name, String enumList) {
		EnumParam p = EnumParam.Create(name, enumList);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected BoolParam CreateLocalBoolParam(String name, boolean value) {
		BoolParam p = BoolParam.Create(name, value);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected ImageParam CreateLocalImageParam(String name, String filename) {
		ImageParam p = ImageParam.Create(name, filename);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected InfoParam CreateLocalInfoParam(String name, String message) {
		InfoParam p = InfoParam.Create(name, message);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	protected Matrix3x3Param CreateLocalMatrix3x3Param(String name) {
		Matrix3x3Param p = Matrix3x3Param.Create(name);
		m_LocalParameters.add(p);
		p.addParamChangeListener(this);
		return p;
	}
	
	
	public Vector<AbstractParam> getParameters() {
		return m_LocalParameters;
	}
	
	public AbstractParam getParamByName(String name) {
		for (int i = 0; i < m_LocalParameters.size(); i++) {
			if (m_LocalParameters.get(i).getName().equals(name)) return m_LocalParameters.get(i);
		}
		return null;
	}
	

}
