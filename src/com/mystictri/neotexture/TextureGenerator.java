package com.mystictri.neotexture;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import engine.base.Utils;
import engine.graphics.synthesis.texture.CacheTileManager;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;

/**
 * This is the public interface to the NeoTexture procedural texture generation
 * library. Note that the current version is heavily under development and none
 * of the interfaces, class names and features are final. Note also that the
 * current version is not yet optimized for speed. This will be done when the
 * interface and functionality becomes more stable.
 * 
 * Visit http://sourceforge.net/projects/neotextureedit/ for the latest version
 * and do not hesitate to contact me about feature requests or bug reports.
 * 
 * @author Holger Dammertz
 * 
 */
public final class TextureGenerator {
	static private boolean useCache = false;
	static private int cacheTileResolution = 256;

	/**
	 * To simplify the interface and texture access only a single graph existis
	 * in which all textures are loaded. This removes the possibility to use
	 * duplicate names but if a user needs this he simply has to manage it in
	 * the client code by clearing and reloading the graphs.
	 */
	static private final TextureGraph graph = new TextureGraph();

	/**
	 * Returns the current version string. Compatibility between the editor and
	 * the runtime library is currently only guaranteed when the same version is
	 * used.
	 */
	public static String getVersion() {
		return TextureVersion.version;
	}

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
	 * Enables or disables the use of the channel caching system when evaluating
	 * a texture. Default is true. Using the caching system can greatly increase
	 * the required memory during evaluation. Use the clearCache method after
	 * evaluating all needed textures or else the cache will be retained.
	 * 
	 * @param v
	 *            used to enable/disable the cache.
	 */
	public static void setUseCache(boolean v) {
		useCache = v;
	}

	/**
	 * Sets the resolution of a single cache tile. For each node in the graph a
	 * cache will be created. Default size is 256
	 * 
	 * @param res
	 *            the x resolution in pixel
	 */
	public static void setCacheTileResolution(int res) {
		cacheTileResolution = res;
	}

	/**
	 * Scans the currently loaded TextureGraph for all export names in the
	 * nodes.
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

	private static int[] tempGetImage(int[] img, int globalXres, int globalYres, TileCacheEntry e) {
		for (int y = 0; y < e.yres; y++) {
			int gy = (y + e.py * e.yres);
			if (gy >= globalYres)
				continue;
			for (int x = 0; x < e.xres; x++) {
				int gx = x + e.px * e.xres;
				if (gx >= globalXres)
					continue;
				img[gx + gy * globalXres] = Utils.vector4ToINTColor_ARGB(e.sample(x, y));
			}
		}

		return img;
	}
	
	//!!TODO: merge with method above
	private static int[] tempGetImage_ABGR(int[] img, int globalXres, int globalYres, TileCacheEntry e) {
		for (int y = 0; y < e.yres; y++) {
			int gy = (y + e.py * e.yres);
			if (gy >= globalYres)
				continue;
			for (int x = 0; x < e.xres; x++) {
				int gx = x + e.px * e.xres;
				if (gx >= globalXres)
					continue;
				img[gx + gy * globalXres] = Utils.vector4ToINTColor_ABGR(e.sample(x, y));
			}
		}

		return img;
	}

	// !!TODO; centralize the image computation method (join it with the one
	// from the Channel class)
	// and figure out a way to use the cache for the library
	private static int[] getImage_ARGB(int xres, int yres, Channel c) {
		int[] img = new int[xres * yres];

		if (useCache) {
			int cxres = cacheTileResolution;
			int cyres = cacheTileResolution;
			int globalXres = xres;
			int globalYres = yres;

			for (int py = 0; py < globalYres / (cyres + 1) + 1; py++) {
				for (int px = 0; px < globalXres / (cxres + 1) + 1; px++) {
					TileCacheEntry e = CacheTileManager.getCache(c, px, py, cxres, cyres, globalXres, globalYres);
					tempGetImage(img, globalXres, globalYres, e);
				}
			}
		} else { // don't use cache
			for (int y = 0; y < yres; y++) {
				// if (progress != null)
				// progress.setProgress(y/(float)img.getHeight());
				for (int x = 0; x < xres; x++) {
					float u = (float) x / (float) xres;
					float v = (float) y / (float) yres;
					img[x + y * xres] = Utils.vector4ToINTColor_ARGB(c.valueRGBA(u, v));
				}
			}
		}

		return img;
	}

	// !!TODO: merge this with the method above
	private static int[] getImage_ABGR(int xres, int yres, Channel c) {
		int[] img = new int[xres * yres];

		if (useCache) {
			int cxres = cacheTileResolution;
			int cyres = cacheTileResolution;
			int globalXres = xres;
			int globalYres = yres;

			for (int py = 0; py < globalYres / (cyres + 1) + 1; py++) {
				for (int px = 0; px < globalXres / (cxres + 1) + 1; px++) {
					TileCacheEntry e = CacheTileManager.getCache(c, px, py, cxres, cyres, globalXres, globalYres);
					tempGetImage_ABGR(img, globalXres, globalYres, e);
				}
			}
		} else { // don't use cache
			for (int y = 0; y < yres; y++) {
				// if (progress != null)
				// progress.setProgress(y/(float)img.getHeight());
				for (int x = 0; x < xres; x++) {
					float u = (float) x / (float) xres;
					float v = (float) y / (float) yres;
					img[x + y * xres] = Utils.vector4ToINTColor_ABGR(c.valueRGBA(u, v));
				}
			}
		}

		return img;
	}

	/**
	 * Clears the cache of each channel. Currently there is no internal maximum
	 * of cache entries thus manual clearing of the cache might be necessary.
	 * This will probably change in a future update...
	 */
	public static void clearCache() {
		CacheTileManager.clearCache();
	}

	/**
	 * Evaluates the node with the given name (it is the export name of the
	 * node) and returns a new int array with the ARGB8 data.
	 * 
	 * @param name
	 *            the export name of the node that should be evaluated into an
	 *            image texture
	 * @param xres
	 * @param yres
	 * @return null if the name was not found in the list of export names else a
	 *         new int[xres*yres] array filled with the RGBA image data
	 */
	public static int[] generateTexture_ARGB(String name, int xres, int yres) {

		for (TextureGraphNode n : graph.allNodes) {
			if (n.texChannel.exportName.get().equals(name)) {
				return getImage_ARGB(xres, yres, n.texChannel);
			}
		}

		return null;
	}
	
	public static int[] generateTexture_ABGR(String name, int xres, int yres) {

		for (TextureGraphNode n : graph.allNodes) {
			if (n.texChannel.exportName.get().equals(name)) {
				return getImage_ABGR(xres, yres, n.texChannel);
			}
		}

		return null;
	}
}
