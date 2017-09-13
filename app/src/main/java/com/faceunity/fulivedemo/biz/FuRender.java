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
package com.faceunity.fulivedemo.biz;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.faceunity.fulivedemo.common.Consts;

import com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder;
import com.faceunity.fulivedemo.util.gles.Texture2dProgram;
import com.faceunity.fulivedemo.util.gles.drawer.CameraClipFrameRect;
import com.faceunity.fulivedemo.util.gles.drawer.FaceClipDrawer;
import com.faceunity.fulivedemo.util.gles.drawer.FaceRectPointsDrawer;
import com.faceunity.fulivedemo.util.gles.drawer.FullFrameRect;
import com.faceunity.fulivedemo.util.gles.drawer.FaceMarksPointsDrawer;

import com.faceunity.fulivedemo.util.gles.utils.PointDrawer;
import com.faceunity.fulivedemo.util.MiscUtil;
import com.faceunity.wrapper.faceunity;

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
public class FuRender implements GLSurfaceView.Renderer ,SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "MyGLRenderer";

    //当onCameraChange 设置为true
    private boolean isNeedSwitchCameraSurfaceTexture = false;
    //activity pause 状态
    private boolean isInPause = false;

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
    private long frameAvailableTimeStamp;
    private long resumeTimeStamp;
    private boolean isFirstOnFrameAvailable;
    private long oneHundredFrameFUTime;


//    static int mFaceBeautyItem = 0; //美颜道具
//    static int mEffectItem = 0; //贴纸道具
//    static int mGestureItem = 0; //手势道具
//    static int[] itemsArray = {mFaceBeautyItem, mEffectItem, mGestureItem};


    private IRenderMsg mIRenderMsg;
    //private TestActivity.MainHandler mMainHandler;

    private GLSurfaceView glSf;
    //private FuModule mFu;
    //private FuControler mFuControler;
    private FuDrawer mFuDrawer;

    private FuMovieEncoder mFuMovieEncoder;



    /**
     * @param fuControler
     * @param glv
     */
    public FuRender(Context context, FuManager fuControler, GLSurfaceView glv) {
        this.glSf = glv;
        mFuDrawer = new FuDrawer(fuControler);
        //mFuControler = fuControler;
        //mFu = mFuControler.getFu();
        //resume时间用创建时间代替
        resumeTimeStamp = System.nanoTime();
        isFirstOnFrameAvailable = true;
        mFuMovieEncoder = new FuMovieEncoder();
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return mCameraSurfaceTexture;
    }

    public void setCameraSize(int cameraWidth, int cameraHeight) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
    }

    public void clearFrameId() {
        mFuDrawer.clearFrameId();
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


    public void startRecording() {
        mFuMovieEncoder.startRecording();
    }

    public void stopRecording() {
        mFuMovieEncoder.stopRecording();
    }

    public void setRecordListener(TextureMovieEncoder.OnEncoderStatusUpdateListener listener){
       mFuMovieEncoder.setRecordListener(listener);
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
        mFuMovieEncoder.init(mFullScreenFUDisplay,mCameraSurfaceTexture);
        switchCameraSurfaceTexture();
        mFuDrawer.init();
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




    @Override
    public void onDrawFrame(GL10 gl) {
        //pause状态
        if (isInPause) {
            return;
        }
        //首帧 先保留这句
        if (isFirstCameraOnDrawFrame) {
            isFirstCameraOnDrawFrame = false;
            glSf.requestRender();
            return;
        }
        //fu的首帧检测，暂时去掉了
        //checkFirstFrame();
        //更换surfaceTexuture
        if (isNeedSwitchCameraSurfaceTexture) {
            switchCameraSurfaceTexture();
        }
        //打印
        print();

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


        if (mCameraNV21Byte == null || mCameraNV21Byte.length == 0) {
            Log.e(TAG, "camera nv21 bytes null");
            glSf.requestRender();
            return;
        }

        int fuTex = mFuDrawer.draw(mCameraNV21Byte,mCameraTextureId,cameraWidth,cameraHeight);


        if (mFullScreenFUDisplay != null)
            mFullScreenFUDisplay.drawFrame(fuTex, mtx);
        else
            throw new RuntimeException("HOW COULD IT HAPPEN!!! mFullScreenFUDisplay is null!!!");

        drawCustom(mtx);
        mFuMovieEncoder.record(mtx,fuTex);

        if (!isInPause) glSf.requestRender();
    }



    private void drawCustom(float[] mtx) {

        //绘制Avatar模式下的镜头内容以及landmarks
        if (true) {
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

    /**
     * fu检测首帧，适配部分机型的，暂时去掉，后续观察
     */
    private void checkFirstFrame() {
        /**
         * If camera texture data not ready there will be low possibility in meizu note3 which maybe causing black screen.
         */
//        封装先去掉首帧验证
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
    }

    /**
     * 遗留打印
     * @deprecated
     */
    private void print() {
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
    }


    public static interface IRenderMsg {
        void onFrameTracking(boolean isTracking);
        void handleCameraStartPreview();
    }
}