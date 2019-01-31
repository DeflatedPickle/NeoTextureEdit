/**
    Copyright (C) 2010,2012  Holger Dammertz

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

package com.mystictri.neotextureedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.mystictri.neotextureedit.parameters.AbstractParameterEditor;
import engine.parameters.SpectralControlParam;

public class SpectralControlParameterEditor extends AbstractParameterEditor implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = -4583024426461981794L;
	
	private SpectralControlParam m_Param;
	
	boolean needUpdate = false;
	
	float mouseValue = -1.0f;
	int mouseX, mouseY;
	
	public SpectralControlParameterEditor(SpectralControlParam param) {
		m_Param = param;
		int width = NAME_WIDTH + 2*BUTTON_WIDTH + TEXTFIELD_WIDTH;
		int height = width/2;
		setPreferredSize(new Dimension(width, height));
		setSize(getPreferredSize());
		setLayout(null);
		
		//setBorder(BorderFactory.createTitledBorder(m_Param.getName()));
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	
	public void paint(Graphics g) {
		super.paint(g);
		
		g.clearRect(0, 0, getWidth(), getHeight());
		
		int num = m_Param.getEndBand() - m_Param.getStartBand() + 1;
		
		int dw = getWidth()/num;
		int height = getHeight();
		
		g.setColor(Color.blue);
		for (int i = 0; i < num; i++) {
			int band = m_Param.getStartBand() + i;
			float h = m_Param.get(band, 0.5f);
			int ih = (int)(h*height);
			g.fillRect(i*dw, height - ih, dw, ih);
		}
		
		g.setColor(Color.lightGray);
		for (int i = 0; i < num; i++) {
			g.drawLine((i+1)*dw, 0, (i+1)*dw, height);
		}
		

		if (mouseValue >= 0.0f) {
			int py = mouseY;
			int px = mouseX;
			
			String text = String.format("%.4f", mouseValue);
			if (mouseY < getHeight()/4) py += 32;
			else py -= 8;
			if (mouseX > getWidth()/2) {
				px -= g.getFontMetrics().stringWidth(text);
			}
			
			if (py < 0) py = 0;
			if (py > getHeight()-16) py = getHeight()-16;
				
			g.setColor(Color.black);
			g.drawString(text, px, py);
			g.setColor(Color.white);
			g.drawString(text, px-1, py-1);
			mouseValue = -1.0f;
		}

	}
	
	/** sets the spectral control parameter to the value where the mouse (mx, my) points to
	 * CTRL-Key allows for continous update (else the m_Param is set to silent)
	 * @param mx mouse x position
	 * @param my mouse y position
	 * @param modifier the current key modifier associated with the mouse event
	 */
	void setAtMouse(int mx, int my, int modifier) {
		if ((InputEvent.CTRL_MASK & modifier) != 0) {
			m_Param.setSilent(false);
		} else {
			m_Param.setSilent(true);
		}
		
		int num = m_Param.getEndBand() - m_Param.getStartBand() + 1;
		int band = m_Param.getStartBand() + (mx*num)/getWidth();
		
		if (band < m_Param.getStartBand() || band > m_Param.getEndBand()) return;
		
		float h = 1.0f - (float)my/(float)getHeight();
		if (h > 1.0f) h = 1.0f;
		if (h < 0.0f) h = 0.0f;
		
		m_Param.set(band, h);
		needUpdate = true;
		mouseX = mx;
		mouseY = my;
		mouseValue = h;
		repaint();
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		m_Param.setSilent(false);
		if (needUpdate) m_Param.notifyParamChangeListener();
		mouseValue = -1.0f;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setAtMouse(e.getX(), e.getY(), e.getModifiers());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		m_Param.setSilent(false);
		if (needUpdate) m_Param.notifyParamChangeListener();
		mouseValue = -1.0f;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		setAtMouse(e.getX(), e.getY(), e.getModifiers());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

}
