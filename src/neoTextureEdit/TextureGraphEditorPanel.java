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
import java.io.File;
import java.io.IOException;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.mystictri.neoTexture.TextureGraph;
import com.mystictri.neoTexture.TextureGraph.TextureNodeConnection;
import com.mystictri.neoTexture.TextureGraphNode;
import com.mystictri.neoTexture.TextureGraphNode.ConnectionPoint;

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
	
	TextureGraph graph = new TextureGraph();
	
	
	public class TextureGraphDropTarget extends DropTargetAdapter {

		public void drop(DropTargetDropEvent e) {
			TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(TextureEditor.INSTANCE.dragndropChannel));
			n.setLocation(e.getLocation());
			graph.addNode(n);
			setSelectedNode(n);
			//addNode(new DrawableTextureGraphNode(n));
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
	
	
	
	
	private void askFileAndExportTexture(int resX, int resY) {
		TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.setDialogTitle("Export Texture to " + resX + "x" + resY + " Image...");
		if (TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.getSelectedFile().getAbsolutePath();
			if (!name.endsWith(".png"))
				name += ".png";
			try {
				ImageIO.write(graph.selectedNodes.lastElement().getChannel()
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
					graph.addNode(new TextureGraphNode(chan), mousePosition.x, mousePosition.y);
				} else { // try to replace an existing node as good as possible
					TextureGraphNode node = graph.selectedNodes.get(0);
					if (node != null) {
						Channel chan = (Channel) mi.classType.newInstance();
						TextureGraphNode newNode = new TextureGraphNode(chan);
						graph.replaceNode(node, newNode);
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
				graph.addNode(toCopyTextureGraphNode.cloneThisNode(), mousePosition.x, mousePosition.y);
			}
		} else if (e.getSource() == copyChannelMenuItem) {
			if (graph.selectedNodes.size() > 0) {
				toCopyTextureGraphNode = graph.selectedNodes.get(0).cloneThisNode();
			} else {
				Logger.logError(this, "no selection in copyChannel popup menu.");
			}
		} else if (e.getSource() == replacepasteChannelMenuItem) {
			if (toCopyTextureGraphNode == null) {
				Logger.logError(this, "No node copied to replace paste.");
			} else if (graph.selectedNodes.size() > 0) {
				graph.replaceNode(graph.selectedNodes.get(0), toCopyTextureGraphNode.cloneThisNode());
			} else {
				Logger.logError(this, "no selection in insert-replaceChannel popup menu.");
			}
		} else if (e.getSource() == previewChannelMenuItem) {
			if (graph.selectedNodes.size() > 0) {
				setPreviewNode(null);
				setPreviewNode(graph.selectedNodes.get(0));
			}
		} else if (e.getSource() == cloneChannelMenuItem) { // --------------------------------------------------------
			if (graph.selectedNodes.size() > 0) {
				TextureGraphNode orig = graph.selectedNodes.get(0);
				TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(graph.selectedNodes.get(0).getChannel()));
				graph.addNode(n, orig.getX()+32, orig.getY()+32);
				repaint();
			} else {
				Logger.logError(this, "no selection in cloneChannel popup menu.");
			}
		} else if (e.getSource() == swapInputsChannelMenuItem) { // --------------------------------------------------------
			TextureGraphNode node = graph.selectedNodes.get(0);
			if (node != null) {
				if (node.getChannel().getNumInputChannels() < 2) return;
				ConnectionPoint p0 = node.getInputConnectionPointByChannelIndex(0);
				ConnectionPoint p1 = node.getInputConnectionPointByChannelIndex(1);
				TextureNodeConnection c0 = graph.getConnectionAtInputPoint(p0);
				TextureNodeConnection c1 = graph.getConnectionAtInputPoint(p1);
				graph.removeConnection(c0);
				graph.removeConnection(c1);
				if (c0 != null && c1 != null) {
					ConnectionPoint temp = c0.target;
					c0.target = c1.target;
					c1.target = temp;
					graph.addConnection(c0);
					graph.addConnection(c1);
				} else if (c1 != null) {
					c1.target = p0;
					graph.addConnection(c1);
				} else if (c0 != null) {
					c0.target = p1;
					graph.addConnection(c0);
				} else {
					return;
				}
				repaint();
			}
		} else if (e.getSource() == addToPresetsChannelMenuItem) { // --------------------------------------------------------
			if (graph.selectedNodes.size() > 0) {
				if (graph.selectedNodes.get(0).getChannel() instanceof Pattern)
					TextureEditor.INSTANCE.m_PatternSelector.addPatternPreset((Pattern)Channel.cloneChannel((Pattern)graph.selectedNodes.get(0).getChannel()));
				else Logger.logError(this, "Invalid action 'Add to Presets': selected node is not a pattern");
			} else Logger.logError(this, "Invalid action 'Add To Presets': no selected nodes exists.");
		} else if (e.getSource() == deleteChannelMenuItem) { // --------------------------------------------------------
			graph.deleteSelection();
			repaint();
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
				TextureGraphNode n = graph.selectedNodes.get(0);
				if (n.getChannel().chechkInputChannels()) { 
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
	
	
	
	public void setSelectedNode(TextureGraphNode node) {
		paramEditorPanel.setTextureNode(node);
		graph.setSelectedNode(node);
	}

	
	public void deleteFullGraph() {
		paramEditorPanel.setTextureNode(null);
		setPreviewNode(null);
		graph.deleteFullGraph();
		repaint();
		
	}
	
	
	public void save(String filename) {
		graph.save(filename);
	}
	
	public boolean load(String filename, boolean eraseOld) {
		return graph.load(filename, eraseOld);
	}

	private void _deleteNode(TextureGraphNode node, boolean removeFromSelected) {
		//remove(drawnode);
		if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.notifyTextureNodeRemoved(node);
		if (paramEditorPanel.getActiveTextureNode() == node)
			paramEditorPanel.setTextureNode(null);
		if (previewNode == node) setPreviewNode(null);
		graph._deleteNode(node, removeFromSelected);
		repaint();
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
			previewNode.getChannel().removeChannelChangeListener(this);
		}
		if (n == null) {
			previewNode = null;
		} else {
			previewNode = n;
			previewNode.getChannel().addChannelChangeListener(this);
		}
		updatePreview();
	}
	
	void updatePreview() {
		if (previewNode != null) {
			if (previewNode.getChannel().chechkInputChannels()) {
				previewNode.getChannel().computeImage(previewImage, null, 0);
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
		for (TextureNodeConnection c : graph.allConnections) {
			Point p0 = c.source.getWorldSpaceCenter();
			Point p1 = c.target.getWorldSpaceCenter();
			drawConnectionLine(g, p1.x, p1.y, p0.x, p0.y);
		}

		g.setColor(Color.yellow);
		for (TextureGraphNode n : graph.selectedNodes) {
			//Rectangle r = new n.getBounds();
			g.drawRect(n.getX() - 2, n.getY() - 2, n.width + 4, n.height + 4);
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
			//TextureGraphNode node = (TextureGraphNode)e.getComponent();
			//!!TODO: refactoring artifact
			connectionTarget = e.getPoint();
			//!!TODO: connectionTarget.x += node.getX();
			//!!TODO: connectionTarget.y += node.getY();
			repaint();
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
		if (node.getChannel() instanceof Pattern) 
			addToPresetsChannelMenuItem.setEnabled(true);
		else addToPresetsChannelMenuItem.setEnabled(false);
		
		replacepasteChannelMenuItem.setEnabled(toCopyTextureGraphNode != null);
		selectedChannelPopupMenu.show(this, x, y); //!!TODO: refactoring artifact
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
		} 
		//!!TODO: refactoring artifact; select node here with helper
		/*else if (e.getComponent().getClass() == TextureGraphNode.class) { // clicked on a TextureGraphNode
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
					connectionSource = pat.getOutputConnectionPoint();
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
		} */else if (e.isPopupTrigger()) {
			showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
		} else {
			setSelectedNode(null);
		}

		repaint();
	}
	
	

	public void mouseReleased(MouseEvent e) {
		mousePosition = e.getPoint();
		//!!TODO: refactoring artifact
		/*
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

		} else */if (e.isPopupTrigger()) {
			showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
		}

		nodeDragging = false;
		connectionDragging = false;
		desktopDragging = false;
		
		repaint();
	}


	public void channelChanged(Channel source) {
		if ((previewNode != null) && (previewNode.getChannel() == source)){
			updatePreview();
		}
	}
}
