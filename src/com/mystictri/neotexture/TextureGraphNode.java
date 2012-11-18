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

package com.mystictri.neotexture;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.Vector;

import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;

/**
 * A TextureNode represents a Channel (Pattern or Filter) from
 * the texture generation source code. It also manages the connections.
 * It contains also an absolute position that should be used for graph rendering.
 * 
 * !!TODO: simplify the location handling (and remove most of the methods)
 * 
 * @author Holger Dammertz
 * 
 */
public final class TextureGraphNode {
	public static final int width = 64 + 8; 
	public static final int height = 64 + 16 + 12;
	
	// this is currently a 'workaround' for the TextureGraphEditorPanel to efficiently store preview images
	public Object userData = null;
	
	Channel texChannel;
	
	int posX, posY;
	boolean folded = false;
	
	public void setLocation(int x, int y) {
		posX = x;
		posY = y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		if (folded) return height - 64;
		return height;
	}
	
	
	public int getX() {
		return posX;
	}
	
	public int getY() {
		return posY;
	}
	
	public boolean isFolded() {
		return folded;
	}
	
	public void setFolded(boolean f) {
		folded = f;
	}
	
	
	public Channel getChannel() {
		return texChannel;
	}
	
	public void movePosition(int dx, int dy) {
		posX += dx;
		posY += dy;
	}

	// saves only the node; not the Connection!!
	public void save(Writer w, TextureGraphNode n) throws IOException {
		w.write(String.format("%d %d %d\n", n.getX(), n.getY(), folded?1:0));
		Channel.saveChannel(w, texChannel);
	}

	public static TextureGraphNode load(Scanner s) {
		int x = s.nextInt();
		int y = s.nextInt();
		boolean isFolded = false;
		if (s.hasNextInt()) { // this check is needed to be compatible to files savec in version 0.6.3 and earlier (where no folding existed)
			isFolded = (s.nextInt() == 1); 
		}
		TextureGraphNode ret = new TextureGraphNode(Channel.loadChannel(s));
		ret.setLocation(x, y);
		ret.setFolded(isFolded);
		return ret;
	}
	
	/**
	 * @return a true copy of the current node
	 */
	public TextureGraphNode cloneThisNode() {
		TextureGraphNode ret = new TextureGraphNode(Channel.cloneChannel(texChannel));
		ret.setLocation(getX(), getY());
		return ret;
	}

	
	/**
	 * Checks if the given point (in world coordinates) is contained inside the node
	 * by checking if (getX() <= x <= getX()+width) && (getY() <= y <= getY()+width))
	 * @param x the x position in world coordinates
	 * @param y the y position in world coordinates
	 * @return
	 */
	public boolean containsPoint(int x, int y) {
		return (x >= posX) && (x <= posX+width) && (y >= posY) && (y <= posY + height);
	}
	
	/**
	 * A ConnectionPoint is attached to a specific TextureGraphNode parent and represents
	 * either an input or an output of this TextureGraphNode. It has an internal position
	 * and size that are relative to the parent.
	 * 
	 * @author Holger Dammertz
	 *
	 */
	public static class ConnectionPoint {
		int x, y;
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
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			if (y > 16 && parent.isFolded()) return y - 64; //!!HACK
			return y;
		}
		

		public int getWorldSpaceX() {
			return parent.getX() + getX() + 4;
		}

		public int getWorldSpaceY() {
			return parent.getY() + getY() + 4;
		}

	
		// Utility method to check if the mouse position is inside the
		// connection point
		public boolean inside(int px, int py) {
			return ((px >= getX()+parent.getX()) && (px <= (getX()+parent.getX() + width)) && (py >= getY()+parent.getY()) && (py <= (getY()+parent.getY() + height)));
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

