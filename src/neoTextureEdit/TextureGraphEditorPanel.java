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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import neoTextureEdit.TextureGraphNode.ConnectionPoint;
import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.graphics.synthesis.texture.Pattern;

/**
 * This is the main texture graph editing panel that is used to create and modify
 * a texture generation graph using TextureGraphNode objects.
 * @author Holger Dammertz
 *
 */
class TextureGraphEditorPanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener, ChannelChangeListener {
	private static final long serialVersionUID = 4535161419971720668L;
	int dragStartX = 0;
	int dragStartY = 0;

	// currently all operations on nodes with the mouse expect that the clicked node is the selected node
	final Vector<TextureGraphNode> selectedNodes = new Vector<TextureGraphNode>();
	final Vector<TextureGraphNode> allNodes = new Vector<TextureGraphNode>();
	final Vector<TextureNodeConnection> allConnections = new Vector<TextureNodeConnection>();

	boolean nodeDragging = false;
	boolean desktopDragging = false;
	boolean connectionDragging = false;
	TextureGraphNode.ConnectionPoint connectionSource = null;
	Point connectionOrigin;
	Point connectionTarget;

	Point mousePosition;

	JPopupMenu newChannelPopupMenu;
	JMenuItem newChannelInsertMenuItem;
	
	JPopupMenu selectedChannelPopupMenu;
	JMenuItem addToPresetsChannelMenuItem;
	TextureGraphNode toCopyTextureGraphNode;
	JMenuItem previewChannelMenuItem;
	JMenuItem copyChannelMenuItem;
	JMenuItem replacepasteChannelMenuItem;
	JMenuItem swapInputsChannelMenuItem;
	JMenuItem deleteChannelMenuItem;
	JMenuItem cloneChannelMenuItem;

	JMenuItem openGLDiffuseMenuItem;
	JMenuItem openGLNormalMenuItem;
	JMenuItem openGLSpecularMenuItem;
	JMenuItem openGLHeightmapMenuItem;
	
	JMenu replaceChannelMenu;

	ChannelParameterEditorPanel paramEditorPanel;
	
	// This is a first experiment for a preview image inside the graph editor;
	// TODO: replace this with the selectable preview image from the ChannelParameterEditor
	TextureGraphNode previewNode;
	BufferedImage previewImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
	
	
	/**
	 * This connection node only holds the meta data; the actual connection of
	 * the channels is done in the add/remove methods from the TextureGraphEditorPanel
	 * @author Holger Dammertz
	 * 
	 */
	class TextureNodeConnection implements ChannelChangeListener {
		public TextureGraphNode.ConnectionPoint source; // this is an output-node
		public TextureGraphNode.ConnectionPoint target; // this is an input-node
		
		public TextureNodeConnection(TextureGraphNode.ConnectionPoint s, TextureGraphNode.ConnectionPoint t) {
			if (s.channelIndex != -1) {
				System.err.println("ERROR in TextureNodeConnection: source of " + s.parent + " is not an output node");
				return;
			}
			if (t.channelIndex == -1) {
				System.err.println("ERROR in TextureNodeConnection: target of " + t.parent + " is not an input node");
				return;
			}
			source = s;
			target = t;
		}

		public void channelChanged(Channel channelSource) {
			if (source.parent.texChannel != channelSource) {
				System.err.println("ERROR in TextureNodeConnection: got change event from unexpexted Channel.");
				return;
			}
			target.parent.texChannel.parameterChanged(null);
		}
	}
	
	public class TextureGraphDropTarget extends DropTargetAdapter {

		public void drop(DropTargetDropEvent e) {
			TextureGraphNode n = new TextureGraphNode(TextureGraphNode.cloneChannel(TextureEditor.INSTANCE.dragndropChannel));
			n.setLocation(e.getLocation());
			addNode(n);
			setSelectedNode(n);
			repaint();
			//TextureGraphNode.cloneChannel(ppl.pat);
			//addNode(node)
			//System.out.println("DROP da bOmB " + e);
			//System.out.println();
		}
	}

	
	public TextureGraphEditorPanel() {
		setBackground(Color.darkGray);
		setLayout(null);

		addMouseListener(this);
		addMouseMotionListener(this);

		createPopupMenus();
		paramEditorPanel = new ChannelParameterEditorPanel();
		
		DropTarget t = new DropTarget();
		try {
			t.addDropTargetListener(new TextureGraphDropTarget());
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}
		setDropTarget(t);
	}

	// first saving version: simple ascii test
	public void save(String filename) {
		Logger.log(this, "Saving TextureGraph to " + filename);
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(filename));

			w.write("Nodes " + allNodes.size() + "\n");
			// first save all the nodes
			for (TextureGraphNode n : allNodes) {
				n.save(w, n);
			}
			// now all the connections
			w.write(allConnections.size()+"\n");
			for (TextureNodeConnection c : allConnections) {
				w.write(allNodes.indexOf(c.source.parent)+ " ");
				w.write(allNodes.indexOf(c.target.parent)+ " ");
				w.write(c.target.channelIndex+ "\n");
			}
			// now the openGL settings
			if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.save(w);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean load(String filename, boolean eraseOld) {
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader(filename)));
			if (eraseOld) deleteFullGraph();
			
			int offset = allNodes.size();

			s.next();
			int numNodes = s.nextInt();
			for (int i = 0; i < numNodes; i++) {
				TextureGraphNode n = TextureGraphNode.load(s);
				addNode(n);
			}
			int numConnections = s.nextInt();
			for (int i = 0; i < numConnections; i++) {
				TextureGraphNode.ConnectionPoint sourcePoint = allNodes.get(offset + s.nextInt()).outputConnectionPoint;
				TextureGraphNode.ConnectionPoint targetPoint = allNodes.get(offset + s.nextInt()).getInputConnectionPointByChannelIndex(s.nextInt());
				addConnection(new TextureNodeConnection(sourcePoint, targetPoint));
			}
			if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.load(s);
		} catch (FileNotFoundException e) {
			Logger.logError(this, "Could not load " + filename);
			return false;
		} catch (InputMismatchException ime) {
			ime.printStackTrace();
			Logger.logError(this, "Could not load " + filename);
			return false;
		}
		repaint();
		return true;
	}

	public ChannelParameterEditorPanel getParameterEditorPanel() {
		return paramEditorPanel;
	}

	private void createPopupMenus() {
		newChannelPopupMenu = new JPopupMenu("Create Channel");
		replaceChannelMenu = new JMenu("Replace Channel");

		newChannelInsertMenuItem = createChannelPopupMenuItem(newChannelPopupMenu, "Paste Node");
		newChannelPopupMenu.addSeparator();
		for (Class<?> c : TextureEditor.INSTANCE.allPatterns) {
			newChannelPopupMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, true, false));
			replaceChannelMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, true, true));
		}
		newChannelPopupMenu.addSeparator();
		replaceChannelMenu.addSeparator();
		for (Class<?> c : TextureEditor.INSTANCE.allChannels) {
			newChannelPopupMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, false, false));
			replaceChannelMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, false, true));
		}

		selectedChannelPopupMenu = new JPopupMenu();
		cloneChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Clone");
		copyChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Copy");
		replacepasteChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Paste Replace");
		
		selectedChannelPopupMenu.addSeparator();
		previewChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Preview Node");
		
		JMenu exportImage = new JMenu("Export Image"); // export sub menu
		selectedChannelPopupMenu.add(exportImage);
		createChannelPopupSubMenuItem(exportImage, "256x256");
		createChannelPopupSubMenuItem(exportImage, "512x512");
		createChannelPopupSubMenuItem(exportImage, "1024x1024");
		createChannelPopupSubMenuItem(exportImage, "specify").setActionCommand("arbitraryResolutionExport");

		addToPresetsChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Add to Presets");
		swapInputsChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Swap First 2 Inputs");
		selectedChannelPopupMenu.add(replaceChannelMenu);
		selectedChannelPopupMenu.addSeparator();

		if (TextureEditor.GL_ENABLED) {
			JMenu opengl = new JMenu("OpenGL"); // export sub menu
			selectedChannelPopupMenu.add(opengl);
			openGLDiffuseMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Diffuse");
			openGLNormalMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Normal");
			openGLSpecularMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Specular");
			openGLHeightmapMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Height");
		}

		deleteChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Delete");
		
	}

	private JMenuItem createChannelPopupSubMenuItem(JMenu menu, String name) {
		JMenuItem ret = new JMenuItem(name);
		ret.addActionListener(this);
		menu.add(ret);
		return ret;
	}

	private JMenuItem createChannelPopupMenuItem(JPopupMenu menu, String name) {
		JMenuItem ret = new JMenuItem(name);
		ret.addActionListener(this);
		menu.add(ret);
		return ret;
	}

	class CreateMenuItem extends JMenuItem {
		private static final long serialVersionUID = 3053710502290069301L;
		public Class<?> classType;
		public boolean isAReplaceCall; // this is set to true in events from the replace channel menu

		public CreateMenuItem(String name, boolean isReplace) {
			super(name);
			isAReplaceCall = isReplace;
		}

		public CreateMenuItem(String name, ImageIcon icon, boolean isReplace) {
			super(name, icon);
			isAReplaceCall = isReplace;
		}
	}

	private CreateMenuItem createMenuItem_CreateFilter(String name, Class<?> c, boolean genIcon, boolean replace) {
		CreateMenuItem ret;
		if (genIcon) {
			Channel chan = null;
			try {
				chan = (Channel) c.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			ret = new CreateMenuItem(name, new ImageIcon(chan.createAndComputeImage(16, 16, null, 0)), replace);
		} else
			ret = new CreateMenuItem(name, replace);
		ret.classType = c;
		ret.addActionListener(this);
		return ret;
	}
	
	
	/**
	 * Replaces oldNode in the texture graph with newNode and trys to reconnect all
	 * TextureNodeConnection as meaningfull as possible. newNode should not yet
	 * have been added or have any connections! 
	 * The basic process is:
	 *   First newNode is inserted; then all outputConnections from oldNode are removed
	 *   and added to newNode; finally the same process is done linearly of the input connections.
	 *   Afterwards oldNode is deleted by calling deleteNode.
	 * @param oldNode
	 * @param newNode
	 */
	private void replaceNode(TextureGraphNode oldNode, TextureGraphNode newNode) {
		addNode(newNode, oldNode.getX(), oldNode.getY());
		
		Vector<TextureNodeConnection> outConns = getAllConnectionsAtOutputPoint(oldNode.outputConnectionPoint);
		removeConnections(outConns);
		for (TextureNodeConnection c : outConns) {
			c.source = newNode.outputConnectionPoint;
			addConnection(c);
		}
		
		// the first point in 
		for (int i = 1; i < oldNode.allConnectionPoints.size(); i++) { 
			TextureNodeConnection c = getConnectionAtInputPoint(oldNode.allConnectionPoints.get(i));
			if (c != null) { 
				removeConnection(c);
				if (i < newNode.allConnectionPoints.size()) {
					c.target = newNode.allConnectionPoints.get(i);
					addConnection(c);
				}
			}
		}
		
		_deleteNode(oldNode, true);
	}
	
	
	private void askFileAndExportTexture(int resX, int resY) {
		TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.setDialogTitle("Export Texture to " + resX + "x" + resY + " Image...");
		if (TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.getSelectedFile().getAbsolutePath();
			if (!name.endsWith(".png"))
				name += ".png";
			try {
				ImageIO.write(selectedNodes.lastElement().texChannel
						.createAndComputeImage(resX, resY, TextureEditor.INSTANCE.m_ProgressDialog, 0), "png", new File(name));
				Logger.log(this, "Saved image to " + name + ".");
			} catch (IOException exc) {
				exc.printStackTrace();
				Logger.logError(this, "IO Exception while exporting image: " + exc);
			}
		}
	}

	/**
	 * This is the main action method for TextureGraphEditorPanel. Here the actions from the popup-menus
	 * are processed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().getClass() == CreateMenuItem.class) { // this was one menu item from the create new channel menu
			CreateMenuItem mi = (CreateMenuItem)e.getSource();

			try {
				if (!mi.isAReplaceCall) { // insert a new Node
					Channel chan = (Channel) mi.classType.newInstance();
					addNode(new TextureGraphNode(chan), mousePosition.x, mousePosition.y);
				} else { // try to replace an existing node as good as possible
					TextureGraphNode node = selectedNodes.get(0);
					if (node != null) {
						Channel chan = (Channel) mi.classType.newInstance();
						TextureGraphNode newNode = new TextureGraphNode(chan);
						replaceNode(node, newNode);
					} else {
						Logger.logWarning(this, "No node selected for replace.");
					}
				}
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == newChannelInsertMenuItem) {
			if (toCopyTextureGraphNode == null) {
				Logger.logError(this, "No node copied to insert.");
			} else {
				addNode(toCopyTextureGraphNode.cloneThisNode(), mousePosition.x, mousePosition.y);
			}
		} else if (e.getSource() == copyChannelMenuItem) {
			if (selectedNodes.size() > 0) {
				toCopyTextureGraphNode = selectedNodes.get(0).cloneThisNode();
			} else {
				Logger.logError(this, "no selection in copyChannel popup menu.");
			}
		} else if (e.getSource() == replacepasteChannelMenuItem) {
			if (toCopyTextureGraphNode == null) {
				Logger.logError(this, "No node copied to replace paste.");
			} else if (selectedNodes.size() > 0) {
				replaceNode(selectedNodes.get(0), toCopyTextureGraphNode.cloneThisNode());
			} else {
				Logger.logError(this, "no selection in insert-replaceChannel popup menu.");
			}
		} else if (e.getSource() == previewChannelMenuItem) {
			if (selectedNodes.size() > 0) {
				setPreviewNode(null);
				setPreviewNode(selectedNodes.get(0));
			}
		} else if (e.getSource() == cloneChannelMenuItem) { // --------------------------------------------------------
			if (selectedNodes.size() > 0) {
				TextureGraphNode orig = selectedNodes.get(0);
				TextureGraphNode n = new TextureGraphNode(TextureGraphNode.cloneChannel(selectedNodes.get(0).texChannel));
				addNode(n, orig.getX()+32, orig.getY()+32);
				repaint();
			} else {
				Logger.logError(this, "no selection in cloneChannel popup menu.");
			}
		} else if (e.getSource() == swapInputsChannelMenuItem) { // --------------------------------------------------------
			TextureGraphNode node = selectedNodes.get(0);
			if (node != null) {
				if (node.texChannel.getNumInputChannels() < 2) return;
				ConnectionPoint p0 = node.getInputConnectionPointByChannelIndex(0);
				ConnectionPoint p1 = node.getInputConnectionPointByChannelIndex(1);
				TextureNodeConnection c0 = getConnectionAtInputPoint(p0);
				TextureNodeConnection c1 = getConnectionAtInputPoint(p1);
				removeConnection(c0);
				removeConnection(c1);
				if (c0 != null && c1 != null) {
					ConnectionPoint temp = c0.target;
					c0.target = c1.target;
					c1.target = temp;
					addConnection(c0);
					addConnection(c1);
				} else if (c1 != null) {
					c1.target = p0;
					addConnection(c1);
				} else if (c0 != null) {
					c0.target = p1;
					addConnection(c0);
				} else {
					return;
				}
				repaint();
			}
		} else if (e.getSource() == addToPresetsChannelMenuItem) { // --------------------------------------------------------
			if (selectedNodes.size() > 0) {
				if (selectedNodes.get(0).texChannel instanceof Pattern)
					TextureEditor.INSTANCE.m_PatternSelector.addPatternPreset((Pattern)TextureGraphNode.cloneChannel((Pattern)selectedNodes.get(0).texChannel));
				else Logger.logError(this, "Invalid action 'Add to Presets': selected node is not a pattern");
			} else Logger.logError(this, "Invalid action 'Add To Presets': no selected nodes exists.");
		} else if (e.getSource() == deleteChannelMenuItem) { // --------------------------------------------------------
			deleteSelection();
		} else if (e.getActionCommand().equals("arbitraryResolutionExport")) {
			String resolution = JOptionPane.showInputDialog(this, "Specify your desried resolution (for example 1024x1024)", "What Resolution?", JOptionPane.QUESTION_MESSAGE);
			if (resolution != null && resolution.matches("\\d+x\\d+")) {
				int resX = Integer.parseInt(resolution.substring(0, resolution.indexOf('x')));
				int resY = Integer.parseInt(resolution.substring(resolution.indexOf('x') + 1, resolution.length()));
				askFileAndExportTexture(resX, resY);
			}
		} else if (e.getActionCommand().matches("\\d+x\\d+")) {
			String s = e.getActionCommand();
			int resX = Integer.parseInt(s.substring(0, s.indexOf('x')));
			int resY = Integer.parseInt(s.substring(s.indexOf('x') + 1, s.length()));
			askFileAndExportTexture(resX, resY);
		}
		else {
			// ----------------------- OpenGL ---------------------------
			if (TextureEditor.GL_ENABLED) {
				TextureGraphNode n = selectedNodes.get(0);
				if (n.texChannel.chechkInputChannels()) { 
					if (e.getSource() == openGLDiffuseMenuItem) {
						TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setDiffuseTextureNode(n);
						repaint();
					} else if (e.getSource() == openGLNormalMenuItem) {
						TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setNormalTextureNode(n);
						repaint();
					} else if (e.getSource() == openGLSpecularMenuItem) {
						TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setSpecWeightTextureNode(n);
						repaint();
					} else if (e.getSource() == openGLHeightmapMenuItem) {
						TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setHeightmapTextureNode(n);
						repaint();
					}
				} else Logger.logWarning(this, "Incomplete channel for preview."); 
			// --------------------------------------------------------
			} 
		}

	}

	public void addNode(TextureGraphNode node) {
		add(node);
		allNodes.add(node);
		node.addMouseListener(this);
		node.addMouseMotionListener(this);
	}

	public void addNode(TextureGraphNode node, int x, int y) {
		addNode(node);
		node.setLocation(x, y);
		setSelectedNode(node);
	}
	
	// utility method to draw a conneciton line.
	public static void drawConnectionLine(Graphics g, int x0, int y0, int x1, int y1) {
		g.drawLine(x0, y0, x0, y0 - 8);
		g.drawLine(x0, y0 - 8, x1, y1 + 8);
		g.drawLine(x1, y1 + 8, x1, y1);
	}
	
	
	
	public void setPreviewNode(TextureGraphNode n) {
		if (n == previewNode) return;
		if (previewNode != null) {
			previewNode.texChannel.removeChannelChangeListener(this);
		}
		if (n == null) {
			previewNode = null;
		} else {
			previewNode = n;
			previewNode.texChannel.addChannelChangeListener(this);
		}
		updatePreview();
	}
	
	void updatePreview() {
		if (previewNode != null) {
			if (previewNode.texChannel.chechkInputChannels()) {
				previewNode.texChannel.computeImage(previewImage, null, 0);
			} else {
				Graphics g = previewImage.getGraphics();
				g.fillRect(0, 0, previewImage.getWidth(), previewImage.getHeight());
			}
		}
		repaint();
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		// draw the connection lines
		g.setColor(Color.white);
		for (TextureNodeConnection c : allConnections) {
			Point p0 = c.source.getWorldSpaceCenter();
			Point p1 = c.target.getWorldSpaceCenter();
			drawConnectionLine(g, p1.x, p1.y, p0.x, p0.y);
		}

		g.setColor(Color.yellow);
		for (TextureGraphNode n : selectedNodes) {
			Rectangle r = n.getBounds();
			g.drawRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
		}
		
		if (TextureEditor.GL_ENABLED) {
			TextureEditor.INSTANCE.m_OpenGLPreviewPanel.drawTokens(g);
		}
		
		if (connectionDragging) {
			g.setColor(Color.red);
			drawConnectionLine(g, connectionTarget.x, connectionTarget.y, connectionOrigin.x, connectionOrigin.y);
		}
		
		
		if (previewNode != null) {
			g.setColor(Color.blue);
			g.drawLine(previewNode.getX(), previewNode.getY(), previewImage.getWidth()/2, previewImage.getHeight()/2);
			g.drawImage(previewImage, 0, 0, this);
		}

	}
	
	public void mouseDragged(MouseEvent e) {
		if (desktopDragging) { // moving the desktop with the mouse
			int dx =  e.getXOnScreen() - dragStartX; dragStartX = e.getXOnScreen();
			int dy =  e.getYOnScreen() - dragStartY; dragStartY = e.getYOnScreen();
			moveDesktop(dx, dy);
		} else if (nodeDragging) {
			// TextureNode node = (TextureNode) e.getComponent();
			Point p = getLocation();
			p.x = e.getXOnScreen() + dragStartX;
			p.y = e.getYOnScreen() + dragStartY;
			e.getComponent().setLocation(p);

			repaint();
		} else if (connectionDragging) {
			TextureGraphNode node = (TextureGraphNode) e.getComponent();
			connectionTarget = e.getPoint();
			connectionTarget.x += node.getX();
			connectionTarget.y += node.getY();
			repaint();
		}
	}
	
	
	private void _deleteNode(TextureGraphNode node, boolean removeFromSelected) {
		removeConnections(getAllConnectionsAtOutputPoint(node.outputConnectionPoint));
		removeConnections(getConnectionsAtAllInputPoints(node));
		remove(node);
		allNodes.remove(node);
		if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.notifyTextureNodeRemoved(node);
		if (removeFromSelected) selectedNodes.remove(node);
		if (paramEditorPanel.getActiveTextureNode() == node)
			paramEditorPanel.setTextureNode(null);
		if (previewNode == node) setPreviewNode(null);
		repaint();
	}

	public void deleteFullGraph() {
		paramEditorPanel.setTextureNode(null);
		setPreviewNode(null);
		removeConnections(allConnections);
		for (TextureGraphNode n : allNodes) {
			remove(n);
		}
		selectedNodes.clear();
		allNodes.clear();
		repaint();
		
	}

	public void deleteSelection() {
		for (TextureGraphNode n : selectedNodes) {
			/*removeConnections(getAllConnectionsAtOutputPoint(n.outputConnectionPoint));
			removeConnections(getConnectionsAtAllInputPoints(n));
			allNodes.remove(n);
			remove(n);*/
			_deleteNode(n, false);
		}
		selectedNodes.clear();
		repaint();
	}

	public void setSelectedNode(TextureGraphNode node) {
		selectedNodes.clear();
		paramEditorPanel.setTextureNode(node);
		if (node != null) {
			selectedNodes.add(node);
			setComponentZOrder(node, 0);
		}
	}
	
	
	public void moveDesktop(int dx, int dy) {
		for (int i = 0; i < getComponentCount(); i++) {
			getComponent(i).setLocation(getComponent(i).getX()+dx, getComponent(i).getY()+dy);
		}
		repaint();
	}
	
	/**
	 *  computes the bounding box of all positions and centers it computes the bounding box of all positions and centers it
	 */
	public void centerDesktop() {
		if (getComponentCount() < 1) return;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			minX = Math.min(minX, c.getX());
			minY = Math.min(minY, c.getY());
			maxX = Math.max(maxX, c.getX() + c.getWidth());
			maxY = Math.max(maxY, c.getY() + c.getHeight());
		}
		int dx = getWidth()/2 - (minX+maxX)/2;
		int dy = getHeight()/2 - (minY+maxY)/2;
		moveDesktop(dx, dy);
	}

	
	void showSelectedChannelPopupMenu(TextureGraphNode node, int x, int y) {
		if (node.texChannel instanceof Pattern) 
			addToPresetsChannelMenuItem.setEnabled(true);
		else addToPresetsChannelMenuItem.setEnabled(false);
		
		replacepasteChannelMenuItem.setEnabled(toCopyTextureGraphNode != null);
		selectedChannelPopupMenu.show(node, x, y);
	}
	
	void showNewChannelPopupMenu(Component c, int x, int y) {
		newChannelInsertMenuItem.setEnabled(toCopyTextureGraphNode != null);
		newChannelPopupMenu.show(c, x, y);
	}
	
	// !!TODO: addToSelection

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
		mousePosition = e.getPoint();

		if (e.getButton() == 2 || (e.isAltDown() || e.isControlDown())) { // Desktop Dragging
			dragStartX = e.getXOnScreen();
			dragStartY = e.getYOnScreen();
			desktopDragging = true;
		} else if (e.getComponent().getClass() == TextureGraphNode.class) { // clicked on a TextureGraphNode
			TextureGraphNode pat = (TextureGraphNode) e.getComponent();
			if (e.getButton() == 3) { // Popup menu for a TextureGraphNode
				setSelectedNode(pat);
				showSelectedChannelPopupMenu(pat, e.getX(), e.getY());
			} else { // if it was not a popup we look if we clicked on a connection point or on the rest of the node
				Point p = e.getComponent().getLocation();
				dragStartX = -e.getXOnScreen() + p.x;
				dragStartY = -e.getYOnScreen() + p.y;
				int actionType = pat.getActionTypeForMouseClick(e.getX(), e.getY());
				if (actionType == 1) { // want to drag the position of the node
					setSelectedNode(pat);
					nodeDragging = true;
				} 
				else if (actionType == 2) { // dragging from the output node of a channel
					connectionDragging = true;
					connectionSource = pat.outputConnectionPoint;
					connectionOrigin = connectionSource.getWorldSpaceCenter();
					connectionTarget = e.getPoint();
					connectionTarget.x += p.getX();
					connectionTarget.y += p.getY();
				}
				else if (actionType < 0) { // dragging an existing connection of an input away
					int index = -actionType - 1;
					TextureGraphNode.ConnectionPoint inputPoint = pat.getInputConnectionPointByChannelIndex(index);
					TextureNodeConnection connection = getConnectionAtInputPoint(inputPoint);
					if (connection != null) {
						removeConnection(connection);
						connectionDragging = true;
						connectionSource = connection.source;
						connectionOrigin = connectionSource.getWorldSpaceCenter();
						// pat.updatePreviewImage();
						connectionTarget = e.getPoint();
						connectionTarget.x += p.getX();
						connectionTarget.y += p.getY();
					}
				}
			}
		} else if (e.isPopupTrigger()) {
			showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
		} else {
			setSelectedNode(null);
		}

		repaint();
	}
	
	public TextureNodeConnection getConnectionAtInputPoint(TextureGraphNode.ConnectionPoint input) {
		for (TextureNodeConnection c : allConnections) {
			if (c.target == input) return c;
		}
		return null;
	}
	
	public Vector<TextureNodeConnection> getAllConnectionsAtOutputPoint(ConnectionPoint output) {
		Vector<TextureNodeConnection> ret = new Vector<TextureNodeConnection>();
		for (TextureNodeConnection c : allConnections) {
			if (c.source == output) ret.add(c);
		}
		return ret;
	}

	public Vector<TextureNodeConnection> getConnectionsAtAllInputPoints(TextureGraphNode node) {
		Vector<TextureNodeConnection> ret = new Vector<TextureNodeConnection>();
		for (int i = 0; i < node.allConnectionPoints.size(); i++) {
			TextureNodeConnection c = getConnectionAtInputPoint(node.allConnectionPoints.get(i));
			if (c != null) ret.add(c);
		}
		return ret;
	}
	
	public void removeConnections(Vector<TextureNodeConnection> conns) {
		if (conns == allConnections) {
			conns = new Vector<TextureNodeConnection>(allConnections);
		}
		for (int i = 0; i < conns.size(); i++) {
			removeConnection(conns.get(i));
		}
	}
	
	public void removeConnection(TextureNodeConnection c) {
		if (c == null) return;
		if (allConnections.remove(c)) {
			c.source.parent.texChannel.removeChannelChangeListener(c);
			c.target.parent.texChannel.setInputChannel(c.target.channelIndex, null);
		} else {
			System.err.println("ERROR in removeConnection: got invalid connection " + c);
		}
	}
	
	public boolean checkForCycle(ConnectionPoint source, ConnectionPoint target) {
		if (target == null) return false;
		if (source == null) return false;
		if (source == target.parent.outputConnectionPoint) return true;
		
		boolean cycle = false;
		
		Vector<TextureNodeConnection> conns = getAllConnectionsAtOutputPoint(target.parent.outputConnectionPoint);
		for (int i = 0; i < conns.size(); i++) {
			cycle |= checkForCycle(source, conns.get(i).target);
		}
		
		return cycle;
	}
	
	public boolean addConnection(TextureNodeConnection c) {
		if (checkForCycle(c.source, c.target)) {
			System.out.println("WARNING: cycles not allowed!");
			return false;
		}
		
		// remove a possible connection at the target input connection point
		TextureNodeConnection inputConnection = getConnectionAtInputPoint(c.target);
		if (inputConnection != null) removeConnection(inputConnection); 
		
		c.target.parent.texChannel.setInputChannel(c.target.channelIndex, c.source.parent.texChannel);
		c.source.parent.texChannel.addChannelChangeListener(c);
		allConnections.add(c);
		return true;
	}
	
	public Vector<TextureGraphNode> getAllNodes() {
		return allNodes;
	}

	public void mouseReleased(MouseEvent e) {
		mousePosition = e.getPoint();

		if (e.getSource().getClass() == TextureGraphNode.class) {
			TextureGraphNode pat = (TextureGraphNode) e.getComponent();
			Point loc = e.getPoint();
			loc.x += pat.getX();
			loc.y += pat.getY();

			Component c = getComponentAt(loc);
			if (c.getClass() == TextureGraphNode.class) {
				TextureGraphNode targetPat = (TextureGraphNode) c;
				if (connectionDragging) { // we are currently drawing a connection
					int actionType = targetPat.getActionTypeForMouseClick(loc.x - targetPat.getX(), loc.y - targetPat.getY());
					if (actionType < 0) { // we dragged onto an input node
						int index = -actionType - 1;
						
						TextureGraphNode.ConnectionPoint inputPoint = targetPat.getInputConnectionPointByChannelIndex(index);
						addConnection(new TextureNodeConnection(connectionSource, inputPoint));
					}
				}
			}

		} else if (e.isPopupTrigger()) {
			showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
		}

		nodeDragging = false;
		connectionDragging = false;
		desktopDragging = false;
		
		repaint();
	}


	public void channelChanged(Channel source) {
		if ((previewNode != null) && (previewNode.texChannel == source)){
			updatePreview();
		}
	}
}
