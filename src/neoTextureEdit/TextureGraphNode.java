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

package neoTextureEdit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;


import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.graphics.synthesis.texture.Pattern;
import engine.graphics.synthesis.texture.Channel.ChannelVizType;
import engine.parameters.AbstractParam;

/**
 * A TextureNode represents graphically a Channel (Pattern or Filter) from
 * the texture generation source code. It also manages the connections
 * 
 * @author Holger Dammertz
 * 
 */
class TextureGraphNode extends JPanel implements ChannelChangeListener {
	private static final long serialVersionUID = 1513202488392453658L;

	public static final Color ms_PatternColor = new Color(0x929AAF);
	public static final Color ms_SlowColor = new Color(128,16,16);
	
	static final int width = 64 + 8; 
	static final int height = 64 + 16 +12;
	
	static final int helpW = 16;
	static final int helpH = 16;
	static final int helpX = width - helpW;
	static final int helpY = 0;
	
	static final Font font = new Font("Sans", Font.PLAIN, 10);

	BufferedImage previewImage;
	Channel texChannel;
	Color bgColor = Color.gray;
	
	boolean threadIsRecomputing = false;
	
	
	public static Channel cloneChannel(Channel c) {
		StringWriter sw = new StringWriter();
		try {
			saveChannel(sw, c);
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loadChannel(new Scanner(sw.getBuffer().toString()));
	}

	/**
	 * This method saves a Channel to a given writer by storing all parameters. For
	 * Parameter storage all spaces in a name are replaced with an underscore _
	 * @param w The Writer to write to
	 * @param c The Channel that will be saved
	 * @throws IOException
	 */
	public static void saveChannel(Writer w, Channel c) throws IOException {
		w.write(c.getClass().getName() + "\n");
		for (int i = 0; i < c.m_LocalParameters.size(); i++) {
			AbstractParam param = c.m_LocalParameters.get(i);
			w.write(param.getName().replace(' ', '_') + " ");
			param.save(w);
		}
		// transformation
		w.write("transformation ");
		for (int i = 0; i < 9; i++)
			w.write(c.transformation.get(i) + " ");
		w.write("endparameters\n");
	}

	public static Channel loadChannel(Scanner s) {
		try {
			AbstractParam.SILENT = true;
			Channel c = (Channel) Class.forName(s.next()).newInstance();
			//Logger.log(null, "loadChannel " + c);
			
			String t;
			while (!(t = s.next()).equals("endparameters")) {
				AbstractParam param;
				if ((param = c.getParamByName(t.replace('_', ' '))) != null) {
					param.load(s);
				} else if (t.equals("transformation")){
					c.transformation.set(Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()),Float.parseFloat(s.next()));
				} else {
					Logger.logWarning(null, " loading of param " + t + " failed.");
				}
			}
			AbstractParam.SILENT = false;
			c.parameterChanged(null);
			return c;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			AbstractParam.SILENT = false;
		}
		return null;
	}

	// saves only the node; not the Connection!!
	public void save(Writer w, TextureGraphNode n) throws IOException {
		w.write(String.format("%d %d\n", n.getX(), n.getY()));
		saveChannel(w, texChannel);
	}

	public static TextureGraphNode load(Scanner s) {
		int x = s.nextInt();
		int y = s.nextInt();
		TextureGraphNode ret = new TextureGraphNode(loadChannel(s));
		ret.setLocation(x, y);
		return ret;
	}
	
	/**
	 * @return a true copy of the current node
	 */
	public TextureGraphNode cloneThisNode() {
		TextureGraphNode ret = new TextureGraphNode(cloneChannel(texChannel));
		ret.setLocation(getLocation());
		return ret;
	}

	class ConnectionPoint {
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
	
	/** 
	 * all connection points for this Node (the first one is the outputConnectionPoint
	 */
	Vector<ConnectionPoint> allConnectionPoints = new Vector<ConnectionPoint>();

	public TextureGraphNode(Channel channel) {
		setPreferredSize(new Dimension(width, height));
		setSize(getPreferredSize());
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
		
		if (texChannel instanceof Pattern) bgColor = ms_PatternColor;
		
		if (texChannel.vizType != ChannelVizType.NORMAL) bgColor = ms_SlowColor; 
		

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

		channel.addChannelChangeListener(this);
		updatePreviewImage();
	}

	public void updatePreviewImage() {
		if ((texChannel != null) && (texChannel.chechkInputChannels())) {
			if (previewImage == null)
				previewImage = texChannel.createAndComputeImage(64, 64, null, 0);
			else
				texChannel.computeImage(previewImage, null, 0);
		} else {
			previewImage = null;
		}
		repaint();
	}

	public void paint(Graphics g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (threadIsRecomputing) return;
		
		g.drawImage(previewImage, 4, 12+12, this);
		
		g.setFont(font);
		
		g.setColor(Color.white);
		g.drawString(texChannel.getName(), 2, 12+8);

		g.setColor(Color.white);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		
		for (int i = 0; i < allConnectionPoints.size(); i++) {
			allConnectionPoints.get(i).draw(g);
		}
		
		g.setColor(Color.yellow);
		g.drawString("?", helpX+6, helpY+12);
		
		if (texChannel.isMarkedForExport()) {
			g.drawString("E", 4, helpY+10);
			/*int h = g.getFontMetrics().getHeight() + 2;
			int x = getX() + 2;
			int y = getY() + h;
			
			g.setColor(new Color(0x00505084));
			g.fillRect(x, y-h, 12, h);
			g.setColor(Color.white);
			
			g.drawString("E", x+1, y-2);*/
		}
	}
	




	// this method gets the canvas from the parent component and draws all
	// conneciton lines
	// that are stored in the input nodes
//	public void drawConnections(Graphics g) {
//		g.setColor(Color.blue);
//		for (int i = 0; i < connPoints.size(); i++) {
//			ConnectionPoint cp = connPoints.get(i);
//			if (cp.connection != null) {
//				TextureNodeConnection c = cp.connection;
//				if (c.sourceNode != null) {
//					Point p0 = c.targetConnectionPoint.getWorldSpaceCenter();
//					Point p1 = c.sourceNode.outputConnectionPoint.getWorldSpaceCenter();
//
//					drawConnectionLine(g, p0.x, p0.y, p1.x, p1.y);
//				}
//			}
//		}
//
//	}

	// 1: drag component
	// 2: output node clicked
	// 3: Help button clicked
	// < 0: input node clicked; index = -val - 1
	public int getActionTypeForMouseClick(int x, int y) {
		// check if we clicked in the output node
		if (outputConnectionPoint.inside(x, y)) {
			return 2;
		}
		// now check if we clicked into an input node
		for (int i = 0; i < allConnectionPoints.size(); i++) {
			ConnectionPoint cp = allConnectionPoints.get(i);
			if (cp.inside(x, y))
				return -cp.channelIndex - 1;
		}
		
		if ((x >= helpX) && (x <= (helpX + helpW)) && (y >= helpY) && (y <= (helpY + helpH))) {
			JOptionPane.showMessageDialog(this, texChannel.getHelpText(), texChannel.getName() + " Help", JOptionPane.PLAIN_MESSAGE);
			return 3;
		}


		return 1;
	}
	
	
	public void channelChanged(Channel source) {
		updatePreviewImage();
	}

}

