package com.faceunity.fulivedemo.biz;

import android.content.Context;

/**
 * Created by arno on 2017/9/12.
 */

public class FuPst implements IFuView,IFuView.ILifeCycle{
    public FuManager getFuManager() {
        return mFuManager;
    }

    FuManager mFuManager;
    FuModule mFuModule;
    public FuPst(Context context) {
        mFuManager = new FuManager(context);
        mFuModule = mFuManager.getModule();
    }

    @Override
    public void onBlurLevelSelected(int level) {
        mFuModule.mFaceBeautyBlurLevel = level;
    }

    @Override
    public void onCheekThinSelected(int progress, int max) {
        mFuModule.mFaceBeautyCheekThin = 1.0f * progress / max;
    }

    @Override
    public void onColorLevelSelected(int progress, int max) {
        mFuModule.mFaceBeautyColorLevel = 1.0f * progress / max;
    }

    @Override
    public void onEffectItemSelected(String effectItemName) {
        mFuManager.setEffectItem(effectItemName);
    }

    @Override
    public void onEnlargeEyeSelected(int progress, int max) {
        mFuModule.mFaceBeautyEnlargeEye = 1.0f * progress / max;
    }

    @Override
    public void onFilterSelected(String filterName) {
        mFuModule.mFilterName = filterName;
    }

    @Override
    public void onRedLevelSelected(int progress, int max) {
        mFuModule.mFaceBeautyRedLevel = 1.0f * progress / max;
    }


    @Override
    public void onFaceShapeLevelSelected(int progress, int max) {
        mFuModule.mFaceShapeLevel = (1.0f * progress) / max;
    }

    @Override
    public void onFaceShapeSelected(int faceShape) {
        mFuModule.mFaceShape = faceShape;
    }


    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        mFuManager.removeMessage();
        mFuManager.getModule().mEffectItem = 0;
        mFuManager.getModule().mFaceBeautyItem = 0;
    }

    @Override
    public void onDestroy() {
        mFuManager.quite();
    }
}
