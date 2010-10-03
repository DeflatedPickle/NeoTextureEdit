package com.mystictri.neotexture;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import engine.base.Utils;
import engine.graphics.synthesis.texture.CacheTileManager;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.FilterColorCorrect;
import engine.graphics.synthesis.texture.FilterColorize;
import engine.graphics.synthesis.texture.PatternBrick;

/**
 * Just a temporary test class for the TextureGenerator
 * 
 * @author Holger Dammertz
 *
 */
public class Test {
	
	
	private static int[] tempGetImage(int[] img, int globalXres, int globalYres, TileCacheEntry e) {
			for (int y = 0; y < e.yres; y++) {
				for (int x = 0; x < e.xres; x++) {
					img[x + e.px*e.xres + (y + e.py*e.yres) * globalXres] = Utils.vector4ToINTColor(e.sample(x, y));
				}
			}

		return img;
	}

	public static void main(String[] args) {
		try {
			TextureGenerator.loadGraph(new FileInputStream("data/examples/example_Bricks.tgr"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (String s : TextureGenerator.getTextureNames())
			System.out.println(s);
		
		
		Channel c2 = new PatternBrick();
		
		Channel c = new FilterColorize();
		c.setInputChannel(0, c2);
		
		int cxres = 256;
		int cyres = 256;
		int globalXres = 2048;
		int globalYres = 2048;
		int border = 0;
		
		int[] imgData = new int[globalXres * globalYres];

		for (int py = 0; py < globalYres/cyres; py++) {
			for (int px = 0; px < globalXres/cxres; px++) {
				TileCacheEntry e = CacheTileManager.getCache(c, px, py, cxres, cyres, border, globalXres, globalYres);
				tempGetImage(imgData, globalXres, globalYres, e);
			}
		}

		
		BufferedImage img = new BufferedImage(globalXres, globalXres, BufferedImage.TYPE_INT_ARGB);
		
		JFrame f = new JFrame();
		f.setLayout(new FlowLayout());
		img.setRGB(0, 0, globalXres, globalYres, imgData, 0, globalXres);
		f.add(new JLabel(new ImageIcon(img)));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
		
		
		/*
		int size = 256;

		TextureGenerator.setUseCache(true);
		JFrame f = new JFrame();
		f.setLayout(new FlowLayout());
		int N = 2;
		long time = 0;
		for (int k = 0; k < N; k++) {
			BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			long startTime = System.nanoTime();
			int[] i = TextureGenerator.generateTexture_ARGB("brick_" + ((k % 2) == 0 ? "d" : "n"), size, size);
			time += System.nanoTime() - startTime;
			img.setRGB(0, 0, size, size, i, 0, size);
			f.add(new JLabel(new ImageIcon(img)));
			System.out.print(".");
		}
		System.out.printf("Time per image: %f%n", (double) time / (N * 1000.0 * 1000.0));
		
		
		TextureGenerator.clearCache();

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);*/
	}

}
