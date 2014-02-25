package com.mystictri.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

// Simple float image class that allows to save into Radiance .hdr format
public class RGBEImage {
	int resX;
	int resY;
	float[] data;
	
	public RGBEImage(int w, int h) {
		resX = w;
		resY = h;
		data = new float[resX*resY*3];
		
		// some test data
		for (int i = 0; i < resX*resY*3; i++) {
			data[i] = ((float)Math.sin((i/3 + i/100000.0f)*i/200000.0f) + 1.0f) * 3.0f;
		}
	}
	
	
	public void saveToFile(String filename) {
		try {
			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			
			WriteHeader(file, resX, resY);
			WritePixels(file, data, resX*resY);
			
			file.close();
			
			System.out.println("Saved " + filename);
		} catch (FileNotFoundException fnfe) {
			System.err.println("Could not save HDR Image to " + filename);
			System.err.println(fnfe);
		} catch (IOException ioe) {
			System.err.println("Could not save HDR Image to " + filename);
			System.err.println(ioe);
		}
		
	}

	private void WriteHeader(RandomAccessFile file, int width, int height) throws IOException {
		file.writeBytes("#?RGBE\n");
		file.writeBytes("FORMAT=32-bit_rle_rgbe\n\n");
		file.writeBytes("-Y " + height + " +X " + width + "\n");
	}
	
	private void WritePixels(RandomAccessFile file, float[] data, int numpixels) throws IOException {
		for (int i = 0; i < numpixels; i++) {
			int idx = i * 3;
			int rgbe = float2rgbe(data[idx + 0], data[idx + 1], data[idx + 2]);
			
			file.writeInt(rgbe);
		}
	}

	private int _frexp_exponent = 0;
	private double _frexp_mantissa = 0;

	// from http://stackoverflow.com/questions/1552738/is-there-a-java-equivalent-of-frexp
	private void _frexp(double value) {
		long bits = Double.doubleToLongBits(value);
		double realMant = 1.;

		// Test for NaN, infinity, and zero.
		if (Double.isNaN(value) || value + value == value || Double.isInfinite(value)) {
			_frexp_exponent = 0;
			_frexp_mantissa = value;
		} else {

			boolean neg = (bits < 0);
			int exponent = (int) ((bits >> 52) & 0x7ffL);
			long mantissa = bits & 0xfffffffffffffL;

			if (exponent == 0) {
				exponent++;
			} else {
				mantissa = mantissa | (1L << 52);
			}

			// bias the exponent - actually biased by 1023.
			// we are treating the mantissa as m.0 instead of 0.m
			// so subtract another 52.
			exponent -= 1075;
			realMant = mantissa;

			// normalize
			while (realMant >= 1.0) {
				mantissa >>= 1;
				realMant /= 2.;
				exponent++;
			}

			if (neg) {
				realMant = realMant * -1;
			}

			_frexp_exponent = exponent;
			_frexp_mantissa = realMant;
		}
	}

	private int float2rgbe(float red, float green, float blue) {
		float v;
		int e;
		int rgbe = 0;
		
		v = red;
		if (green > v)
			v = green;
		if (blue > v)
			v = blue;
		if (v < 1e-32) {
			return rgbe;
		} else {
			_frexp(v);
			v = (float)_frexp_mantissa * 256.0f / v;
			e = _frexp_exponent;
			rgbe |= ((int)(red * v)&0xFF) << 24;
			rgbe |= ((int)(green * v)&0xFF) << 16;
			rgbe |= ((int)(blue * v)&0xFF) << 8;
			rgbe |= ((int)(e + 128)&0xFF) << 0;
		}
		return rgbe;
	}
	
	
	
	public static void main(String[] args) {
		// create a test image for testing
		RGBEImage test = new RGBEImage(512, 512);
		test.saveToFile("test.hdr");
	}
}
