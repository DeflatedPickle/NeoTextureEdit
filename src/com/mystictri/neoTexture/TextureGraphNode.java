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

package com.mystictri.neoTexture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.Channel.ChannelVizType;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.graphics.synthesis.texture.Pattern;

/**
 * A TextureNode represents graphically a Channel (Pattern or Filter) from
 * the texture generation source code. It also manages the connections
 * 
 * @author Holger Dammertz
 * 
 */
public final class TextureGraphNode {
	public static final int width = 64 + 8; 
	public static final int height = 64 + 16 +12;
	
	Channel texChannel;
	
	Point loc = new Point();
	
	public void setLocation(int x, int y) {
		loc.setLocation(x, y);
	}
	
	public void setLocation(Point p) {
		loc.setLocation(p);
	}
	
	public int getX() {
		return loc.x;
	}
	
	public int getY() {
		return loc.y;
	}
	
	public Point getLocation() {
		return loc;
	}
	
	public Channel getChannel() {
		return texChannel;
	}

	// saves only the node; not the Connection!!
	public void save(Writer w, TextureGraphNode n) throws IOException {
		w.write(String.format("%d %d\n", n.getX(), n.getY()));
		Channel.saveChannel(w, texChannel);
	}

	public static TextureGraphNode load(Scanner s) {
		int x = s.nextInt();
		int y = s.nextInt();
		TextureGraphNode ret = new TextureGraphNode(Channel.loadChannel(s));
		ret.setLocation(x, y);
		return ret;
	}
	
	/**
	 * @return a true copy of the current node
	 */
	public TextureGraphNode cloneThisNode() {
		TextureGraphNode ret = new TextureGraphNode(Channel.cloneChannel(texChannel));
		ret.setLocation(getLocation());
		return ret;
	}

	
	public static class ConnectionPoint {
		public int x, y;
		//public int direction; // 0 input; 1 output
		public Channel.OutputType type;
		public int channelIndex; // -1: input node; else the output connections
		public int width = 8;
		public int height = 8;

		//public TextureNodeConnection connection; // only input nodes have a
		// connection

		public TextureGraphNode parent;
		
		public ConnectionPoint(TextureGraphNode parent, int x, int y, int index, Channel.OutputType type) {
			this.x = x;
			this.y = y;
			//this.direction = dir;
			this.type = type;
			this.parent = parent;
			this.channelIndex = index;
		}

		public Point getWorldSpaceCenter() {
			Point p = parent.getLocation();
			p.x += x + 4;
			p.y += y + 4;
			return p;
		}

		public void draw(Graphics g) {
			if (channelIndex == -1) { // input
				g.setColor(Color.green);
				g.fillRect(x, y, width, height);
			} else { // output
				g.setColor(Color.red);
				g.fillRect(x, y, width, height);
			}
		}

		// Utility method to check if the mouse position is insied the
		// connection point
		public boolean inside(int px, int py) {
			return ((px >= x) && (px <= (x + width)) && (py >= y) && (py <= (y + height)));
		}
	}

	/** 
	 * Each node has only a single output connection point which is stored
	 * here; it is also
	 * the first element in the connPoints list.
	 */
	ConnectionPoint outputConnectionPoint;
	
	
	public ConnectionPoint getOutputConnectionPoint() {
		return outputConnectionPoint;
	}
	
	/** 
	 * all connection points for this Node (the first one is the outputConnectionPoint
	 */
	Vector<ConnectionPoint> allConnectionPoints = new Vector<ConnectionPoint>();
	
	
	public Vector<ConnectionPoint> getAllConnectionPointsVector() {
		return allConnectionPoints;
	}

	public TextureGraphNode(Channel channel) {
		if (channel != null)
			setChannel(channel);
	}

	public ConnectionPoint getInputConnectionPointByChannelIndex(int index) {
		for (int i = 0; i < allConnectionPoints.size(); i++) {
			if (allConnectionPoints.get(i).channelIndex == index)
				return allConnectionPoints.get(i);
		}
		Logger.logError(this, "no connection point found for index " + index + " in class " + this);
		return null;
	}

	

	public void setChannel(Channel channel) {
		if (channel == texChannel)
			return;
		texChannel = channel;
		
		//if (texChannel instanceof Pattern) bgColor = ms_PatternColor;
		
		//if (texChannel.vizType != ChannelVizType.NORMAL) bgColor = ms_SlowColor; 
		
		allConnectionPoints.clear();
		// output
		outputConnectionPoint =  new ConnectionPoint(this, width / 2 - 4, height - 8, -1, texChannel.getOutputType());
		allConnectionPoints.add(outputConnectionPoint);
		// inputs
		int x = 8;
		for (int i = 0; i < texChannel.getNumInputChannels(); i++) {
			allConnectionPoints.add(new ConnectionPoint(this, x, 0, i, texChannel.getChannelInputType(i)));
			x += 12;
		}

		//channel.addChannelChangeListener(this);
		//updatePreviewImage();
	}

	

	
}

