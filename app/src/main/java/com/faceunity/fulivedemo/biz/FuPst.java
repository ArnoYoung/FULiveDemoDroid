package com.faceunity.fulivedemo.biz;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import com.faceunity.fulivedemo.common.Consts;
import com.faceunity.wrapper.faceunity;

/**
 * Created by arno on 2017/9/12.
 *
 */
@SuppressWarnings("deprecation")
public class FuPst implements IFuPst,IFuPst.ILifeCycle{
    private final static String TAG = "FUDualInputToTextureEg";
    private final int DEFAULT_CAMERA_DIRECTION = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private final int DEFAULT_CAMERA_WIDITH = 1280;
    private final int DEFAULT_CAMERA_HEIGHT = 720;
    private FuCamera mFuCamera;
    private FuRender mGLRenderer;
    //李小龙显示脸部点阵
    private boolean isInAvatarMode = false;
    private FuManager mFuManager;
    private FuModule mFuModule;

    private IFuView mFuView;
    private CameraPreviewCallBack mPreviewCallBack;
    private Handler mHandler;
    public FuPst(IFuView fuView) {
        mFuView = fuView;
        mFuManager = new FuManager(mFuView.getContext());
        mFuModule = mFuManager.getModule();
        mHandler = new Handler();
        mGLRenderer = new FuRender(mFuView.getContext(),mFuManager, mFuView.getGlsv());
        mGLRenderer.setIRenderMsg(new RenderMsg());
        mGLRenderer.setRecordListener(mFuView);
        mFuCamera = new FuCamera(mFuView.getContext());
        mPreviewCallBack = new CameraPreviewCallBack();
        initGlsv(mFuView.getGlsv());
    }

    private void initGlsv(GLSurfaceView glsv){
        glsv.setEGLContextClientVersion(2);
        glsv.setRenderer(mGLRenderer);
        glsv.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * camera返回的图像并不一定是设置的大小（因为可能并不支持）
     * @return 相机预览尺寸
     */
    public Camera.Size getCameraSize(){
        return mFuCamera.getCamera().getParameters().getPreviewSize();
    }

    @Override
    public void onResume() {
        mFuCamera.openCamera(DEFAULT_CAMERA_DIRECTION,DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        mGLRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
        mGLRenderer.setCameraSize(getCameraSize().width, getCameraSize().height);
        mGLRenderer.setInPause(false);
        mPreviewCallBack.isInPause = false;
    }

    @Override
    public void onPause() {
        mGLRenderer.setInPause(false);
        mFuManager.removeMessage();
        mFuManager.getModule().mEffectItem = 0;
        mFuManager.getModule().mFaceBeautyItem = 0;
        mFuCamera.releaseCamera();
        mFuView.getGlsv().queueEvent(new Runnable() {
            @Override
            public void run() {
                mGLRenderer.notifyPause();
                mGLRenderer.destroySurfaceTexture();
                //切忌使用一个已经destroy的item
                faceunity.fuDestroyAllItems();
                faceunity.fuOnDeviceLost();
                mGLRenderer.clearFrameId();
            }
        });
        mPreviewCallBack.isInPause = true;
    }

    @Override
    public void onDestroy() {
        mFuManager.quite();
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
        isInAvatarMode = effectItemName.equals("lixiaolong.bundle");
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
    public void onCameraChange() {
        mFuCamera.releaseCamera();
        mGLRenderer.setCameraNV21Byte(null);
        mGLRenderer.clearFrameId();

        mFuCamera.openCamera(mFuCamera.getCameraFacingDriectionReversal(),DEFAULT_CAMERA_WIDITH,DEFAULT_CAMERA_HEIGHT);
        mGLRenderer.setNeedSwitchCameraSurfaceTexture(true);
        mGLRenderer.setCameraFacingDirection(mFuCamera.getCameraFacingDriection());
    }

    @Override
    public void onStartRecording() {
        mGLRenderer.startRecording();
    }

    @Override
    public void onStopRecording() {
        mGLRenderer.stopRecording();
    }

    private class RenderMsg implements FuRender.IRenderMsg {

        @Override
        public void onFrameTracking(final boolean isTracking) {
            mFuView.onFrameTracking(isTracking);
        }

        @Override
        public void handleCameraStartPreview() {
            mFuCamera.setPreviewCallback(mPreviewCallBack);
            mFuCamera.handleCameraStartPreview(mGLRenderer.getCameraSurfaceTexture());

        }
    }

    private class CameraPreviewCallBack implements Camera.PreviewCallback{
        boolean isInPause = false;
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (Consts.VERBOSE_LOG) {
                Log.i(TAG, "onPreviewFrame len " + data.length);
                Log.i(TAG, "onPreviewThread " + Thread.currentThread());
            }
            mGLRenderer.setCameraNV21Byte(isInPause ? null : data);
        }
    }


}
