package neoTextureEdit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.mystictri.neoTexture.TextureGraphNode;
import com.mystictri.neoTexture.TextureGraphNode.ConnectionPoint;

import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;

public class DrawableTextureGraphNode  extends JPanel implements ChannelChangeListener  {
	private static final long serialVersionUID = 1513202488192453658L;

	public static final Color ms_PatternColor = new Color(0x929AAF);
	public static final Color ms_SlowColor = new Color(128,16,16);
	

	static final int helpW = 16;
	static final int helpH = 16;
	static final int helpX = TextureGraphNode.width - helpW;
	static final int helpY = 0;
	
	static final Font font = new Font("Sans", Font.PLAIN, 10);

	BufferedImage previewImage;
	Color bgColor = Color.gray;

	boolean threadIsRecomputing = false;
	
	final TextureGraphNode node;
	
	public DrawableTextureGraphNode(TextureGraphNode node) {
		this.node = node;
		
		setPreferredSize(new Dimension(TextureGraphNode.width, TextureGraphNode.height));
		setSize(getPreferredSize());
	}

	
	public void updatePreviewImage() {
		if ((node.getChannel() != null) && (node.getChannel().chechkInputChannels())) {
			if (previewImage == null)
				previewImage = node.getChannel().createAndComputeImage(64, 64, null, 0);
			else
				node.getChannel().computeImage(previewImage, null, 0);
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
		g.drawString(node.getChannel().getName(), 2, 12+8);

		g.setColor(Color.white);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		
		
		Vector<ConnectionPoint> allCPs = node.getAllConnectionPointsVector();
		for (int i = 0; i < allCPs.size(); i++) {
		//	allCPs.get(i).draw(g);
		}
		
		g.setColor(Color.yellow);
		g.drawString("?", helpX+6, helpY+12);
		
		if (node.getChannel().isMarkedForExport()) {
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
		if (node.getOutputConnectionPoint().inside(x, y)) {
			return 2;
		}
		// now check if we clicked into an input node
		Vector<ConnectionPoint> allCPs = node.getAllConnectionPointsVector();
		for (int i = 0; i < allCPs.size(); i++) {
			ConnectionPoint cp = allCPs.get(i);
			if (cp.inside(x, y))
				return -cp.channelIndex - 1;
		}
		
		if ((x >= helpX) && (x <= (helpX + helpW)) && (y >= helpY) && (y <= (helpY + helpH))) {
			JOptionPane.showMessageDialog(this, node.getChannel().getHelpText(), node.getChannel().getName() + " Help", JOptionPane.PLAIN_MESSAGE);
			return 3;
		}


		return 1;
	}
	
	
	public void channelChanged(Channel source) {
		updatePreviewImage();
	}

	
}
