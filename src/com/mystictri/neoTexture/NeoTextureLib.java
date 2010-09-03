package com.mystictri.neoTexture;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import engine.base.Utils;
import engine.graphics.synthesis.texture.Channel;

/**
 * This is the public interface to the NeoTexture procedural texture generation library.
 * 
 * @author Holger Dammertz
 *
 */
public final class NeoTextureLib {
	static private final TextureGraph graph = new TextureGraph();
	
	/**
	 * Parses a well formatted texture graph as saved by TextureGraph.save
	 * 
	 * @param is 
	 * @return true if the loading produced no critical error
	 */
	public static boolean loadGraph(InputStream is) {
		return graph.load(new Scanner(is));
	}
	
	
	/**
	 * Scans the currently loaded TextureGraph for all export names in the nodes.
	 * 
	 * @return A list of all the export names in the currently loaded graph
	 */
	public static List<String> getTextureNames() {
		LinkedList<String> l = new LinkedList<String>();
		
		for (TextureGraphNode n : graph.allNodes) {
			if (n.texChannel.exportName.get() != "") {
				l.add(n.texChannel.exportName.get());
			}
		}
		
		return l;
	}
	
	//!!TODO; centralize the image computation method (join it with the one from the Channel class)
	//        and figure out a way to use the cache for the library
	private static int[] getImage_ARGB(int xres, int yres, Channel c) {
		int[] img = new int[xres*yres];
		
		for (int y = 0; y < yres; y++) {
			//if (progress != null) progress.setProgress(y/(float)img.getHeight());
			for (int x = 0; x < xres; x++) {
				float u = (float)x/(float)xres;
				float v = (float)y/(float)yres;
				img[x+y*xres] = Utils.vector4ToINTColor(c.valueRGBA(u, v));
			}
		}
		
		return img;
	}
	
	
	/**
	 * Evaluates the node with the given name (it is the export name of the node) and returns
	 * a new int array with the ARGB data. Currently it uses no cache and thus needs to fully
	 * evaluate the graph for each pixel.
	 * 
	 * @param name the export name of the node that should be evaluated into an image texture
	 * @param xres 
	 * @param yres
	 * @return null if the name was not found in the list of export names else
	 *         a new int[xres*yres] array filled with the RGBA image data
	 */
	public static int[] generateTexture_ARGB(String name, int xres, int yres) {
		
		for (TextureGraphNode n : graph.allNodes) {
			if (n.texChannel.exportName.get().equals(name)) {
				return getImage_ARGB(xres, yres, n.texChannel);
			}
		}
		
		return null;
	}
	
	
	
	public static void main(String[] args) {
		try {
			loadGraph(new FileInputStream("data/examples/example_Bricks.tgr"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		for (String s : getTextureNames()) System.out.println(s);
		
		int size = 256;
		
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		int[] i = generateTexture_ARGB("brick_d", size, size);
		img.setRGB(0, 0, size, size, i, 0, size);

		JFrame f = new JFrame();
		f.add(new JLabel(new ImageIcon(img)));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
