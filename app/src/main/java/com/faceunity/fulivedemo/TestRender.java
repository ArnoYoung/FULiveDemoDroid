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
package com.faceunity.fulivedemo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.faceunity.fulivedemo.gles.drawer.CameraClipFrameRect;
import com.faceunity.fulivedemo.gles.drawer.FaceClipDrawer;
import com.faceunity.fulivedemo.gles.drawer.FaceRectPointsDrawer;
import com.faceunity.fulivedemo.gles.drawer.FullFrameRect;
import com.faceunity.fulivedemo.gles.drawer.FaceMarksPointsDrawer;
import com.faceunity.fulivedemo.gles.Texture2dProgram;
import com.faceunity.fulivedemo.gles.utils.PointDrawer;
import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class TestRender implements GLSurfaceView.Renderer ,SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "MyGLRenderer";
    //
    private final boolean IS_USE_BEAUTY = true;
    private final boolean IS_USE_GESTURE = false;
    //当onCameraChange 设置为true
    private boolean isNeedSwitchCameraSurfaceTexture = false;
    //activity pause 状态
    private boolean isInPause = false;
    //是否需要重新获取贴纸
    //private boolean isNeedEffectItem = true;
    //log userd
    private long currentFrameCnt = 0;
    private boolean isBenchmarkFPS = true;
    private boolean isBenchmarkTime = false;
    private long lastOneHundredFrameTimeStamp = 0;
    //camera预览数据
    private byte[] mCameraNV21Byte;
    private byte[] fuImgNV21Bytes;
    //相机前后
    private int cameraFacingDirection = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //相机视频宽告
    private int cameraWidth = 1280;
    private int cameraHeight = 720;
    //帧记录
    private int mFrameId = 0;
    //第一次绘制 会取消
    private boolean isFirstCameraOnDrawFrame;
    //脸部捕捉的状态 0:捕捉到
    private int faceTrackingStatus = 0;

    //全局纹理id
    private int mCameraTextureId;
    //创建显示纹理
    private FullFrameRect mFullScreenCamera;
    //
    private SurfaceTexture mCameraSurfaceTexture;

    //显示全屏幕
    private FullFrameRect mFullScreenFUDisplay;
    //绘制整体的小窗口
    private CameraClipFrameRect cameraClipFrameRect;
    //绘制捕捉到的脸小窗口
    private FaceClipDrawer mFaceClipDrawer;
    //在左上角绘制脸部标记点 对应CameraClipFrameRect窗口
    private FaceMarksPointsDrawer landmarksPoints;
    //在整体窗口绘制脸部标记点
    private FaceRectPointsDrawer faceRectPoints;
    private PointDrawer mPointDrawer ;

    //特质标记点
    private float[] landmarksData = new float[75 * 2];
    //脸部框架
    private float[] faceRectData = new float[4];
    //打印相关
    long frameAvailableTimeStamp;
    long resumeTimeStamp;
    boolean isFirstOnFrameAvailable;


//    static int mFaceBeautyItem = 0; //美颜道具
//    static int mEffectItem = 0; //贴纸道具
//    static int mGestureItem = 0; //手势道具
//    static int[] itemsArray = {mFaceBeautyItem, mEffectItem, mGestureItem};


    private IRenderMsg mIRenderMsg;
    //private TestActivity.MainHandler mMainHandler;

    private GLSurfaceView glSf;
    private FuModule mFu;
    private FuControler mFuControler;

    public TestRender(FuControler fuControler, GLSurfaceView glv) {

        this.glSf = glv;
        mFuControler = fuControler;
        mFu = mFuControler.getFu();
        //resume时间用创建时间代替
        resumeTimeStamp = System.nanoTime();
        isFirstOnFrameAvailable = true;
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return mCameraSurfaceTexture;
    }

    public void setCameraSize(int cameraWidth, int cameraHeight) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
    }

    public void setFrameId(int frameId) {
        mFrameId = frameId;
    }


    public void setIRenderMsg(IRenderMsg renderMsg) {
        mIRenderMsg = renderMsg;
    }

    public void setCameraFacingDirection(int cameraFacingDirection) {
        this.cameraFacingDirection = cameraFacingDirection;
    }

    public void setInPause(boolean inPause) {
        isInPause = inPause;
    }

    public void setCameraNV21Byte(byte[] cameraNV21Byte) {
        mCameraNV21Byte = cameraNV21Byte;
    }

    public void setNeedSwitchCameraSurfaceTexture(boolean needSwitchCameraSurfaceTexture) {
        isNeedSwitchCameraSurfaceTexture = needSwitchCameraSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated fu version " + faceunity.fuGetVersion());

        mFullScreenFUDisplay = new FullFrameRect(new Texture2dProgram(
                Texture2dProgram.ProgramType.TEXTURE_2D));
        mFullScreenCamera = new FullFrameRect(new Texture2dProgram(
                Texture2dProgram.ProgramType.TEXTURE_EXT));
        mCameraTextureId = mFullScreenCamera.createTextureObject();

        cameraClipFrameRect = new CameraClipFrameRect(0.4f, 0.4f * 0.8f); //clip 20% vertical
        mFaceClipDrawer = new FaceClipDrawer(mCameraTextureId);

        landmarksPoints = new FaceMarksPointsDrawer();//如果有证书权限可以获取到的话，绘制人脸特征点
        faceRectPoints = new FaceRectPointsDrawer();//如果有证书权限可以获取到的话，绘制人脸特征点
        mPointDrawer = new PointDrawer();

        switchCameraSurfaceTexture();

        try {
            InputStream is = Consts.appContext.getAssets().open("v3.mp3");
            byte[] v3data = new byte[is.available()];
            int len = is.read(v3data);
            is.close();
            faceunity.fuSetup(v3data, null, authpack.A());
            //faceunity.fuSetMaxFaces(1);//设置最大识别人脸数目
            Log.e(TAG, "fuSetup v3 len " + len);

            if (IS_USE_BEAUTY) {
                is = Consts.appContext.getAssets().open("face_beautification.mp3");
                byte[] itemData = new byte[is.available()];
                len = is.read(itemData);
                Log.e(TAG, "beautification len " + len);
                is.close();
                mFu.mFaceBeautyItem = faceunity.fuCreateItemFromPackage(itemData);
            }

            if (IS_USE_GESTURE) {
                is = Consts.appContext.getAssets().open("heart.mp3");
                byte[] itemData = new byte[is.available()];
                len = is.read(itemData);
                Log.e(TAG, "heart len " + len);
                is.close();
                mFu.mGestureItem = faceunity.fuCreateItemFromPackage(itemData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFuControler.onEffectItemSelected(mFuControler.getEffectFileName());
        isFirstCameraOnDrawFrame = true;
    }

    public void switchCameraSurfaceTexture() {
        Log.e(TAG, "switchCameraSurfaceTexture");
        isNeedSwitchCameraSurfaceTexture = false;
        if (mCameraSurfaceTexture != null) {
            faceunity.fuOnCameraChange();
            destroySurfaceTexture();
        }
        mCameraSurfaceTexture = new SurfaceTexture(mCameraTextureId);
        Log.e(TAG, "send start camera message");
        mIRenderMsg.handleCameraStartPreview();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged " + width + " " + height);
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float)  width/height;
        mFaceClipDrawer.setProjectionMatrix(ratio);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (isFirstOnFrameAvailable) {
            frameAvailableTimeStamp = System.nanoTime();
            isFirstOnFrameAvailable = false;
            Log.i(TAG, "first frame available time cost " +
                    (frameAvailableTimeStamp - resumeTimeStamp) / MiscUtil.NANO_IN_ONE_MILLI_SECOND);
        }
        if (Consts.VERBOSE_LOG) {
            Log.i(TAG, "onFrameAvailable");
        }

    }



    long oneHundredFrameFUTime;
    @Override
    public void onDrawFrame(GL10 gl) {

        if (isInPause) {
            //glSf.requestRender();
            return;
        }

        if (isNeedSwitchCameraSurfaceTexture) {
            switchCameraSurfaceTexture();
        }

        //首帧 先保留这句
        if (isFirstCameraOnDrawFrame) {
            isFirstCameraOnDrawFrame = false;
            glSf.requestRender();
            return;
        }

        /**
         * If camera texture data not ready there will be low possibility in meizu note3 which maybe causing black screen.
         */
        //封装先去掉首帧验证
//        while (cameraDataAlreadyCount < 2) {
//            if (Consts.VERBOSE_LOG) {
//                Log.e(TAG, "while cameraDataAlreadyCount < 2");
//            }
//            if (isFirstCameraOnDrawFrame) {
//                glSf.requestRender();
//                return;
//            }
//            synchronized (prepareCameraDataLock) {
//                //block until new camera frame comes.
//                try {
//                    prepareCameraDataLock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        //打印
        if (++currentFrameCnt == 100) {
            currentFrameCnt = 0;
            long tmp = System.nanoTime();
            if (isBenchmarkFPS)
                Log.e(TAG, "dualInput FPS : " + (1000.0f * MiscUtil.NANO_IN_ONE_MILLI_SECOND / ((tmp - lastOneHundredFrameTimeStamp) / 100.0f)));
            lastOneHundredFrameTimeStamp = tmp;
            if (isBenchmarkTime)
                Log.e(TAG, "dualInput cost time avg : " + oneHundredFrameFUTime / 100.f / MiscUtil.NANO_IN_ONE_MILLI_SECOND);
            oneHundredFrameFUTime = 0;
        }

        /**
         * 获取camera数据, 更新到texture
         */
        float[] mtx = new float[16];
        if (mCameraSurfaceTexture != null) {
            try {
                mCameraSurfaceTexture.updateTexImage();
                mCameraSurfaceTexture.getTransformMatrix(mtx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            throw new RuntimeException("HOW COULD IT HAPPEN!!! mCameraSurfaceTexture is null!!!");

        final int isTracking = faceunity.fuIsTracking();
        if (isTracking != faceTrackingStatus) {
            Arrays.fill(landmarksData, 0);
            Arrays.fill(faceRectData, 0);
            mIRenderMsg.onFrameTracking(isTracking == 0);
            faceTrackingStatus = isTracking;
        }
        if (Consts.VERBOSE_LOG) {
            Log.e(TAG, "isTracking " + isTracking);
        }

        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "color_level", mFu.mFaceBeautyColorLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "blur_level", mFu.mFaceBeautyBlurLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "filter_name", mFu.mFilterName);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "cheek_thinning", mFu.mFaceBeautyCheekThin);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "eye_enlarging", mFu.mFaceBeautyEnlargeEye);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "face_shape", mFu.mFaceShape);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "face_shape_level", mFu.mFaceShapeLevel);
        faceunity.fuItemSetParam(mFu.mFaceBeautyItem, "red_level", mFu.mFaceBeautyRedLevel);

        //faceunity.fuItemSetParam(mFaceBeautyItem, "use_old_blur", 1);

        if (mCameraNV21Byte == null || mCameraNV21Byte.length == 0) {
            Log.e(TAG, "camera nv21 bytes null");
            glSf.requestRender();
            glSf.requestRender();
            return;
        }

        boolean isOESTexture = true; //Tip: camera texture类型是默认的是OES的，和texture 2D不同
        int flags = isOESTexture ? faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE : 0;
        boolean isNeedReadBack = false; //是否需要写回，如果是，则入参的byte[]会被修改为带有fu特效的；支持写回自定义大小的内存数组中，即readback custom img
        flags = isNeedReadBack ? flags | faceunity.FU_ADM_FLAG_ENABLE_READBACK : flags;
        if (isNeedReadBack) {
            if (fuImgNV21Bytes == null) {
                fuImgNV21Bytes = new byte[mCameraNV21Byte.length];
            }
            System.arraycopy(mCameraNV21Byte, 0, fuImgNV21Bytes, 0, mCameraNV21Byte.length);
        } else {
            fuImgNV21Bytes = mCameraNV21Byte;
        }
        flags |= cameraFacingDirection == Camera.CameraInfo.CAMERA_FACING_FRONT ? 0 : faceunity.FU_ADM_FLAG_FLIP_X;

        long fuStartTime = System.nanoTime();
            /*
             * 这里拿到fu处理过后的texture，可以对这个texture做后续操作，如硬编、预览。
             */
        int fuTex = faceunity.fuDualInputToTexture(fuImgNV21Bytes, mCameraTextureId, flags,
                cameraWidth, cameraHeight, mFrameId++, mFu.itemArray());
        long fuEndTime = System.nanoTime();
        oneHundredFrameFUTime += fuEndTime - fuStartTime;

        //int fuTex = faceunity.fuBeautifyImage(mCameraTextureId, flags,
        //            cameraWidth, cameraHeight, mFrameId++, new int[] {mEffectItem, mFaceBeautyItem});
        //mFullScreenCamera.drawFrame(mCameraTextureId, mtx);
        if (mFullScreenFUDisplay != null)
            mFullScreenFUDisplay.drawFrame(fuTex, mtx);
        else
            throw new RuntimeException("HOW COULD IT HAPPEN!!! mFullScreenFUDisplay is null!!!");

        /**
         * 绘制Avatar模式下的镜头内容以及landmarks
         **/
        if (true) {
            float[] clipTextures;
            //cameraClipFrameRect.drawFrame(mCameraTextureId, mtx);
            faceunity.fuGetFaceInfo(0, "landmarks", landmarksData);
            faceunity.fuGetFaceInfo(0, "face_rect", faceRectData);
            faceRectPoints.refreshFulll(faceRectData, cameraWidth, cameraHeight,cameraFacingDirection != Camera.CameraInfo.CAMERA_FACING_FRONT);

            faceRectPoints.draw();
            landmarksPoints.refreshFulll(landmarksData, cameraWidth, cameraHeight, cameraFacingDirection != Camera.CameraInfo.CAMERA_FACING_FRONT);
            landmarksPoints.draw();
            mPointDrawer.updateVertex(landmarksPoints.getFaceOutline());
            mPointDrawer.draw();
            mFaceClipDrawer.refreshClipTextures2(landmarksPoints.getFaceOutline());
            mFaceClipDrawer.draw(mtx);

//                faceRectPoints.refresh(faceRectData, cameraWidth, cameraHeight, 0.1f, 0.8f, currentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT);
//                faceRectPoints.draw();
//                landmarksPoints.refresh(landmarksData, cameraWidth, cameraHeight, 0.1f, 0.8f, currentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT);
//                landmarksPoints.draw();

        }

//        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(START_RECORDING)) {
//            videoFileName = MiscUtil.createFileName() + "_camera.mp4";
//            File outFile = new File(videoFileName);
//            mTextureMovieEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
//                    outFile, cameraHeight, cameraWidth,
//                    3000000, EGL14.eglGetCurrentContext(), mCameraSurfaceTexture.getTimestamp()
//            ));
//
//            //forbid click until start or stop success
//            mTextureMovieEncoder.setOnEncoderStatusUpdateListener(new TextureMovieEncoder.OnEncoderStatusUpdateListener() {
//                @Override
//                public void onStartSuccess() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.e(TAG, "start encoder success");
//                            mRecordingBtn.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//
//                @Override
//                public void onStopSuccess() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.e(TAG, "stop encoder success");
//                            mRecordingBtn.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//            });
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(TestActivity.this, "video file saved to "
//                            + videoFileName, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(IN_RECORDING)) {
//            mTextureMovieEncoder.setTextureId(mFullScreenFUDisplay, fuTex, mtx);
//            mTextureMovieEncoder.frameAvailable(mCameraSurfaceTexture);
//        }

        if (!isInPause) glSf.requestRender();
    }

    public void notifyPause() {
        faceTrackingStatus = 0;

//        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(IN_RECORDING)) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mRecordingBtn.performClick();
//                }
//            });
//        }

        if (mFullScreenFUDisplay != null) {
            mFullScreenFUDisplay.release(false);
            mFullScreenFUDisplay = null;
        }

        if (mFullScreenCamera != null) {
            mFullScreenCamera.release(false);
            mFullScreenCamera = null;
        }
    }

    public void destroySurfaceTexture() {
        if (mCameraSurfaceTexture != null) {
            mCameraSurfaceTexture.release();
            mCameraSurfaceTexture = null;
        }
    }

    public static interface IRenderMsg {
        void onFrameTracking(boolean isTracking);
        void handleCameraStartPreview();
    }
}