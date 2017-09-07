package com.faceunity.fulivedemo.gles.drawer;

/**
 * Created by lirui on 2017/4/10.
 */

import com.faceunity.fulivedemo.gles.utils.BlockDrawer;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 */
public class FaceClipDrawer extends BlockDrawer{
    /**
     * Prepares the object.
     */
    public FaceClipDrawer(int textureId) {
        super(textureId);
    }

    /**
     * 更新脸部显示顶点和抠图纹理
     * @param data 抠图的顶点坐标
     */
    public synchronized void refreshClipTextures2(float[] data){
        float[] tempCoords = getVertexCoords().updateCache(0,data.length);
        //转换为抠图的纹理坐标
        for (int i = 0;i < tempCoords.length;i+=2){
            tempCoords[i] = (data[i] + 1f)/2f;
            tempCoords[i + 1] = (data[i + 1] + 1)/2f;
        }
        updateTexture(tempCoords);
        updateVertex(tempCoords);
        updateOrders(tempCoords);
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
