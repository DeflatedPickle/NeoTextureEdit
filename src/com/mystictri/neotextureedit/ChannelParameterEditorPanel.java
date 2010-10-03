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

package com.mystictri.neotextureedit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import com.mystictri.neotexture.TextureGraphNode;

import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.parameters.AbstractParam;
import engine.parameters.BoolParam;
import engine.parameters.ColorGradientParam;
import engine.parameters.ColorParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import engine.parameters.ImageParam;
import engine.parameters.InfoParam;
import engine.parameters.IntParam;
import engine.parameters.Matrix3x3Param;
import engine.parameters.ParamChangeListener;
import engine.parameters.TextParam;

/**
 * The ChannelParameterEditorPanel is used by the TextureEditor to
 * show and edit all parameters of the selected Channel.
 * @author Holger Dammertz
 *
 */
class ChannelParameterEditorPanel extends JPanel implements ChannelChangeListener, ActionListener {
	private static final long serialVersionUID = 6344417563998225104L;
	private static final int previewImageSize = 256;
	
	public static final int scrollBarSpace = 24; // reserves free space for the somtimes necessary scrollbar on the side of the parameters
	
	private TextureGraphNode node; // the node that gets modified
	private BufferedImage previewImage;
	private JLabel benchmarkLabel;
	
	JPanel scrollPaneContent;
	JScrollPane scrollPane;
	
	JPanel previewPanel;
	
	int previewImageMode = 0;


	public ChannelParameterEditorPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(previewImageSize+scrollBarSpace, 800));
		setSize(getPreferredSize());

		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, previewImageSize+scrollBarSpace, getHeight() - previewImageSize);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(24);
		scrollPane.getVerticalScrollBar().setBlockIncrement(24);
		scrollPaneContent = new JPanel();
		scrollPaneContent.setLayout(null);
		scrollPane.getViewport().add(scrollPaneContent);
		add(scrollPane);
		
		// This is the preview panel containing the RGB switch buttons;
		
		previewPanel = new JPanel();
		previewPanel.setBounds(0, getHeight() - previewImageSize-16, previewImageSize+scrollBarSpace, previewImageSize+scrollBarSpace);
		previewPanel.setLayout(null);
		JToggleButton buttRGB = new JToggleButton(TextureEditor.INSTANCE.Get_IconRGB());
		buttRGB.setActionCommand("RGB");
		buttRGB.setToolTipText("Display RGB");
		buttRGB.setBounds(previewImageSize, 1*scrollBarSpace, scrollBarSpace, scrollBarSpace);
		buttRGB.addActionListener(this);
		previewPanel.add(buttRGB);
		JToggleButton buttRGBA = new JToggleButton(TextureEditor.INSTANCE.Get_IconRGBA());
		buttRGBA.setActionCommand("RGBA");
		buttRGBA.setToolTipText("Display RGBA blended");
		buttRGBA.setBounds(previewImageSize, 2*scrollBarSpace, scrollBarSpace, scrollBarSpace);
		buttRGBA.addActionListener(this);
		previewPanel.add(buttRGBA);
		JToggleButton buttA = new JToggleButton(TextureEditor.INSTANCE.Get_IconA());
		buttA.setActionCommand("A");
		buttA.setToolTipText("Display Alpha as grayscale");
		buttA.setBounds(previewImageSize, 3*scrollBarSpace, scrollBarSpace, scrollBarSpace);
		buttA.addActionListener(this);
		previewPanel.add(buttA);
		
		ButtonGroup group = new ButtonGroup();
		group.add(buttRGB);
		group.add(buttRGBA);
		group.add(buttA);
		buttRGB.setSelected(true);

		benchmarkLabel = new JLabel("");
		benchmarkLabel.setBounds(8, 8, 200, 16);
		previewPanel.add(benchmarkLabel);

		add(previewPanel);
		previewPanel.setVisible(false);
	}

	public TextureGraphNode getActiveTextureNode() {
		return node;
	}
	
	
	static class InfoLabel extends JLabel implements ParamChangeListener {
		private static final long serialVersionUID = 1260357768086468919L;
		InfoParam param;
		
		public InfoLabel(InfoParam p) {
			setPreferredSize(new Dimension(256,24));
			setSize(getPreferredSize());
			param = p;
			p.addParamChangeListener(this);
			setText(p.get());
			
		}

		@Override
		public void parameterChanged(AbstractParam source) {
			setText(param.get());
		}
	}
	
	/**
	 * Checks the class of param with all known types and returns an appropriate ParameterEditor as component
	 * @param param
	 * @return null if the param was unknown
	 */
	public static Component getEditorForParam(AbstractParam param) {
		if (param.getClass() == FloatParam.class)
			return new FloatParameterEditor((FloatParam) param);
		else if (param.getClass() == IntParam.class)
			return new IntParameterEditor((IntParam) param);
		else if (param.getClass() == TextParam.class)
			return new TextParameterEditor((TextParam) param);
		else if (param.getClass() == BoolParam.class)
			return new BoolParameterEditor((BoolParam) param);
		else if (param.getClass() == ColorParam.class)
			return new ColorParameterEditor((ColorParam) param, TextureEditor.INSTANCE.m_ColorChooser);
		else if (param.getClass() == EnumParam.class)
			return new EnumParameterEditor((EnumParam) param);
		else if (param.getClass() == ColorGradientParam.class)
			return new GradientEditorPanel((ColorGradientParam) param);
		else if (param.getClass() == ImageParam.class)
			return new ImageParameterEditor((ImageParam) param);
		else if (param.getClass() == InfoParam.class)
			return new InfoLabel((InfoParam) param);
		else if (param.getClass() == Matrix3x3Param.class)
			return new Matrix3x3ParameterEditor((Matrix3x3Param) param);
		else
			return null;
	}
	

	public void setTextureNode(TextureGraphNode n) {
		if (n == node)
			return;
		if (node != null) 
			node.getChannel().removeChannelChangeListener(this);
		
		scrollPaneContent.removeAll();
		node = n;
		if (n != null) {
			node.getChannel().addChannelChangeListener(this);
			Channel c = n.getChannel();
			Vector<AbstractParam> params = c.getParameters();
			int x = 8;
			int y = 8;
			JLabel title = new JLabel(" Type: " + c.getClass().getSimpleName());
			title.setBorder(BorderFactory.createEtchedBorder());
			title.setBounds(x, y, 240, 24);
			y += 30;
			scrollPaneContent.add(title);

			Component editor;
			for (AbstractParam param : params) {
				editor = getEditorForParam(param);
				if (editor == null) {
					editor = new JLabel(param.getName());
					editor.setBounds(x, y, 128, 24);
				}
				editor.setLocation(x, y);
				y += editor.getHeight() + 4;
				scrollPaneContent.add(editor);
			}
			y += 8;
			/*editor = new Matrix3x3ParameterEditor(c.transformation, c);
			editor.setLocation(x, y);
			y += editor.getHeight();
			scrollPaneContent.add(editor);*/

			scrollPaneContent.setPreferredSize(new Dimension(previewImageSize, y));
			scrollPaneContent.setSize(scrollPaneContent.getPreferredSize());
			previewPanel.setVisible(true);
		} else { // node == null
			scrollPaneContent.setPreferredSize(new Dimension(previewImageSize, 0));
			scrollPaneContent.setSize(scrollPaneContent.getPreferredSize());
			benchmarkLabel.setText("");
			previewPanel.setVisible(false);
		}

		channelChanged(null);
	}

	public void paint(Graphics g) {
		scrollPane.setBounds(0, 0, previewImageSize+scrollBarSpace, getHeight() - previewImageSize - scrollBarSpace);
		previewPanel.setBounds(0, getHeight() - previewPanel.getHeight(), previewImageSize+scrollBarSpace, previewImageSize+scrollBarSpace);
		super.paint(g);
		if (node != null) {
			//g.drawImage(previewImage, scrollBarSpace/2, getHeight() - previewImageSize - scrollBarSpace/2, this);
			g.drawImage(previewImage, 0, getHeight() - previewImageSize , this);
		}
	}

	@Override
	public void channelChanged(Channel source) {
		//System.out.println("ChannelParameterEditor: Channel Change " + node);
		if (node == null) {
			previewImage = null;
		} else if (node.getChannel().chechkInputChannels()) {
			if (previewImage == null)
				previewImage = ChannelUtils.createAndComputeImage(node.getChannel(), previewImageSize, previewImageSize, null, previewImageMode);
			else
				ChannelUtils.computeImage(node.getChannel(), previewImage, null, previewImageMode);
			benchmarkLabel.setText("Benchmark: "+ChannelUtils.lastComputationTime);
		} else {
			previewImage = null;
		}
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("RGB")) {
			if (previewImageMode != 0) {
				previewImageMode = 0;
				channelChanged(null);
			}
		} else if (c.equals("RGBA")) {
			if (previewImageMode != 1) {
				previewImageMode = 1;
				channelChanged(null);
			}
		} else if (c.equals("A")) {
			if (previewImageMode != 2) {
				previewImageMode = 2;
				channelChanged(null);
			}
		} 
		
	}
}

