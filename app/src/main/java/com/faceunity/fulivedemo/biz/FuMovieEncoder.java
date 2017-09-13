package com.faceunity.fulivedemo.biz;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.widget.Toast;

import com.faceunity.fulivedemo.common.Consts;
import com.faceunity.fulivedemo.util.MiscUtil;
import com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder;
import com.faceunity.fulivedemo.util.gles.drawer.FullFrameRect;

import java.io.File;

import static com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder.IN_RECORDING;
import static com.faceunity.fulivedemo.util.encoder.TextureMovieEncoder.START_RECORDING;

/**
 * Created by arno on 2017/9/12.
 */

public class FuMovieEncoder {
    private static final String TAG = "FuMovieEncoder";
    private String videoFileName;
    TextureMovieEncoder mTextureMovieEncoder;
    private FullFrameRect mFullScreenFUDisplay;
    private SurfaceTexture mCameraSurfaceTexture;
    private int mCameraHeight = 720;
    private int mCameraWidht = 1280;

    public FuMovieEncoder(FullFrameRect fullFrameRect,SurfaceTexture surfaceTexture) {
        init(fullFrameRect, surfaceTexture);
    }

    public FuMovieEncoder(){

    }

    public void init(FullFrameRect fullFrameRect, SurfaceTexture surfaceTexture) {
        mFullScreenFUDisplay = fullFrameRect;
        mCameraSurfaceTexture = surfaceTexture;
    }

    public void updateSurfaceTexture(SurfaceTexture surfaceTexture){
        mCameraSurfaceTexture = surfaceTexture;
    }

    public void setRecordSize(int cameraHeight, int cameraWidth){
        mCameraWidht = cameraWidth;
        mCameraHeight = cameraHeight;
    }

    public void startRecording() {
        MiscUtil.Logger(TAG, "start recording", false);
        mTextureMovieEncoder = new TextureMovieEncoder();
    }

    public void stopRecording() {
        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(IN_RECORDING)) {
            MiscUtil.Logger(TAG, "stop recording", false);
            mTextureMovieEncoder.stopRecording();
        }
    }

    private TextureMovieEncoder.OnEncoderStatusUpdateListener mRecordListener;
    public void setRecordListener(TextureMovieEncoder.OnEncoderStatusUpdateListener listener){
        mRecordListener = listener;
    }

    public void record(float[] mtx, int fuTex) {
        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(START_RECORDING)) {
            videoFileName = MiscUtil.createFileName() + "_camera.mp4";
            File outFile = new File(videoFileName);
            mTextureMovieEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                    outFile, mCameraHeight, mCameraWidht,
                    3000000, EGL14.eglGetCurrentContext(), mCameraSurfaceTexture.getTimestamp()
            ));

            //forbid click until start or stop success
            if (mRecordListener != null)
                mTextureMovieEncoder.setOnEncoderStatusUpdateListener(mRecordListener);
        }

        if (mTextureMovieEncoder != null && mTextureMovieEncoder.checkRecordingStatus(IN_RECORDING)) {
            mTextureMovieEncoder.setTextureId(mFullScreenFUDisplay, fuTex, mtx);
            mTextureMovieEncoder.frameAvailable(mCameraSurfaceTexture);
        }
    }
}
