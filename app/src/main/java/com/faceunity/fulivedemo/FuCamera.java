package com.faceunity.fulivedemo;


import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by arno on 2017/9/5.
 */
@SuppressWarnings("deprecation")
public class FuCamera{
    final String TAG = "fuCamera";
    //相机尺寸
    private int cameraWidth = 1280;
    private int cameraHeight = 720;
    private final int PREVIEW_BUFFER_COUNT = 3;
    private byte[][] previewCallbackBuffer;
    private Camera.PreviewCallback mPreviewCallback;
    private Camera mCamera;

    //相机正反面
    private int cameraFacingDriection = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Context mContext;

    public FuCamera(Context context) {
        mContext = context;
    }

    public void setCamera(Camera camera){
        mCamera = camera;
    }

    public Camera getCamera(){
        return mCamera;
    }

    /**
     * 获取相机方向
     * @return
     */
    public int getCameraFacingDriection() {
        return cameraFacingDriection;
    }

    /**
     * 取目前相机相反方向
     * @return
     */
    public int getCameraFacingDriectionReversal() {
        if (cameraFacingDriection == Camera.CameraInfo.CAMERA_FACING_BACK)
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * 初始化指定的相机device
     * @param cameraDirection 0 back ；1 front
     * @param desiredWidth 0：默认预览尺寸
     * @param desiredHeight 0：默认预览尺寸
     */
    public void openCamera(int cameraDirection, int desiredWidth, int desiredHeight) {
        Log.i(TAG, "openCamera");

        if (mCamera != null) {
            throw new RuntimeException("camera already initialized");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraId = 0;
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraDirection) {
                cameraId = i;
                mCamera = Camera.open(i);
                break;
            }
        }
        //默认尺寸
        if (desiredWidth == 0){
            Camera.Size size = mCamera.getParameters().getPreviewSize();
            cameraWidth = size.width;
            cameraHeight = size.height;
        }

        if (mCamera == null) {
            Toast.makeText(Consts.appContext,
                            "Open Camera Failed! Make sure it is not locked!", Toast.LENGTH_SHORT)
                            .show();
            throw new RuntimeException("unable to open camera");
        }

        CameraUtils.setCameraDisplayOrientation((Activity) mContext, cameraId, mCamera);
        Camera.Parameters parameters = mCamera.getParameters();

        /**
         * 设置对焦，会影响camera吞吐速率
         */
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        /**
         * 设置fps
         * */
        int[] closetFramerate = CameraUtils.closetFramerate(parameters, 30);
        Log.i(TAG, "closet framerate min " + closetFramerate[0] + " max " + closetFramerate[1]);
        parameters.setPreviewFpsRange(closetFramerate[0], closetFramerate[1]);

        CameraUtils.choosePreviewSize(parameters, desiredWidth, desiredHeight);
        mCamera.setParameters(parameters);

        cameraFacingDriection = cameraDirection;
        cameraWidth = desiredWidth;
        cameraHeight = desiredHeight;
    }

    /**
     * 关闭释放设备
     */
    public void releaseCamera() {
        Log.i(TAG, "release camera");
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewTexture(null);
                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * set preview and start preview after the surface created
     */
    public void handleCameraStartPreview(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "handleCameraStartPreview");

        if (previewCallbackBuffer == null) {
            Log.i(TAG, "allocate preview callback buffer");
            previewCallbackBuffer = new byte[PREVIEW_BUFFER_COUNT][cameraWidth * cameraHeight * 3 / 2];
        }
        mCamera.setPreviewCallbackWithBuffer(new PreviewCallback());
        for (int i = 0; i < PREVIEW_BUFFER_COUNT; i++){
            mCamera.addCallbackBuffer(previewCallbackBuffer[i]);
        }

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    /**
     * 预览毁掉
     * @param previewCallback
     */
    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public class PreviewCallback implements Camera.PreviewCallback{
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mCamera.addCallbackBuffer(data);
            if (mPreviewCallback != null)
                mPreviewCallback.onPreviewFrame(data,camera);
        }
    }

}
