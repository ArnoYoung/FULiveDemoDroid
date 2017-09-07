/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faceunity.fulivedemo.gles.drawer;

import android.util.Log;
import android.view.MotionEvent;

import com.faceunity.fulivedemo.gles.utils.PointDrawer;

import java.util.Arrays;

/**
 *
 */
public class FaceRectPointsDrawer extends PointDrawer{

    public FaceRectPointsDrawer() {

    }

    /**
     * 脸的矩形框绘制在左上角
     * @param faceData
     * @param fullWidth 视频流宽度 对应y轴
     * @param fullHeight 视频流的高度 对应x轴
     * @param topClipRatio 对应左上角视频窗口
     * @param heightClipRatio 对应左上角视频窗口
     * @param isFlip 是否反转
     */
    public synchronized void refresh(float[] faceData, int fullWidth, int fullHeight, float topClipRatio, float heightClipRatio, boolean isFlip) {

        float[] vertexCoordsCache = genFacePoints(faceData,fullWidth,fullHeight);
        //adjust to get the coords
        for (int i = 0; i < vertexCoordsCache.length; i += 2) {
            float x, y;
            x = (isFlip ? (fullWidth - vertexCoordsCache[i]) : vertexCoordsCache[i]) / fullWidth;
            y = (vertexCoordsCache[i + 1]) / fullHeight;

            //adjust corresponds to clip to camera preview and show only top left (0.4, 0.4 * 0.8)
            x = (x - topClipRatio) / heightClipRatio;
            x = x * 0.64f + 0.36f;
            y = y * 0.8f + 0.2f;

            vertexCoordsCache[i] = -y * 1.0f;
            vertexCoordsCache[i + 1] = x * 1.0f;
        }
        updateVertex(vertexCoordsCache);
        print(vertexCoordsCache);
    }

    /**
     * 对应全屏脸部矩形框
     * @param faceData 脸部矩形框四子节
     * @param fullWidth 视频流宽度 对应y轴
     * @param fullHeight 视频流的高度 对应x轴
     * @param isFlip 是否反转
     */
    public synchronized float[] refreshFulll(float[] faceData, int fullWidth, int fullHeight, boolean isFlip) {
        float[] pointsCoords = genFacePoints(faceData,fullWidth,fullHeight);
        //adjust to get the coords
        for (int i = 0; i < pointsCoords.length; i += 2) {
            float y, x;
            y = (isFlip ? (fullWidth - pointsCoords[i]) : pointsCoords[i]) / fullWidth;
            x = (pointsCoords[i + 1]) / fullHeight;
            y = -1 + 2 * y;
            x = 1 - 2 * x;

            pointsCoords[i] =  x;
            pointsCoords[i + 1] = y;
        }
        updateVertex(pointsCoords);
        print(pointsCoords);
        return pointsCoords;

    }

    /**
     * 将脸部矩形框数据转换为四点的坐标
     * @param datas
     * @param fullWidth
     * @param fullHeight
     * @return
     */
    private float[] genFacePoints(float[] datas,float fullWidth,float fullHeight){
        float[] rt = getVertextCoods().updateCache(0,8);
        float xMin = datas[0];
        float yMax = datas[3];
        float xMax = datas[2];
        float yMin = datas[1];

        rt[0] = xMin;
        rt[1] = yMin;

        rt[2] = xMax;
        rt[3] = yMin;

        rt[4] = xMax;
        rt[5] = yMax;

        rt[6] = xMin;
        rt[7] = yMax;

        for (int j= 0;j<rt.length;j++){
            if (j%2 == 0){
                rt[j] = fullWidth - rt[j];
            }else {
                rt[j] = fullHeight - rt[j];
            }
        }
        return rt;
    }

    int i = 0;
    private void print(float[] landmarksData) {
        //打印点位置
        i ++;
        if (i > 20){
            StringBuilder builder = new StringBuilder();
            i = 0;
            for (float f: getVertextCoods().datas()){
                builder.append(f + " ;");
            }

            builder.append("\n");
            for (float f:landmarksData){
                builder.append(f + " ;");
            }
            Log.d("@facerect",builder.toString());
        }
    }


}
