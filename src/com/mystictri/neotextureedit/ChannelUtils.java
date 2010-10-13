package com.mystictri.neotextureedit;

import java.awt.image.BufferedImage;

import engine.base.Logger;
import engine.base.Utils;
import engine.base.Vector3;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ProgressBarInterface;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;

/**
 * A collection of helper methods that use channels
 * @author Holger Dammertz
 *
 */
public final class ChannelUtils {
	public static boolean useCache = true;
	
	
	public static final int minCacheSize = 64;

	
	// !!TODO: not thread save
	public static long lastComputationTime;

	
	public static void computeImage(Channel c, final BufferedImage img, final ProgressBarInterface progress, final int mode) {
		_computeImage(c, img, progress, mode);
	}

	/**
	 * Computes a image from the given channel; mode 0: RGB, 1: blended with
	 * background; 2: alpha as grayscale
	 */
	private static void _computeImage(Channel c, BufferedImage img, ProgressBarInterface progress, int mode) {

		if (!c.chechkInputChannels()) {
			Logger.logError(null, "Computing image from incomplete channel not possible!");
			return;
		}
		// HACK_cache.clear();

		// DELTA Hack for Derivative:
		// deltaU = 1.0f/(float)img.getWidth();
		// deltaV = 1.0f/(float)img.getHeight();

		if (progress != null)
			progress.startProgress();
		long time = System.currentTimeMillis();

		TileCacheEntry tce = null;
		if (useCache && (img.getWidth() >= minCacheSize && img.getHeight() >= minCacheSize)) {
			 tce = CacheTileManager.getCache(c, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
		}
		
		for (int y = 0; y < img.getHeight(); y++) {
				if (progress != null)
					progress.setProgress(y / (float) img.getHeight());
				for (int x = 0; x < img.getWidth(); x++) {
					float u = (float) x / (float) img.getWidth();
					float v = (float) y / (float) img.getHeight();

					final Vector4 col;
					if (tce == null) col = c.valueRGBA(u, v);
					else col = tce.sample(x, y);
					
					final Vector3 color = new Vector3();
					// !!UGH TODO: optimize this!!
					if (mode == 0)
						color.set(col.getVector3());
					else if (mode == 1) {
						float bg = ((((x + y) / 8) % 2) != 0) ? 1.0f : 0.75f;
						col.x = col.x * col.w + bg * (1.0f - col.w);
						col.y = col.y * col.w + bg * (1.0f - col.w);
						col.z = col.z * col.w + bg * (1.0f - col.w);
						color.set(col.getVector3());
					} else if (mode == 2) {
						color.set(col.w);
					} else
						Logger.logError(null, "Wrong in computeImage");
					int val = Utils.vector3ToINTColor(color);
					img.setRGB(x, y, val);
				}
			}
		

		lastComputationTime = System.currentTimeMillis() - time;
		if (progress != null)
			progress.endProgress();
	}

	/**
	 * A Utility method that creates a new image and fills it with the values of
	 * the pattern in [0, 1)
	 * 
	 * @param xres
	 * @param yres
	 * @return
	 */
	public static BufferedImage createAndComputeImage(Channel c, int xres, int yres, ProgressBarInterface progress, int mode) {
		BufferedImage ret = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);

		computeImage(c, ret, progress, mode);

		return ret;
	}
}
