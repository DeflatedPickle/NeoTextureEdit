package engine.graphics.synthesis.texture;

import java.awt.image.BufferedImage;

import engine.base.FMath;
import engine.base.Utils;
import engine.base.Vector4;
import engine.base.datastructure.NdVector;
import engine.base.datastructure.PointKDTree;
import engine.parameters.AbstractParam;
import engine.parameters.ImageParam;
import engine.parameters.InfoParam;
import engine.parameters.IntParam;

/**
 * This is a first experimental test to integrate texture synthesis into
 * NeoTextureEdit. The main design problems with integration are that usually
 * this only works on a grid. For this a local buffer is used which is somewhat
 * inefficient as when caching is enabled an additional buffer is created per
 * Pattern.
 * 
 * Blah
 * 
 * @author Holger Dammertz
 * 
 */
public final class PatternSynthesis extends Pattern {
	ImageParam image = CreateLocalImageParam("InputImg", "");
	InfoParam resolutionInfo = CreateLocalInfoParam("InputRes", "Resolution: ");
	IntParam targetResolution = CreateLocalIntParam("TargetRes", 32, 1, Integer.MAX_VALUE);
	IntParam borderWidth = CreateLocalIntParam("Border", 2, 1, 5);
	IntParam seed = CreateLocalIntParam("Seed", 0, 0, Integer.MAX_VALUE);

	int _targetRes = 0;
	// FloatBuffer targetBuffer;

	int _srcRes = 0;
	int srcBuffer[];
	int tgtBuffer[];
	
	int border = borderWidth.get();

	public String getName() {
		return "Synthesis";
	}

	public String getHelpText() {
		return "EXPERIMENTAL: still in development\n " +
				"Currently it is SLOW and uses a huge amount of memory. \n" +
				"Please use only SMALL input patches.";
	}
	
	public PatternSynthesis() {
		vizType = ChannelVizType.SLOW;
	}

	final int sampleSrc(int x, int y) {
		x += _srcRes;
		y += _srcRes;
		x = x % _srcRes;
		y = y % _srcRes;

		return srcBuffer[x + y * _srcRes];
	}

	final int sampleTarget(int x, int y) {
		x += _targetRes;
		y += _targetRes;
		x = x % _targetRes;
		y = y % _targetRes;

		return tgtBuffer[x + y * _targetRes];
	}

	final int colorDist(int c0, int c1) {
		int r = ((c0 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF);
		int g = ((c0 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF);
		int b = ((c0 >> 0) & 0xFF) - ((c1 >> 0) & 0xFF);
		return r * r + g * g + b * b;
	}
	
	
	final int findBestMatch_KDTree(int x, int y) {
		NdVector p = new NdVector(kdtree.getDimension());

		int num = 0;
		for (int sj = -border; sj <= 0; sj++) {
			for (int si = -border; si <= border; si++) {
				if ((sj == 0) && (si == 0))
					break;
				int tcolor = sampleTarget(x + si, y + sj);
				Vector4 color = Utils.RGBAToVector4(tcolor);
				p.set(num++, color.x);
				p.set(num++, color.y);
				p.set(num++, color.z);
			}
		}

		FeaturePoint fp = kdtree.getNearest(p);
		return fp.color;
		
	}

	final int findBestMatch_BruteForce(int x, int y) {
		int bestX = 0;
		int bestY = 0;
		float bestdist = Float.MAX_VALUE;

		for (int sy = border; sy < _srcRes; sy++) {
			for (int sx = border; sx < _srcRes - border; sx++) {
				float dist = 0;

				int num = 0;
				for (int sj = -border; sj <= 0; sj++) {
					for (int si = -border; si <= border; si++) {
						if ((sj == 0) && (si == 0))
							break;

						int scolor = sampleSrc(sx + si, sy + sj);
						int tcolor = sampleTarget(x + si, y + sj);

						dist += colorDist(scolor, tcolor);
						num++;
					}
				}

				if (num > 0) {
					dist = dist / num;
					if (dist < bestdist) {
						bestdist = dist;
						bestX = sx;
						bestY = sy;
					}
				}
			}
		}

		return sampleSrc(bestX, bestY);
	}

	void initTargetBuffer() {
		BufferedImage src = image.getImage();
		if (src == null)
			return;

		//long time = System.currentTimeMillis();

		System.out.println("initTargetBuffer:");
		if (targetResolution.get() != _targetRes) {
			_targetRes = targetResolution.get();
			System.out.println("  Createing new Target Buffer with target res: " + _targetRes);
			tgtBuffer = new int[_targetRes * _targetRes];
		}

		System.out.println("  Filling with random samples");
		FMath.setSeed(seed.get());
		// now fill with random pixels
		for (int i = 0; i < tgtBuffer.length; i++) {
			int x = (int) (FMath.random() * _srcRes);
			int y = (int) (FMath.random() * _srcRes);
			tgtBuffer[i] = src.getRGB(x, y); // !!TOOPT
			// tgtBuffer[i] = FMath.randomInt();
		}

		System.out.print("  Searching for best match:");
		for (int y = 0; y < _targetRes; y++) {
			if (y % 8 == 0)
				System.out.print(".");

			for (int x = 0; x < _targetRes; x++) {
				int sx = x;// %_targetRes;
				int sy = y;// %_targetRes;
				
				tgtBuffer[sx + sy * _targetRes] = findBestMatch_KDTree(sx, sy);
				//tgtBuffer[sx + sy * _targetRes] = findBestMatch_BruteForce(sx, sy);
			}
		}
		System.out.println();

		//lastComputationTime = System.currentTimeMillis() - time;
		//System.out.println("Time: " + (lastComputationTime) / 1000.0f);

	}

	/**
	 * !!TODO: use original colors instead of floats here for memory savings!!
	 * @author Holger Dammertz
	 *
	 */
	static class FeaturePoint extends NdVector {
		public int color = -1;

		public FeaturePoint(int d) {
			super(d);
		}
	}
	

	PointKDTree<FeaturePoint> kdtree;

	void initSrcBuffer(BufferedImage i) {
		border = borderWidth.get();
		
		System.out.println("    Initializing src buffer");
		_srcRes = i.getWidth();
		srcBuffer = new int[_srcRes * _srcRes];
		i.getRGB(0, 0, _srcRes, _srcRes, srcBuffer, 0, _srcRes);

		int dimension = ((2 * border + 1) * border + border) * 3;
		System.out.println("  Dimension is " + dimension + " (Border = "+border+")");
		
		System.out.println("   Creating KD:");
		kdtree = new PointKDTree<FeaturePoint>(dimension);
		for (int sy = border; sy < _srcRes; sy++) {
			for (int sx = border; sx < _srcRes - border; sx++) {

				FeaturePoint p = new FeaturePoint(dimension);

				p.color = srcBuffer[sx + sy * _srcRes];

				int num = 0;
				for (int sj = -border; sj <= 0; sj++) {
					for (int si = -border; si <= border; si++) {
						if ((sj == 0) && (si == 0))
							break;
						int scolor = sampleSrc(sx + si, sy + sj);

						Vector4 color = Utils.RGBAToVector4(scolor);
						p.set(num++, color.x);
						p.set(num++, color.y);
						p.set(num++, color.z);
					}
				}
				kdtree.addWithoutDuplis(p, 0.001f);
			}
		}
		

		kdtree.build();

	}

	protected Vector4 _valueRGBA(float u, float v) {
		if (tgtBuffer == null) {
			return new Vector4(0, 0, 0, 0);
		} else {
			int x = (int) (u * _targetRes);
			int y = (int) (v * _targetRes);
			int idx = (x + y * _targetRes);
			return Utils.RGBAToVector4(tgtBuffer[idx]);
			// return new
			// Vector4(targetBuffer.get(idx+0),targetBuffer.get(idx+1),targetBuffer.get(idx+2),targetBuffer.get(idx+3));
		}
	}

	public void parameterChanged(AbstractParam source) {
		if (source == null || source == image || source == borderWidth) {
			BufferedImage i = image.getImage();
			if (i != null) {
				if (i.getWidth() != i.getHeight()) {
					System.err.println("ERROR: input image needs be square.");
					return;
				}
				resolutionInfo.set("Resolution: " + i.getWidth() + "x" + i.getHeight());
				initSrcBuffer(i);
				initTargetBuffer();
			} else {
				resolutionInfo.set("Resolution: ");
			}
		} else if (source == targetResolution || source == seed) {
			BufferedImage i = image.getImage();
			if (i != null) initTargetBuffer();
		}

		super.parameterChanged(source);
	}

}
