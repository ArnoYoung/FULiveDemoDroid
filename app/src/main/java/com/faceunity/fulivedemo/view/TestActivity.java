package com.faceunity.fulivedemo.view;
import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.faceunity.fulivedemo.R;
import com.faceunity.fulivedemo.biz.FuPst;
import com.faceunity.fulivedemo.biz.IFuView;


/**
 * 这个Activity演示了从Camera取数据,用fuDualInputToTexure处理并预览展示
 * 所谓dual input，指从cpu和gpu同时拿数据，
 * cpu拿到的是nv21的byte数组，gpu拿到的是对应的texture
 * <p>
 * Created by lirui on 2016/12/13.
 */


public class TestActivity extends FUBaseUIActivity
        implements IFuView {
    FuPst mFuPst;
    GLSurfaceView mGLSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsv);
        mFuPst = new FuPst(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mFuPst.onResume();
        //camera返回的图像并不一定是设置的大小（因为可能并不支持）
        Camera.Size size = mFuPst.getCameraSize();
        AspectFrameLayout aspectFrameLayout = (AspectFrameLayout) findViewById(R.id.afl);
        aspectFrameLayout.setAspectRatio(1.0f * size.height / size.width);
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFuPst.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    public void onFrameTracking(final boolean isTracking) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFaceTrackingStatusImageView.setVisibility(isTracking? View.VISIBLE:View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRecordStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordingBtn.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onRecordStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordingBtn.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onBlurLevelSelected(int level) {
        mFuPst.onBlurLevelSelected(level);
    }

    @Override
    protected void onCheekThinSelected(int progress, int max) {
        mFuPst.onCheekThinSelected(progress,max);
    }

    @Override
    protected void onColorLevelSelected(int progress, int max) {
        mFuPst.onColorLevelSelected(progress,max);
    }

    @Override
    protected void onEffectItemSelected(String effectItemName) {
        mFuPst.onEffectItemSelected(effectItemName);
    }

    @Override
    protected void onEnlargeEyeSelected(int progress, int max) {
        mFuPst.onEnlargeEyeSelected(progress,max);
    }

    @Override
    protected void onFilterSelected(String filterName) {
        mFuPst.onFilterSelected(filterName);
    }

    @Override
    protected void onRedLevelSelected(int progress, int max) {
        mFuPst.onRedLevelSelected(progress,max);
    }


    @Override
    protected void onFaceShapeLevelSelected(int progress, int max) {
        mFuPst.onFaceShapeLevelSelected(progress,max);
    }

    @Override
    protected void onFaceShapeSelected(int faceShape) {
        mFuPst.onFaceShapeSelected(faceShape);
    }


    @Override
    protected void onCameraChange() {
        mFuPst.onCameraChange();
    }

    @Override
    protected void onStartRecording() {
        mFuPst.onStartRecording();
    }

    @Override
    protected void onStopRecording() {
        mFuPst.onStopRecording();
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public GLSurfaceView getGlsv() {
        return mGLSurfaceView;
    }
}
