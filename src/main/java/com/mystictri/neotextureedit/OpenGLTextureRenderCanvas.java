package com.mystictri.neotextureedit;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.swing.JPopupMenu;

import de.javagl.obj.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import engine.base.FMath;
import engine.base.Utils;
import engine.base.Vector3;
import engine.graphics.synthesis.texture.Channel;
import engine.parameters.AbstractParam;
import engine.parameters.EnumParam;
import engine.parameters.FloatParam;
import engine.parameters.IntParam;
import engine.parameters.LocalParameterManager;
import engine.parameters.ParamChangeListener;

/**
 * This canvas is used to preview a texture in an OpenGL renderer. It is not implemented for performance but for
 * easy and flexible preview rendering (thus some textures are managed as individual textures even though they
 * may be combined with other textures).
 *
 * @author Holger Dammertz
 */
class OpenGLTextureRenderCanvas extends AWTGLCanvas implements Runnable, MouseListener, MouseMotionListener, KeyListener, ParamChangeListener {
    private static final long serialVersionUID = -1713673512688807546L;

    static final boolean USE_THREAD = true; // This is a temp variable to experiment with a thread for rendering vs. selective repaint on mouse clicks (the second
    // method currently has the problem of disappearing render windows; the first has a higher cpu load

    boolean initialized = false;
    Thread renderThread;

    public boolean running = true;
    public boolean pause = true;

    final int TEXTURE_RESX = 256;
    final int TEXTURE_RESY = 256;

    int GLXres, GLYres;

    long timer = 0;
    long dtCount = 0;
    long frames = 0;

    //!!TODO: encapsulate shader into an extra class
    int texidDiffuse = 0;
    int texidNormal = 0;
    int texidSpecWeight = 0;
    int texidHeightmap = 0;

    int previewProgram = 0;

    int u_WS_EyePos_loc;
    int u_CameraONB_loc;
    int _2dDiffuseMap_loc;
    int _2dNormalMap_loc;
    int _2dSpecWeightMap_loc;
    int _2dHeightMap_loc;

    int u_SpecularPower_loc;
    int u_POM_Strength_loc;
    int u_TexScale_loc;
    int u_Ambient_loc;

    JPopupMenu settingsPopupMenu;

    HashMap<String, Obj> objHashMap = new HashMap<>();

    static class GLPreviewParameters extends LocalParameterManager {
        public EnumParam previewObject = CreateLocalEnumParam("Object", "Plane,Cube,Cylinder,Monkey"); // Sphere
        public FloatParam specularPower = CreateLocalFloatParam("Spec. Power", 20.0f, 0.f, 200.0f);
        public FloatParam pomStrength = CreateLocalFloatParam("POM Strength", 0.25f, 0.f, 1.0f).setDefaultIncrement(0.125f);
        public FloatParam ambient = CreateLocalFloatParam("Ambient", 0.5f, 0.f, 1.0f).setDefaultIncrement(0.125f);
        public IntParam texScaleU = CreateLocalIntParam("TexScale U", 1, 1, 8);
        public IntParam texScaleV = CreateLocalIntParam("TexScale V", 1, 1, 8);

        // hidden parameters
        public FloatParam rotX = CreateLocalFloatParam("CamRotX", 45.0f, 3.f, 89.0f);
        public FloatParam rotY = CreateLocalFloatParam("CamRotY", 0.0f, -1.f, 360.0f);
        public FloatParam camDist = CreateLocalFloatParam("CamDist", 2.5f, 0.5f, 8.0f);

        {
            rotX.hidden = true;
            rotY.hidden = true;
            camDist.hidden = true;
        }
    }

    GLPreviewParameters params = new GLPreviewParameters();

    // very basic preview shader (does compute the camera transform extra to provide world space coordinates for the fragment shader)
    String previewVertexShader =
            "uniform vec3 u_WS_EyePos; " +
                    "uniform mat3 u_CameraONB; " +
                    "attribute vec3 a_OS_tangent; " +

                    "varying vec3 v_WS_vertex; " +
                    "varying vec3 v_WS_normal; " +
                    "varying vec3 v_WS_tangent; " +

                    "void main(void) { " +
                    "	gl_TexCoord[0] = gl_MultiTexCoord0; " +

                    "	v_WS_vertex = vec3(gl_ModelViewMatrix * gl_Vertex); " +
                    "	v_WS_normal = vec3(gl_NormalMatrix * gl_Normal); " +
                    "	v_WS_tangent = vec3(gl_NormalMatrix * a_OS_tangent); " +

                    "	gl_Position = gl_ProjectionMatrix * vec4((v_WS_vertex - u_WS_EyePos) * u_CameraONB, 1); " +
                    "}";

    // very basic preview shader
    String previewFragmentShader =
            "uniform vec3 u_WS_EyePos; " +

                    "uniform sampler2D _2dTex0; " +
                    "uniform sampler2D _2dNormalMap; " +
                    "uniform sampler2D _2dSpecWeightMap; " +
                    "uniform sampler2D _2dHeightMap; " +

                    "varying vec3 v_WS_vertex; " +
                    "varying vec3 v_WS_normal; " +
                    "varying vec3 v_WS_tangent; " +
                    "" +
                    "uniform float u_SpecularPower; " +
                    "uniform float u_POM_Strength; " +
                    "uniform vec2 u_TexScale; " +
                    "uniform float u_Ambient; " +
                    "" +

                    "void main(void){ " +
                    "	vec3 lightPos = vec3(20.0, 60.0, 40.0); " +

                    "	vec3 lightDir = normalize(lightPos - v_WS_vertex); " +

                    "	mat3 onb; " +
                    "	onb[2] = normalize(v_WS_normal); " +
                    "	onb[0] = normalize(v_WS_tangent - dot(onb[2], v_WS_tangent)*onb[2]); " +
                    "	onb[1] = cross(onb[2], onb[0]); " +

                    "	vec3 rayDir = v_WS_vertex - u_WS_EyePos; " +
                    "	vec3 eyeDir = normalize(rayDir * onb); " +

                    "	vec3 pos = vec3(gl_TexCoord[0].xy * u_TexScale, 1.0); " +

                    "   rayDir = eyeDir; " +
                    "   if (rayDir.z > 0.0) rayDir.z = -rayDir.z; " +
                    "	if (u_POM_Strength > 0.0) {" +
                    "     rayDir.z /= 0.25*u_POM_Strength; " +
                    "     rayDir.x *= 0.5; " +
                    "     rayDir.y *= 0.5; " +
                    "     for (int i = 0; i < 512; i++) { " +
                    "     	  if (pos.z < 1.0-(texture2D(_2dHeightMap, pos.xy).r)) break; " +
                    "   	  pos += rayDir*0.001; " +
                    "     } " +
                    "   } " +

                    "	vec3 normal = onb * normalize(texture2D(_2dNormalMap, pos.xy).rgb * 2.0 - 1.0); " +

                    "	vec3 refl = reflect(lightDir, normal); " +
                    "	float SpecWeightyness = texture2D(_2dSpecWeightMap, pos.xy).x;" +
                    "	float spec = pow(max(dot(refl, eyeDir), 0.0), u_SpecularPower)*0.5*SpecWeightyness; " +
                    "	float diff = dot(lightDir, normal); " +
                    "	if (diff < 0.0) diff = 0.0; " +
                    "	diff = min(diff+u_Ambient, 1.0); " +
                    "	vec4 color = vec4(texture2D(_2dTex0, pos.xy).xyz, 0.0) * diff + spec; " +

                    "	gl_FragColor = color; " +
                    "}";

    int getUniformLocation(String loc_name, int programID) {
        byte[] bs = loc_name.getBytes();
        ByteBuffer b = Utils.allocByteBuffer(bs.length + 1);
        b.put(bs);
        b.put((byte) 0);
        b.flip();
        int ret = ARBShaderObjects.glGetUniformLocationARB(programID, b);
        if (ret == -1) {
            TextureEditor.logger.warn("Shader: " + programID + ": could not get uniform location " + loc_name);
        }
        Util.checkGLError();
        return ret;
    }

    void loadShaderLocations() {
        u_WS_EyePos_loc = getUniformLocation("u_WS_EyePos", previewProgram);
        u_CameraONB_loc = getUniformLocation("u_CameraONB", previewProgram);

        _2dDiffuseMap_loc = getUniformLocation("_2dTex0", previewProgram);
        _2dNormalMap_loc = getUniformLocation("_2dNormalMap", previewProgram);
        _2dSpecWeightMap_loc = getUniformLocation("_2dSpecWeightMap", previewProgram);
        _2dHeightMap_loc = getUniformLocation("_2dHeightMap", previewProgram);

        u_SpecularPower_loc = getUniformLocation("u_SpecularPower", previewProgram);
        u_POM_Strength_loc = getUniformLocation("u_POM_Strength", previewProgram);
        u_TexScale_loc = getUniformLocation("u_TexScale", previewProgram);
        u_Ambient_loc = getUniformLocation("u_Ambient", previewProgram);

        bindTangentAttribute("a_OS_tangent");
    }

    public void bindTangentAttribute(String name) {
        ByteBuffer nameb = Utils.allocByteBuffer(name.length() + 1);
        nameb.put(name.getBytes());
        nameb.put((byte) 0);
        nameb.flip();
        GL20.glBindAttribLocation(previewProgram, 1, nameb);
    }

    public OpenGLTextureRenderCanvas(int xres, int yres, JPopupMenu settingsPopupMenu) throws LWJGLException {
        super();

        GLXres = xres;
        GLYres = yres;
        this.settingsPopupMenu = settingsPopupMenu;

        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);

        for (AbstractParam p : params.m_LocalParameters) {
            if (p.hidden) continue;
            p.addParamChangeListener(this);
        }

        var folder = new File(this.getClass().getResource("/models/").getPath());

        for (var file : Objects.requireNonNull(folder.listFiles())) {
            if (FilenameUtils.getExtension(file.getName()).equals("obj")) {
                TextureEditor.logger.info("Found an OBJ model: " + file.getName());

                try {
                    objHashMap.put(FilenameUtils.getBaseName(file.getName()), ObjUtils.convertToRenderable(ObjReader.read(FileUtils.openInputStream(file))));

                    TextureEditor.logger.info("Loaded an OBJ model: " + file.getName());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startRenderThread() {
        if (!USE_THREAD) return;

        if (renderThread == null) {
            renderThread = new Thread(this);
            renderThread.start();
        }
        else {
            System.err.println("WARNING: Render Thread is already running");
        }
    }

    public void run() {
        while (renderThread.isAlive()) {
            repaint();
            try {
                Thread.sleep(1000 / 10);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    void initGLState() {
        GL11.glViewport(0, 0, getWidth(), getHeight());
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        //GL11.glClearColor(62.0f / 100.0f, 77.0f / 100.0f, 100.0f / 100.0f, 1.0f);
        GL11.glClearColor(64.0f / 255.0f, 64.0f / 255.0f, 64.0f / 255.0f, 1.0f);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(60.0f, (float) GLXres / (float) GLYres, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        previewProgram = getShaderProgram(previewVertexShader, previewFragmentShader);

        loadShaderLocations();

        // hmm: temp initialization here
        // create the empty textures (later they are updated from the channels)
        texidDiffuse = create2dTexture(0x7F7FFFFF);
        texidNormal = create2dTexture(0x7F7FFFFF);
        texidSpecWeight = create2dTexture(0xFFFFFFFF);
        texidHeightmap = create2dTexture(0xFFFFFFFF);

    }

    boolean requestUpdateDiffuse = false;
    boolean requestUpdateNormal = false;
    boolean requestUpdateSpecWeight = false;
    boolean requestUpdateHeightmap = false;
    Channel _updateDiffuse = null;
    Channel _updateNormal = null;
    Channel _updateSpecWeight = null;
    Channel _updateHeightmap = null;

    public synchronized void updateDiffuseMap(Channel c) {
        if (c != null && !c.chechkInputChannels()) {
            TextureEditor.logger.warn("Incomplete input channel in diffuse map");
            _updateDiffuse = null;
        }
        else {
            _updateDiffuse = c;
        }
        requestUpdateDiffuse = true;
        repaint();
    }

    public synchronized void updateNormalMap(Channel c) {
        if (c != null && !c.chechkInputChannels()) {
            TextureEditor.logger.warn("Incomplete input channel in normal map");
            _updateNormal = null;
        }
        else {
            _updateNormal = c;
        }
        requestUpdateNormal = true;
        repaint();
    }

    public synchronized void updateSpecWeightMap(Channel c) {
        if (c != null && !c.chechkInputChannels()) {
            TextureEditor.logger.warn("Incomplete input channel in specular map");
            _updateSpecWeight = null;
        }
        else {
            _updateSpecWeight = c;
        }
        requestUpdateSpecWeight = true;
        repaint();
    }

    public synchronized void updateHeightMap(Channel c) {
        if (c != null && !c.chechkInputChannels()) {
            TextureEditor.logger.warn("Incomplete input channel in height map");
            _updateHeightmap = null;
        }
        else {
            _updateHeightmap = c;
        }
        requestUpdateHeightmap = true;
        repaint();
    }

    public FloatBuffer m_CamONB = Utils.allocFloatBuffer(9);

    final Vector3 UP = new Vector3(0, 0, 1);

    void updateCamera() {
        final Vector3 u = new Vector3();
        final Vector3 v = new Vector3();
        final Vector3 w = new Vector3();
        final Vector3 eye = new Vector3();
        float t = (params.rotY.get() - 90) * ((float) Math.PI / 180.0f);
        float p = (90 - params.rotX.get()) * ((float) Math.PI / 180.0f);

        w.set(FMath.cos(t) * FMath.sin(p), FMath.sin(t) * FMath.sin(p), FMath.cos(p));
        w.normalize();
        u.cross_ip(UP, w);
        u.normalize();
        v.cross_ip(w, u);

        m_CamONB.put(0, u.x);
        m_CamONB.put(1, u.y);
        m_CamONB.put(2, u.z);
        m_CamONB.put(3, v.x);
        m_CamONB.put(4, v.y);
        m_CamONB.put(5, v.z);
        m_CamONB.put(6, w.x);
        m_CamONB.put(7, w.y);
        m_CamONB.put(8, w.z);

        eye.mult_add_ip(params.camDist.get(), w);

        ARBShaderObjects.glUniform3fARB(u_WS_EyePos_loc, eye.x, eye.y, eye.z);
        ARBShaderObjects.glUniformMatrix3ARB(u_CameraONB_loc, false, m_CamONB);
    }

    synchronized void render() {
        // Process the requests made from another thread:

        if (requestUpdateDiffuse) {
            if (_updateDiffuse != null)
                update2dTexture(ChannelUtils.createAndComputeImage(_updateDiffuse, TEXTURE_RESX, TEXTURE_RESY, null, 0), texidDiffuse);
            else update2dTexture_ConstanctColor(0x7F7FFFFF, texidDiffuse);
            _updateDiffuse = null;
            requestUpdateDiffuse = false;
        }
        if (requestUpdateNormal) {
            if (_updateNormal != null)
                update2dTexture(ChannelUtils.createAndComputeImage(_updateNormal, TEXTURE_RESX, TEXTURE_RESY, null, 0), texidNormal);
            else update2dTexture_ConstanctColor(0x7F7FFFFF, texidNormal);
            _updateNormal = null;
            requestUpdateNormal = false;
        }
        if (requestUpdateSpecWeight) {
            if (_updateSpecWeight != null)
                update2dTexture(ChannelUtils.createAndComputeImage(_updateSpecWeight, TEXTURE_RESX, TEXTURE_RESY, null, 0), texidSpecWeight);
            else update2dTexture_ConstanctColor(0xFFFFFFFF, texidSpecWeight);
            _updateSpecWeight = null;
            requestUpdateSpecWeight = false;
        }
        if (requestUpdateHeightmap) {
            if (_updateHeightmap != null)
                update2dTexture(ChannelUtils.createAndComputeImage(_updateHeightmap, TEXTURE_RESX, TEXTURE_RESY, null, 0), texidHeightmap);
            else update2dTexture_ConstanctColor(0xFFFFFFFF, texidHeightmap);
            _updateHeightmap = null;
            requestUpdateHeightmap = false;
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        ARBShaderObjects.glUseProgramObjectARB(previewProgram);
        updateCamera();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texidDiffuse);
        ARBShaderObjects.glUniform1iARB(_2dDiffuseMap_loc, 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texidNormal);
        ARBShaderObjects.glUniform1iARB(_2dNormalMap_loc, 1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 2);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texidSpecWeight);
        ARBShaderObjects.glUniform1iARB(_2dSpecWeightMap_loc, 2);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 3);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texidHeightmap);
        ARBShaderObjects.glUniform1iARB(_2dHeightMap_loc, 3);

        ARBShaderObjects.glUniform1fARB(u_SpecularPower_loc, params.specularPower.get());
        ARBShaderObjects.glUniform1fARB(u_POM_Strength_loc, params.pomStrength.get());
        ARBShaderObjects.glUniform2fARB(u_TexScale_loc, params.texScaleU.get(), params.texScaleV.get());
        ARBShaderObjects.glUniform1fARB(u_Ambient_loc, params.ambient.get());

        // TODO: Make the model picking more dynamic
        String modelName = "";
        if (params.previewObject.getEnumPos() == 0) {
            modelName = "plane";
        }
        else if (params.previewObject.getEnumPos() == 1) {
            modelName = "cube";
        }
        else if (params.previewObject.getEnumPos() == 2) {
            modelName = "cylinder";
        }
        else if (params.previewObject.getEnumPos() == 3) {
            modelName = "suzanne";
        }

        var finalModelName = modelName;
        // var modeList = Arrays.asList(GL11.GL_POLYGON, GL11.GL_LINES, GL11.GL_POINTS);
        var modeList = Arrays.asList(GL11.GL_TRIANGLE_STRIP);

        for (var mode : modeList) {
            GL11.glBegin(mode);

            IntStream.range(0, objHashMap.get(finalModelName).getNumVertices()).forEachOrdered(e -> {
                var normal = objHashMap.get(finalModelName).getNormal(e);
                GL11.glNormal3f(normal.getX(), normal.getY(), normal.getZ());

                var texture = objHashMap.get(finalModelName).getTexCoord(e);
                GL11.glTexCoord2f(texture.getX(), texture.getY());

                var vertex = objHashMap.get(finalModelName).getVertex(e);
                GL11.glVertex3f(vertex.getX(), vertex.getY(), vertex.getZ());

                System.out.println(String.format("Index: %d, X: %f, Y: %f, Z: %f", e, vertex.getX(), vertex.getY(), vertex.getZ()));
            });
            System.out.println("------------------------------");

            GL11.glEnd();
        }

        // var model = objHashMap.get(modelName);

        // var indices = ObjData.getFaceVertexIndices(model, 3);
        // var vertices = ObjData.getVertices(model);
        // var texture = ObjData.getTexCoords(model, 2);
        // var normals = ObjData.getNormals(model);

        // var vertexHandle = GL15.glGenBuffers();
        // GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexHandle);
        // GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        // GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0L);
        // GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        // GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertices.capacity());

        // GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        // GL15.glDeleteBuffers(vertexHandle);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    protected void paintGL() {
        try {
            if (!initialized) {
                if (getContext() != null) {
                    initGLState();
                    timer = System.currentTimeMillis();
                    initialized = true;
                }
            }

            if (initialized) {
                render();
                swapBuffers();
            }
        }
        catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    int mouseOX = 0;
    int mouseOY = 0;
    int mouseButton;

    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - mouseOX;
        int dy = e.getY() - mouseOY;

        mouseOX = e.getX();
        mouseOY = e.getY();

        if (e.isShiftDown()) {
            params.camDist.increment(dy / 10.0f);
        }
        else {
            params.rotX.increment(dy);
            params.rotY.increment(-dx);

            if (params.rotY.get() >= 360) params.rotY.increment(-360);
            if (params.rotY.get() < 0) params.rotY.increment(360);
        }

        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        System.out.println(e);
    }

    public void mouseEntered(MouseEvent e) {
        requestFocus();
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mouseOX = e.getX();
        mouseOY = e.getY();
        mouseButton = e.getButton();

        if (e.isPopupTrigger()) {
            settingsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public int genTexID() {
        IntBuffer i = Utils.allocIntBuffer(1);
        GL11.glGenTextures(i);
        return i.get(0);
    }

    ByteBuffer bbuf = Utils.allocByteBuffer(TEXTURE_RESX * TEXTURE_RESY * 4);

    //!!TODO: it is inefficient to do the texture creation over a temporary BufferedImage as a procedural texture channel alrady operates
    //        on a float buffer that could be directly send to OpenGL for conversion (or at least converted directly in the convertImageData
    //        function below.
    public int create2dTexture(int color) {
        int id = genTexID();

        IntBuffer ibuf = bbuf.asIntBuffer();
        for (int i = 0; i < ibuf.capacity(); i++) ibuf.put(i, color);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        int targetFormat = GL11.GL_RGBA;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, targetFormat, TEXTURE_RESX, TEXTURE_RESY, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf.asIntBuffer());
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, targetFormat, TEXTURE_RESX, TEXTURE_RESY, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf);

        return id;
    }

    public synchronized void update2dTexture_ConstanctColor(int color, int id) {
        IntBuffer ibuf = bbuf.asIntBuffer();
        for (int i = 0; i < ibuf.capacity(); i++) ibuf.put(i, color);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        int targetFormat = GL11.GL_RGBA;
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, targetFormat, TEXTURE_RESX, TEXTURE_RESY, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf.asIntBuffer());
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, targetFormat, TEXTURE_RESX, TEXTURE_RESY, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf);
    }

    // here are the same performance problems as in create2dTexture
    public synchronized void update2dTexture(BufferedImage img, int id) {
        if (img.getWidth() != TEXTURE_RESX || img.getHeight() != TEXTURE_RESY) {
            TextureEditor.logger.error("TextureResolution does not match image resolution for update");
            return;
        }
        convertImageData(bbuf.asIntBuffer(), img);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        //int targetFormat = (img.getColorModel().hasAlpha()) ? GL11.GL_RGBA : GL11.GL_RGB;
        int targetFormat = GL11.GL_RGBA;
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, targetFormat, TEXTURE_RESX, TEXTURE_RESY, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf.asIntBuffer());
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, targetFormat, TEXTURE_RESX, TEXTURE_RESY, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, bbuf);
    }

    // !!TODO: this is a slow conversion operation because each byte is touched by hand...
    public void convertImageData(IntBuffer result, BufferedImage img) {
        int xres = img.getWidth();
        int yres = img.getHeight();
        int[] data = new int[xres * yres];
        img.getRGB(0, 0, xres, yres, data, 0, xres);

        for (int i = 0; i < xres * yres; i++) {
            int A = ((data[i] >> 24) & 0xFF);
            int R = ((data[i] >> 16) & 0xFF);
            int G = ((data[i] >> 8) & 0xFF);
            int B = ((data[i] >> 0) & 0xFF);
            data[i] = (R << 24) | (G << 16) | (B << 8) | (A << 0);
        }

        result.put(data);
        result.rewind();
    }

    ByteBuffer loadShaderCodeFromFile(String filename) {
        byte[] code = null;

        try {
            InputStream fs = OpenGLPreviewPanel.class.getResourceAsStream(filename);
            if (fs == null) {
                System.err.println("Error opening file " + filename);
                return null;
            }
            fs.read(code = new byte[fs.available()]);
            fs.close();
        }
        catch (IOException e) {
            System.out.println(e);
            return null;
        }

        ByteBuffer shaderCode = Utils.allocByteBuffer(code.length);

        shaderCode.put(code);
        shaderCode.flip();

        return shaderCode;
    }

    private void printLogInfo(int obj, String name) {
        IntBuffer iVal = Utils.allocIntBuffer(1);
        ARBShaderObjects.glGetObjectParameterARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB, iVal);

        int length = iVal.get();
        if (length > 1) {
            System.out.println();
            ByteBuffer infoLog = Utils.allocByteBuffer(length);
            iVal.flip();
            ARBShaderObjects.glGetInfoLogARB(obj, iVal, infoLog);
            byte[] infoBytes = new byte[length];
            infoLog.get(infoBytes);
            String out = new String(infoBytes);
            System.out.println("Info log: " + name + "\n" + out);
        }

        Util.checkGLError();
    }

    public int getShaderProgram(String vcode, String fcode) {
        ByteBuffer vc = ByteBuffer.allocateDirect(vcode.length());
        vc.put(vcode.getBytes());
        vc.flip();
        ByteBuffer fc = ByteBuffer.allocateDirect(fcode.length());
        fc.put(fcode.getBytes());
        fc.flip();
        return getShaderProgram(vc, fc);
    }

    /**
     * Loads a vertex and fragment shader pair, compiles them and links them to a ProgramObjectARB
     *
     * @param name
     * @return the program object ID
     */
    public int getShaderProgram(ByteBuffer vcode, ByteBuffer fcode) {
        int progID = -1;

        int vid = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
        int fid = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

        ARBShaderObjects.glShaderSourceARB(vid, vcode);
        ARBShaderObjects.glCompileShaderARB(vid);
        printLogInfo(vid, "Compile Vertex Shader");

        ARBShaderObjects.glShaderSourceARB(fid, fcode);
        ARBShaderObjects.glCompileShaderARB(fid);
        printLogInfo(fid, "Compile Fragment Shader");

        progID = ARBShaderObjects.glCreateProgramObjectARB();
        ARBShaderObjects.glAttachObjectARB(progID, vid);
        ARBShaderObjects.glAttachObjectARB(progID, fid);

        ARBShaderObjects.glLinkProgramARB(progID);
        printLogInfo(progID, "Link");
        ARBShaderObjects.glValidateProgramARB(progID);
        printLogInfo(progID, "Validate");

        return progID;
    }

    public void parameterChanged(AbstractParam source) {
        repaint();
    }
}

