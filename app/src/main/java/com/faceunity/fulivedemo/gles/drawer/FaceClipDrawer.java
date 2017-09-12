package com.faceunity.fulivedemo.gles.drawer;

/**
 * Created by lirui on 2017/4/10.
 */

import android.opengl.Matrix;
import android.util.Log;

import com.faceunity.fulivedemo.gles.utils.ArrayObject;
import com.faceunity.fulivedemo.gles.utils.BlockDrawer;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 */
public class FaceClipDrawer extends BlockDrawer{
    private static final int CST_POINT_BOTTOM = 8;
    ArrayObject mProjectionMatrix = new ArrayObject(new float[16]);
    ArrayObject mViewMatrix = new ArrayObject(new float[16]);
    float mWHRatio = 1;
    private static final int CACHE_VERTEXT_MTX_CACHE = 0;
    private static final int CACHE_VERTEXT_COORDS_CACHE0 = 0;
    private static final int CACHE_VERTEXT_COORDS_CACHE1 = 1;
    /**
     * Prepares the object.
     */
    public FaceClipDrawer(int textureId) {
        super(textureId);
        initViewMetrix();
    }

    /**
     * 更新脸部显示顶点和抠图纹理
     * @param data 抠图的顶点坐标
     */
    public synchronized void refreshClipTextures2(float[] data){
        float[] textureCoords = getVertexCoords().updateCache(CACHE_VERTEXT_COORDS_CACHE0,data.length);
        //转换为抠图的纹理坐标
        for (int i = 0;i < textureCoords.length;i+=2){
            textureCoords[i] = (data[i] + 1f)/2f;
            textureCoords[i + 1] = (data[i + 1] + 1)/2f;
        }
        updateTexture(textureCoords);
        //updateVertex(tempCoords);
        updateVertex(data);
        updateOrders(textureCoords);
        updateVertextMvp(data);
    }

    public void updateVertex(float[] datas){
        float[] cache2 = getVertexCoords().updateCache(CACHE_VERTEXT_COORDS_CACHE1,datas);
        float dx = datas[datas.length - 2] / 2;
        float dy = datas[datas.length - 1];

        for (int j = 0; j < datas.length; j+=2) {
            cache2[j] = datas[j] * mWHRatio;
            cache2[j] -= dx;
            cache2[j + 1] -= dy;
        }

        super.updateVertex(cache2);
    }

    //long rotation = 0;
    private void updateVertextMvp(float[] datas){
//        double xc = datas[datas.length - 2];
//        double yc = datas[datas.length - 1];
//        double xb = datas[2 * CST_POINT_BOTTOM - 2];
//        double yb = datas[2 * CST_POINT_BOTTOM - 1];
//
//        double r = (xb - xc)/(yb - yc);

        double xc = datas[0];
        double yc = datas[1];
        double xb = datas[28];
        double yb = datas[29];

        double r = - (yb - yc)/(xb - xc) * 1.2;

        double rt = (float) (Math.atan(r)/Math.PI) * 180;
        Log.i("rotation","rotation:" + r + ":" + rt);
        float[] mtxCache = getVertextMartrix().updateCache(CACHE_VERTEXT_MTX_CACHE,16);
        Matrix.multiplyMM(mtxCache, 0, mProjectionMatrix.datas(), 0, mViewMatrix.datas(), 0);
        Matrix.translateM(mtxCache,0,-0.5f * mWHRatio,0.5f,0);
        Matrix.scaleM(mtxCache,0,0.5f,0.5f,1f);
        Matrix.rotateM(mtxCache,0, (float) rt,0,0,1f);
        getVertextMartrix().updateData(mtxCache);




    }
    private void initViewMetrix(){
        Matrix.setLookAtM(mViewMatrix.datas(), 0,  0f, 0f, -3,  0f, 0f, 0f,  0f, 1.0f, 0.0f);
    }

    public void setProjectionMatrix(float whRatio){
        mWHRatio = whRatio;
        Matrix.frustumM(mProjectionMatrix.datas(),0,whRatio,-whRatio,-1,1,3,7);
    }



    //    /**
//     * 以左上角为原点 缩小
//     * @param xRadio
//     * @param yRadio
//     */
//    public void initVertexRadio(float xRadio, float yRadio) {
//        //right, full screen x
//        mVertexCoords[2] = mVertexCoords[6] = -1.0f + 2 * xRadio;
//        //bottom full screen y
//        mVertexCoords[1] = mVertexCoords[3] = 1.0f - 2 * yRadio;
//        //FULL_RECTANGLE_VERTEXT_BUF.clear();
//        mVertextBuf.put(mVertexCoords);
//        mVertextBuf.position(0);
//    }
}
