package com.faceunity.fulivedemo.biz;

/**
 * Created by arno on 2017/9/5.
 */

public class FuModule {

    public int mFaceBeautyItem = 0; //美颜道具
    public int mEffectItem = 0; //贴纸道具
    public int mGestureItem = 0; //手势道具
    //int[] itemsArray = {mFaceBeautyItem, mEffectItem, mGestureItem};
    public String mFilterName = "";
    public float mFaceBeautyColorLevel = 0.2f;
    public float mFaceBeautyBlurLevel = 6.0f;
    public float mFaceBeautyCheekThin = 1.0f;
    public float mFaceBeautyEnlargeEye = 0.5f;
    public float mFaceBeautyRedLevel = 0.5f;
    public int mFaceShape = 3;
    public float mFaceShapeLevel = 0.5f;

    public int[] itemArray(){
        return  new int[]{mFaceBeautyItem, mEffectItem, mGestureItem};
    }

    public static class EffectItem{
        public int effectItem =  0;
        public String netUrl ;
        public String nativeUrl;
        public String assetUrl;
    }
}
