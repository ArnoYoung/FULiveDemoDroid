package com.faceunity.fulivedemo.gles.utils;

/**
 * Created by lirui on 2017/4/10.
 */

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.sql.Driver;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 */
public class BlockDrawer implements IDrawer {

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = vec4(texture2D(sTexture, vTextureCoord).rgb, 1.0);\n" +
                    "}\n";
    private static final String TAG = "CameraClipFrameRect";
    private static final int COORDS_PER_VERTEX = 2;
    private static final int SIZEOF_FLOAT = 4;
    private static final int VERTEXT_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT; // 4 bytes per vertex
    private static final int TEXTURE_STRIDE = 0;
    private static final int CACHE_SIZE = 2;
    //顶点坐标
    private ArrayObject mVertexCoords;
    //纹理坐标
    private ArrayObject mTextureCoords;
    // 绘制顶点的顺序
    private short[] mDrawOrder;
    private  FloatBuffer mVertextBuf;
    private  FloatBuffer mTextureBuf;
    private  ShortBuffer mOrderBuf;
    private float[] mMVPMatrix = new float[16];

    //Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    private final int mTextureId;
    private float[] mTextureMatrix;
    /**
     * Prepares the object.
     */
    public BlockDrawer(int textureId) {
        mTextureId = textureId;
        initData();
        initGl();

    }

    private void initData() {
        mTextureMatrix = new float[16];
        Matrix.setIdentityM(mMVPMatrix,0);
        mVertexCoords = new ArrayObject(new float[]{
                -1.0f, -1.0f,   // 0 bottom left
                1.0f, -1.0f,   // 1 bottom right
                -1.0f, 1.0f,   // 2 top left
                1.0f, 1.0f,   // 3 top right
        },CACHE_SIZE);
        //全屏
        mTextureCoords = new ArrayObject(new float[]{
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
        },CACHE_SIZE);
        //绘制顺序
        mDrawOrder = new short[]{0, 2, 1, 0, 3, 2};

        mVertextBuf = GlUtil.createFloatBuffer(mVertexCoords.datas());
        mTextureBuf = GlUtil.createFloatBuffer(mTextureCoords.datas());
        mOrderBuf = GlUtil.createShotBuffer(mDrawOrder);

    }

    private void initGl() {
        // prepare shaders and OpenGL program
        int vertexShader = GlUtil.loadShader(
                GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = GlUtil.loadShader(
                GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_EXT);

        mProgramHandle = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgramHandle, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgramHandle, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgramHandle);                  // create OpenGL program executables

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
    }

    /**
     * 更新顶点坐标
     * @param vertextCoords
     */
    public void updateVertex(float[] vertextCoords) {
        //如果长度有变化先跟新buffer长度
        if (mVertextBuf == null || mVertexCoords.datas() == null || vertextCoords.length != mVertexCoords.length()){
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(vertextCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());
            // create a floating point buffer from the ByteBuffer
            mVertextBuf = bb.asFloatBuffer();
        }
        //这里注意用哪种方式赋值
        //mVertexCoords.setDatas(vertextCoords);
        mVertexCoords.updateData(vertextCoords);
        // add the coordinates to the FloatBuffer
        mVertextBuf.put(vertextCoords);
        // set the buffer to read the first coordinate
        mVertextBuf.position(0);
    }

    public ArrayObject getTextureCoords(){
        return mTextureCoords;
    }

    public ArrayObject getVertexCoords(){
        return mVertexCoords;
    }
    /**
     * 更新顶点坐标
     * @param textureCoords
     */
    public void updateTexture(float[] textureCoords) {
        //如果长度有变化先跟新buffer长度
        if (mTextureBuf == null || mTextureCoords.datas() == null || textureCoords.length != mTextureCoords.length()){
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(textureCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());
            // create a floating point buffer from the ByteBuffer
            mTextureBuf = bb.asFloatBuffer();
        }
        //这里注意用哪种方式赋值
        //mVertexCoords.setDatas(vertextCoords);
        mTextureCoords.updateData(textureCoords);
        // add the coordinates to the FloatBuffer
        mTextureBuf.put(textureCoords);
        // set the buffer to read the first coordinate
        mTextureBuf.position(0);
    }


    /**
     * 更新绘制顺序
     * @param coords
     */
    public void updateOrders(float[] coords){
        int tgNum = coords.length/2 -1;
        if (mDrawOrder.length != tgNum * 3 ){
            short coreIndex = (short) (coords.length/2-1);
            mDrawOrder = new short[tgNum * 3];

            for (short i = 0; i< tgNum - 1;i ++){
                mDrawOrder[i*3 + 2] = (short) (i + 1);
                mDrawOrder[i*3 + 1] = i;
                mDrawOrder[i*3] = coreIndex;
            }
            mDrawOrder[mDrawOrder.length - 3] = 0;
            mDrawOrder[mDrawOrder.length - 2] = (short) (coreIndex - 1);
            mDrawOrder[mDrawOrder.length - 3] = coreIndex;
            mOrderBuf = GlUtil.createShotBuffer(mDrawOrder);
        }
    }

    public void draw(float[] m){
        mTextureMatrix = m;
        draw();
    }

    @Override
    public void draw() {
        //Matrix.setIdentityM(texMatrix,0);
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mTextureMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, VERTEXT_STRIDE, mVertextBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, TEXTURE_STRIDE, mTextureBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCoords.length() / COORDS_PER_VERTEX);
        GlUtil.checkGlError("glDrawArrays");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mOrderBuf);
        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
    }
}
