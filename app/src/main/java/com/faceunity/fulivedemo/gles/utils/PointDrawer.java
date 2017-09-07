package com.faceunity.fulivedemo.gles.utils;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by arno on 2017/9/7.
 */

public class PointDrawer implements IDrawer{
    private static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "uniform float uPointSize;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  gl_PointSize = uPointSize;" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    //number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;
    private static final int SIZE_FLOAT = 4;
    private static final int VERTEXT_STRIDE = COORDS_PER_VERTEX * SIZE_FLOAT; // 4 bytes per vertex
    private static final int CACHE_SIZE = 2;

    //handle
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mPointSizeHandle;
    //顶点坐标buffer
    private   FloatBuffer mVertexBuffer;
    //点坐标
    private ArrayObject mVertexCoords;
    //点大小
    protected float mPointSize = 18.0f;
    //点的的个数
    //protected int vertexCount;
    //private int vertexCount = mVertexCoords.length / COORDS_PER_VERTEX;
    //点的颜色
    protected float mPointsClolr[] = { 0.63671875f, 0.0f, 0.0f, 1.0f };

    private static float[] originMtx;
    private static float[] flipMtx;
    static {
        originMtx = GlUtil.IDENTITY_MATRIX;
        flipMtx = Arrays.copyOf(originMtx, originMtx.length);
    }


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public PointDrawer() {
        mVertexCoords = new ArrayObject(null,CACHE_SIZE);
        // prepare shaders and OpenGL program
        int vertexShader = GlUtil.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GlUtil.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }


    /**
     * 更新顶点坐标
     * @param vertextCoords
     */
    public void updateVertex(float[] vertextCoords) {
        //如果长度有变化先跟新buffer长度
        if (mVertexBuffer == null || mVertexCoords.datas() == null || vertextCoords.length != mVertexCoords.length()){
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(vertextCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());
            // create a floating point buffer from the ByteBuffer
            mVertexBuffer = bb.asFloatBuffer();
        }
        //这里注意用哪种方式赋值
        //mVertexCoords.setDatas(vertextCoords);
        mVertexCoords.updateData(vertextCoords);
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(vertextCoords);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);
    }

    /**
     * 设置点的尺寸
     * @param pointSize px
     */
    public void setPointSize(float pointSize) {
        mPointSize = pointSize;
    }

    /**
     * 设置点的颜色
     * @param color
     */
    public void setPointColor(float[] color){
        mPointsClolr = color;
    }

    public ArrayObject getVertextCoods(){
        return mVertexCoords;
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    @Override
    public synchronized void draw() {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEXT_STRIDE, mVertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mPointsClolr, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtil.checkGlError("glGetUniformLocation");

        mPointSizeHandle = GLES20.glGetUniformLocation(mProgram, "uPointSize");
        GlUtil.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, originMtx, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        GLES20.glUniform1f(mPointSizeHandle, mPointSize);
        GlUtil.checkGlError("glUniform1f");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexCoords.length() / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


}
