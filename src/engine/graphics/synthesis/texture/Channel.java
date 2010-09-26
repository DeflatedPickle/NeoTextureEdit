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

package engine.graphics.synthesis.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

import engine.base.FMath;
import engine.base.Logger;
import engine.base.Utils;
import engine.base.Vector3;
import engine.base.Vector4;
import engine.parameters.AbstractParam;
import engine.parameters.LocalParameterManager;
import engine.parameters.TextParam;

/**
 * A channel is an Procedural Texture Generation interface that can get 0 or
 * more inputs and produces a single output value (either scalar or RGB)
 * 
 * @author Holger Dammertz
 * 
 */
public abstract class Channel extends LocalParameterManager {
	public TextParam exportName = CreateLocalTextParam("ExportName", "");
	Channel[] inputChannels;

	/**
	 * This is used for rendering and should be set in the constructor if it is
	 * not a normal channel (for example for slow bitmap operations)
	 */
	public enum ChannelVizType {
		NORMAL, SLOW
	};

	public ChannelVizType vizType = ChannelVizType.NORMAL;

	public static final class CacheEntry {
		public FloatBuffer values;
		int xres, yres;

		public CacheEntry(int x, int y) {
			xres = x;
			yres = y;
			values = FloatBuffer.allocate(x * y * 4);
		}

		public void copyTo(CacheEntry c) {
			c.values.rewind();
			values.rewind();
			if (c.values.capacity() != values.capacity()) {
				Logger.logError(this, "Invalid array sizes for cache copyTo.");
				return;
			}
			c.values.put(values);
		}

		public void put(int i, Vector4 val) {
			values.put(i * 4 + 0, val.x);
			values.put(i * 4 + 1, val.y);
			values.put(i * 4 + 2, val.z);
			values.put(i * 4 + 3, val.w);
		}

		public Vector4 sample(float u, float v) {
			int x = ((int) (u * xres));
			int y = ((int) (v * yres));
			while (x < 0)
				x += xres;
			while (y < 0)
				y += yres;
			while (x >= xres)
				x -= xres;
			while (y >= yres)
				y -= xres;
			int i = (x + y * xres) * 4;
			return new Vector4(values.get(i + 0), values.get(i + 1), values.get(i + 2), values.get(i + 3));
			// ret.set(values.get(i+0), values.get(i+1), values.get(i+2),
			// values.get(i+3));
		}

		public Vector4 du(float u, float v) {
			float u0 = (u - 1.0f / xres);
			if (u0 < 0)
				u0 += 1.0f;
			float u1 = (u + 1.0f / xres);
			if (u1 >= 1)
				u1 -= 1.0f;
			return sample(u1, v).sub_ip(sample(u0, v));
		}

		public Vector4 dv(float u, float v) {
			float v0 = (v - 1.0f / yres);
			if (v0 < 0)
				v0 += 1.0f;
			float v1 = (v + 1.0f / yres);
			if (v1 >= 1)
				v1 -= 1.0f;
			return sample(u, v1).sub_ip(sample(u, v0));
		}

	}

	CacheEntry cache;
	boolean cacheInvalid = true;

	/**
	 * Clears the local cache of this node.
	 */
	public void clearCache() {
		cache = null;
		cacheInvalid = true;
	}

	// !!TODO: not thread save
	public static long lastComputationTime;

	public String getName() {
		return "Channel";
	}

	/** Checks if the channel is marked for export into a texture image */
	public boolean isMarkedForExport() {
		return (exportName.get().length() > 0);
	}

	public String getHelpText() {
		return "No Help Text for this channel";
	}

	public enum OutputType {
		SCALAR, RGBA
	}

	protected Vector<ChannelChangeListener> changeListener = new Vector<ChannelChangeListener>();

	public void addChannelChangeListener(ChannelChangeListener listener) {
		changeListener.add(listener);
	}

	public void removeChannelChangeListener(ChannelChangeListener listener) {
		if (!changeListener.remove(listener)) {
			Logger.logWarning(this, "tried to remove nonexisting ChannelChangeListener " + listener + " from " + this);
		}
	}

	/**
	 * We just notify all channel change listener if a parameter changed. This
	 * method can also be called with source == null; it then means that a
	 * parameter silently changed. Subclasses of channel who override this
	 * method needs to account for source==null and recreate all internal data
	 * structures accordingly.
	 */
	public void parameterChanged(AbstractParam source) {
		cacheInvalid = true;

		for (ChannelChangeListener c : changeListener) {
			c.channelChanged(this);
		}
	}

	protected Channel() {
	}

	protected Channel(int numInputChannels) {
		inputChannels = new Channel[numInputChannels];
	}

	public void setInputChannel(int idx, Channel c) {
		if (inputChannels[idx] != c) {
			inputChannels[idx] = c;
			parameterChanged(null);
		}
	}

	abstract public OutputType getChannelInputType(int idx);

	public int getNumInputChannels() {
		if (inputChannels == null)
			return 0;
		return inputChannels.length;
	}

	public boolean chechkInputChannels() {
		if (inputChannels == null)
			return true;
		boolean ret = true;
		for (int i = 0; i < inputChannels.length; i++) {
			if (inputChannels[i] != null) {
				if (!inputChannels[i].chechkInputChannels())
					return false;
			} else
				return false;

		}
		return ret;
	}


	public Vector4 valueRGBA(float u, float v) {
		Vector4 val = _valueRGBA(u - FMath.ffloor(u), v - FMath.ffloor(v));
		return val;
	}

	public static float deltaU = 1.0f / 256.0f;
	public static float deltaV = 1.0f / 256.0f;

	public Vector4 du1f(float u, float v) {
		return new Vector4(valueRGBA(u + deltaU, v)).sub_ip(valueRGBA(u - deltaU, v));
	}

	public Vector4 dv1f(float u, float v) {
		return new Vector4(valueRGBA(u, v + deltaV)).sub_ip(valueRGBA(u, v - deltaV));
	}

	CacheEntry getCache(int xres, int yres) {
		if (cacheInvalid)
			return null;
		if (cache.xres == xres && cache.yres == yres)
			return cache;
		return null;
	}

	CacheEntry getEmptyCacheEntry(int xres, int yres) {
		if (cache != null && cache.xres == xres && cache.yres == yres)
			return cache;
		else {
			CacheEntry c = new CacheEntry(xres, yres);
			putCache(c);
			return c;
		}
	}

	void putCache(CacheEntry c) {
		cache = c;
	}

	public CacheEntry valuesRGBA_Cached(int xres, int yres) {
		CacheEntry c = getCache(xres, yres);
		if (c != null)
			return c;
		else {
			cacheInvalid = false;
			return _valuesRGBA_Cached(xres, yres);
		}
	}

	protected void cache_function(Vector4 out, CacheEntry[] in, float u, float v) {
		out.set(1, 0, 0, 1);
	}

	// This function should be overwritten in
	protected CacheEntry _valuesRGBA_Cached(int xres, int yres) {
		CacheEntry out = getEmptyCacheEntry(xres, yres);
		CacheEntry[] input = null;
		Vector4[] inps = null;
		int num = 0;

		if (getNumInputChannels() > 0) {
			num = getNumInputChannels();
			input = new CacheEntry[num];
			inps = new Vector4[num];
			for (int i = 0; i < num; i++)
				input[i] = inputChannels[i].valuesRGBA_Cached(xres, yres);
		}

		final Vector4 temp = new Vector4();
		for (int i = 0; i < num; i++)
			inps[i] = new Vector4();

		for (int y = 0; y < yres; y++) {
			for (int x = 0; x < xres; x++) {
				int idx = (x + y * xres);
				float u = (float) x / (float) xres;
				float v = (float) y / (float) yres;
				if (num > 0)
					cache_function(temp, input, u, v);
				else
					temp.set(valueRGBA(u, v));
				out.put(idx, temp);
			}
		}
		return out;
	}

	// these three methods need to be overwritten in the subclasses

	public OutputType getOutputType() {
		return OutputType.SCALAR;
	}

	protected Vector4 _valueRGBA(float u, float v) {
		return new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
	}

	/*
	 * protected float _value1f(float u, float v) { return 0.0f; }
	 */

	// ------------------------------------------------------

	public static boolean useCache = true;

	public void computeImage(final BufferedImage img, final ProgressBarInterface progress, final int mode) {
		_computeImage(img, progress, mode);
	}

	/**
	 * Computes a image from the given channel; mode 0: RGB, 1: blended with
	 * background; 2: alpha as grayscale
	 */
	private void _computeImage(BufferedImage img, ProgressBarInterface progress, int mode) {

		if (!chechkInputChannels()) {
			Logger.logError(this, "Computing image from incomplete channel not possible!");
			return;
		}
		// HACK_cache.clear();

		// DELTA Hack for Derivative:
		// deltaU = 1.0f/(float)img.getWidth();
		// deltaV = 1.0f/(float)img.getHeight();

		if (progress != null)
			progress.startProgress();
		long time = System.currentTimeMillis();

		if (useCache && img.getWidth() == 256) {
			CacheEntry temp = valuesRGBA_Cached(img.getWidth(), img.getHeight());
			// System.out.println("USING CACHE!!");
			for (int y = 0; y < img.getHeight(); y++) {
				if (progress != null)
					progress.setProgress(y / (float) img.getHeight());
				for (int x = 0; x < img.getWidth(); x++) {
					final Vector4 col = new Vector4(temp.values.get((x + y * temp.xres) * 4 + 0), temp.values.get((x + y * temp.xres) * 4 + 1), temp.values
							.get((x + y * temp.xres) * 4 + 2), temp.values.get((x + y * temp.xres) * 4 + 3));
					final Vector3 color = new Vector3();
					// !!UGH TODO: optimize this!!
					if (mode == 0)
						color.set(col.getVector3());
					else if (mode == 1) {
						float bg = ((((x + y) / 8) % 2) == 1) ? 1.0f : 0.75f;
						col.x = col.x * col.w + bg * (1.0f - col.w);
						col.y = col.y * col.w + bg * (1.0f - col.w);
						col.z = col.z * col.w + bg * (1.0f - col.w);
						color.set(col.getVector3());
					} else if (mode == 2) {
						color.set(col.w);
					} else
						Logger.logError(this, "Wrong in computeImage");
					int val = Utils.vector3ToINTColor(color);
					img.setRGB(x, y, val);
				}
			}

		} else {
			for (int y = 0; y < img.getHeight(); y++) {
				if (progress != null)
					progress.setProgress(y / (float) img.getHeight());
				for (int x = 0; x < img.getWidth(); x++) {
					float u = (float) x / (float) img.getWidth();
					float v = (float) y / (float) img.getHeight();

					final Vector4 col = valueRGBA(u, v);
					final Vector3 color = new Vector3();
					// !!UGH TODO: optimize this!!
					if (mode == 0)
						color.set(col.getVector3());
					else if (mode == 1) {
						float bg = ((((x + y) / 8) % 2) == 1) ? 1.0f : 0.75f;
						col.x = col.x * col.w + bg * (1.0f - col.w);
						col.y = col.y * col.w + bg * (1.0f - col.w);
						col.z = col.z * col.w + bg * (1.0f - col.w);
						color.set(col.getVector3());
					} else if (mode == 2) {
						color.set(col.w);
					} else
						Logger.logError(this, "Wrong in computeImage");
					int val = Utils.vector3ToINTColor(color);
					img.setRGB(x, y, val);
				}
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
	public BufferedImage createAndComputeImage(int xres, int yres, ProgressBarInterface progress, int mode) {
		BufferedImage ret = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);

		computeImage(ret, progress, mode);

		return ret;
	}

	/**
	 * Creates a full copy of the given channel by using an internal
	 * StringWriter and by calling the saveChannel and loadChannel method
	 * 
	 * @param c
	 * @return a full copy of the given channel as if it was loaded from disk
	 */
	public static Channel cloneChannel(Channel c) {
		StringWriter sw = new StringWriter();
		try {
			saveChannel(sw, c);
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loadChannel(new Scanner(sw.getBuffer().toString()));
	}

	/**
	 * This method saves a Channel to a given writer by storing all parameters.
	 * For Parameter storage all spaces in a name are replaced with an
	 * underscore _
	 * 
	 * @param w
	 *            The Writer to write to
	 * @param c
	 *            The Channel that will be saved
	 * @throws IOException
	 */
	public static void saveChannel(Writer w, Channel c) throws IOException {
		w.write(c.getClass().getName() + "\n");
		for (int i = 0; i < c.m_LocalParameters.size(); i++) {
			AbstractParam param = c.m_LocalParameters.get(i);
			w.write(param.getName().replace(' ', '_') + " ");
			param.save(w);
		}
		w.write("endparameters\n");
	}

	public static Channel loadChannel(Scanner s) {
		try {
			AbstractParam.SILENT = true;
			Channel c = (Channel) Class.forName(s.next()).newInstance();
			// Logger.log(null, "loadChannel " + c);

			String t;
			while (!(t = s.next()).equals("endparameters")) {
				AbstractParam param;
				if ((param = c.getParamByName(t.replace('_', ' '))) != null) {
					param.load(s);
				} else {
					Logger.logWarning(null, " loading of param " + t + " failed.");
				}
			}
			AbstractParam.SILENT = false;
			c.parameterChanged(null);
			return c;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			AbstractParam.SILENT = false;
		}
		return null;
	}
}
