package com.faceunity.fulivedemo.gles.drawer;

/**
 * Created by lirui on 2017/4/10.
 */

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.faceunity.fulivedemo.gles.utils.GlUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 */
public class BacFaceClipFrameRect {

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

    /**
     * A "full" square, extending from -1 to +1 in both dimensions.  When the model/view/projection
     * matrix is identity, this will exactly cover the viewport.
     * <p>
     * The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out
     * right with external textures from SurfaceTexture.)
     */
    private static  float mVertexCoords[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };

    static float clipTop = 0.1f;
    static float clipBottom = 0.9f;

    private static  float mTextureCoords[] = {
            0.0f, clipTop,     // 0 bottom left
            1.0f, clipTop,     // 1 bottom right
            0.0f, clipBottom,     // 2 top left
            1.0f, clipBottom      // 3 top right
    };

    // 绘制顶点的顺序
    private short mDrawOrder[] = {0, 2, 1, 0, 3, 2};
    private  FloatBuffer mVertextBuf;
    private  FloatBuffer mTextureBuf;
    private  ShortBuffer mOrderBuf;
    private float[] mMVPMatrix = new float[16];
    //private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
    //private Texture2dProgram mProgram;

    // Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    //private FloatBuffer mVertexArray;
    //private FloatBuffer mTexCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;
    private int mTexCoordStride;

    private static final int SIZEOF_FLOAT = 4;

    private static final String TAG = "CameraClipFrameRect";

    /**
     * Prepares the object.
     */
    public BacFaceClipFrameRect() {
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

        /**
         * private static final float FULL_RECTANGLE_COORDS[] = {
         -1.0f, -1.0f,   // 0 bottom left
         1.0f, -1.0f,   // 1 bottom right
         -1.0f, 1.0f,   // 2 top left
         1.0f, 1.0f,   // 3 top right
         };
         */

        mVertextBuf = GlUtil.createFloatBuffer(mVertexCoords);
        mTextureBuf = GlUtil.createFloatBuffer(mTextureCoords);
        mOrderBuf = GlUtil.createShotBuffer(mDrawOrder);

        mCoordsPerVertex = 2;
        mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
        mVertexCount = mVertexCoords.length / mCoordsPerVertex;

        Matrix.setIdentityM(mMVPMatrix,0);
        //Matrix.translateM(mMVPMatrix,0,0.3f,0.3f,0);
    }

    /**
     * 以左上角为原点 缩小
     * @param xRadio
     * @param yRadio
     */
    public void initVertexRadio(float xRadio, float yRadio) {
        //right, full screen x
        mVertexCoords[2] = mVertexCoords[6] = -1.0f + 2 * xRadio;
        //bottom full screen y
        mVertexCoords[1] = mVertexCoords[3] = 1.0f - 2 * yRadio;
        //FULL_RECTANGLE_VERTEXT_BUF.clear();
        mVertextBuf.put(mVertexCoords);
        mVertextBuf.position(0);
    }

    public void refreshTextureCoords(float[] textureCoords){
        if (mTextureCoords.length != textureCoords.length){
            mTextureCoords = textureCoords;
            mTextureBuf = GlUtil.createFloatBuffer(textureCoords);
        }else {
            mTextureCoords = textureCoords;
            mTextureBuf.put(mTextureCoords);
            mTextureBuf.position(0);
        }

    }

    public void refreshVertex(float[] vertex){
        if (vertex.length != mVertexCoords.length){
            mVertexCoords = vertex;
            mVertextBuf = GlUtil.createFloatBuffer(vertex);
        }else {
            mVertexCoords = vertex;
            mVertextBuf.put(mVertexCoords);
            mVertextBuf.position(0);
        }

    }

    //中间产物
    private float[] tempCoords1 = new float[8];
    private float[] tempCoords2 = new float[8];

    /**
     * @param data 抠图的顶点坐标
     */
    public void refreshClipTextures(float[] data){
        //中间数组
        if (tempCoords1.length != data.length)
            tempCoords1 = new float[data.length];
        if (tempCoords2.length != data.length)
            tempCoords2 = new float[data.length];
        //转换为抠图的纹理坐标
        for (int i = 0;i < tempCoords2.length;i+=2){
            tempCoords2[i] = (data[i] + 1f)/2f;
            tempCoords2[i + 1] = (data[i + 1] + 1)/2f;
        }

//        tempCoords1[0]  = tempCoords2[2];
//        tempCoords1[1]  = tempCoords2[3];
//
//        tempCoords1[2]  = tempCoords2[4];
//        tempCoords1[3]  = tempCoords2[5];
//
//        tempCoords1[4]  = tempCoords2[0];
//        tempCoords1[5]  = tempCoords2[1];
//
//        tempCoords1[6]  = tempCoords2[6];
//        tempCoords1[7]  = tempCoords2[7];
        tempCoords1[0]  = tempCoords2[0];
        tempCoords1[1]  = tempCoords2[1];

        tempCoords1[2]  = tempCoords2[2];
        tempCoords1[3]  = tempCoords2[3];

        tempCoords1[4]  = tempCoords2[4];
        tempCoords1[5]  = tempCoords2[5];

        tempCoords1[6]  = tempCoords2[6];
        tempCoords1[7]  = tempCoords2[7];
        refreshTextureCoords(tempCoords1);
//
//        tempCoords2[0]  = data[2];
//        tempCoords2[1]  = data[3];
//
//        tempCoords2[2]  = data[4];
//        tempCoords2[3]  = data[5];
//
//        tempCoords2[4]  = data[0];
//        tempCoords2[5]  = data[1];
//
//        tempCoords2[6]  = data[6];
//        tempCoords2[7]  = data[7];

        tempCoords1[0]  = tempCoords1[0] -1;
        tempCoords1[2]  = tempCoords1[2] -1;
        tempCoords1[4]  = tempCoords1[4] -1;
        tempCoords1[6]  = tempCoords1[6] -1;
        refreshVertex(tempCoords1);
    }

    /**
     * 更新脸部显示顶点和抠图纹理
     * @param data 抠图的顶点坐标
     */
    public void refreshClipTextures2(float[] data){
        //中间数组
        if (tempCoords1.length != data.length)
            tempCoords1 = new float[data.length];
        if (tempCoords2.length != data.length)
            tempCoords2 = new float[data.length];
        //转换为抠图的纹理坐标
        for (int i = 0;i < tempCoords2.length;i+=2){
            tempCoords2[i] = (data[i] + 1f)/2f;
            tempCoords2[i + 1] = (data[i + 1] + 1)/2f;
        }

//        tempCoords1[0]  = tempCoords2[2];
//        tempCoords1[1]  = tempCoords2[3];
//
//        tempCoords1[2]  = tempCoords2[4];
//        tempCoords1[3]  = tempCoords2[5];
//
//        tempCoords1[4]  = tempCoords2[0];
//        tempCoords1[5]  = tempCoords2[1];
//
//        tempCoords1[6]  = tempCoords2[6];
//        tempCoords1[7]  = tempCoords2[7];

//        tempCoords1[0]  = tempCoords2[0];
//        tempCoords1[1]  = tempCoords2[1];
//
//        tempCoords1[2]  = tempCoords2[2];
//        tempCoords1[3]  = tempCoords2[3];
//
//        tempCoords1[4]  = tempCoords2[4];
//        tempCoords1[5]  = tempCoords2[5];
//
//        tempCoords1[6]  = tempCoords2[6];
//        tempCoords1[7]  = tempCoords2[7];
//
//        float[] temps = new float[tempCoords2.length - 2];
//        for (int i =  0;i<temps.length;i++){
//            temps[i] = tempCoords2[i];
//        }
        refreshTextureCoords(tempCoords2);
//
//        tempCoords2[0]  = data[2];
//        tempCoords2[1]  = data[3];
//
//        tempCoords2[2]  = data[4];
//        tempCoords2[3]  = data[5];
//
//        tempCoords2[4]  = data[0];
//        tempCoords2[5]  = data[1];
//
//        tempCoords2[6]  = data[6];
//        tempCoords2[7]  = data[7];


//        tempCoords1[0]  = tempCoords1[0] -1;
//        tempCoords1[2]  = tempCoords1[2] -1;
//        tempCoords1[4]  = tempCoords1[4] -1;
//        tempCoords1[6]  = tempCoords1[6] -1;
        refreshVertex(tempCoords2);

        refreshOrder(tempCoords2);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void refreshOrder(float[] coords){
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

    /**
     * Releases resources.
     * <p>
     * This must be called with the appropriate EGL context current (i.e. the one that was
     * current when the constructor was called).  If we're about to destroy the EGL context,
     * there's no value in having the caller make it current just to do this cleanup, so you
     * can pass a flag that will tell this function to skip any EGL-context-specific cleanup.
     */
    public void release(boolean doEglCleanup) {
        /*if (mProgram != null) {
            if (doEglCleanup) {
                mProgram.release();
            }
            mProgram = null;
        }*/
    }

    /**
     * Draws a viewport-filling rect, texturing it with the specified texture object.
     */
    public void drawFrame(int textureId, float[] texMatrix) {
        //Matrix.setIdentityM(texMatrix,0);
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, mVertextBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, mTexCoordStride, mTextureBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GlUtil.checkGlError("glDrawArrays");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mOrderBuf);
        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
    }
}
