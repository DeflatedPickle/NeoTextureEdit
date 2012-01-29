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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import engine.base.FMath;
import engine.base.Logger;
import engine.base.Vector4;
import engine.graphics.synthesis.texture.CacheTileManager.TileCacheEntry;
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
	HashMap<CacheTileManager.ResolutionTag, TileCacheEntry> cacheEntries = null;
	
	public TextParam exportName = CreateLocalTextParam("ExportName", "");
	Channel[] inputChannels;
	
	{
		exportName.setSilent(true); // the exportName should not notify the change listener (at least currently)
	}

	/**
	 * This is used for rendering and should be set in the constructor if it is
	 * not a normal channel (for example for slow bitmap operations)
	 */
	public enum ChannelVizType {
		NORMAL, SLOW
	};

	public ChannelVizType vizType = ChannelVizType.NORMAL;

	
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
	 * 
	 * This method overrides LocalParameterManager.parameterChanged
	 */
	@Override
	public void parameterChanged(AbstractParam source) {
		CacheTileManager.setEntrysDirty(this);
		
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

	public static float deltaFac = 4.0f;
	public static float deltaU = 1.0f / (deltaFac*256.0f);
	public static float deltaV = 1.0f / (deltaFac*256.0f);

	public Vector4 du1f(float u, float v) {
		return new Vector4(valueRGBA(u + deltaU, v)).sub_ip(valueRGBA(u, v)).mult_ip(deltaFac);
	}

	public Vector4 dv1f(float u, float v) {
		return new Vector4(valueRGBA(u, v + deltaV)).sub_ip(valueRGBA(u, v)).mult_ip(deltaFac);
	}
	
	
	// these three methods need to be overwritten in the subclasses

	public OutputType getOutputType() {
		return OutputType.SCALAR;
	}

	protected Vector4 _valueRGBA(float u, float v) {
		return new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
	}

	
	void cache_function(Vector4 out, TileCacheEntry[] in, int localX, int localY, float u, float v) {
		out.set(1, 0, 0, 1);
	}

	/*
	 * protected float _value1f(float u, float v) { return 0.0f; }
	 */

	// ------------------------------------------------------



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
			AbstractParam.GLOBAL_SILENT = true;
			
			String name = s.next();
			
			// This is an internal conversion to be still compatible with older file formats and name changes
			// !!TODO: as soon as the file format is final or a huge change happened this should be removed
			name = name.replace("FilterBrightnessContrast", "FilterColorCorrect");
			
			Channel c = (Channel) Class.forName(name).newInstance();
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
			AbstractParam.GLOBAL_SILENT = false;
			c.parameterChanged(null);
			return c;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			AbstractParam.GLOBAL_SILENT = false;
		}
		return null;
	}
}
