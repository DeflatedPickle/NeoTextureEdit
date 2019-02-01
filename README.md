# NeoTextureEdit
A graph-based procedural texture editor to create 2d seamless textures for real-time rendering applications.

NeoTextureEdit is an easy to use graph-based procedural seamless
texture editor. Using continuous basis functions it can generate
arbitrary resolution images without quality degradation. Its main
purpose is to produce high quality textures for real time rendering
applications that can be stored in a few kB and synthesized on
application startup. But it can also be used to generate off-line
images.

## Original
This is a fork. The original version can be found here:
http://sourceforge.net/projects/neotextureedit/

# NeoTexture Library
The NeoTexture Library can be used to load a texture graph generated
by NeoTextureEdit and create the according textures at runtime.

# Usage Notes
To learn the usage of the editor you can look at the provided example
textures in the examples directory (a Wiki will be made soon).

NeoTextureEdit saves its settings on exit in a file called
'TextureEdtiorSettings'.  This file will be saved in the directory
from where you start the editor.  This file also contains your pattern
presets. Deleting this file resets to the factory settings.

# Release Notes
## Current
The release notes for the next version.

#### Next:
 - Changed the build tool from Ant to Gradle
 - Removed the included dependencies and moved them to Gradle
 - Removed the included logging library in favour of Log4J
 - Moved the parameter editors to a new package
 - Moved the minus buttons on the `FloatParameterEditor` and the `IntParameterEditor` to be next to the plus button
 - Removed the `JButton`'s and `JFormattedTextField`'s from `FloatParameterEditor` and the `IntParameterEditor` in favour of `JSpinner`'s.

## Old Release Notes
These were either unreleased or were made when the repository used SVN.

#### 0.6.3:
 - Blur filter added

#### 0.6.1.1:
 - pattern brick bug fix
 
#### 0.6.1:
 - license change to GNU LGPL v.3
 - external run time generation library now available
 - changed the texture graph file format slightly
 - Export String (with replace options %f %r) for marking channels for export
 - Command Line option for export to image (now batch export possible; also with wildcards)
 - SCM changed from svn to git on sourceforge
 - Basic drag-all feature in gradient editor (not yet very robust)
 
#### 0.5.3:
 - New Pattern: Bitmap: allows to load images as generator patterns
 - New Pattern: Function: basic sin/saw/square function as generator pattern
 - Command line options for loading texture graph files and disabling OpenGL
 - Switched to LWJGL 2.3
 - several minor fixes/tweaks: among others are new presets, auto-loading 
   of example at first start; switched to system UI instead of nimbus for
   now

#### 0.5.3a
 - Command line options for loading texture file and disabling OpenGL
 - Experimental: Synthesis Pattern
 - Function Pattern: basic sin/saw/square function as generator pattern
 - Possible fix for the 2.3 LWJGL
 - Bitmap Pattern: allows to load images as generator patterns
   
#### 0.5.2a 
 - some new pattern presets
 - export of (almost) arbitrary resolution now works
 - switched for release back from LWJGL 2.3 to LWJGL 2.2.2 because of MacOS problems
 - re-enabled autosave into exit.tgr when quitting program
 - added ambient term to OpenGL preview
 - fixed some OpenGL preview bugs
 - open texture file from command line parameter
 - added popup-menu to OpenGL preview with clear textures option
   
#### 0.5.1a:
 - OpenGL Preview of Textures
