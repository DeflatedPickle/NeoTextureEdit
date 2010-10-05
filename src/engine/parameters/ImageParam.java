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

package engine.parameters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.mystictri.neotextureedit.TextureEditor;

import engine.base.Logger;
import engine.base.Utils;

//!!TODO: does not work when the path contains spaces!!
public class ImageParam extends AbstractParam {
	BufferedImage image = null;
	String filename = "";

	private ImageParam(String name, String filename) {
		this.name = name;
		loadImage(filename);
		notifyParamChangeListener();
	}

	/**
	 * Saves only the relative path (relative to the path of the opened .tgr graph)
	 * to the image not the image itself. To store the path
	 * all spaces are replaced with colons (:) for easier string parsing.
	 */
	public void save(Writer w) throws IOException {
		String relativePath;
		// store only the relative path to the image (relative to the .tgr files);
		if (TextureEditor.INSTANCE.m_CurrentFile != null) relativePath = Utils.getRelativePath(TextureEditor.INSTANCE.m_CurrentFile.getParentFile(), new File(filename));
		else relativePath = filename;
		
		w.write(relativePath.replace(' ', ':') + " ");
	}

	/**
	 * Expects as next token the filename. Replaces all occurences of a colon
	 * in the filename with a space and then tries to load the image from disk.
	 */
	public void load(Scanner s) {
		String path = s.next().replace(':', ' ');
		if (TextureEditor.INSTANCE.m_CurrentFile != null) path = TextureEditor.INSTANCE.m_CurrentFile.getParent() + File.separator + path;
		System.out.println(path);
		loadImage(path);
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public String getImageFilename() {
		return filename;
	}
	
	private boolean _loadImage() {
		try {
			image = ImageIO.read(new File(filename));
			if (image == null) {
				System.err.println("WARNING: could not load " + filename);
				return false;
			}
			Logger.log(this, "Loaded " + filename);
		} catch (IOException e) {
			System.err.println(e);
			return false;
		}
		notifyParamChangeListener();
		return true;
	}

	public boolean loadImage(String filename) {
		if (filename.length() < 3) return false;
		if (filename.equals(this.filename)) return true;
		this.filename = filename;
		return _loadImage();
	}
	
	public boolean reloadeImage() {
		return _loadImage();
	}
	
	
	public static ImageParam create(String name, String filename) {
		ImageParam ret = new ImageParam(name, filename);
		return ret;
	}
	
	public static ImageParam createManaged(String name, String filename) {
		ImageParam ret = create(name, filename);
		ParameterManager.add(ret);
		return ret;
	}

}
