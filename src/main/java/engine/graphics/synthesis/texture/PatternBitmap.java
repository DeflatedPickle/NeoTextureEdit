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

import engine.base.Utils;
import engine.parameters.AbstractParam;
import engine.parameters.EnumParam;
import engine.parameters.ImageParam;
import engine.parameters.InfoParam;
import org.joml.Vector4f;

public final class PatternBitmap extends Pattern {
	ImageParam image = CreateLocalImageParam("Image", "");
	InfoParam resolutionInfo = CreateLocalInfoParam("Resolution", "Resolution: ");
	EnumParam interpolation = CreateLocalEnumParam("Interpolation", "Nearest,Linear").setEnumPos(1);
	
	
	{
		image.addParamChangeListener(this);
	}
	
	public String getName() {
		return "Bitmap";
	}
	
	public String getHelpText() {
		return "This is still an experimental node and many parameters/settings\n" +
				"are missing. The loaded image is left untouched and is sampled.";
	}
	
	Vector4f sampleColorNearest(BufferedImage img, float u, float v) {
		int x = ((int)(u * img.getWidth() + 0.5f)) % img.getWidth();
		int y = ((int)(v * img.getHeight() + 0.5f)) % img.getHeight();
		return Utils.RGBAToVector4(img.getRGB(x, y));
	}
	
	Vector4f sampleColorLinear(BufferedImage img, float u, float v) {
		int rx = img.getWidth();
		int ry = img.getHeight();
		int x = (int)(u * rx);
		int y = (int)(v * ry);
		float mx = (u * rx) - x;
		float my = (v * ry) - y;
		x = x%rx;
		y = y%ry;
		
		Vector4f c0 = Utils.RGBAToVector4(img.getRGB(x, y)); 
		Vector4f c1 = Utils.RGBAToVector4(img.getRGB((x+1)%rx, y)); 
		Vector4f c2 = Utils.RGBAToVector4(img.getRGB(x, (y+1)%ry)); 
		Vector4f c3 = Utils.RGBAToVector4(img.getRGB((x+1)%rx, (y+1)%ry));
		
		c0.lerp(c1, mx);
		c2.lerp(c3, mx);
		c0.lerp(c2, my);
		
		return c0;
	}
	
	
	
	protected Vector4f _valueRGBA(float u, float v) {
		BufferedImage img = image.getImage();
		if (img == null) {
			return new Vector4f(0, 0, 0, 0);
		} else {
			if (interpolation.getEnumPos() == 0) return sampleColorNearest(img, u, v);
			else return sampleColorLinear(img, u, v);
		}
	}

	
	public void parameterChanged(AbstractParam source) {
		if (source == null || source == image) {
			BufferedImage i = image.getImage();
			if (i != null) {
				resolutionInfo.set("Resolution: "+i.getWidth()+"x"+i.getHeight());
			} else {
				resolutionInfo.set("Resolution: ");
			}
		}
		super.parameterChanged(source);
	}
	
}
