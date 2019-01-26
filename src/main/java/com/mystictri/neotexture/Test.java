package com.mystictri.neotexture;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Just a temporary test class for the TextureGenerator
 * 
 * @author Holger Dammertz
 *
 */
public class Test {
	
	public static void main(String[] args) {
		try {
			TextureGenerator.loadGraph(new FileInputStream("data/examples/example_RustyMetal.tgr"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (String s : TextureGenerator.getTextureNames())
			System.out.println(s);
		
		
		
		int size = 256;

		TextureGenerator.setUseCache(false);
		JFrame f = new JFrame();
		f.setLayout(new FlowLayout());
		long time = 0;
		int N = 0;
		for (String s : TextureGenerator.getTextureNames()) {
			BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			long startTime = System.nanoTime();
			int[] i = TextureGenerator.generateTexture_ARGB(s, size, size);
			time += System.nanoTime() - startTime;
			img.setRGB(0, 0, size, size, i, 0, size);
			f.add(new JLabel(new ImageIcon(img)));
			System.out.print(".");
			N++;
		}
		System.out.printf("Time per image: %f%n", (double) time / (N * 1000.0 * 1000.0));
		
		
		TextureGenerator.clearCache();

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

}
