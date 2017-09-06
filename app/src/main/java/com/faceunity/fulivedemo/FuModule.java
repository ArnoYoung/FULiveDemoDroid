package com.faceunity.fulivedemo;

/**
 * Created by arno on 2017/9/5.
 */

public class FuModule {

    int mFaceBeautyItem = 0; //美颜道具
    int mEffectItem = 0; //贴纸道具
    int mGestureItem = 0; //手势道具
    //int[] itemsArray = {mFaceBeautyItem, mEffectItem, mGestureItem};
    String mFilterName;
    float mFaceBeautyColorLevel = 0.2f;
    float mFaceBeautyBlurLevel = 6.0f;
    float mFaceBeautyCheekThin = 1.0f;
    float mFaceBeautyEnlargeEye = 0.5f;
    float mFaceBeautyRedLevel = 0.5f;
    int mFaceShape = 3;
    float mFaceShapeLevel = 0.5f;

    public int[] itemArray(){
        return  new int[]{mFaceBeautyItem, mEffectItem, mGestureItem};
    }
}
