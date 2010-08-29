package neoTextureEdit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.parameters.AbstractParam;


/**
 * This is a first preview experiment; currently it is just a dump collection of code to make it work.
 * When all the basic features are fixed it needs a serious cleanup.
 * 
 * @author Holger Dammertz
 *
 */
public class OpenGLPreviewPanel extends JPanel implements ChannelChangeListener, ActionListener {
	private static final long serialVersionUID = -1758179855070867819L;
	
	static final int GLXres = 256+128+128;
	static final int GLYres = 256+128;
	private OpenGLTextureRenderCanvas glcanvas;
	
	
	JPanel parameterPanel;
	JPopupMenu settingsPopupMenu;
	

	public OpenGLPreviewPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(GLXres + 16+ 512,GLYres+16));
		setSize(getPreferredSize());
		
		createPopupMenu();
		initGL();
		createParameterPanel();
	}
	
	void createPopupMenu() {
		settingsPopupMenu = new JPopupMenu();
		JMenu menu = new JMenu("Clear Texture");
		settingsPopupMenu.add(menu);
		createPopupMenuItem(menu, "Diffuse", "ClearDiffuse");
		createPopupMenuItem(menu, "Normal", "ClearNormal");
		createPopupMenuItem(menu, "Specular", "ClearSpecular");
		createPopupMenuItem(menu, "Heightmap", "ClearHeightmap");
	}
	
	private JMenuItem createPopupMenuItem(JMenu menu, String name, String action) {
		JMenuItem ret = new JMenuItem(name);
		if (action == null) action = name;
		ret.setActionCommand(action);
		ret.addActionListener(this);
		menu.add(ret);
		return ret;
	}

	/*
	private JMenuItem createPopupMenuItem(JPopupMenu menu, String name, String action) {
		JMenuItem ret = new JMenuItem(name);
		if (action == null) action = name;
		ret.setActionCommand(action);
		ret.addActionListener(this);
		menu.add(ret);
		return ret;
	}
	*/
	

	@Override
	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("ClearDiffuse")) {
			setDiffuseTextureNode(null);
			getParent().repaint();
		} else if (c.equals("ClearNormal")) {
			setNormalTextureNode(null);
			getParent().repaint();
		} else if (c.equals("ClearSpecular")) {
			setSpecWeightTextureNode(null);
			getParent().repaint();
		} else if (c.equals("ClearHeightmap")) {
			setHeightmapTextureNode(null);
			getParent().repaint();
		}
		
	}
	
	
	void createParameterPanel() {
		if (!TextureEditor.GL_ENABLED) return; 
		if (parameterPanel == null)	parameterPanel = new JPanel();
		else parameterPanel.removeAll();
		parameterPanel.setLayout(null);
		parameterPanel.setBounds(glcanvas.getWidth() + 16, 0, 512, glcanvas.getHeight());
		int x = 0;
		int y = 8;
		
		int oldNameWidth = AbstractParameterEditor.NAME_WIDTH;
		AbstractParameterEditor.NAME_WIDTH = 128;
		for (AbstractParam param : glcanvas.params.m_LocalParameters) {
			if (param.hidden) continue;
			Component c = ChannelParameterEditorPanel.getEditorForParam(param);
			if (c != null) {
				c.setLocation(x, y); y += c.getHeight();
				parameterPanel.add(c);
			} else {
				Logger.logWarning(this, "Could not create an editor for parameter " + param.getName());
			}
		}
		add(parameterPanel);
		AbstractParameterEditor.NAME_WIDTH = oldNameWidth;
	}
	
	
	private void token(Graphics g, int ofs, TextureGraphNode n, String s) {
		if (n == null) return;
		int h = g.getFontMetrics().getHeight() + 2;
		int x = n.getX() + n.getWidth() + 2;
		int y = n.getY() + h*(ofs+1);
		
		g.setColor(new Color(0x00505084));
		g.fillRect(x, y-h, 12, h);
		g.setColor(Color.white);
		
		g.drawString(s, x+1, y-2);
	}
	
	/** Marks the texture nodes used for preview */
	public void drawTokens(Graphics g) {
		token(g, 0, diffuseTexNode, "D");
		token(g, 1, normalTexNode, "N");
		token(g, 2, specWeightTexNode, "S");
		token(g, 3, heightmapTexNode, "H");
	}
	
	
	public void quit() {
		glcanvas.running = false;
	}
	
	public void save(Writer w) throws IOException  {
		w.write("glpreview ");
		// now store the 4 preview nodes
		w.write(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.indexOf(diffuseTexNode) + " ");
		w.write(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.indexOf(normalTexNode) + " ");
		w.write(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.indexOf(specWeightTexNode) + " ");
		w.write(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.indexOf(heightmapTexNode) + " ");
		
		if (TextureEditor.GL_ENABLED) {
			for (AbstractParam p : glcanvas.params.m_LocalParameters) {
				w.write(p.getName().replace(' ', '_') + " ");
				p.save(w);
			}
		}
		
		w.write("endglpreview\n");
	}
	
	public void load(Scanner s) {
		resetPreview();
		if (!s.hasNext()) return;
		s.next(); // glpreview
		
		int idx;
		if ((idx = s.nextInt())!= -1) setDiffuseTextureNode(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.get(idx));
		if ((idx = s.nextInt())!= -1) setNormalTextureNode(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.get(idx));
		if ((idx = s.nextInt())!= -1) setSpecWeightTextureNode(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.get(idx));
		if ((idx = s.nextInt())!= -1) setHeightmapTextureNode(TextureEditor.INSTANCE.m_GraphDrawPanel.allNodes.get(idx));
		
		String t;
		while (!(t = s.next()).equals("endglpreview")) {
			if (TextureEditor.GL_ENABLED) {
				AbstractParam param;
				if ((param = glcanvas.params.getParamByName(t.replace('_', ' '))) != null) {
					param.load(s);
				} else {
					Logger.logWarning(null, " loading of param " + t + " failed.");
				}
			}
		}
		
		// we need to recreate the parameter panel here so that all loaded values are reflected
		createParameterPanel();
	}
	
	
	boolean initGL() {
		try {
			glcanvas = new OpenGLTextureRenderCanvas(GLXres, GLYres, settingsPopupMenu);
			glcanvas.setBounds(8, 8, GLXres, GLYres);
			Display.setParent(glcanvas);
			add(glcanvas);
			glcanvas.startRenderThread();
			TextureEditor.GL_ENABLED = true; // if an exception accurs this stays false
			return true;
		} catch (LWJGLException e) {
			e.printStackTrace();
		} catch (UnsatisfiedLinkError le) {
			le.printStackTrace();
		}
		return false;
	}
	
	
	public void resetPreview() {
		setDiffuseTextureNode(null);
		setNormalTextureNode(null);
		setSpecWeightTextureNode(null);
		setHeightmapTextureNode(null);
	}
	
	
	TextureGraphNode diffuseTexNode;
	TextureGraphNode normalTexNode;
	TextureGraphNode specWeightTexNode;
	TextureGraphNode heightmapTexNode;
	
	private final TextureGraphNode setTexNode(TextureGraphNode o, TextureGraphNode n) {
		if (o != null) o.texChannel.removeChannelChangeListener(this);
		if (n == null) return null; 
		n.texChannel.addChannelChangeListener(this);
		return n;
	}
	
	/**
	 * This function should be called by the TextureGraphEditorPanel when a node is removed to let the
	 * preview-panel check if it was used as a texture and accordingly remove it from here.
	 * @param node
	 */
	public void notifyTextureNodeRemoved(TextureGraphNode node) {
		if (node == diffuseTexNode) setDiffuseTextureNode(null);
		if (node == normalTexNode) setNormalTextureNode(null);
		if (node == specWeightTexNode) setSpecWeightTextureNode(null);
		if (node == heightmapTexNode) setHeightmapTextureNode(null);
	}
	
	
	public void setDiffuseTextureNode(TextureGraphNode c) {
		diffuseTexNode = setTexNode(diffuseTexNode, c);
		if (!TextureEditor.GL_ENABLED) return;
		if (diffuseTexNode != null) channelChanged(diffuseTexNode.texChannel);
		else glcanvas.updateDiffuseMap(null);
	}

	public void setNormalTextureNode(TextureGraphNode c) {
		normalTexNode = setTexNode(normalTexNode, c); 
		if (!TextureEditor.GL_ENABLED) return;
		if (normalTexNode != null) channelChanged(normalTexNode.texChannel);
		else glcanvas.updateNormalMap(null);
	}
	
	public void setSpecWeightTextureNode(TextureGraphNode c) {
		specWeightTexNode = setTexNode(specWeightTexNode, c); 
		if (!TextureEditor.GL_ENABLED) return;
		if (specWeightTexNode != null) channelChanged(specWeightTexNode.texChannel);
		else glcanvas.updateSpecWeightMap(null);
	}

	public void setHeightmapTextureNode(TextureGraphNode c) {
		heightmapTexNode = setTexNode(heightmapTexNode, c); 
		if (!TextureEditor.GL_ENABLED) return;
		if (heightmapTexNode != null) channelChanged(heightmapTexNode.texChannel);
		else glcanvas.updateHeightMap(null);
	}

	
	public void channelChanged(Channel source) {
		if (!TextureEditor.GL_ENABLED) return;
		if ((diffuseTexNode != null) && (source == diffuseTexNode.texChannel)) glcanvas.updateDiffuseMap(diffuseTexNode.texChannel);
		if ((normalTexNode != null) && (source == normalTexNode.texChannel)) glcanvas.updateNormalMap(normalTexNode.texChannel);
		if ((specWeightTexNode != null) && (source == specWeightTexNode.texChannel)) glcanvas.updateSpecWeightMap(specWeightTexNode.texChannel);
		if ((heightmapTexNode != null) && (source == heightmapTexNode.texChannel)) glcanvas.updateHeightMap(heightmapTexNode.texChannel);
	}
	
	

}
