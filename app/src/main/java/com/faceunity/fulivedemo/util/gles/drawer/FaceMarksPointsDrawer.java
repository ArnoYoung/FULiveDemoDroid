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
package com.faceunity.fulivedemo.util.gles.drawer;

import android.util.Log;

import com.faceunity.fulivedemo.util.gles.utils.ArrayObject;
import com.faceunity.fulivedemo.util.gles.utils.PointDrawer;
import com.faceunity.wrapper.faceunity;

/**
 *
 */
public class FaceMarksPointsDrawer extends PointDrawer{

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public FaceMarksPointsDrawer() {
        setPointSize(6.0f);
        setPointColor(new float[]{ 0.63671875f, 0.76953125f, 0.22265625f, 1.0f });
    }

    public void refresh(float[] landmarksData, int fullWidthY, int fullHeightX, float topClipRatio, float heightClipRatio, boolean isFlip) {
        float[] pointsCoords = getVertextCoods().updateCache(0,landmarksData);
        for (int i = 0; i < 150; i++) pointsCoords[i] = landmarksData[i];
        //adjust to get the coords
        for (int i = 0; i < landmarksData.length; i += 2) {
            float x, y;
            x = (isFlip ? (fullWidthY - pointsCoords[i]) : pointsCoords[i]) / fullWidthY;
            y = (pointsCoords[i + 1]) / fullHeightX;

            //adjust corresponds to clip to camera preview and show only top left (0.4, 0.4 * 0.8)
            x = (x - topClipRatio) / heightClipRatio;
            x = x * 0.64f + 0.36f;
            y = y * 0.8f + 0.2f;

            pointsCoords[i] = -y * 1.0f;
            pointsCoords[i + 1] = x * 1.0f;
        }
        updateVertex(pointsCoords);
    }


    public float[] refreshFulll(float[] landmarksData, int fullWidth, int fullHeight,boolean isFlip) {
        float[] pointsCoords = getVertextCoods().updateCache(0,landmarksData);
        for (int i = 0; i < landmarksData.length; i += 2) {
            float y, x;
            y = (isFlip ? (fullWidth - pointsCoords[i]) : pointsCoords[i]) / fullWidth;
            x = (pointsCoords[i + 1]) / fullHeight;

            //adjust corresponds to clip to camera preview and show only top left (0.4, 0.4 * 0.8)
            //y = (y - topClipRatio) / heightClipRatio;
            y = -1 + 2 * y;
            x = 1 - 2 * x;

            pointsCoords[i] =  x;
            pointsCoords[i + 1] = y;
        }
        updateVertex(pointsCoords);
        //打印点位置
        print(landmarksData, pointsCoords);
        return pointsCoords;
    }

    /**
     * 获取脸部轮廓线顶点坐标
     * @return
     */
    public float[] getFaceOutline() {
        float[] pointsCoords = getVertextCoods().datas();
        float[] faces = new float[22 * 2];
        for (int i = 0;i<faces.length - 6;i++){
            faces[i] = pointsCoords[i];
        }
        float dx = pointsCoords[148] - pointsCoords[146];
        dx = dx/4;
        faces[31] = faces[31] + 2 * dx / 3;
        faces[33] = faces[33] + 3 * dx / 4;
        faces[35] = faces[35] + dx;

        faces[36] = pointsCoords[46];
        faces[37] = pointsCoords[47] + dx ;
        faces[38] = pointsCoords[44];
        faces[39] = pointsCoords[45] + 3 * dx / 4;
        faces[40] = pointsCoords[42];
        faces[41] = pointsCoords[43] + 2 * dx / 3;

//        faces[40] = faces[38];
//        faces[41] = faces[39];


        faces[faces.length - 2] = pointsCoords[39 * 2];
        faces[faces.length - 1 ] = pointsCoords[39 * 2 + 1];
        return faces;
    }

    ArrayObject mFaceRotation = new ArrayObject(null,2);
    private float[] getFaceRotation(){
        mFaceRotation.updateCache(4);
        faceunity.fuGetFaceInfo(0, "face_rect", mFaceRotation.cache());
        return mFaceRotation.cache();
    }

    int i;
    /**
     * 打印脸部元数据和转换后的顶点坐标数据
     * @param landmarksData
     * @param pointsCoords
     */
    private void print(float[] landmarksData, float[] pointsCoords) {
        i ++;
        if (i > 25){
            StringBuilder builder = new StringBuilder();
            i = 0;
            for (float f:pointsCoords){
                builder.append(f + " ;");
            }

            builder.append("\n");
            for (float f:landmarksData){
                builder.append(f + " ;");
            }
            Log.d("@facerect",builder.toString());

            StringBuilder strRotation = new StringBuilder();
            float[] rotations = getFaceRotation();
            strRotation.append("\n");
            for (float f:rotations){
                strRotation.append(f + " ;");
            }
            Log.d("@facerotation",strRotation.toString());

        }
    }
}
