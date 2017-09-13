package com.faceunity.fulivedemo.view;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.faceunity.fulivedemo.biz.FuCamera;
import com.faceunity.fulivedemo.biz.FuManager;
import com.faceunity.fulivedemo.R;
import com.faceunity.fulivedemo.biz.FuPst;
import com.faceunity.fulivedemo.biz.FuRender;
import com.faceunity.fulivedemo.common.Consts;
import com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder;
import com.faceunity.wrapper.faceunity;


/**
 * 这个Activity演示了从Camera取数据,用fuDualInputToTexure处理并预览展示
 * 所谓dual input，指从cpu和gpu同时拿数据，
 * cpu拿到的是nv21的byte数组，gpu拿到的是对应的texture
 * <p>
 * Created by lirui on 2016/12/13.
 */


public class TestActivity extends FUBaseUIActivity
        implements Camera.PreviewCallback {

    final static String TAG = "FUDualInputToTextureEg";
    final int DEFAULT_CAMERA_DIRECTION = Camera.CameraInfo.CAMERA_FACING_FRONT;
    final int DEFAULT_CAMERA_WIDITH = 1280;
    final int DEFAULT_CAMERA_HEIGHT = 720;
    FuCamera mFuCamera;
    GLSurfaceView mGLSurfaceView;
    FuRender mGLRenderer;
    //FuManager mFuControler;
    //记录pause 状态
    boolean isInPause = false;
    //李小龙显示脸部点阵
    boolean isInAvatarMode = false;
    FuPst mFuPst;

    private TextureMovieEncoder.OnEncoderStatusUpdateListener mRecordListener;

    private class RenderMsg implements FuRender.IRenderMsg {

        @Override
        public void onFrameTracking(final boolean isTracking) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isTracking) {
                        mFaceTrackingStatusImageView.setVisibility(View.VISIBLE);

                    } else {
                        mFaceTrackingStatusImageView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        @Override
        public void handleCameraStartPreview() {
            mFuCamera.handleCameraStartPreview(mGLRenderer.getCameraSurfaceTexture());
            mFuCamera.setPreviewCallback(TestActivity.this);
        }
    }

    private class RecordListener implements TextureMovieEncoder.OnEncoderStatusUpdateListener{
        @Override
        public void onStartSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Log.e(TAG, "start encoder success");
                mRecordingBtn.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onStopSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Log.e(TAG, "stop encoder success");
                mRecordingBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mFuPst = new FuPst(this);
        //mMainHandler = new MainHandler(this);

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsv);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mGLRenderer = new FuRender(this,mFuPst.getFuManager(), mGLSurfaceView);
        mGLRenderer.setIRenderMsg(new RenderMsg());
        mGLSurfaceView.setRenderer(mGLRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mFuCamera = new FuCamera(this);
        mGLRenderer.setRecordListener(new RecordListener());

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        setInPause(false);
        super.onResume();
        mFuCamera.openCamera(DEFAULT_CAMERA_DIRECTION,DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        mGLRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
        /**
         * 请注意这个地方, camera返回的图像并不一定是设置的大小（因为可能并不支持）
         */
        Camera.Size size = mFuCamera.getCamera().getParameters().getPreviewSize();

        mGLRenderer.setCameraSize(size.width, size.height);
        AspectFrameLayout aspectFrameLayout = (AspectFrameLayout) findViewById(R.id.afl);
        aspectFrameLayout.setAspectRatio(1.0f * size.height / size.width);
        mGLSurfaceView.onResume();
        Log.i(TAG, "open camera size width : " + size.width + " height : " + size.height);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        setInPause(true);
        super.onPause();

        mFuCamera.releaseCamera();
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mGLRenderer.notifyPause();
                mGLRenderer.destroySurfaceTexture();
                mFuPst.onPause();
                //Note: 切忌使用一个已经destroy的item
                faceunity.fuDestroyAllItems();
                //glRenderer.setNeedEffectItem(true);
                faceunity.fuOnDeviceLost();
                mGLRenderer.clearFrameId();
            }
        });
        mGLSurfaceView.onPause();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (Consts.VERBOSE_LOG) {
            Log.i(TAG, "onPreviewFrame len " + data.length);
            Log.i(TAG, "onPreviewThread " + Thread.currentThread());
        }

        mGLRenderer.setCameraNV21Byte(isInPause ? null : data);

    }

    public int getCameraFacingDriection() {
        return mFuCamera.getCameraFacingDriection();
    }


    public void setInPause(boolean inPause) {
        isInPause = inPause;
        mGLRenderer.setInPause(inPause);
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
        isInAvatarMode = effectItemName.equals("lixiaolong.bundle");
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
        Log.i(TAG, "onCameraChange");
        mGLRenderer.setNeedSwitchCameraSurfaceTexture(true);
        mFuCamera.releaseCamera();
        mGLRenderer.setCameraNV21Byte(null);
        mGLRenderer.clearFrameId();
        mFuCamera.openCamera(mFuCamera.getCameraFacingDriectionReversal(),DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        mGLRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
    }

    @Override
    protected void onStartRecording() {
        mGLRenderer.startRecording();
    }

    @Override
    protected void onStopRecording() {
        mGLRenderer.stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
