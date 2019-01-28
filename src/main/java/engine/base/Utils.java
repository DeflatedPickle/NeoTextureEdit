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

package engine.base;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * A Collection of utility functions that are used in different projects.
 * 
 * @author Holger Dammertz
 * 
 */
public final class Utils {

	public static FloatBuffer allocFloatBuffer(int size) {
		return ByteBuffer.allocateDirect(4 * size).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static IntBuffer allocIntBuffer(int size) {
		return ByteBuffer.allocateDirect(4 * size).order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	public static ByteBuffer allocByteBuffer(int size) {
		return ByteBuffer.allocateDirect(size);
	}

	private static long time;

	public static void startTimer() {
		time = System.currentTimeMillis();
	}

	// prints and resets timer;
	public static void printTimer(String blah) {
		System.out.println("TIMER: " + blah + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
	}

	public static void matrix4x4f_Mult(float[] result, float[] a, float[] b) {
		result[0] = a[0] * b[0] + a[1] * b[4] + a[2] * b[8] + a[3] * b[12];
		result[1] = a[0] * b[1] + a[1] * b[5] + a[2] * b[9] + a[3] * b[13];
		result[2] = a[0] * b[2] + a[1] * b[6] + a[2] * b[10] + a[3] * b[14];
		result[3] = a[0] * b[3] + a[1] * b[7] + a[2] * b[11] + a[3] * b[15];

		result[4] = a[4] * b[0] + a[5] * b[4] + a[6] * b[8] + a[7] * b[12];
		result[5] = a[4] * b[1] + a[5] * b[5] + a[6] * b[9] + a[7] * b[13];
		result[6] = a[4] * b[2] + a[5] * b[6] + a[6] * b[10] + a[7] * b[14];
		result[7] = a[4] * b[3] + a[5] * b[7] + a[6] * b[11] + a[7] * b[15];

		result[8] = a[8] * b[0] + a[9] * b[4] + a[10] * b[8] + a[11] * b[12];
		result[9] = a[8] * b[1] + a[9] * b[5] + a[10] * b[9] + a[11] * b[13];
		result[10] = a[8] * b[2] + a[9] * b[6] + a[10] * b[10] + a[11] * b[14];
		result[11] = a[8] * b[3] + a[9] * b[7] + a[10] * b[11] + a[11] * b[15];

		result[12] = a[12] * b[0] + a[13] * b[4] + a[14] * b[8] + a[15] * b[12];
		result[13] = a[12] * b[1] + a[13] * b[5] + a[14] * b[9] + a[15] * b[13];
		result[14] = a[12] * b[2] + a[13] * b[6] + a[14] * b[10] + a[15] * b[14];
		result[15] = a[12] * b[3] + a[13] * b[7] + a[14] * b[11] + a[15] * b[15];
	}

	public static float length3f(float[] v) {
		return FMath.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	}

	public static final int floatRGBToINTColor(float fr, float fg, float fb) {
		int r = ((int) (fr * 255.0f)) & 0xFF;
		int g = ((int) (fg * 255.0f)) & 0xFF;
		int b = ((int) (fb * 255.0f)) & 0xFF;
		return (0xFF000000 | (r << 16) | (g << 8) | (b << 0));
	}

	public static final int floatRGBAToINTColor(float fr, float fg, float fb, float fa) {
		int r = ((int) (fr * 255.0f)) & 0xFF;
		int g = ((int) (fg * 255.0f)) & 0xFF;
		int b = ((int) (fb * 255.0f)) & 0xFF;
		int a = ((int) (fa * 255.0f)) & 0xFF;
		return ((a << 24) | (r << 16) | (g << 8) | (b << 0));
	}

	public static final int vector3ToINTColor(Vector3f c) {
		int r = ((int) (c.x * 255.0f)) & 0xFF;
		int g = ((int) (c.y * 255.0f)) & 0xFF;
		int b = ((int) (c.z * 255.0f)) & 0xFF;
		return (0xFF000000 | (r << 16) | (g << 8) | (b << 0));
	}

	public static final int vector4ToINTColor_ARGB(Vector4f c) {
		int r = ((int) (c.x * 255.0f)) & 0xFF;
		int g = ((int) (c.y * 255.0f)) & 0xFF;
		int b = ((int) (c.z * 255.0f)) & 0xFF;
		int a = ((int) (c.w * 255.0f)) & 0xFF;
		return ((a << 24) | (r << 16) | (g << 8) | (b << 0));
	}

	public static final int vector4ToINTColor_ABGR(Vector4f c) {
		int r = ((int) (c.x * 255.0f)) & 0xFF;
		int g = ((int) (c.y * 255.0f)) & 0xFF;
		int b = ((int) (c.z * 255.0f)) & 0xFF;
		int a = ((int) (c.w * 255.0f)) & 0xFF;
		return ((a << 24) | (b << 16) | (g << 8) | (r << 0));
	}

	public static Vector4f RGBAToVector4(int c) {
		float a = ((c >> 24) & 0xFF) / 255.0f;
		float r = ((c >> 16) & 0xFF) / 255.0f;
		float g = ((c >> 8) & 0xFF) / 255.0f;
		float b = ((c >> 0) & 0xFF) / 255.0f;
		return new Vector4f(r, g, b, a);
	}

	/*
	 * public static Vector3 rgbToHSV_ip(Vector3 rgb) { float rgb_max =
	 * rgb.getMax(); float rgb_min = rgb.getMin();
	 * 
	 * float v = rgb_max; if (v == 0) return rgb; float inv_v = 1.0f / rgb_max;
	 * rgb_max *= inv_v; rgb_min *= inv_v; float s = rgb_max - rgb_min; if (s ==
	 * 0) return rgb.set(0, 0, v);
	 * 
	 * rgb.mult_ip(inv_v); float inv_s = 1.0f / s;
	 * 
	 * rgb.sub_ip(rgb_min).mult_ip(inv_s); rgb_max *= inv_s; rgb_min *= inv_s;
	 * 
	 * float h = 0.0f; if (rgb_max == rgb.x) { h = 0.0f + 60.0f * (rgb.y -
	 * rgb.z); if (h < 0.0) { h += 360.0; } } else if (rgb_max == rgb.y) { h =
	 * 120.0f + 60.0f * (rgb.z - rgb.x); } else { h = 240.0f + 60.0f * (rgb.x -
	 * rgb.y); }
	 * 
	 * return rgb.set(h, s, v); }
	 */

	//	private static final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_);
	//	public static Vector3 rgbToHSV_ip(Vector3 rgb) {
	//		//float [] temp = Color.RGBtoHSB(rgb.x, rgb.y, rgb.z, new float[3]);
	//		//rgb.set(temp[0], temp[1], temp[2]);
	//		colorSpace.
	//	}

	public static Vector3f rgbToHSV_ip(Vector3f rgb) {
		Vector3f temp = new Vector3f(rgb);
		int maxi = temp.maxComponent();
		int mini = temp.minComponent();

		rgb.z = temp.get(maxi);
		if (temp.get(maxi) == 0) {
			rgb.x = rgb.y = 0;
		} else if (temp.get(maxi) == temp.get(mini)) {
			rgb.x = rgb.y = 0;
		} else {
			rgb.y = 255.0f * (1.0f - (temp.get(mini) / temp.get(maxi)));
			rgb.x = 60.0f * ((maxi * 2) + (temp.get((maxi + 1) % 3) - temp.get((maxi + 2) % 3)) / (temp.get(maxi) - temp.get(mini)));
		}
		if (rgb.x < 0)
			rgb.x += 360.0f;

		return rgb;
	}

	public static Vector3f hsvToRGB_ip(Vector3f hsv) {
		int maxc;

		if (hsv.x > 300)
			hsv.x -= 360.0f;
		if (hsv.x < 60)
			maxc = 0;
		else if (hsv.x >= 180)
			maxc = 2;
		else
			maxc = 1;

		Vector3f temp = new Vector3f();

		temp.setComponent(maxc, hsv.z);

		float min = (hsv.z * (255.0f - hsv.y)) / 255.0f;
		float temphue = (((hsv.x / 60.0f) - (2 * maxc)) * (hsv.z - min));

		if (temphue > 0) {
			temp.setComponent((maxc + 2) % 3, min);
			temp.setComponent((maxc + 1) % 3, min + temphue);
		} else {
			temp.setComponent((maxc + 1) % 3, min);
			temp.setComponent((maxc + 2) % 3, min - temphue);
		}

		return hsv.set(temp);
	}

	public static final void saveINTImage(String filename, int[] pixels, int resX, int resY) {
		BufferedImage image = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, resX, resY, pixels, 0, resX);
		try {
			ImageIO.write(image, "png", new File(filename));
			System.out.println("Saved screenshot to " + filename + ".");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Vector3f createSphereSample_Uniform(float u, float v) {
		Vector3f dir = new Vector3f();
		u = 2.0f * FMath.PI * u;
		dir.x = 2.0f * FMath.cos(u) * FMath.sqrt(v * (1.0f - v));
		dir.y = 2.0f * FMath.sin(u) * FMath.sqrt(v * (1.0f - v));
		dir.z = (1.0f - 2.0f * v);
		return dir;
	}

	public static Vector3f createHemisphereSample_Uniform(float u, float v) {
		Vector3f dir = new Vector3f();
		float r1 = v * 2.0f * FMath.PI;
		float r2 = 1.0f - u;
		float sp = FMath.sqrt(1.0f - r2 * r2);
		dir.x = FMath.cos(r1) * sp;
		dir.y = FMath.sin(r1) * sp;
		dir.z = r2;
		return dir;
	}

	public static Vector3f createHemisphereSample_Cosinus(float u, float v) {
		Vector3f dir = new Vector3f();
		float r1 = v * 2.0f * FMath.PI;
		float r2 = FMath.sqrt(1.0f - u);
		float sp = FMath.sqrt(1.0f - r2 * r2);
		dir.x = FMath.cos(r1) * sp;
		dir.y = FMath.sin(r1) * sp;
		dir.z = r2;
		return dir;
	}

	/*
	 * public static Vector3 createHemisphereSample_CosinusPowK(float u, float
	 * v) { Vector3 dir = new Vector3(); //TODO return dir; }
	 */

	// solve eigenvectors ;)
	public static class EigenVectorValues2D {
		public float lambda1, lambda2;
		public float v1x, v1y, v2x, v2y;
	}

	EigenVectorValues2D solve_EigenVectorValues_2D(float A, float B, float C, float D) {
		EigenVectorValues2D ev = new EigenVectorValues2D();
		if (B * C <= 0.1e-20) {
			ev.lambda1 = A;
			ev.v1x = 1;
			ev.v1y = 0;
			ev.lambda2 = D;
			ev.v2x = 0;
			ev.v2y = 1;
			return ev;
		}

		float tr = A + D;
		float det = A * D - B * C;
		float S = FMath.sqrt((tr / 2) * (tr / 2) - det);
		ev.lambda1 = tr / 2 + S;
		ev.lambda2 = tr / 2 - S;

		float SS = FMath.sqrt(Math.max((A - D) / 2.0f * (A - D) / 2.0f + B * C, 0.0f));
		if (A - D < 0) {
			ev.v1x = C;
			ev.v1y = -(A - D) / 2 + SS;
			ev.v2x = +(A - D) / 2 - SS;
			ev.v2y = B;
		} else {
			ev.v2x = C;
			ev.v2y = -(A - D) / 2 - SS;
			ev.v1x = +(A - D) / 2 + SS;
			ev.v1y = B;
		}

		float n1 = FMath.sqrt((ev.v1x) * (ev.v1x) + (ev.v1y) * (ev.v1y));
		ev.v1x /= n1;
		ev.v1y /= n1;
		float n2 = FMath.sqrt((ev.v2x) * (ev.v2x) + (ev.v2y) * (ev.v2y));
		ev.v2x /= n2;
		ev.v2y /= n2;
		if (ev.lambda1 < 0) {
			ev.lambda1 = -ev.lambda1;
			ev.v1x = -ev.v1x;
			ev.v1y = -ev.v1y;
		}
		if (ev.lambda2 < 0) {
			ev.lambda2 = -ev.lambda2;
			ev.v2x = -ev.v2x;
			ev.v2y = -ev.v2y;
		}
		return ev;
	}

	static byte intToUnsignedByte(int i) {
		return (byte) ((i <= 0x7F) ? i : i - 0x100);
	}

	static void float2rgbe(ByteBuffer buf, float red, float green, float blue) {
		float v;
		int e;

		v = red;
		if (green > v)
			v = green;
		if (blue > v)
			v = blue;
		if (v < 1e-32) {
			buf.put(0, (byte) 0);
			buf.put(1, (byte) 0);
			buf.put(2, (byte) 0);
			buf.put(3, (byte) 0);
		} else {
			e = FMath.getExp(v);
			v = FMath.getManissa(v) * 256.0f / v;
			buf.put(0, intToUnsignedByte((int) (red * v)));
			buf.put(1, intToUnsignedByte((int) (green * v)));
			buf.put(2, intToUnsignedByte((int) (blue * v)));
			buf.put(3, intToUnsignedByte((int) (e + 128)));
		}
	}

	// HDR Export
	static void saveHDRImage(String filename, float[] image, int width, int height) {
		try {
			ByteBuffer buf = ByteBuffer.allocate(4);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			out.write("#?RGBE\n".getBytes());
			out.write("FORMAT=32-bit_rgbe\n\n".getBytes());
			out.write(String.format("-Y %d +X %d\n", height, width).getBytes());

			for (int y = height - 1; y >= 0; y--) {
				for (int i = 0; i < width; i++) {
					float2rgbe(buf, image[y * width * 3 + i * 3 + 0], image[y * width * 3 + i * 3 + 1], image[y * width * 3 + i * 3 + 2]);
					out.write(buf.array());
				}
			}

			System.out.println("Saved " + filename);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static List<String> getPathList(File f) {
		List<String> l = new ArrayList<String>();
		File r;
		try {
			r = f.getCanonicalFile();
			while (r != null) {
				l.add(r.getName());
				r = r.getParentFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			l = null;
		}
		return l;
	}

	/**
	 * figure out a string representing the relative path of 'f' with respect to
	 * 'r'
	 * 
	 * @param r
	 *            home path
	 * @param f
	 *            path of file
	 */
	private static String matchPathLists(List<String> r, List<String> f) {
		int i;
		int j;
		String s;
		// start at the beginning of the lists
		// iterate while both lists are equal
		s = "";
		i = r.size() - 1;
		j = f.size() - 1;

		// first eliminate common root
		while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for (; i >= 0; i--) {
			s += ".." + File.separator;
		}

		// for each level in the file path, add the path
		for (; j >= 1; j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		s += f.get(j);

		return s;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory example :
	 * home = /a/b/c f = /a/d/e/x.txt s = getRelativePath(home,f) =
	 * ../../d/e/x.txt
	 * 
	 * @param home
	 *            base path, should be a directory, not a file, or it doesn't
	 *            make sense
	 * @param f
	 *            file to generate path for
	 * @return path from home to f as a string
	 */
	public static String getRelativePath(File home, File f) {
		List<String> homelist;
		List<String> filelist;
		String s;

		homelist = getPathList(home);
		filelist = getPathList(f);
		s = matchPathLists(homelist, filelist);
		return s;
	}

	public static void main(String[] args) {
		Vector3f col = new Vector3f(0.4392157f, 0.6745098f, 0.71372549f);
		col = new Vector3f(135 / 255.0f, 33 / 255.0f, 38 / 255.0f);
		System.out.println(col);
		System.out.println(rgbToHSV_ip(col));
		System.out.println(hsvToRGB_ip(col));
	}

}
