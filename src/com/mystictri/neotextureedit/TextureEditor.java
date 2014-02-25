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

package com.mystictri.neotextureedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;

import apple.dts.samplecode.osxadapter.OSXAdapter;

import com.mystictri.neotexture.TextureGenerator;
import com.mystictri.neotexture.TextureGraphNode;
import com.mystictri.neotexture.TextureVersion;

import engine.base.Logger;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.Pattern;
import engine.graphics.synthesis.texture.PatternChecker;
import engine.graphics.synthesis.texture.ProgressBarInterface;

/**
 * This is the main window of NeoTextureEdit. It is more or less a singelton
 * class that manages all components in the editor.
 * 
 * @author Holger Dammertz
 * 
 */
public class TextureEditor implements ActionListener, KeyListener {
	private static final long serialVersionUID = -5567955539436014517L;
	public static boolean GL_ENABLED = false; // on init this is set to true if
												// we can successfully
												// initialize GL
	public static TextureEditor INSTANCE = null;

	// Check that we are on Mac OS X. This is crucial to loading and using the
	// OSXAdapter class.
	public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

	Preferences preferences = Preferences.userRoot().node("com.mystictri.NeoTextureEdit");

	static final String programVersionNumber = TextureGenerator.getVersion();

	// String m_CurrentFileName = null;
	public File m_CurrentFile;
	String m_CurrentPath = null;
	public Channel dragndropChannel = null;

	// The default Presets that are loaded when no preset file is found
	// final String defaultNTEPresets =
	// "18\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 5 1.0 1.0 1.0 1.0 0.0 0.83137256 0.827451 0.827451 1.0 0.37198067 0.0 0.0 0.0 1.0 0.5458937 0.78039217 0.7764706 0.7764706 1.0 0.65700483 1.0 0.99607843 0.99607843 1.0 1.0 ScaleX 3.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.5 StartBand 1 EndBand 3 Seed -1 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 8 0.7882353 0.49803922 0.023529412 1.0 0.0 0.91764706 0.8784314 0.05490196 1.0 0.10628019 0.89 0.329199 0.177758 1.0 0.15458937 0.5562499 0.20574935 0.11109874 1.0 0.21256039 0.0 0.0 0.0 1.0 0.28985506 0.14565827 0.13333333 0.11988795 1.0 0.4589372 0.50980395 0.46666667 0.41960785 1.0 0.68599033 0.8156863 0.8117647 0.8117647 1.0 1.0 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.875 StartBand 2 EndBand 8 Seed -1 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternCellular\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 9 0.198883 0.3 0.135 1.0 0.0 0.406805 0.613636 0.276136 1.0 0.10337928 0.324169 0.454545 0.114714 1.0 0.29771367 0.098346 0.136364 0.085909 1.0 0.44657052 0.196692 0.272727 0.171818 1.0 0.5964217 0.272727 0.21499 0.203431 1.0 0.68787223 0.430776 0.666667 0.326636 1.0 0.78330016 0.36939 0.42 0.3444 1.0 0.90258443 0.091517 0.26 0.0 1.0 0.99999994 Cell_Type 4 Distance_ 0 PointGen 0 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 16 RandColor false Periodic true endparameters\nengine.graphics.synthesis.texture.PatternCellular\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 0.8 0.8 0.0 1.0 1.0 Cell_Type 0 Distance_ 0 PointGen 0 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 26 RandColor false Periodic true endparameters\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 0.5764706 0.5411765 0.5019608 1.0 0.004830897 0.36862746 0.3529412 0.32156864 1.0 0.20772946 0.18998075 0.16630016 0.11761947 1.0 0.37681162 0.006127451 0.006127451 0.006127451 1.0 0.64734304 0.42352942 0.39607844 0.3529412 1.0 0.82608694 0.34901962 0.29803923 0.2627451 1.0 1.0 ScaleX 4.0 ScaleY 5.0 ValueScale 1.0 Persistence 0.75 StartBand 1 EndBand 8 Seed 22 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 12 1.0 1.0 1.0 1.0 0.09178744 0.0 0.4 0.4 1.0 0.13043478 0.0 0.4 0.4 1.0 0.17874396 1.0 1.0 1.0 1.0 0.22705314 1.0 1.0 1.0 1.0 0.44444445 0.0 0.4 0.4 1.0 0.49758455 0.0 0.4 0.4 1.0 0.5410628 1.0 1.0 1.0 1.0 0.589372 1.0 1.0 1.0 1.0 0.7487923 0.0 0.4 0.4 1.0 0.8115942 0.0 0.4 0.4 1.0 0.8647343 1.0 1.0 1.0 1.0 0.9178744 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.6875 StartBand 1 EndBand 10 Seed 17 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 9 1.0 1.0 1.0 1.0 0.09178744 0.6 0.6 0.0 1.0 0.14975846 1.0 1.0 1.0 1.0 0.22705314 1.0 1.0 1.0 1.0 0.44444445 0.6 0.6 0.0 1.0 0.5169082 1.0 1.0 1.0 1.0 0.589372 1.0 1.0 1.0 1.0 0.7487923 0.6 0.6 0.0 1.0 0.8357488 1.0 1.0 1.0 1.0 0.9178744 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.6875 StartBand 1 EndBand 4 Seed 15 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternCellular\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.5636701 0.89 0.329199 0.177758 1.0 0.7153562 1.0 1.0 0.0 1.0 0.8352055 0.91 0.574437 0.0 1.0 0.8820228 0.727273 0.127938 0.14837 1.0 0.99999994 Cell_Type 4 Distance_ 0 PointGen 2 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 23 RandColor false Periodic true endparameters\nengine.graphics.synthesis.texture.PatternBrick\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 4 0.0 0.0 0.0 1.0 0.0 0.7254902 0.36078432 0.36078432 1.0 0.08695652 0.44313726 0.21176471 0.21176471 1.0 0.531401 1.0 0.4 0.4 1.0 1.0 NumX 4 NumY 6 Shift 0.5 RandShift 0.1 GapX 0.03 GapY 0.03 Smooth 0.05 RandColor true endparameters\nengine.graphics.synthesis.texture.PatternBrick\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 0.003921569 0.003921569 0.003921569 1.0 0.0 0.003921569 0.003921569 0.003921569 1.0 0.08695652 0.654902 0.4862745 0.23529412 1.0 0.16425121 0.34 0.128091 0.107291 1.0 0.44927537 0.74509805 0.39215687 0.16862746 1.0 0.7198068 0.204545 0.121376 0.046011 1.0 1.0 NumX 14 NumY 1 Shift 0.5 RandShift 0.1 GapX 0.005000001 GapY 0.0 Smooth 0.009375 RandColor true endparameters\nengine.graphics.synthesis.texture.PatternFunction\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.5636701 0.89 0.329199 0.177758 1.0 0.7153562 1.0 1.0 0.0 1.0 0.8352055 0.91 0.574437 0.0 1.0 0.8820228 0.727273 0.127938 0.14837 1.0 0.99999994 FunctionU 0 FunctionV 0 Combiner 0 ScaleX 13.0 ScaleY 3.0 endparameters\nengine.graphics.synthesis.texture.PatternFunction\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 4 0.0 0.0 0.0 1.0 0.0 0.2 0.2 0.2 1.0 0.62801933 0.0 0.8 0.8 1.0 0.9130435 1.0 1.0 1.0 1.0 1.0 FunctionU 0 FunctionV 0 Combiner 2 ScaleX 4.0 ScaleY 4.0 endparameters\nengine.graphics.synthesis.texture.PatternPerlinNoise\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 1.0 1.0 0.2657005 1.0 1.0 1.0 1.0 0.68599033 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.75 StartBand 1 EndBand 10 Seed 2 Periodic true endparameters\nengine.graphics.synthesis.texture.PatternTile\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 BorderX 0.1 BorderY 0.1 Smooth 0.025 endparameters\nengine.graphics.synthesis.texture.PatternCellular\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 1.0 1.0 0.0 1.0 0.096618354 0.0 0.0 0.0 1.0 0.28502417 Cell_Type 0 Distance_ 0 PointGen 1 Seed 0 Intensity 1.0 Jitter 0.625 NumPoints 64 RandColor false Periodic true endparameters\nengine.graphics.synthesis.texture.PatternCellular\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.2173913 1.0 1.0 1.0 1.0 0.41062802 Cell_Type 4 Distance_ 0 PointGen 2 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 32 RandColor false Periodic true endparameters\nengine.graphics.synthesis.texture.PatternChecker\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 ScaleX 1.0 ScaleY 1.0 endparameters\nengine.graphics.synthesis.texture.PatternConstantColor\n Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color 0.8 0.4 0.7 endparameters\n";
	String NTEPresetString;
	final String defaultNTEPresets = "18 "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 5 1.0 1.0 1.0 1.0 0.0 0.83137256 0.827451 0.827451 1.0 0.37198067 0.0 0.0 0.0 1.0 0.5458937 0.78039217 0.7764706 0.7764706 1.0 0.65700483 1.0 0.99607843 0.99607843 1.0 1.0 ScaleX 3.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.5 Spectral_Control 4 0.5 1.0 0.5 0.25 StartBand 1 EndBand 3 Seed -1 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 8 0.7882353 0.49803922 0.023529412 1.0 0.0 0.91764706 0.8784314 0.05490196 1.0 0.10628019 0.89 0.329199 0.177758 1.0 0.15458937 0.5562499 0.20574935 0.11109874 1.0 0.21256039 0.0 0.0 0.0 1.0 0.28985506 0.14565827 0.13333333 0.11988795 1.0 0.4589372 0.50980395 0.46666667 0.41960785 1.0 0.68599033 0.8156863 0.8117647 0.8117647 1.0 1.0 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.875 Spectral_Control 9 0.5 0.5 1.0 0.875 0.765625 0.6699219 0.58618164 0.51290894 0.44879532 StartBand 2 EndBand 8 Seed -1 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternCellular "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 9 0.198883 0.3 0.135 1.0 0.0 0.406805 0.613636 0.276136 1.0 0.10337928 0.324169 0.454545 0.114714 1.0 0.29771367 0.098346 0.136364 0.085909 1.0 0.44657052 0.196692 0.272727 0.171818 1.0 0.5964217 0.272727 0.21499 0.203431 1.0 0.68787223 0.430776 0.666667 0.326636 1.0 0.78330016 0.36939 0.42 0.3444 1.0 0.90258443 0.091517 0.26 0.0 1.0 0.99999994 Cell_Type 4 Distance_ 0 PointGen 0 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 16 RandColor false Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternCellular "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 0.8 0.8 0.0 1.0 1.0 Cell_Type 0 Distance_ 0 PointGen 0 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 26 RandColor false Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 0.5764706 0.5411765 0.5019608 1.0 0.004830897 0.36862746 0.3529412 0.32156864 1.0 0.20772946 0.18998075 0.16630016 0.11761947 1.0 0.37681162 0.006127451 0.006127451 0.006127451 1.0 0.64734304 0.42352942 0.39607844 0.3529412 1.0 0.82608694 0.34901962 0.29803923 0.2627451 1.0 1.0 ScaleX 4.0 ScaleY 5.0 ValueScale 1.0 Persistence 0.75 Spectral_Control 9 0.5 1.0 0.75 0.5625 0.421875 0.31640625 0.23730469 0.17797852 0.13348389 StartBand 1 EndBand 8 Seed 22 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 12 1.0 1.0 1.0 1.0 0.09178744 0.0 0.4 0.4 1.0 0.13043478 0.0 0.4 0.4 1.0 0.17874396 1.0 1.0 1.0 1.0 0.22705314 1.0 1.0 1.0 1.0 0.44444445 0.0 0.4 0.4 1.0 0.49758455 0.0 0.4 0.4 1.0 0.5410628 1.0 1.0 1.0 1.0 0.589372 1.0 1.0 1.0 1.0 0.7487923 0.0 0.4 0.4 1.0 0.8115942 0.0 0.4 0.4 1.0 0.8647343 1.0 1.0 1.0 1.0 0.9178744 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.6875 Spectral_Control 11 0.5 1.0 0.6875 0.47265625 0.32495117 0.22340393 0.1535902 0.105593264 0.07259537 0.04990932 0.034312658 StartBand 1 EndBand 10 Seed 17 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 9 1.0 1.0 1.0 1.0 0.09178744 0.6 0.6 0.0 1.0 0.14975846 1.0 1.0 1.0 1.0 0.22705314 1.0 1.0 1.0 1.0 0.44444445 0.6 0.6 0.0 1.0 0.5169082 1.0 1.0 1.0 1.0 0.589372 1.0 1.0 1.0 1.0 0.7487923 0.6 0.6 0.0 1.0 0.8357488 1.0 1.0 1.0 1.0 0.9178744 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.6875 Spectral_Control 5 0.5 1.0 0.6875 0.47265625 0.32495117 StartBand 1 EndBand 4 Seed 15 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternCellular "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.5636701 0.89 0.329199 0.177758 1.0 0.7153562 1.0 1.0 0.0 1.0 0.8352055 0.91 0.574437 0.0 1.0 0.8820228 0.727273 0.127938 0.14837 1.0 0.99999994 Cell_Type 4 Distance_ 0 PointGen 2 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 23 RandColor false Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternBrick "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 4 0.0 0.0 0.0 1.0 0.0 0.7254902 0.36078432 0.36078432 1.0 0.08695652 0.44313726 0.21176471 0.21176471 1.0 0.531401 1.0 0.4 0.4 1.0 1.0 NumX 4 NumY 6 Shift 0.5 RandShift 0.1 GapX 0.03 GapY 0.03 Smooth 0.05 RandColor true endparameters "
			+ "engine.graphics.synthesis.texture.PatternBrick "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 0.003921569 0.003921569 0.003921569 1.0 0.0 0.003921569 0.003921569 0.003921569 1.0 0.08695652 0.654902 0.4862745 0.23529412 1.0 0.16425121 0.34 0.128091 0.107291 1.0 0.44927537 0.74509805 0.39215687 0.16862746 1.0 0.7198068 0.204545 0.121376 0.046011 1.0 1.0 NumX 14 NumY 1 Shift 0.5 RandShift 0.1 GapX 0.005000001 GapY 0.0 Smooth 0.009375 RandColor true endparameters "
			+ "engine.graphics.synthesis.texture.PatternFunction "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 6 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.5636701 0.89 0.329199 0.177758 1.0 0.7153562 1.0 1.0 0.0 1.0 0.8352055 0.91 0.574437 0.0 1.0 0.8820228 0.727273 0.127938 0.14837 1.0 0.99999994 FunctionU 0 FunctionV 0 Combiner 0 ScaleX 13.0 ScaleY 3.0 endparameters "
			+ "engine.graphics.synthesis.texture.PatternFunction "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 4 0.0 0.0 0.0 1.0 0.0 0.2 0.2 0.2 1.0 0.62801933 0.0 0.8 0.8 1.0 0.9130435 1.0 1.0 1.0 1.0 1.0 FunctionU 0 FunctionV 0 Combiner 2 ScaleX 4.0 ScaleY 4.0 endparameters "
			+ "engine.graphics.synthesis.texture.PatternPerlinNoise "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 1.0 1.0 0.2657005 1.0 1.0 1.0 1.0 0.68599033 ScaleX 2.0 ScaleY 2.0 ValueScale 1.0 Persistence 0.75 Spectral_Control 11 0.5 1.0 0.75 0.5625 0.421875 0.31640625 0.23730469 0.17797852 0.13348389 0.100112915 0.07508469 StartBand 1 EndBand 10 Seed 2 Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternTile "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 BorderX 0.1 BorderY 0.1 Smooth 0.025 endparameters "
			+ "engine.graphics.synthesis.texture.PatternCellular "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 1.0 1.0 0.0 1.0 0.096618354 0.0 0.0 0.0 1.0 0.28502417 Cell_Type 0 Distance_ 0 PointGen 1 Seed 0 Intensity 1.0 Jitter 0.625 NumPoints 64 RandColor false Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternCellular "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.2173913 1.0 1.0 1.0 1.0 0.41062802 Cell_Type 4 Distance_ 0 PointGen 2 Seed 0 Intensity 1.0 Jitter 0.0 NumPoints 32 RandColor false Periodic true endparameters "
			+ "engine.graphics.synthesis.texture.PatternChecker "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color_Mapping 2 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 ScaleX 1.0 ScaleY 1.0 endparameters "
			+ "engine.graphics.synthesis.texture.PatternConstantColor "
			+ "ExportName \"\" Transformation 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 Color 0.8 0.4 0.7 endparameters ";

	Vector<Class<?>> allPatterns = new Vector<Class<?>>();
	Vector<Class<?>> allChannels = new Vector<Class<?>>();

	ColorChooserDialog m_ColorChooser;
	JFileChooser m_TextureFileChooser_SaveLoadGraph;
	JFileChooser m_TextureFileChooser_SaveLoadImage;
	ProgressDialog m_ProgressDialog;

	String title = "NeoTextureEdit - Version " + programVersionNumber;

	String help_message = "NeoTextureEdit " + programVersionNumber + " Help Overview\n\n"
			+ "Right Click on empty space: create texture channels and patterns\n" + "Middle Click: Drag the Graph Canvas\n"
			+ "Left Click on channel/pattern: select and drag\n" + "Richt Click on channel/pattern: additional options\n" + "\n"
			+ "Parameter editor (right side for selected channel/pattern)\n"
			+ "         Shift-Click on increment/decrement buttons uses smaller value.\n" + "\n" + "Gradient Editor:\n"
			+ "         Double Click inserts or edits a color node\n" + "         Shift Click and drag on color node edits alpha value\n";

	String about_message = "NeoTextureEdit\n\n" + "Version: " + programVersionNumber + "\n"
			+ "(c) Copyright Holger Dammertz 2010, 2011, 2012. All rights reserved.\n"
			+ "Visit http://sourceforge.net/projects/neotextureedit/\n\n"
			+ "NeoTextureEdit and the runtime generation library NeoTexture are\n"
			+ "licensed under the GNU LGPL v.3. See the files COPYING and\n" + "COPYING.LESSER for details.\n\n"
			+ "This program is distributed in the hope that it will be useful,\n"
			+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n";

	// + "Send comments, suggestions, bugs to holger.dammertz@googlemail.com";

	private void tempTest_FindAllPatternsAndChannelClasses() {
		String packName = Channel.class.getPackage().getName();
		String[] files = null;
		/*
		 * String dir = "/" + packName.replace('.', '/'); try { URL url =
		 * Channel.class.getResource(dir); if (url != null) { File f = new
		 * File(url.toURI()); files = f.list(); } else
		 */{
			// System.err.println("WARNING: automatic loading of patterns/channels failed; adding hardcoded set");
			String[] f = { "FilterBlend.class", "FilterColorCorrect.class", "FilterColorize.class", "FilterEmboss.class", "FilterMask.class",
					"FilterNormalMap.class", "FilterWarp.class", "Pattern.class", "PatternBrick.class", "PatternCellular.class",
					"PatternChecker.class", "PatternConstantColor.class", "PatternGradient.class", "PatternPerlinNoise.class",
					"PatternTile.class", "PatternFunction.class", "PatternBitmap.class", "FilterIlluminate.class", "FilterCombine.class",
					"FilterTransform.class", "FilterBlur.class", "FilterModulus.class", "FilterMath1.class" };
			files = f;
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i].endsWith(".class")) {
				String className = files[i].substring(0, files[i].length() - 6);
				try {
					Class<?> c = Class.forName(packName + "." + className);
					Object o = c.newInstance();
					if (o instanceof Pattern) {
						if (c != Pattern.class)
							allPatterns.add(c);
					} else if (o instanceof Channel)
						allChannels.add(c);

					// output of loaded classes for easy copy-paste for jar
					// version
					// if (url != null)
					// System.out.print("\"" + files[i] + "\",");

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					// e.printStackTrace(); // happens because of possible
					// abstract classes or interfaces
				} catch (IllegalAccessException e) {
					// e.printStackTrace(); also happens
				}
			}
			// Class.forName()
		}
		/*
		 * } catch (URISyntaxException e) { e.printStackTrace(); }
		 */
	}

	/**
	 * This is the graphical representation of a Pattern Preset in the
	 * PatternSelectorPanel
	 * 
	 * @author Holger Dammertz
	 * 
	 */
	static class PatternPresetLabel extends JLabel {
		private static final long serialVersionUID = -6943441006941948327L;
		Image previewImage;
		ImageIcon icon;
		public Pattern pat;

		public PatternPresetLabel(Pattern p) {
			pat = p;
			Dimension size = new Dimension(64, 64);
			setPreferredSize(size);
			setSize(size);
			previewImage = ChannelUtils.createAndComputeImage(pat, 64, 64, null, 0);
			icon = new ImageIcon(previewImage);
			this.setIcon(icon);
		}

		public void paint(Graphics g) {
			super.paint(g);
		}
	}

	/**
	 * This panel is used to show, select and delete the presets of Patterns.
	 * 
	 * @author Holger Dammertz
	 * 
	 */
	class PatternSelectorPanel extends JPanel implements ActionListener, MouseListener {
		private static final long serialVersionUID = 5732720988651708823L;

		JScrollPane scroller;
		JPanel patternPanel;
		GridLayout patternPanelLayout;
		int py = 0;
		Vector<Pattern> presets = new Vector<Pattern>();

		JPopupMenu presetPopupMenu;
		PatternPresetLabel clickedPreset = null;

		public PatternSelectorPanel() {
			Dimension size = new Dimension(96, 128);
			setPreferredSize(size);
			setSize(size);
			setBorder(BorderFactory.createEtchedBorder());
			scroller = new JScrollPane();
			setLayout(new BorderLayout());
			add(scroller, BorderLayout.CENTER);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			scroller.getVerticalScrollBar().setBlockIncrement(16);

			patternPanel = new JPanel();
			patternPanelLayout = new GridLayout(0, 1);
			patternPanelLayout.setVgap(8);
			patternPanel.setLayout(patternPanelLayout);

			createPopupMenu();
			// patternPanel.addMouseListener(this);

			scroller.getViewport().add(patternPanel);
		}

		void createPopupMenu() {
			presetPopupMenu = new JPopupMenu();

			createPopupMenuItem(presetPopupMenu, "Delete Preset");
		}

		JMenuItem createPopupMenuItem(JPopupMenu menu, String name) {
			JMenuItem item = new JMenuItem(name);
			menu.add(item);
			item.addActionListener(this);
			return item;
		}

		/**
		 * Loads a list presets from the NTEPresetString variable using a
		 * scanner. For each preset the addPatternPreset method is called.
		 */
		void loadPresets() {
			try {
				Scanner s = new Scanner(NTEPresetString);
				int numNodes = s.nextInt();
				for (int i = 0; i < numNodes; i++) {
					Channel c = Channel.loadChannel(s);
					if (c instanceof Pattern) {
						addPatternPreset((Pattern) c);
					} else {
						Logger.logError(this, "Preset file contained non-Patterns; ignoring them.");
					}
				}
			} catch (Exception e) {
				Logger.logError(this, "Failed to load presets");
			}
		}

		/**
		 * Saves all entries in the preset list to the global string
		 * NTEPresetString using a StringWriter and the
		 * TextureGraphNode.saveChannel method (Presets are just Patterns).
		 */
		void savePresets() {
			try {
				StringWriter sw = new StringWriter();
				sw.write(presets.size() + "\n");
				for (int i = 0; i < presets.size(); i++) {
					Channel.saveChannel(sw, presets.get(i));
				}
				sw.close();
				NTEPresetString = sw.getBuffer().toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Adds a given Pattern p to the list of presets
		 * 
		 * @param p
		 */
		public void addPatternPreset(Pattern p) {
			presets.add(p);
			PatternPresetLabel pw = new PatternPresetLabel(p);
			// pw.setLocation(0, py);
			// py += pw.getHeight() + 8;
			patternPanel.add(pw);
			// patternPanel.setPreferredSize(new Dimension(pw.getWidth(), py));

			pw.setTransferHandler(new TransferHandler("text"));
			MouseListener ml = new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						PatternPresetLabel ppl = (PatternPresetLabel) e.getSource();
						dragndropChannel = ppl.pat;
						JComponent jc = (JComponent) e.getSource();
						TransferHandler th = jc.getTransferHandler();
						th.exportAsDrag(jc, e, TransferHandler.COPY);
					}
				}
			};
			pw.addMouseListener(ml);
			pw.addMouseListener(this);

			patternPanel.revalidate();
			repaint();
		}

		/**
		 * Deletes a given preset from the list of presets.
		 * 
		 * @param p
		 */
		public void removePatternPreset(PatternPresetLabel p) {
			if (!presets.remove(clickedPreset.pat))
				Logger.logWarning(this, "Delete of selected preset failed");
			patternPanel.remove(clickedPreset);

			patternPanel.revalidate();
			repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			if (command.equals("Delete Preset")) {
				if (clickedPreset != null) {
					removePatternPreset(clickedPreset);
					clickedPreset = null;
				} else {
					Logger.logWarning(this, "Delete Preset called but no preset selected.");
				}
			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Object c = e.getSource();
			if ((e.getButton() == MouseEvent.BUTTON3) && (c instanceof PatternPresetLabel)) {
				clickedPreset = (PatternPresetLabel) c;
				presetPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			} else {
				clickedPreset = null;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	/**
	 * This is the file selection filter used in the open/save dialogs.
	 * 
	 * @author Holger Dammertz
	 * 
	 */
	static class TextureEditorFilenameFilter extends FileFilter {
		private String m_Extensions;
		private String m_Description;

		public TextureEditorFilenameFilter(String extensions, String description) {
			m_Extensions = extensions.toLowerCase();
			m_Description = description;
		}

		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				if (m_Extensions.indexOf(extension.toLowerCase()) != -1) {
					return true;
				} else {
					return false;
				}
			}

			return false;
		}

		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

		// The description of this filter
		public String getDescription() {
			return m_Description;
		}
	}

	PatternSelectorPanel m_PatternSelector;
	TextureGraphEditorPanel m_GraphDrawPanel;
	public OpenGLPreviewPanel m_OpenGLPreviewPanel; // !!TODO: remove the public
													// here
	JPanel m_CenterPanel;

	JFrame m_MainFrame;
	JMenuBar m_MainMenuBar;
	JMenuItem m_File_Save_Item;

	public PatternSelectorPanel getPatternSelectorPanel() {
		return m_PatternSelector;
	}

	public TextureGraphEditorPanel getTextureGraphEditorPanel() {
		return m_GraphDrawPanel;
	}

	public OpenGLPreviewPanel getOpenGLPreviewPanel() {
		return m_OpenGLPreviewPanel;
	}

	static class ProgressDialog extends JDialog implements ProgressBarInterface {
		private static final long serialVersionUID = 4543000728695540838L;

		JProgressBar progbar;
		int width = 380;

		public ProgressDialog(JFrame parent) {
			super(parent);
			setPreferredSize(new Dimension(width + 16, 32));
			setSize(getPreferredSize());
			setLayout(null);
			JPanel panel = new JPanel();
			panel.setLayout(null);
			panel.setBounds(0, 0, getWidth(), getHeight());
			panel.setBorder(BorderFactory.createEtchedBorder());
			add(panel);

			progbar = new JProgressBar(0, width);
			progbar.setBounds(8, 8, width, 16);
			panel.add(progbar);
			progbar.setValue(0);

			setResizable(false);
			setUndecorated(true);
		}

		public void setProgress(float val) {
			progbar.setValue((int) (val * width));
			paint(getGraphics());
		}

		public void startProgress() {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			setLocation(getParent().getX() + getParent().getWidth() / 2 - getWidth() / 2, getParent().getY() + getParent().getHeight() / 2
					- getHeight() / 2);
			setVisible(true);
		}

		public void endProgress() {
			setVisible(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

	}

	public void exitTextureEditor() {
		if (GL_ENABLED)
			m_OpenGLPreviewPanel.quit();
		m_GraphDrawPanel.save("exit.tgr");
		saveExitParameters();
		System.exit(0);
	}

	JMenuItem createMenuItem(JMenu menu, String name, String action, char mnemonic, KeyStroke ks) {
		JMenuItem ret;
		ret = new JMenuItem(name);
		ret.addActionListener(this);
		ret.setActionCommand(action);
		ret.setMnemonic(mnemonic);
		menu.add(ret);
		if (ks != null)
			ret.setAccelerator(ks);
		return ret;
	}

	JCheckBoxMenuItem createCheckboxMenuItem(JMenu menu, String name, String action, char mnemonic, KeyStroke ks) {
		JCheckBoxMenuItem ret;
		ret = new JCheckBoxMenuItem(name);
		ret.addActionListener(this);
		ret.setActionCommand(action);
		ret.setMnemonic(mnemonic);
		menu.add(ret);
		if (ks != null)
			ret.setAccelerator(ks);
		ret.setSelected(ChannelUtils.useCache);
		return ret;
	}

	void createMainMenu() {
		int modifierMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		m_MainMenuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic('F');
		m_MainMenuBar.add(file);
		createMenuItem(file, "New", "file_new", 'N', null);
		createMenuItem(file, "Open", "file_open", 'O', KeyStroke.getKeyStroke(KeyEvent.VK_O, modifierMask));
		createMenuItem(file, "Import", "file_import", 'I', KeyStroke.getKeyStroke(KeyEvent.VK_I, modifierMask));
		m_File_Save_Item = createMenuItem(file, "Save", "file_save", 's', null);
		m_File_Save_Item.setEnabled(false);
		m_File_Save_Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, modifierMask));
		createMenuItem(file, "Save as", "file_saveas", 'a', null);
		if (!MAC_OS_X) {
			file.addSeparator();
			createMenuItem(file, "Exit", "file_exit", 'x', KeyStroke.getKeyStroke(KeyEvent.VK_Q, modifierMask));
		}
		JMenu view = new JMenu("View");
		view.setMnemonic('V');
		m_MainMenuBar.add(view);
		createMenuItem(view, "Center", "view_center", 'C', null);
		createMenuItem(view, "Show/Hide OpenGL Preview", "view_OpenGLPreview", 'P', null).setEnabled(GL_ENABLED);

		JMenu options = new JMenu("Options");
		options.setMnemonic('O');
		m_MainMenuBar.add(options);
		createCheckboxMenuItem(options, "Use Cache", "options_toggle_usecache", 'C', null);

		JMenu help = new JMenu("Help");
		help.setMnemonic('H');
		m_MainMenuBar.add(help);
		createMenuItem(help, "Help", "help_dialog", 'H', KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.addSeparator();
		createMenuItem(help, "About", "about_dialog", 'A', null);
	}

	public void setCurrentFileName(String name) {
		if (name == null) {
			if (m_File_Save_Item != null)
				m_File_Save_Item.setEnabled(false);
			setTitle(" - " + title);
			return;
		}
		m_CurrentFile = new File(name);
		if (m_File_Save_Item != null)
			m_File_Save_Item.setEnabled(true);
		setTitle(m_CurrentFile.getName() + " " + title);
	}

	public void setTitle(String title) {
		if (m_MainFrame != null)
			m_MainFrame.setTitle(title);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("file_new")) {
			m_GraphDrawPanel.deleteFullGraph();
			if (GL_ENABLED)
				m_OpenGLPreviewPanel.resetPreview();
			setCurrentFileName(null);
		} else if (c.equals("file_open")) {
			m_TextureFileChooser_SaveLoadGraph.setDialogTitle("Loading texture graph from ...");
			if (m_TextureFileChooser_SaveLoadGraph.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String name = m_TextureFileChooser_SaveLoadGraph.getSelectedFile().getAbsolutePath();
				m_GraphDrawPanel.load(name, true);
				setCurrentFileName(name);
			}
		} else if (c.equals("file_import")) {
			m_TextureFileChooser_SaveLoadGraph.setDialogTitle("Import (append) texture graph from ...");
			if (m_TextureFileChooser_SaveLoadGraph.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String name = m_TextureFileChooser_SaveLoadGraph.getSelectedFile().getAbsolutePath();
				m_GraphDrawPanel.load(name, false);
			}
		} else if (c.equals("file_save")) {
			if (m_CurrentFile != null)
				m_GraphDrawPanel.save(m_CurrentFile.getAbsolutePath());
		} else if (c.equals("file_saveas")) {
			m_TextureFileChooser_SaveLoadGraph.setDialogTitle("Saving texture graph as ...");
			if (m_TextureFileChooser_SaveLoadGraph.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				String name = m_TextureFileChooser_SaveLoadGraph.getSelectedFile().getAbsolutePath();
				if (!name.endsWith(".tgr"))
					name += ".tgr";
				m_GraphDrawPanel.save(name);
				setCurrentFileName(name);
			}
		} else if (c.equals("file_exit")) {
			if (m_MainFrame != null)
				m_MainFrame.dispose();
		} else if (c.equals("view_center")) {
			m_GraphDrawPanel.centerDesktop();
		} else if (c.equals("view_OpenGLPreview")) {
			if (!GL_ENABLED) {
				Logger.logError(this, "Tried to show OpenGL preview, but it is not initialized.");
			} else {
				m_OpenGLPreviewPanel.setVisible(!m_OpenGLPreviewPanel.isVisible());
				m_CenterPanel.validate();
			}
		} else if (c.equals("options_toggle_usecache")) {
			ChannelUtils.useCache = !ChannelUtils.useCache;

		} else if (c.equals("help_dialog")) {
			JOptionPane.showMessageDialog(null, help_message, "NeoTextureEdit - Help", JOptionPane.PLAIN_MESSAGE);
		} else if (c.equals("about_dialog")) {
			JOptionPane.showMessageDialog(null, about_message, "About NeoTextureEdit", JOptionPane.PLAIN_MESSAGE);
		}

	}

	static class CommandLineOptions {
		String filename; // this is the last given name in the list of
							// filenames;
		Vector<String> allFileNames = new Vector<String>(); // used so that
															// wildcards work
															// for example for
															// export
		String exportPath = ".";
		int exportResX = 512;
		int exportResY = 512;
		boolean exportOnly = false;
		boolean useOpenGL = true;

		void parse(String[] args) {
			for (int i = 0; i < args.length; i++) {
				String a = args[i];

				if (!a.startsWith("-")) {
					filename = a;
					allFileNames.add(a);
				} else if (a.equalsIgnoreCase("-e")) { // export
					exportOnly = true;
					useOpenGL = false;
				} else if (a.equalsIgnoreCase("-p")) {
					exportPath = args[++i];
				} else if (a.equalsIgnoreCase("-r")) {
					String s = args[++i];
					if (s.matches("\\d+x\\d+")) {
						exportResX = Integer.parseInt(s.substring(0, s.indexOf('x')));
						exportResY = Integer.parseInt(s.substring(s.indexOf('x') + 1, s.length()));
					} else {
						System.out.println("Error in resolution string. Expected sth like 512x512; got " + s);
						System.exit(0);
					}
				} else if (a.equalsIgnoreCase("--disableGL")) {
					useOpenGL = false;
				} else if (a.equalsIgnoreCase("--help") || a.equalsIgnoreCase("-h")) {
					System.out.println("\nNeoTextureEdit " + "Version: " + programVersionNumber + " "
							+ "(c) Copyright Holger Dammertz 2010. All rights reserved.\n");
					System.out.println("Usage: neotextureedit [filename] [options]");
					System.out.println(" Options: ");
					System.out.println("    -h, --help                this help");
					System.out.println("    -e                        export the given file and exit");
					System.out.println("    -r  128x128               set the output resolution for the export");
					System.out.println("    -p path                   set the path for export");
					System.out.println("    --disableGL               disable the use of the OpenGL preview");
					System.out.println("\nContact and bug reports at http://sourceforge.net/projects/neotextureedit");
					System.exit(0);
				}
			}
		}
	};

	final CommandLineOptions commandLineOptions = new CommandLineOptions();

	static class StdOutProgressBar implements ProgressBarInterface {
		float count = 0;

		@Override
		public void endProgress() {
			System.out.println();
		}

		@Override
		public void setProgress(float val) {
			while (count < val) {
				count += 1.0f / 32.0f;
				System.out.print(".");
			}
		}

		@Override
		public void startProgress() {
			count = 0;
		}
	}

	/**
	 * Called when the command line option is given to export. Exits after
	 * exporting all marked nodes in the given file.
	 */
	void exportTexturesToImages() {
		if (commandLineOptions.allFileNames.size() == 0) {
			System.out.println("Export error: need at least one filename of texture graph to load.");
			System.exit(0);
		}

		ChannelUtils.useCache = false;

		for (String filename : commandLineOptions.allFileNames) {
			TextureGraphEditorPanel te = new TextureGraphEditorPanel();
			te.load(filename, true);

			for (TextureGraphNode n : te.graph.getAllNodes()) {
				if (n.getChannel().isMarkedForExport()) {
					String exportname = n.getChannel().exportName.get();
					String f = filename;
					// ugly
					String tmp_filename = f.substring(f.lastIndexOf("\\") + 1, f.length() - 4);
					tmp_filename = tmp_filename.substring(tmp_filename.lastIndexOf("/") + 1);
					exportname = exportname.replaceAll("\\%f", tmp_filename);
					exportname = exportname.replaceAll("\\%r", commandLineOptions.exportResX + "x" + commandLineOptions.exportResY);
					exportname = commandLineOptions.exportPath + "/" + exportname + ".png";
					System.out.println("Exporting " + exportname);
					try {
						ImageIO.write(ChannelUtils.createAndComputeImage(n.getChannel(), commandLineOptions.exportResX,
								commandLineOptions.exportResY, new StdOutProgressBar(), 0), "png", new File(exportname));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		System.exit(0);
	}

	/**
	 * This method creates the main window for a stand alone NeoTextureEdit
	 * application
	 */
	public void createMainFrame() {
		m_MainFrame = new JFrame();
		m_MainFrame.addKeyListener(this);

		setTitle(title);
		m_MainFrame.setIconImage(ChannelUtils.createAndComputeImage(new PatternChecker(2, 2), 16, 16, null, 0));
		m_MainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Window win = e.getWindow();
				win.dispose();
			}

			public void windowClosed(WindowEvent je) {
				exitTextureEditor();
				System.exit(0);
			}
		});

		m_MainFrame.setLayout(new BorderLayout());

		m_CenterPanel = new JPanel(new BorderLayout());

		m_CenterPanel.add(m_PatternSelector, BorderLayout.WEST);
		m_CenterPanel.add(m_GraphDrawPanel, BorderLayout.CENTER);

		if (GL_ENABLED) {
			m_CenterPanel.add(m_OpenGLPreviewPanel, BorderLayout.SOUTH);
		}

		m_MainFrame.getContentPane().add(m_GraphDrawPanel.getParameterEditorPanel(), BorderLayout.EAST);
		m_MainFrame.getContentPane().add(m_CenterPanel, BorderLayout.CENTER);

		createMainMenu();

		m_MainFrame.setJMenuBar(m_MainMenuBar);

		m_ProgressDialog = new ProgressDialog(m_MainFrame);
	}

	// GradientEditorPanel m_GradientEditorPanel;

	public TextureEditor(String[] args) {
		if (INSTANCE != null)
			Logger.logFatal(this, "Multiple instances of TextureEditor are not allowed.");
		INSTANCE = this;
		// now parse the command line
		commandLineOptions.parse(args);

		if (commandLineOptions.exportOnly) {
			exportTexturesToImages();
		}

		tempTest_FindAllPatternsAndChannelClasses();

		if (commandLineOptions.useOpenGL) {
			try {
				m_OpenGLPreviewPanel = new OpenGLPreviewPanel();
				if (!TextureEditor.GL_ENABLED) { // sth. went wrong on
													// initializing openGL so we
													// disable openGL preview
					// m_OpenGLPreviewPanel = null;
					Logger.logError(this, "Could not initialize OpenGL for Preview Rendering!");
				}
			} catch (NoClassDefFoundError ncdfe) {
				Logger.logError(this, "Could not initialize OpenGL for Preview Rendering!");
				ncdfe.printStackTrace();
			}
		}

		m_ColorChooser = new ColorChooserDialog(null);
		m_PatternSelector = new PatternSelectorPanel();
		m_GraphDrawPanel = new TextureGraphEditorPanel();

		GradientEditorPanel.colorChooser = m_ColorChooser;

		m_TextureFileChooser_SaveLoadGraph = new JFileChooser(".");
		m_TextureFileChooser_SaveLoadGraph.addChoosableFileFilter(new TextureEditorFilenameFilter("tgr", "Texture Graph Files (.tgr)"));

		m_TextureFileChooser_SaveLoadImage = new JFileChooser(".");
		m_TextureFileChooser_SaveLoadImage.addChoosableFileFilter(new TextureEditorFilenameFilter("png", "Image Files (.png)"));

	}

	/**
	 * If you create a stand alone frame using the method createMainFrame this
	 * method needs to be called afterwards
	 */
	public void initialize() {
		loadAndSetExitParameters(commandLineOptions.filename);
		m_PatternSelector.loadPresets();
	}

	private void saveExitParameters() {
		m_PatternSelector.savePresets(); // stores them in a string in
											// NTEPresetString

		preferences.put("NTEPresets", NTEPresetString);

		if (m_MainFrame != null) {
			preferences.putInt("mainWindowPosX", m_MainFrame.getX());
			preferences.putInt("mainWindowPosY", m_MainFrame.getY());
			preferences.putInt("mainWindowSizeX", m_MainFrame.getWidth());
			preferences.putInt("mainWindowSizeY", m_MainFrame.getHeight());
		}

		preferences.putInt("m_ColorChooser.PosX", m_ColorChooser.getX());
		preferences.putInt("m_ColorChooser.PosY", m_ColorChooser.getY());
		preferences.putInt("m_TextureFileChooser_SaveLoadImage.PosX", m_TextureFileChooser_SaveLoadImage.getX());
		preferences.putInt("m_TextureFileChooser_SaveLoadImage.PosY", m_TextureFileChooser_SaveLoadImage.getY());
		preferences.put("m_TextureFileChooser_SaveLoadImage.directory", m_TextureFileChooser_SaveLoadImage.getCurrentDirectory()
				.getAbsolutePath());
		preferences.putInt("m_TextureFileChooser_SaveLoadGraph.PosX", m_TextureFileChooser_SaveLoadGraph.getX());
		preferences.putInt("m_TextureFileChooser_SaveLoadGraph.PosY", m_TextureFileChooser_SaveLoadGraph.getY());
		preferences.put("m_TextureFileChooser_SaveLoadGraph.directory", m_TextureFileChooser_SaveLoadGraph.getCurrentDirectory()
				.getAbsolutePath());

		preferences.put("m_CurrentFileName", m_CurrentFile.getAbsolutePath());
	}

	private void loadAndSetExitParameters(String cmdLine_fileNameToLoad) {
		NTEPresetString = preferences.get("NTEPresets", defaultNTEPresets);

		if (m_MainFrame != null) {
			m_MainFrame.setSize(preferences.getInt("mainWindowSizeX", 1024), preferences.getInt("mainWindowSizeY", 768));
			m_MainFrame.setLocation(preferences.getInt("mainWindowPosX", 0), preferences.getInt("mainWindowPosY", 0));
		}

		m_ColorChooser.setLocation(preferences.getInt("m_ColorChooser.PosX", 0), preferences.getInt("m_ColorChooser.PosY", 0));
		m_TextureFileChooser_SaveLoadImage.setLocation(preferences.getInt("m_TextureFileChooser_SaveLoadImage.PosX", 0),
				preferences.getInt("m_TextureFileChooser_SaveLoadImage.PosY", 0));
		m_TextureFileChooser_SaveLoadImage.setCurrentDirectory(new File(preferences.get("m_TextureFileChooser_SaveLoadImage.directory", ".")));

		m_TextureFileChooser_SaveLoadGraph.setLocation(preferences.getInt("m_TextureFileChooser_SaveLoadGraph.PosX", 0),
				preferences.getInt("m_TextureFileChooser_SaveLoadGraph.PosY", 0));
		m_TextureFileChooser_SaveLoadGraph.setCurrentDirectory(new File(preferences.get("m_TextureFileChooser_SaveLoadGraph.directory", ".")));

		if (cmdLine_fileNameToLoad != null) {
			setCurrentFileName(cmdLine_fileNameToLoad);
			if (!m_GraphDrawPanel.load(m_CurrentFile.getAbsolutePath(), true)) {
				setCurrentFileName(null);
			}
		} else {
			setCurrentFileName(preferences.get("m_CurrentFileName", "examples/example_Bricks.tgr"));
			if (m_CurrentFile.equals("null"))
				setCurrentFileName(null);
			else if (!m_GraphDrawPanel.load(m_CurrentFile.getAbsolutePath(), true)) {
				setCurrentFileName(null);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false); // needed for the
																// gl canvas
		// font for the basic help dialogs
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("Monospaced", Font.PLAIN, 12)));

		// !!TODO: move the libs into the native directory?
		System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + "/lib/lwjgl-2.9.1/native/");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

			LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
			for (int i = 0; i < infos.length; i++) {
				// System.out.println(infos[i]);
			}
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("LookAndFeel Exception: " + e);
		} catch (ClassNotFoundException e) {
			System.err.println("LookAndFeel Exception: " + e);
		} catch (InstantiationException e) {
			System.err.println("LookAndFeel Exception: " + e);
		} catch (IllegalAccessException e) {
			System.err.println("LookAndFeel Exception: " + e);
		}

		TextureEditor te = new TextureEditor(args); // new
													// String[]{"--disableGL"}
		te.createMainFrame();
		te.initialize();
		te.registerForMacOSXEvents();
		te.m_MainFrame.setVisible(true);
	}

	// -------------------------------------------------------------------------------------------------------------

	static final int BUTTONICON_SIZE = 12;
	ImageIcon s_ButtonIconRGB_Selector;
	ImageIcon s_ButtonIconRGBA_Selector;
	ImageIcon s_ButtonIconA_Selector;

	/**
	 * Creates and caches a procedural ImageIcon representing the RGB channel of
	 * an image.
	 * 
	 * @return the generated or cached ImageIcon
	 */
	public ImageIcon Get_IconRGB() {
		if (s_ButtonIconRGB_Selector == null) {
			BufferedImage img = new BufferedImage(BUTTONICON_SIZE, BUTTONICON_SIZE, BufferedImage.TYPE_INT_RGB);
			Graphics g = img.getGraphics();
			g.setColor(Color.red);
			g.fillRect(0, 0, img.getWidth() / 3, img.getHeight());
			g.setColor(Color.green);
			g.fillRect(img.getWidth() / 3, 0, (img.getWidth() / 3), img.getHeight());
			g.setColor(Color.blue);
			g.fillRect(2 * (img.getWidth() / 3), 0, img.getWidth() / 3, img.getHeight());
			s_ButtonIconRGB_Selector = new ImageIcon(img);
		}
		return s_ButtonIconRGB_Selector;
	}

	/**
	 * Creates and caches a procedural ImageIcon representing the RGBA channel
	 * of an image.
	 * 
	 * @return the generated or cached ImageIcon
	 */
	public ImageIcon Get_IconRGBA() {
		if (s_ButtonIconRGBA_Selector == null) {
			BufferedImage img = new BufferedImage(BUTTONICON_SIZE, BUTTONICON_SIZE, BufferedImage.TYPE_INT_RGB);

			for (int y = 0; y < img.getWidth(); y++) {
				for (int x = 0; x < img.getHeight(); x++) {
					int r, g, b;
					r = g = b = 0;
					if (x < img.getWidth() / 3)
						r = 255;
					else if (x < 2 * (img.getWidth() / 3))
						g = 255;
					else
						b = 255;

					if (((x + y) % 4) < 2) {
						r /= 2;
						g /= 2;
						b /= 2;
					}
					img.setRGB(x, y, (r << 16) | (g << 8) | b);
				}
			}

			s_ButtonIconRGBA_Selector = new ImageIcon(img);
		}
		return s_ButtonIconRGBA_Selector;
	}

	/**
	 * Creates and caches a procedural ImageIcon representing the alpha channel
	 * of an image.
	 * 
	 * @return the generated or cached ImageIcon
	 */
	public ImageIcon Get_IconA() {
		if (s_ButtonIconA_Selector == null) {
			BufferedImage img = new BufferedImage(BUTTONICON_SIZE, BUTTONICON_SIZE, BufferedImage.TYPE_INT_RGB);
			Graphics g = img.getGraphics();
			// g.getFontMetrics()
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("A", 2, 11);
			g.drawString("A", 3, 11);

			s_ButtonIconA_Selector = new ImageIcon(img);
		}
		return s_ButtonIconA_Selector;
	}

	/**
	 * Here is currently hard coded the keyboard handling of all key events in
	 * NeoTextureEdit.
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch (arg0.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			System.out.println("DELETE");
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	public void onQuit() {
		if (m_MainFrame != null)
			m_MainFrame.dispose();
	}

	// Generic registration with the Mac OS X application menu
	// Checks the platform, then attempts to register with the Apple EAWT
	// See OSXAdapter.java to see how this is done without directly referencing
	// any Apple APIs
	public void registerForMacOSXEvents() {
		if (MAC_OS_X) {
			try {
				// Generate and register the OSXAdapter, passing it a hash of
				// all the methods we wish to
				// use as delegates for various
				// com.apple.eawt.ApplicationListener methods
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("onQuit", (Class[]) null));
				// OSXAdapter.setAboutHandler(this,
				// getClass().getDeclaredMethod("about", (Class[])null));
				// OSXAdapter.setPreferencesHandler(this,
				// getClass().getDeclaredMethod("preferences", (Class[])null));
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			} catch (Exception e) {
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
	}
}
