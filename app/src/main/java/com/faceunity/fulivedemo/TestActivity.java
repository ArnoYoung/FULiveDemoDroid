package com.faceunity.fulivedemo;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.faceunity.fulivedemo.encoder.TextureMovieEncoder;
import com.faceunity.wrapper.faceunity;
import static com.faceunity.fulivedemo.encoder.TextureMovieEncoder.IN_RECORDING;


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
    GLSurfaceView glSf;
    TestRender glRenderer;
    FuControler mFuControler;

    //MainHandler mMainHandler;

    TextureMovieEncoder mTextureMovieEncoder;
    //记录pause 状态
    boolean isInPause = false;
    //李小龙显示脸部点阵
    boolean isInAvatarMode = false;

    public class RenderMsg implements TestRender.IRenderMsg {

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
            mFuCamera.handleCameraStartPreview(glRenderer.getCameraSurfaceTexture());
            mFuCamera.setPreviewCallback(TestActivity.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mFuControler = new FuControler(this);
        //mMainHandler = new MainHandler(this);

        glSf = (GLSurfaceView) findViewById(R.id.glsv);
        glSf.setEGLContextClientVersion(2);

        glRenderer = new TestRender(mFuControler, glSf);
        glRenderer.setIRenderMsg(new RenderMsg());
        glSf.setRenderer(glRenderer);
        glSf.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mFuCamera = new FuCamera(this);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        setInPause(false);
        super.onResume();
        mFuCamera.openCamera(DEFAULT_CAMERA_DIRECTION,DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        glRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
        /**
         * 请注意这个地方, camera返回的图像并不一定是设置的大小（因为可能并不支持）
         */
        Camera.Size size = mFuCamera.getCamera().getParameters().getPreviewSize();

        glRenderer.setCameraSize(size.width, size.height);
        AspectFrameLayout aspectFrameLayout = (AspectFrameLayout) findViewById(R.id.afl);
        aspectFrameLayout.setAspectRatio(1.0f * size.height / size.width);
        glSf.onResume();
        mFuControler.getFu().mFilterName = EffectAndFilterSelectAdapter.FILTERS_NAME[0];
        Log.i(TAG, "open camera size width : " + size.width + " height : " + size.height);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        setInPause(true);
        super.onPause();
        mFuControler.removeMessage();
        mFuCamera.releaseCamera();
        glSf.queueEvent(new Runnable() {
            @Override
            public void run() {
                glRenderer.notifyPause();
                glRenderer.destroySurfaceTexture();
                mFuControler.getFu().mEffectItem = 0;
                mFuControler.getFu().mFaceBeautyItem = 0;
                //Note: 切忌使用一个已经destroy的item
                faceunity.fuDestroyAllItems();
                //glRenderer.setNeedEffectItem(true);
                faceunity.fuOnDeviceLost();
                glRenderer.setFrameId(0);
            }
        });
        glSf.onPause();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (Consts.VERBOSE_LOG) {
            Log.i(TAG, "onPreviewFrame len " + data.length);
            Log.i(TAG, "onPreviewThread " + Thread.currentThread());
        }

        glRenderer.setCameraNV21Byte(isInPause ? null : data);

    }

    public int getCameraFacingDriection() {
        return mFuCamera.getCameraFacingDriection();
    }


    public void setInPause(boolean inPause) {
        isInPause = inPause;
        glRenderer.setInPause(inPause);
    }

    @Override
    protected void onBlurLevelSelected(int level) {
        mFuControler.onBlurLevelSelected(level);
    }

    @Override
    protected void onCheekThinSelected(int progress, int max) {
        mFuControler.onCheekThinSelected(progress,max);
    }

    @Override
    protected void onColorLevelSelected(int progress, int max) {
        mFuControler.onColorLevelSelected(progress,max);
    }

    @Override
    protected void onEffectItemSelected(String effectItemName) {
        mFuControler.onEffectItemSelected(effectItemName);
        isInAvatarMode = effectItemName.equals("lixiaolong.bundle");
    }

    @Override
    protected void onEnlargeEyeSelected(int progress, int max) {
        mFuControler.onEnlargeEyeSelected(progress,max);
    }

    @Override
    protected void onFilterSelected(String filterName) {
        mFuControler.onFilterSelected(filterName);
    }

    @Override
    protected void onRedLevelSelected(int progress, int max) {
        mFuControler.onRedLevelSelected(progress,max);
    }


    @Override
    protected void onFaceShapeLevelSelected(int progress, int max) {
        mFuControler.onFaceShapeLevelSelected(progress,max);
    }

    @Override
    protected void onFaceShapeSelected(int faceShape) {
        mFuControler.onFaceShapeSelected(faceShape);
    }


    @Override
    protected void onCameraChange() {
        Log.i(TAG, "onCameraChange");
        glRenderer.setNeedSwitchCameraSurfaceTexture(true);
        mFuCamera.releaseCamera();
        glRenderer.setCameraNV21Byte(null);
        glRenderer.setFrameId(0);
        mFuCamera.openCamera(mFuCamera.getCameraFacingDriectionReversal(),DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        glRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
    }

    @Override
    protected void onStartRecording() {
        MiscUtil.Logger(TAG, "start recording", false);
        mTextureMovieEncoder = new TextureMovieEncoder();
    }

    @Override
    protected void onStopRecording() {
        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(IN_RECORDING)) {
            MiscUtil.Logger(TAG, "stop recording", false);
            mTextureMovieEncoder.stopRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mFuControler.quite();
    }
}
