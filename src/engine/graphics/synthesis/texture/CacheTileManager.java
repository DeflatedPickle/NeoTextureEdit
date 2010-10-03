package engine.graphics.synthesis.texture;

import java.nio.FloatBuffer;
import java.util.HashMap;

import engine.base.FMath;
import engine.base.Vector4;

/**
 * !!TODO: need to make sure that no memory leaks appear when a channel is deleted
 * 
 * @author Holger Dammertz
 * 
 */
public final class CacheTileManager {
	// HashMap<K, V>
	private static final HashMap<Channel, HashMap<ResolutionTag, TileCacheEntry>> tiles = new HashMap<Channel, HashMap<ResolutionTag, TileCacheEntry>>();
	
	private static final class ResolutionTag {
		final Integer globalXres;
		final Integer globalYres;
		final Integer xres; // the local x resolution of this tile (without border)
		final Integer yres; // the local y resolution of this tile
		final Integer px, py; // the location in the overall image

		public ResolutionTag(int xres, int yres, int px, int py, int border, int globalXres, int globalYres) {
			this.globalXres = globalXres;
			this.globalYres = globalYres;
			this.xres = xres;
			this.yres = yres;
			this.px = px;
			this.py = py;
			
		}
		
		@Override
		public int hashCode() {
			return globalXres.hashCode() ^ globalYres.hashCode() ^ xres.hashCode() ^ yres.hashCode() ^ px.hashCode() ^ py.hashCode();
		}
	}

	public static final class TileCacheEntry {
		public final int globalXres;
		public final int globalYres;
		public final int xres; // the local x resolution of this tile (without border)
		public final int yres; // the local y resolution of this tile
		public int px, py; // the location in the overall image
		boolean dirty;

		final Channel c;

		final int border; // the border size of this tile

		final FloatBuffer data;
		
		
		public void put(int i, final Vector4 val) {
			data.put(i * 4 + 0, val.x);
			data.put(i * 4 + 1, val.y);
			data.put(i * 4 + 2, val.z);
			data.put(i * 4 + 3, val.w);
		}		
		
		
		public void relocateCache(int px, int py) {
			if (this.px != px || this.py != py) setDirty();
			this.px = px;
			this.py = py;
		}
		
		public Vector4 sample(int x, int y) {
			int i = (x + y * xres) * 4;
			return new Vector4(data.get(i + 0), data.get(i + 1), data.get(i + 2), data.get(i + 3));
			// ret.set(values.get(i+0), values.get(i+1), values.get(i+2),
			// values.get(i+3));
		}

		public TileCacheEntry(Channel c, int xres, int yres, int px, int py, int border, int globalXres, int globalYres) {
			dirty = true;
			
			this.globalXres = globalXres;
			this.globalYres = globalYres;
			this.xres = xres;
			this.yres = yres;
			this.px = px;
			this.py = py;
			this.border = border;
			this.c = c;
			data = FloatBuffer.allocate((xres + 2 * border) * (yres + 2 * border) * 4);
		}

		public void setDirty() {
			dirty = true;
		}
		
		public void compute() {
			if (!dirty) return;
			
			if (c.getNumInputChannels() == 0) { // no input channels
				for (int y = py*yres-border, idx = 0; y < (py+1)*yres+border; y++)
					for (int x = px*xres-border; x < (px+1)*xres+border; x++, idx++) {
						float u = (float)x/(float)globalXres;
						float v = (float)y/(float)globalYres;
						u = u - FMath.ffloor(u);
						v = v - FMath.ffloor(v);
						put (idx, c.valueRGBA(u, v));
					}
			} else {
				TileCacheEntry[] tiles = new TileCacheEntry[c.getNumInputChannels()];
				
				for (int i = 0; i < tiles.length; i++) {
					tiles[i] = getCache(c.inputChannels[i], px, py, xres, yres, border, globalXres, globalYres);
				}
				
				for (int y = py*yres-border, idx = 0, localY = 0; y < (py+1)*yres+border; y++, localY++)
					for (int x = px*xres-border, localX = 0; x < (px+1)*xres+border; x++, idx++, localX++) {
						float u = (float)x/(float)globalXres;
						float v = (float)y/(float)globalYres;
						u = u - FMath.ffloor(u);
						v = v - FMath.ffloor(v);
						Vector4 temp = new Vector4();
						c.cache_function(temp, tiles, localX, localY, u, v);
						put(idx, temp);
					}
			}
			
			dirty = false;
		}
	}
	
	
	public static void setEntrysDirty(Channel c) {
		HashMap<ResolutionTag, TileCacheEntry> channelMap = tiles.get(c);
		if (channelMap == null) return;
		
		for (TileCacheEntry e : channelMap.values()) {
			e.setDirty();
		}
	}
	
	public static void removeChannel(Channel c) {
		tiles.put(c, null);
	}

	public static TileCacheEntry getCache(Channel c, int px, int py, int xres, int yres, int border, int globalXres, int globalYres) {
		HashMap<ResolutionTag, TileCacheEntry> channelMap = tiles.get(c);
		if (channelMap == null) {
			channelMap = new HashMap<ResolutionTag, TileCacheEntry>();
			tiles.put(c, channelMap);
		}
		
		ResolutionTag tag = new ResolutionTag(xres, yres, px, py, border, globalXres, globalYres);
		TileCacheEntry tile = channelMap.get(tag);
		if (tile == null) {
			tile = new TileCacheEntry(c, xres, yres, px, py, border, globalXres, globalYres);
			channelMap.put(tag, tile);
		}
		
		tile.compute();
		return tile;
	}

}
