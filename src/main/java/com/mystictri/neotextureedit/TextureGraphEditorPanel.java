/**
 * Copyright (C) 2010  Holger Dammertz
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mystictri.neotextureedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.mystictri.neotexture.TextureGraph;
import com.mystictri.neotexture.TextureGraph.TextureNodeConnection;
import com.mystictri.neotexture.TextureGraphListener;
import com.mystictri.neotexture.TextureGraphNode;
import com.mystictri.neotexture.TextureGraphNode.ConnectionPoint;

import engine.graphics.synthesis.texture.CacheTileManager;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.Channel.ChannelVizType;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.graphics.synthesis.texture.Pattern;

/**
 * This is the main texture graph editing panel that is used to create and modify
 * a texture generation graph using TextureGraphNode objects.
 *
 * @author Holger Dammertz
 */
public final class TextureGraphEditorPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ChannelChangeListener, TextureGraphListener {
    private static final long serialVersionUID = 4535161419971720668L;
    private int dragStartX = 0;
    private int dragStartY = 0;

    // node drawing settings
    private static final Color col_NodeBG = new Color(85, 85, 85);
    private static final Color col_NodeSlowBG = new Color(128 + 64, 64, 64);
    // private static final Color col_NodeBorder = new Color(255, 255, 255);
    private static final Color col_NodeSelected = new Color(255, 255, 255);
    private static final Color col_NodeOutPort = new Color(0, 230, 64);
    private static final Color col_NodeInPort = new Color(230, 64, 0);
    //private static final BasicStroke connectionLineStroke = new BasicStroke(2.0f);
    private static final BasicStroke connectionLineStroke = new BasicStroke(1.5f);
    //private static final Color ms_PatternColor = new Color(0x929AAF);
    //private static final Color ms_SlowColor = new Color(128,16,16);
    private static final Font font = new Font("Sans", Font.PLAIN, 10);
    private static final int helpW = 16;
    private static final int helpH = 16;
    private static final int helpX = TextureGraphNode.width - helpW;
    private static final int helpY = 0;

    private boolean nodeDragging = false;
    private boolean desktopDragging = false;
    private boolean connectionDragging = false;
    private PreviewWindow draggedWindow = null;
    private TextureGraphNode.ConnectionPoint connectionSource = null;
    private Point connectionOrigin;
    private Point connectionTarget;

    private Point mousePosition = new Point();

    private JPopupMenu newChannelPopupMenu;
    private JMenuItem newChannelInsertMenuItem;

    private JPopupMenu selectedChannelPopupMenu;
    private JMenuItem addToPresetsChannelMenuItem;
    private TextureGraphNode toCopyTextureGraphNode;
    private JMenuItem previewChannelMenuItem;
    private JMenuItem copyChannelMenuItem;
    private JMenuItem replacepasteChannelMenuItem;
    private JMenuItem swapInputsChannelMenuItem;
    private JMenuItem deleteChannelMenuItem;
    private JMenuItem cloneChannelMenuItem;

    private JMenuItem openGLDiffuseMenuItem;
    private JMenuItem openGLNormalMenuItem;
    private JMenuItem openGLSpecularMenuItem;
    private JMenuItem openGLHeightmapMenuItem;

    private ChannelParameterEditorPanel paramEditorPanel;

    TextureGraph graph;

    /**
     * This interface is used by the TextureGraphEditorPanel to notify about changes in the
     * graph when edit operations are performed.
     *
     * @author Holger Dammertz
     */
    public interface EditChangeListener {
        /**
         * Called on each edit operation that changes the graph or the graph layout
         */
        void graphWasEdited();
    }

    private final Vector<EditChangeListener> editChangeListener = new Vector<>();

    // TODO: Move to a different file

    /**
     * Used in the draw method to generate a preview image that is cached
     *
     * @author Holger Dammertz
     */
    class NodePreviewImage implements ChannelChangeListener {
        BufferedImage previewImage;
        TextureGraphNode node;

        NodePreviewImage(TextureGraphNode node) {
            this.node = node;
            node.getChannel().addChannelChangeListener(this);
            updatePreviewImage();
        }

        void updatePreviewImage() {
            if ((node.getChannel() != null) && (node.getChannel().chechkInputChannels())) {
                if (previewImage == null)
                    previewImage = ChannelUtils.createAndComputeImage(node.getChannel(), 64, 64, null, 0);
                else
                    ChannelUtils.computeImage(node.getChannel(), previewImage, null, 0);
            }
            else {
                previewImage = null;
            }
            repaint();
        }

        public void channelChanged(Channel source) {
            updatePreviewImage();
        }
    }

    private int desktopX, desktopY;

    // TODO: Move to a different file
    public class TextureGraphDropTarget extends DropTargetAdapter {

        public void drop(DropTargetDropEvent e) {
            TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(TextureEditor.INSTANCE.dragndropChannel));
            addTextureNode(n, e.getLocation().x - desktopX, e.getLocation().y - desktopY);
            repaint();
        }
    }

    TextureGraphEditorPanel() {
        graph = new TextureGraph();
        graph.graphListener = this;

        setBackground(Color.darkGray);
        setLayout(null);

        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);

        createPopupMenus();
        paramEditorPanel = new ChannelParameterEditorPanel();

        DropTarget t = new DropTarget();
        try {
            t.addDropTargetListener(new TextureGraphDropTarget());
        }
        catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        setDropTarget(t);

        setPreferredSize(new Dimension(512, 512));
    }

    ChannelParameterEditorPanel getParameterEditorPanel() {
        return paramEditorPanel;
    }

    private void createPopupMenus() {
        newChannelPopupMenu = new JPopupMenu("Create Channel");
        JMenu replaceChannelMenu = new JMenu("Replace Channel");

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

    // TODO: Move to a different file
    class CreateMenuItem extends JMenuItem {
        private static final long serialVersionUID = 3053710502290069301L;
        Class<?> classType;
        boolean isAReplaceCall; // this is set to true in events from the replace channel menu

        CreateMenuItem(String name, boolean isReplace) {
            super(name);
            isAReplaceCall = isReplace;
        }

        CreateMenuItem(String name, ImageIcon icon, boolean isReplace) {
            super(name, icon);
            isAReplaceCall = isReplace;
        }
    }

    private CreateMenuItem createMenuItem_CreateFilter(String name, Class<?> c, boolean genIcon, boolean replace) {
        CreateMenuItem ret;
        if (genIcon) {
            Channel chan = null;
            try {
                chan = (Channel) c.getDeclaredConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            ret = new CreateMenuItem(name, new ImageIcon(ChannelUtils.createAndComputeImage(chan, 16, 16, null, 0)), replace);
        }
        else
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
            boolean useCache = ChannelUtils.useCache;
            try {
                ChannelUtils.useCache = false;
                ImageIO.write(ChannelUtils.createAndComputeImage(graph.selectedNodes.lastElement().getChannel(), resX, resY, TextureEditor.INSTANCE.m_ProgressDialog, 3), "png", new File(name));
                TextureEditor.logger.info("Saved image to " + name);
            }
            catch (IOException exc) {
                exc.printStackTrace();
                TextureEditor.logger.error("IO Exception while exporting image: " + exc);
            }
            ChannelUtils.useCache = useCache;
        }
    }

    private void action_DeleteSelectedNodes() {
        graph.deleteSelection();
        repaint();
    }

    /**
     * This is the main action method for TextureGraphEditorPanel. Here the actions from the popup-menus
     * are processed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);

        if (e.getSource().getClass() == CreateMenuItem.class) { // this was one menu item from the create new channel menu
            CreateMenuItem mi = (CreateMenuItem) e.getSource();

            try {
                if (!mi.isAReplaceCall) { // insert a new Node
                    Channel chan = (Channel) mi.classType.getDeclaredConstructor().newInstance();
                    addTextureNode(new TextureGraphNode(chan), mousePosition.x - desktopX, mousePosition.y - desktopY);
                    repaint();
                }
                else { // try to replace an existing node as good as possible
                    TextureGraphNode node = graph.selectedNodes.get(0);
                    if (node != null) {
                        TextureGraphNode newNode = new TextureGraphNode((Channel) mi.classType.getDeclaredConstructor().newInstance());
                        replaceTextureNode(node, newNode);
                        repaint();
                    }
                    else {
                        TextureEditor.logger.warn("No node selected for replace");
                    }
                }
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == newChannelInsertMenuItem) {
            if (toCopyTextureGraphNode == null) {
                TextureEditor.logger.error("No node copied to insert");
            }
            else {
                addTextureNode(toCopyTextureGraphNode.cloneThisNode(), mousePosition.x - desktopX, mousePosition.y - desktopY);
                repaint();
            }
        }
        else if (e.getSource() == copyChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                toCopyTextureGraphNode = graph.selectedNodes.get(0).cloneThisNode();
            }
            else {
                TextureEditor.logger.error("No selection in copyChannel popup menu");
            }
        }
        else if (e.getSource() == replacepasteChannelMenuItem) {
            if (toCopyTextureGraphNode == null) {
                TextureEditor.logger.error("No node copied to replace paste");
            }
            else if (graph.selectedNodes.size() > 0) {
                replaceTextureNode(graph.selectedNodes.get(0), toCopyTextureGraphNode.cloneThisNode());
                repaint();
            }
            else {
                TextureEditor.logger.error("No selection in insert-replaceChannel popup menu");
            }
        }
        else if (e.getSource() == previewChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                addPreviewWindow(graph.selectedNodes.get(0));
                repaint();
            }
        }
        else if (e.getSource() == cloneChannelMenuItem) { // --------------------------------------------------------
            if (graph.selectedNodes.size() > 0) {
                TextureGraphNode orig = graph.selectedNodes.get(0);
                TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(graph.selectedNodes.get(0).getChannel()));
                addTextureNode(n, orig.getX() + 32, orig.getY() + 32);
                repaint();
            }
            else {
                TextureEditor.logger.error("No selection in cloneChannel popup menu");
            }
        }
        else if (e.getSource() == swapInputsChannelMenuItem) { // --------------------------------------------------------
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
                }
                else if (c1 != null) {
                    c1.target = p0;
                    graph.addConnection(c1);
                }
                else if (c0 != null) {
                    c0.target = p1;
                    graph.addConnection(c0);
                }
                else {
                    return;
                }
                repaint();
            }
        }
        else if (e.getSource() == addToPresetsChannelMenuItem) { // --------------------------------------------------------
            if (graph.selectedNodes.size() > 0) {
                if (graph.selectedNodes.get(0).getChannel() instanceof Pattern)
                    TextureEditor.INSTANCE.m_PatternSelector.addPatternPreset((Pattern) Channel.cloneChannel(graph.selectedNodes.get(0).getChannel()));
                else TextureEditor.logger.error("Invalid action 'Add to Presets': selected node is not a pattern");
            }
            else TextureEditor.logger.error("Invalid action 'Add To Presets': no selected nodes exists");
        }
        else if (e.getSource() == deleteChannelMenuItem) { // --------------------------------------------------------
            action_DeleteSelectedNodes();
        }
        else if (e.getActionCommand().equals("arbitraryResolutionExport")) {
            String resolution = JOptionPane.showInputDialog(this, "Specify your desried resolution (for example 1024x1024)", "What Resolution?", JOptionPane.QUESTION_MESSAGE);
            if (resolution != null && resolution.matches("\\d+x\\d+")) {
                int resX = Integer.parseInt(resolution.substring(0, resolution.indexOf('x')));
                int resY = Integer.parseInt(resolution.substring(resolution.indexOf('x') + 1));
                askFileAndExportTexture(resX, resY);
            }
        }
        else if (e.getActionCommand().matches("\\d+x\\d+")) {
            String s = e.getActionCommand();
            int resX = Integer.parseInt(s.substring(0, s.indexOf('x')));
            int resY = Integer.parseInt(s.substring(s.indexOf('x') + 1));
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
                    }
                    else if (e.getSource() == openGLNormalMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setNormalTextureNode(n);
                        repaint();
                    }
                    else if (e.getSource() == openGLSpecularMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setSpecWeightTextureNode(n);
                        repaint();
                    }
                    else if (e.getSource() == openGLHeightmapMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setHeightmapTextureNode(n);
                        repaint();
                    }
                }
                else TextureEditor.logger.warn("Incomplete channel for preview");
                // --------------------------------------------------------
            }
        }
    }

    private void addTextureNode(TextureGraphNode n, int x, int y) {
        graph.addNode(n, x, y);
        setSelectedNode(n);
    }

    private void replaceTextureNode(TextureGraphNode oldNode, TextureGraphNode newNode) {
        graph.replaceNode(oldNode, newNode);
        setSelectedNode(newNode);
    }

    private void setSelectedNode(TextureGraphNode node) {
        paramEditorPanel.setTextureNode(node);
        graph.setSelectedNode(node);
    }

    private void addSelectedNode(TextureGraphNode node) {
        graph.addOrRemoveNodeToSelection(node);
    }

    private boolean isNodeInSelection(TextureGraphNode node) {
        return graph.selectedNodes.contains(node);
    }

    void deleteFullGraph() {
        paramEditorPanel.setTextureNode(null);
        removeAllPreviewWindows();
        graph.deleteFullGraph();
        CacheTileManager.clearCache();
        repaint();
    }

    public void save(String filename) {
        TextureEditor.logger.info("Saving TextureGraph to " + filename);
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(filename));
            graph.save(w);

            // now the openGL settings
            if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.save(w);
            w.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean load(String filename, boolean eraseOld) {
        try {
            Scanner s = new Scanner(new BufferedReader(new FileReader(filename)));
            if (eraseOld) deleteFullGraph();

            graph.load(s);
            if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.load(s);
            repaint();

            return true;
        }
        catch (FileNotFoundException e) {
            TextureEditor.logger.error("Could not load " + filename);
            return false;
        }
        catch (InputMismatchException ime) {
            ime.printStackTrace();
            TextureEditor.logger.error("Could not load " + filename);
            return false;
        }
    }

    @Override
    public void nodeDeleted(TextureGraphNode node) {
        if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.notifyTextureNodeRemoved(node);
        if (paramEditorPanel.getActiveTextureNode() == node)
            paramEditorPanel.setTextureNode(null);

        removePreviewWindowByTextureNode(node);

        repaint();
    }

    // utility method to draw a conneciton line.
    private static void drawConnectionLine(Graphics2D g, int x0, int y0, int x1, int y1) {
        int offset = 6;

        g.drawLine(x0, y0, x0 - offset, y0);
        // g.drawLine(x0, y0 - offset, x1, y1 + offset);
        g.drawLine(x1 + offset, y1, x1, y1);

        var path = new GeneralPath();
        path.moveTo(x0 - offset, y0);
        path.curveTo(x0 - offset - 5, y0 + 15, x1 + offset - 5, y1 - 40, x1 + offset, y1);
        g.draw(path);

        // Draw the control points
        // g.drawOval(x0 - 5, y0 - 40, 15, 15);
        // g.drawOval(x1 - 5, y1 + 15, 15, 15);
    }
	
	/*
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
				ChannelUtils.computeImage(previewNode.getChannel(), previewNodeImage, null, 0);
			} else {
				Graphics g = previewNodeImage.getGraphics();
				g.fillRect(0, 0, previewNodeImage.getWidth(), previewNodeImage.getHeight());
			}
		}
		repaint();
	}
	*/

    private void drawConnectionPoint(Graphics2D g, int ox, int oy, ConnectionPoint p) {
        if (p.channelIndex == -1) { // input
            g.setColor(col_NodeInPort);
            g.fillRect(ox + p.getX(), oy + p.getY(), p.width, p.height);
        }
        else { // output
            g.setColor(col_NodeOutPort);
            g.fillRect(ox + p.getX(), oy + p.getY(), p.width, p.height);
        }
    }

    private void drawNode(Graphics2D g, TextureGraphNode node) {
        final int roundRad = 16;
        if (node.userData == null) node.userData = new NodePreviewImage(node);

        if (node.getChannel().vizType == ChannelVizType.SLOW)
            g.setColor(col_NodeSlowBG);
        else
            g.setColor(col_NodeBG);
        int x = node.getX() + desktopX;
        int y = node.getY() + desktopY;
        int w = node.getWidth();
        int h = node.getHeight();

        g.fillRect(x, y, w, h);

        //if (threadIsRecomputing) return;

        if (!node.isFolded()) {
            // TODO: Properly center the image
            g.drawImage(((NodePreviewImage) node.userData).previewImage, x + 12, y + 12 + 12, this);
        }

        g.setFont(font);

        g.setColor(Color.white);
        g.drawString(node.getChannel().getName(), x + 12, y + 12);

        // g.setColor(col_NodeBorder);
        // g.drawRect(x, y, w, h);

        for (ConnectionPoint p : node.getAllConnectionPointsVector()) {
            drawConnectionPoint(g, x, y, p);
        }

        // g.setColor(Color.white);
        // g.drawString("?", x + helpX + 6, y + helpY + 12);

        g.setColor(Color.white);
        for (var button : node.miniButtons) {
            button.draw(g, node.getX() + desktopX, node.getY() + desktopY);
        }

        // g.setColor(Color.white);
        // g.drawString("-", x + 4, y + 12);

        if (node.getChannel().isMarkedForExport()) {
            g.drawString("E", x + 4, y + helpY + 10);
			/*int h = g.getFontMetrics().getHeight() + 2;
			int x = getX() + 2;
			int y = getY() + h;
			
			g.setColor(new Color(0x00505084));
			g.fillRect(x, y-h, 12, h);
			g.setColor(Color.white);
			
			g.drawString("E", x+1, y-2);*/
        }
    }

    // TODO: Move to a different file
    //ad-hoc solution for button areas
    public static class MiniButton {
        int px, py;
        int w, h;
        String t;

        public MiniButton(int posX, int posY, int width, int height, String type) {
            px = posX;
            py = posY;
            w = width;
            h = height;
            t = type;
        }

        void draw(Graphics2D g, int ox, int oy) {
            g.drawString(t, px + ox + 4, py + oy + h - 4);
        }

        boolean inside(int x, int y) {
            return (x >= px && y >= py && x <= (px + w) && y <= (py + h));
        }
    }

    private Color gridColor = new Color(25, 25, 25);

    private BufferedImage canvas;

    private float zoom = 1.0f;

    public void paint(Graphics gr) {
//		super.paint(gr);

        if (canvas == null || (int) (getWidth() * zoom) != canvas.getWidth() || (int) (getHeight() * zoom) != canvas.getHeight()) {
            canvas = new BufferedImage((int) (getWidth() * zoom), (int) (getHeight() * zoom), BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = (Graphics2D) canvas.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setBackground(Color.DARK_GRAY);
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        g.setColor(gridColor);
        for (int y = desktopY % 16; y < h; y += 16) {
            g.drawLine(0, y, w, y);
        }
        for (int x = desktopX % 16; x < w; x += 16) {
            g.drawLine(x, 0, x, h);
        }

        // draw the connection lines
        g.setColor(Color.white);
        g.setStroke(connectionLineStroke);
        for (TextureNodeConnection c : graph.allConnections) {
            drawConnectionLine(g, desktopX + c.target.getWorldSpaceX(), desktopY + c.target.getWorldSpaceY(), desktopX + c.source.getWorldSpaceX(), desktopY + c.source.getWorldSpaceY());
        }

        g.setColor(col_NodeSelected);
        for (TextureGraphNode n : graph.selectedNodes) {
            //Rectangle r = new n.getBounds();
            g.fillRect(desktopX + n.getX() - 1, desktopY + n.getY() - 1, n.getWidth() + 2, n.getHeight() + 2);
        }

        g.setColor(Color.blue);
        for (TextureGraphNode n : graph.allNodes) {
            //Rectangle r = new n.getBounds();
            //g.drawRect(n.getX(), n.getY(), n.width, n.height);
            drawNode(g, n);
        }

        if (TextureEditor.GL_ENABLED) {
            TextureEditor.INSTANCE.m_OpenGLPreviewPanel.drawTokens(g, desktopX, desktopY);
        }

        if (connectionDragging) {
            g.setColor(Color.red);
            drawConnectionLine(g, connectionTarget.x, connectionTarget.y, desktopX + connectionOrigin.x, desktopY + connectionOrigin.y);
        }

        for (int i = previewWindows.size() - 1; i >= 0; i--) {
            previewWindows.get(i).draw(g);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (getWidth() != canvas.getWidth() || getHeight() != canvas.getHeight()) {
            gr.drawImage(canvas.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
            //gr.drawImage(canvas.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST), 0, 0, null);
        }
        else {
            gr.drawImage(canvas, 0, 0, null);
        }
    }

    private void moveDesktop(int dx, int dy) {
        desktopX += dx;
        desktopY += dy;
    }

    /**
     * computes the bounding box of all positions and centers it computes the bounding box of all positions and centers it
     */
    void centerDesktop() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (TextureGraphNode n : graph.allNodes) {
            minX = Math.min(minX, n.getX());
            minY = Math.min(minY, n.getY());
            maxX = Math.max(maxX, n.getX() + n.getWidth());
            maxY = Math.max(maxY, n.getY() + n.getHeight());
        }
        int dx = getWidth() / 2 - (minX + maxX) / 2;
        int dy = getHeight() / 2 - (minY + maxY) / 2;
        desktopX = dx;
        desktopY = dy;
        repaint();
    }

    private void showSelectedChannelPopupMenu(TextureGraphNode node, int x, int y) {
        if (node.getChannel() instanceof Pattern)
            addToPresetsChannelMenuItem.setEnabled(true);
        else addToPresetsChannelMenuItem.setEnabled(false);

        replacepasteChannelMenuItem.setEnabled(toCopyTextureGraphNode != null);
        selectedChannelPopupMenu.show(this, x, y); //!!TODO: refactoring artifact
    }

    private void showNewChannelPopupMenu(Component c, int x, int y) {
        newChannelInsertMenuItem.setEnabled(toCopyTextureGraphNode != null);
        newChannelPopupMenu.show(c, x, y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (desktopDragging) {
            int dx = (int) ((e.getXOnScreen() * zoom) - dragStartX);
            int dy = (int) ((e.getYOnScreen() * zoom) - dragStartY);
            moveDesktop(dx, dy);
            repaint();
        }
        else if (nodeDragging) {
            for (TextureGraphNode node : graph.selectedNodes) {
                node.movePosition((int) (e.getXOnScreen() * zoom) - dragStartX, (int) (e.getYOnScreen() * zoom) - dragStartY);
            }
            repaint();
        }
        else if (connectionDragging) {
            //connectionTarget = e.getPoint();
            connectionTarget.x = (int) (e.getX() * zoom);
            connectionTarget.y = (int) (e.getY() * zoom);
            repaint();
        }
        else if (draggedWindow != null) {
            int dx = (int) ((e.getXOnScreen()) - dragStartX / zoom);
            int dy = (int) ((e.getYOnScreen()) - dragStartY / zoom);
            draggedWindow.move(dx, dy);
            repaint();
        }

        dragStartX = (int) (e.getXOnScreen() * zoom);
        dragStartY = (int) (e.getYOnScreen() * zoom);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    // 1: drag component
    // 2: output node clicked
    // 3: Help button clicked
    // < 0: input node clicked; index = -val - 1
    private int getActionTypeForMouseClick(int x, int y, TextureGraphNode n) {
        // check if we clicked in the output node
        if (n.getOutputConnectionPoint().inside(x, y)) {
            return 2;
        }
        // now check if we clicked into an input node
        for (ConnectionPoint cp : n.getAllConnectionPointsVector()) {
            if (cp.inside(x, y))
                return -cp.channelIndex - 1;
        }

        // if ((x >= helpX + n.getX()) && (x <= (helpX + n.getX() + helpW)) && (y >= helpY + n.getY()) && (y <= (helpY + n.getY() + helpH))) {
        //     JOptionPane.showMessageDialog(this, n.getChannel().getHelpText(), n.getChannel().getName() + " Help", JOptionPane.PLAIN_MESSAGE);
        //     return 3;
        // }

        // if ((x >= helpX + n.getX() - 12) && (x <= (helpX + n.getX() + helpW - 12)) && (y >= helpY + n.getY()) && (y <= (helpY + n.getY() + helpH))) {
        //     n.setFolded(!n.isFolded());
        //     return 4;
        // }

        return 1;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePosition.x = (int) (e.getX() * zoom);
        mousePosition.y = (int) (e.getY() * zoom);

        dragStartX = (int) (e.getXOnScreen() * zoom);
        dragStartY = (int) (e.getYOnScreen() * zoom);
        int wsX = (int) ((e.getX()) * zoom) - desktopX;
        int wsY = (int) ((e.getY()) * zoom) - desktopY;

        if (e.getButton() == 2 || (e.isControlDown())) { // Desktop Dragging
            desktopDragging = true;
        }
        else if ((draggedWindow = getPreviewWindowAtPosition(mousePosition.x, mousePosition.y)) != null) {
            previewWindows.setElementAt(previewWindows.firstElement(), previewWindows.indexOf(draggedWindow));
            previewWindows.setElementAt(draggedWindow, 0);
            if (draggedWindow.doClick(mousePosition.x, mousePosition.y)) {
                draggedWindow = null;
            }
        }
        else {
            TextureGraphNode node = graph.getNodeAtPosition(wsX, wsY);
            if (node != null) {
                if (e.getButton() == 3) { // Popup menu for a TextureGraphNode
                    if (!isNodeInSelection(node)) setSelectedNode(node);
                    showSelectedChannelPopupMenu(node, e.getX(), e.getY());
                }
                else { // if it was not a popup we look if we clicked on a connection point or on the rest of the node
                    for (var button : node.miniButtons) {
                        if (button.inside(mousePosition.x - node.getX(), mousePosition.y - node.getY())) {
                            switch (button.t) {
                                case "-": {
                                    node.setFolded(!node.isFolded());
                                    break;
                                }

                                case "?": {
                                    JOptionPane.showMessageDialog(this, node.getChannel().getHelpText(), node.getChannel().getName() + " Help", JOptionPane.PLAIN_MESSAGE);
                                    break;
                                }
                            }
                        }
                    }

                    int actionType = getActionTypeForMouseClick(wsX, wsY, node);
                    if (e.isShiftDown()) { // add to selection of nodes
                        addSelectedNode(node);
                    }
                    else if (actionType == 1) { // want to drag the position of the node
                        if (!isNodeInSelection(node)) setSelectedNode(node);
                        nodeDragging = true;
                    }
                    else if (actionType == 2) { // dragging from the output node of a channel
                        connectionDragging = true;
                        connectionSource = node.getOutputConnectionPoint();
                        connectionOrigin = new Point(connectionSource.getWorldSpaceX(), connectionSource.getWorldSpaceY());
                        connectionTarget = e.getPoint();
                    }
                    else if (actionType < 0) { // dragging an existing connection of an input away
                        int index = -actionType - 1;
                        TextureGraphNode.ConnectionPoint inputPoint = node.getInputConnectionPointByChannelIndex(index);
                        TextureNodeConnection connection = graph.getConnectionAtInputPoint(inputPoint);
                        if (connection != null) {
                            graph.removeConnection(connection);
                            connectionDragging = true;
                            connectionSource = connection.source;
                            connectionOrigin = new Point(connectionSource.getWorldSpaceX(), connectionSource.getWorldSpaceY());
                            // pat.updatePreviewImage();
                            connectionTarget = e.getPoint();
                            //connectionTarget.x += node.getX();
                            //connectionTarget.y += node.getY();
                        }
                    }
                }
            }
            else if (e.getButton() == 3) {
                showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
            }
            else {
                setSelectedNode(null);
            }
        }

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePosition.x = (int) (e.getX() * zoom);
        mousePosition.y = (int) (e.getY() * zoom);

        int wsX = (int) ((mousePosition.x) * zoom) - desktopX;
        int wsY = (int) ((mousePosition.y) * zoom) - desktopY;

        //!!TODO: this notifies already on selecting a node; it should fire only when the node was actually moved
        if (nodeDragging || connectionDragging) notifyEditChangeListener();

        if (connectionDragging) {
            TextureGraphNode targetPat = graph.getNodeAtPosition(wsX, wsY);
            if (targetPat != null) {
                int actionType = getActionTypeForMouseClick(wsX, wsY, targetPat);
                if (actionType < 0) { // we dragged onto an input node
                    int index = -actionType - 1;


                    if (e.isControlDown()) { // connect all inputs
                        for (int i = 0; i < targetPat.getChannel().getNumInputChannels(); i++) {
                            TextureGraphNode.ConnectionPoint ip = targetPat.getInputConnectionPointByChannelIndex(i);
                            graph.addConnection(new TextureNodeConnection(connectionSource, ip));
                        }
                    }
                    else { // normal single connection
                        TextureGraphNode.ConnectionPoint inputPoint = targetPat.getInputConnectionPointByChannelIndex(index);
                        graph.addConnection(new TextureNodeConnection(connectionSource, inputPoint));
                    }
                }
            }
        }

        nodeDragging = false;
        connectionDragging = false;
        desktopDragging = false;
        draggedWindow = null;

        repaint();
    }

    public void addEditChangeListener(EditChangeListener listener) {
        editChangeListener.add(listener);
    }

    public boolean removeEditChangeListener(EditChangeListener listener) {
        return editChangeListener.remove(listener);
    }

    //!!TODO: this is not yet correctly called when the parameter of a channel changes
    private void notifyEditChangeListener() {
        //!!TODO: make the undo-stack here also

        for (EditChangeListener l : editChangeListener) l.graphWasEdited();
    }

    @Override
    public void channelChanged(Channel source) {
        notifyEditChangeListener();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // zoom currently disabled
//		if (e.getWheelRotation() < 0) {
//			if (zoom > 0.5f) {
//				//float x = e.getXOnScreen()*zoom;
//				//float y = e.getXOnScreen()*zoom;
//
//				zoom /= 2.0f;
//				repaint();
//			}
//		} else if (e.getWheelRotation() > 0) {
//			if (zoom < 4.0f) {
//				zoom *= 2.0f;
//				repaint();
//			}
//		}
    }

    private Vector<PreviewWindow> previewWindows = new Vector<>();

    /**
     * Checks if a PreviewWindow exists for the given TextureGraphNode and
     * removes it.
     */
    private void removePreviewWindowByTextureNode(TextureGraphNode node) {
        for (int i = 0; i < previewWindows.size(); i++) {
            PreviewWindow w = previewWindows.get(i);
            if (w.previewNode == node) {
                removePreviewWindow(w);
                i--;
            }
        }
    }

    private void removeAllPreviewWindows() {
        for (int i = previewWindows.size() - 1; i >= 0; i--) {
            removePreviewWindow(previewWindows.get(i));
        }
    }

    private void removePreviewWindow(PreviewWindow w) {
        w.previewNode.getChannel().removeChannelChangeListener(w);
        previewWindows.remove(w);
    }

    private void addPreviewWindow(TextureGraphNode node) {
        PreviewWindow w = new PreviewWindow(node);

        previewWindows.add(w);
    }

    private PreviewWindow getPreviewWindowAtPosition(int x, int y) {
        for (PreviewWindow w : previewWindows) {
            if (w.isInside(x, y)) return w;
        }

        return null;
    }

    // TODO: Move to a different file
    class PreviewWindow implements ChannelChangeListener {
        int posX;
        int posY;
        final BufferedImage previewNodeImage;
        BufferedImage tempImage;
        final TextureGraphNode previewNode;

        float zoom = 1.0f;

        Vector<MiniButton> miniButtons = new Vector<>();

        PreviewWindow(TextureGraphNode node) {
            previewNode = node;
            node.getChannel().addChannelChangeListener(this);
            previewNodeImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            updatePreviewImage();

            miniButtons.add(new MiniButton(256 - 16, 0, 16, 16, "X"));

            miniButtons.add(new MiniButton(0, 0, 16, 16, "+"));
            miniButtons.add(new MiniButton(16, 0, 16, 16, "-"));
        }

        void move(int dx, int dy) {
            posX += dx;
            posY += dy;

            if (posX < -128) posX = -128;
            if (posY < -128) posY = -128;

            if (posX > getWidth() - 128) posX = getWidth() - 128;
            if (posY > getHeight() - 128) posY = getHeight() - 128;
        }

        void draw(Graphics2D g) {
            g.drawImage(previewNodeImage, posX, posY + 16, null);

            g.setColor(Color.black);
            g.fillRect(posX, posY, previewNodeImage.getWidth(), 16);
            g.setColor(Color.white);

            g.drawRect(posX + -1, posY + -1, 258, 256 + 16 + 2);

            for (MiniButton b : miniButtons) {
                b.draw(g, posX, posY);
            }

            g.drawString(String.format("Zoom: %.3f", zoom), posX + 48, posY + 12);

//			if (previewNode != null) {
//				g.setColor(Color.blue);
//				g.drawLine(previewNode.getX() + desktopX, previewNode.getY() + desktopY, previewNodeImage.getWidth()/2, previewNodeImage.getHeight()/2);
//				g.drawImage(previewNodeImage, 0, 0, this);
//			}
        }

        /**
         * call this with world space position to make a click inside the window
         */
        boolean doClick(int x, int y) {
            for (MiniButton b : miniButtons) {
                if (b.inside(x - posX, y - posY)) {
                    if ("X".equals(b.t)) {
                        removePreviewWindow(this);
                    }
                    else if ("+".equals(b.t)) {
                        if (zoom < 256.0f) {
                            zoom *= 2.0f;
                            updatePreviewImage();
                        }
                    }
                    else if ("-".equals(b.t)) {
                        if (zoom > 0.125) {
                            zoom /= 2.0f;
                            updatePreviewImage();
                        }
                    }
                    return true;
                }
            }

            return false;
        }

        boolean isInside(int x, int y) {
            int width = previewNodeImage.getWidth();
            int height = previewNodeImage.getHeight() + 16;
            return (x >= posX) && (x <= posX + width) && (y >= posY) && (y <= posY + height);
        }

        void updatePreviewImage() {
            if (previewNode != null) {
                if (previewNode.getChannel().chechkInputChannels()) {

                    if (zoom == 1.0f) {
                        ChannelUtils.computeImage(previewNode.getChannel(), previewNodeImage, null, 0);
                    }
                    else if (zoom > 1.0f) {
                        ChannelUtils.computeImage(previewNode.getChannel(), previewNodeImage, null, 0, (int) (previewNodeImage.getWidth() * zoom), (int) (previewNodeImage.getHeight() * zoom), 0, 0);
                    }
                    else if (zoom < 1.0f) {
                        int lx = (int) (previewNodeImage.getWidth() * zoom);
                        int ly = (int) (previewNodeImage.getHeight() * zoom);
                        if (tempImage == null || tempImage.getWidth() != lx || tempImage.getHeight() != ly) {
                            tempImage = new BufferedImage(lx, ly, BufferedImage.TYPE_INT_RGB);
                        }
                        ChannelUtils.computeImage(previewNode.getChannel(), tempImage, null, 0);
                        Graphics g = previewNodeImage.getGraphics();
                        for (int py = 0; py < previewNodeImage.getHeight(); py += ly) {
                            for (int px = 0; px < previewNodeImage.getWidth(); px += lx) {
                                g.drawImage(tempImage, px, py, null);
                            }
                        }
                    }
                }
                else {
                    Graphics g = previewNodeImage.getGraphics();
                    g.fillRect(0, 0, previewNodeImage.getWidth(), previewNodeImage.getHeight());
                }
            }
        }

        @Override
        public void channelChanged(Channel source) {
            // TODO Auto-generated method stub
            updatePreviewImage();
        }
    }

}
