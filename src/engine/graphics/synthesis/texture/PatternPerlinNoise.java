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

import engine.base.Vector3;
import engine.base.Vector4;
import engine.parameters.AbstractParam;
import engine.parameters.BoolParam;
import engine.parameters.ColorGradientParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;
import engine.parameters.SpectralControlParam;

public final class PatternPerlinNoise extends Pattern {

	
	
	public String getName() {
		return "Perlin Noise";
	}

	public String getHelpText() {
		return "An implementation of Improved Perlin Noise. \n" +
				"See (http://mrl.nyu.edu/~perlin/noise/).\n" +
				"A seed of -1 uses the reference permutation.";
	}
	
	ColorGradientParam colorGradientParam = CreateLocalColorGradientParam("Color Mapping");
	
	final Noise3D_ImprovedPerlin noise = new Noise3D_ImprovedPerlin(1, -1);
	
	FloatParam scaleX = CreateLocalFloatParam("ScaleX", 1.0f, 1.0f, Float.MAX_VALUE);
	FloatParam scaleY = CreateLocalFloatParam("ScaleY", 1.0f, 1.0f, Float.MAX_VALUE);
	FloatParam valueScale = CreateLocalFloatParam("ValueScale", 1.0f, 0.0f, Float.MAX_VALUE).setDefaultIncrement(0.125f);
	FloatParam persistence =  CreateLocalFloatParam("Persistence", 0.5f, 0.0f, Float.MAX_VALUE).setDefaultIncrement(0.125f/2.0f);
	SpectralControlParam spectralControl = CreateLocalSpectralControlParam("Spectral Control");
	IntParam startBand = CreateLocalIntParam("StartBand", 0, 1, 16);
	IntParam endBand = CreateLocalIntParam("EndBand", 8, 1, 16);
	IntParam seed = CreateLocalIntParam("Seed", -1, -1, Integer.MAX_VALUE);
	BoolParam periodic = CreateLocalBoolParam("Periodic", true);
	
	
	public PatternPerlinNoise() {
		spectralControl.setStartEndBand(startBand.get(), endBand.get(), persistence.get());
	}

	public PatternPerlinNoise(float sx, float sy) {
		this();
		scaleX.set(sx);
		scaleY.set(sy);
		spectralControl.setStartEndBand(startBand.get(), endBand.get(), persistence.get());
	}
	
	public void parameterChanged(AbstractParam source) {
		if (source == null || source == seed) {
			noise.setSeed(seed.get());
		}
		
		if (source == null || source == persistence) {
			// when loading and old noise from a file this is not initialized so we do it here
			spectralControl.setSilent(true); // needed to avoid multiple recomputations of the noise
			if (spectralControl.getEndBand() != endBand.get() || source == persistence) {
				float mult = 1.0f;
				for (int i = startBand.get(); i <= endBand.get(); i++) {
					//System.out.println(i + " " + mult + "\n");
					spectralControl.set(i, mult);
					mult *= persistence.get();
				}
				spectralControl.setStartEndBand(startBand.get(), endBand.get(), persistence.get());
			}
			spectralControl.setSilent(false);
		}
		
		if (source == startBand || source == endBand) {
			spectralControl.setSilent(true); // needed to avoid multiple recomputations of the noise
			spectralControl.setStartEndBand(startBand.get(), endBand.get(), persistence.get());
			spectralControl.setSilent(false);
		}
		
		super.parameterChanged(source);
	}

	public Vector4 _valueRGBA(float u, float v) {
		float val = 0.0f;
		float mult = 1.0f;
		float freq = 1.0f;
		
		for (int i = 1; i < startBand.get(); i++) {
			freq *= 2.0f;
		}
		
		
		boolean isPeriodic = periodic.get();
		
		for (int i = startBand.get(); i <= endBand.get(); i++) {
			
			mult = spectralControl.get(i, 0.5f);
			//Torus sampling of noise for periodicity
			/*float x = (4 + 1*FMath.cos(v * 2.0f * FMath.PI)) * FMath.cos(u * 2.0f * FMath.PI);
			float y = (4 + 1*FMath.cos(v * 2.0f * FMath.PI)) * FMath.sin(u * 2.0f * FMath.PI);
			float z = (0 + 1*FMath.sin(v * 2.0f * FMath.PI));
			x *= 0.1f;
			y *= 0.1f;
			z *= 0.1f;
			val += noise.sample(new Vector3(x*freq, y*freq, z*freq))*mult;*/
			
			//val += noise.sample(new Vector3(u*freq*scaleX.get(), v*freq*scaleY.get(), 0.0f))*mult;
			
			// seems to be a better periodic force
			float valueAdd = 0.0f;
			if (isPeriodic) valueAdd = noise.sample3dPeriodic(new Vector3(u*freq*scaleX.get(), v*freq*scaleY.get(),0.0f), (int)(freq*scaleX.get()), (int)(freq*scaleY.get()), 256)*mult;
			else valueAdd = noise.sample(new Vector3(u*freq*scaleX.get(), v*freq*scaleY.get(), 0.0f))*mult;
			
			val += valueAdd;
			
			
			freq *= 2.0f;
			//mult *= persistence.get();
		}
		val = val*0.5f + 0.5f;
		val *= valueScale.get();
		if (val > 1.0f) val = 1.0f;
		if (val < 0.0f) val = 0.0f;
		
		return colorGradientParam.get().getColor(val);
	}
}
